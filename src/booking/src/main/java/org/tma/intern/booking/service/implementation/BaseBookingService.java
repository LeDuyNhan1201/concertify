package org.tma.intern.booking.service.implementation;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import io.quarkus.panache.common.Page;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.tma.intern.booking.dto.request.BookingItemRequest;
import org.tma.intern.booking.dto.request.BookingRequest;
import org.tma.intern.booking.dto.response.BookingItemResponse;
import org.tma.intern.booking.dto.response.BookingResponse;
import org.tma.intern.booking.model.Booking;
import org.tma.intern.booking.model.BookingItem;
import org.tma.intern.booking.mapper.BookingItemMapper;
import org.tma.intern.booking.mapper.BookingMapper;
import org.tma.intern.booking.repository.BookingRepository;
import org.tma.intern.booking.service.BookingItemService;
import org.tma.intern.booking.service.BookingService;
import org.tma.intern.common.base.BaseService;
import org.tma.intern.common.contract.event.*;
import org.tma.intern.common.dto.PageResponse;
import org.tma.intern.common.helper.StringHelper;
import org.tma.intern.common.type.Action;
import org.tma.intern.common.type.BookingStatus;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BaseBookingService extends BaseService implements BookingService {

    @Inject
    BookingItemService bookingItemService;

    @Inject
    BookingRepository bookingRepository;

    @Inject
    BookingMapper bookingMapper;

    @Inject
    BookingItemMapper bookingItemMapper;

    @NonFinal
    @Inject
    @Channel("booking.updated-out")
    Emitter<BookingUpdated> bookingUpdatedEventBus;

    @NonFinal
    @Inject
    @Channel("booking.deleted-out")
    Emitter<BookingDeleted> bookingDeletedEventBus;

    @Override
    public Uni<String> create(BookingCreated event) {
        Booking booking = toBooking(event);
        List<BookingItem> bookingItems = toBookingItems(event.getItems());

        return bookingItemService.isAnyExisted(event.getItems().stream().map(SeatInfo::getId).toList())
            .flatMap(isExisted -> {
                if (isExisted) throw new RuntimeException("Any item is already existed !!!");
                return bookingRepository.persist(booking).flatMap(createdBooking -> {
                    var uniActionCombine = UniActionCombine.<List<String>>builder()
                        .uni(bookingItemService.create(createdBooking.getId(), bookingItems))
                        .action(Action.UPDATE)
                        .entityType(Booking.class)
                        .build();
                    return super.actionWithRollback(
                        uniActionCombine,
                        bookingRepository.deleteById(createdBooking.getId())
                    ).replaceWith(createdBooking.getId().toHexString());
                });
            });
    }

    private Booking toBooking(BookingCreated event) {
        return Booking.builder()
            .concertId(event.getConcertId())
            .concertOwnerId(event.getConcertOwnerId())
            .createdAt(Instant.now())
            .createdBy(event.getCreatedBy())
            .build();
    }

    private List<BookingItem> toBookingItems(List<SeatInfo> seatInfos) {
        return seatInfos.stream().distinct()
            .map(seatInfo -> BookingItem.builder()
                .seatId(seatInfo.getId())
                .seatCode(seatInfo.getCode())
                .price(seatInfo.getPrice())
                .build()
            ).toList();
    }

    @Override
    public Uni<String> update(String id, BookingRequest.UpdatedInfo request) {
        return findByIdAndCheckOwner(id, false).flatMap(existingBooking ->
            createNewItems(existingBooking, request).flatMap(createdItemIds ->
                deleteOldItems(request.oldItems(), createdItemIds).chain(deletedItems -> {
                    updateAuditing(existingBooking);
                    var uniActionCombine = UniActionCombine.builder()
                        .uni(bookingRepository.update(existingBooking).chain(() ->
                            sendBookingUpdatedEvent(
                                existingBooking.getId().toHexString(),
                                existingBooking.getConcertId(),
                                deletedItems.stream().map(BookingItem::getSeatId).toList(),
                                createdItemIds,
                                request.newItems().stream().map(BookingItemRequest.Info::seatId).toList()
                            )
                        ))
                        .action(Action.UPDATE)
                        .entityType(Booking.class)
                        .build();
                    return super.actionWithRollback(
                        uniActionCombine,
                        rollbackUpdate(existingBooking.getId(), deletedItems, createdItemIds)
                    ).replaceWith(id);
                })
            ));
    }

    private Uni<List<String>> createNewItems(Booking booking, BookingRequest.UpdatedInfo request) {
        List<BookingItem> newItems = request.newItems().stream().distinct().map(bookingItemMapper::toEntity).toList();

        var uniActionCombine = UniActionCombine.<List<String>>builder()
            .action(Action.CREATE)
            .uni(bookingItemService.create(booking.getId(), newItems))
            .entityType(BookingItem.class)
            .build();

        return super.assertActionFail(uniActionCombine);
    }

    private Uni<List<BookingItem>> deleteOldItems(
        List<String> oldItemIds,
        List<String> createdItemIds
    ) {
        var uniActionCombine = UniActionCombine.<List<BookingItem>>builder()
            .uni(bookingItemService.findByIds(oldItemIds).collect().asList()
                .call(deletedItems ->
                    bookingItemService.delete(
                        deletedItems.stream().map(
                            bookingItem -> bookingItem.getId().toHexString()
                        ).toList()
                    )
                )
            )
            .action(Action.DELETE)
            .entityType(BookingItem.class)
            .build();

        return super.actionWithRollback(
            uniActionCombine,
            bookingItemService.delete(createdItemIds).replaceWithVoid()
        );
    }

    private Uni<List<String>> rollbackUpdate(
        ObjectId bookingId,
        List<BookingItem> deletedItems,
        List<String> createdItemIds
    ) {
        var uniDeleteCombine = UniActionCombine.<List<String>>builder()
            .uni(bookingItemService.delete(createdItemIds))
            .action(Action.DELETE)
            .entityType(BookingItem.class)
            .build();

        var uniCreateCombine = UniActionCombine.<List<String>>builder()
            .uni(bookingItemService.create(bookingId, deletedItems))
            .action(Action.CREATE)
            .entityType(Booking.class)
            .build();

        return super.handleRollbackFailure(uniDeleteCombine).chain(
            () -> super.handleRollbackFailure(uniCreateCombine)
        );
    }

    private Uni<Void> sendBookingUpdatedEvent(
        String bookingId,
        String concertId,
        List<String> oldItemSeatIds,
        List<String> newItemIds,
        List<String> newItemSeatIds
    ) {
        BookingUpdated event = BookingUpdated.newBuilder()
            .setBookingId(bookingId)
            .setConcertId(concertId)
            .setOldItems(buildSeatIds(oldItemSeatIds))
            .setNewItems(buildExistedItems(newItemIds, newItemSeatIds)).build();

        return super.commitSentEvent(bookingUpdatedEventBus, BookingUpdated.class, event);
    }

    private List<SeatId> buildSeatIds(List<String> seatIds) {
        return seatIds.stream()
            .map(seatId -> SeatId.newBuilder()
                .setValue(seatId)
                .build()
            ).toList();
    }

    private List<ExistedItem> buildExistedItems(List<String> newItemIds, List<String> seatIds) {
        return IntStream.range(0, newItemIds.size())
            .mapToObj(index -> ExistedItem.newBuilder()
                .setId(newItemIds.get(index))
                .setSeatId(seatIds.get(index))
                .build()
            ).collect(Collectors.toList());
    }

    @Override
    public Uni<String> update(String id, BookingStatus status) {
        var uniActionCombine = UniActionCombine.<String>builder()
            .action(Action.UPDATE)
            .uni(
                findByIdAndCheckOwner(id, false)
                    .flatMap(existingBooking -> {
                            existingBooking.setStatus(status);
                            updateAuditing(existingBooking);
                            return bookingRepository.persist(existingBooking)
                                .map(saved -> saved.getId().toHexString());
                        }
                    )
            )
            .entityType(Booking.class)
            .build();
        return super.assertActionFail(uniActionCombine);
    }

    @Override
    public Uni<String> softDelete(String id) {
        var uniActionCombine = UniActionCombine.<String>builder()
            .action(Action.UPDATE)
            .uni(
                findByIdAndCheckOwner(id, false)
                    .flatMap(existingBooking -> {
                            existingBooking.setDeleted(!existingBooking.isDeleted());
                            updateAuditing(existingBooking);
                            return bookingRepository.persist(existingBooking)
                                .map(saved -> saved.getId().toHexString());
                        }
                    )
            )
            .entityType(Booking.class)
            .build();
        return super.assertActionFail(uniActionCombine);
    }

    @Override
    public Uni<String> delete(String id) {
        var uniActionCombine = UniActionCombine.<String>builder()
            .action(Action.DELETE)
            .uni(findByIdAndCheckOwner(id, false)
                .flatMap(booking -> deleteBookingWithItems(StringHelper.safeParse(id), booking))
            )
            .entityType(Booking.class)
            .build();
        return super.assertActionFail(uniActionCombine);
    }

    private Uni<Void> sendBookingDeletedEvent(
        String bookingId,
        String concertId,
        List<String> items
    ) {
        BookingDeleted event = BookingDeleted.newBuilder()
            .setUserId(identityContext.getPrincipleName())
            .setBookingId(bookingId)
            .setConcertId(concertId)
            .setItems(
                items.stream()
                    .map(seatId -> SeatId.newBuilder()
                        .setValue(seatId)
                        .build()
                    ).toList()
            )
            .build();

        return commitSentEvent(bookingDeletedEventBus, BookingDeleted.class, event);
    }

    private Uni<String> deleteBookingWithItems(ObjectId bookingId, Booking booking) {
        return bookingItemService.findAllByBookingId(bookingId.toHexString())
            .flatMap(bookingItems -> {
                List<ObjectId> itemIds = bookingItems.stream().map(BookingItem::getId).distinct().toList();
                return bookingItemService.delete(
                    itemIds.stream().map(ObjectId::toHexString).toList()
                ).chain(() -> {
                    var uniActionCombine = UniActionCombine.<Void>builder()
                        .uni(bookingRepository.deleteById(bookingId).onItem()
                            .invoke(isDeleted -> {
                                if (!isDeleted) {
                                    throw super.actionFailed(Action.DELETE, Booking.class, null);
                                }
                            }).chain(() ->
                                sendBookingDeletedEvent(
                                    bookingId.toHexString(),
                                    booking.getConcertId(),
                                    bookingItems.stream().map(BookingItem::getSeatId).toList()
                                )
                            )
                        )
                        .action(Action.DELETE)
                        .entityType(BookingItem.class)
                        .build();
                    return super.actionWithRollback(
                        uniActionCombine,
                        bookingItemService.create(booking.getId(), bookingItems)
                    ).replaceWith(bookingId.toHexString());
                });
            });
    }

    @Override
    public Uni<BookingResponse.Details> details(String id) {
        return super.assertNotFoundAndReturn(
            findByIdAndCheckOwner(id, false)
                .map(bookingMapper::toDetailsDto)
                .flatMap(bookingDetails ->
                    bookingItemService.findAllByBookingId(id)
                        .map(this::toItemsDetailsResponse)
                        .replaceWith(bookingDetails)
                ),
            Booking.class
        );
    }

    @Override
    public Uni<BookingResponse.Preview> preview(String id) {
        return super.assertNotFoundAndReturn(
            findByIdAndCheckOwner(id, true)
                .map(bookingMapper::toPreviewDto)
                .flatMap(previewBooking ->
                    bookingItemService.findAllByBookingId(id)
                        .map(this::toItemsDetailsResponse)
                        .replaceWith(previewBooking)
                ),
            Booking.class
        );
    }

    @Override
    public Uni<PageResponse<BookingResponse.Details>> search(int offset, int limit) {
        var query = Filters.and(Filters.eq("deleted", false));

        return Uni.combine().all().unis(
            bookingRepository.find(query, Indexes.descending("created_at"))
                .page(Page.of(offset, limit))
                .project(BookingResponse.Details.class)
                .list(),
            bookingRepository.count(query)

        ).asTuple().flatMap(tuple -> {
            List<BookingResponse.Details> bookings = tuple.getItem1();
            Long total = tuple.getItem2();
            return toBookingsDetailsResponse(bookings).map(
                details -> PageResponse.of(details, offset, limit, total)
            );
        });
    }

    @Override
    public Uni<PageResponse<BookingResponse.Details>> bookingsOfMyConcerts(int index, int limit) {
        var query = Filters.and(
            Filters.eq(Booking.FIELD_CONCERT_OWNER_ID, identityContext.getPrincipleName()),
            Filters.eq(Booking.FIELD_IS_DELETED, false)
        );

        return Uni.combine().all().unis(
            bookingRepository.find(query, Indexes.descending(Booking.FIELD_CREATED_AT))
                .page(Page.of(index, limit))
                .project(BookingResponse.Details.class)
                .list(),
            bookingRepository.count(query)

        ).asTuple().flatMap(tuple -> {
            List<BookingResponse.Details> bookings = tuple.getItem1();
            Long total = tuple.getItem2();
            return toBookingsDetailsResponse(bookings).map(
                details -> PageResponse.of(details, index, limit, total)
            );
        });
    }

    /* --------- Private methods for [UPDATE] --------- */
    public void updateAuditing(Booking booking) {
        booking.setUpdatedAt(Instant.now());
        booking.setUpdatedBy(identityContext.getPrincipleName());
    }

    /* --------- Private methods for [READ] --------- */
    private Uni<Booking> findByIdAndCheckOwner(String id, boolean checkConcertOwner) {
        return super.assertNotFound(bookingRepository.findById(StringHelper.safeParse(id)), Booking.class)
            .invoke(booking ->
                checkOwner(checkConcertOwner ? booking.getConcertOwnerId() : booking.getCreatedBy())
            );
    }

    private List<BookingItemResponse.Details> toItemsDetailsResponse(List<BookingItem> bookingItems) {
        return bookingItems.stream().map(bookingItemMapper::toDetailsDto).toList();
    }

    private Uni<List<BookingResponse.Details>> toBookingsDetailsResponse(List<BookingResponse.Details> bookings) {
        List<Uni<BookingResponse.Details>> detailUnis = bookings.stream().map(this::toBookingDetailsResponse).toList();
        return Uni.combine().all().unis(detailUnis).with(list ->
            list.stream().map(BookingResponse.Details.class::cast).toList()
        );
    }

    private Uni<BookingResponse.Details> toBookingDetailsResponse(BookingResponse.Details booking) {
        return bookingItemService.findAllByBookingId(booking.getId()).map(items -> {
            booking.setItems(items.stream().map(bookingItemMapper::toDetailsDto).toList());
            return booking;
        });
    }

}
