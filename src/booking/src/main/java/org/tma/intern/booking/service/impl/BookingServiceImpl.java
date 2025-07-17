package org.tma.intern.booking.service.impl;

import io.quarkus.panache.common.Page;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.tma.intern.booking.dto.request.BookingItemRequest;
import org.tma.intern.booking.dto.request.BookingRequest;
import org.tma.intern.booking.dto.response.BookingItemResponse;
import org.tma.intern.booking.dto.response.BookingResponse;
import org.tma.intern.booking.entity.Booking;
import org.tma.intern.booking.entity.BookingItem;
import org.tma.intern.booking.mapper.BookingItemMapper;
import org.tma.intern.booking.mapper.BookingMapper;
import org.tma.intern.booking.repository.BookingRepository;
import org.tma.intern.booking.service.BookingItemService;
import org.tma.intern.booking.service.BookingService;
import org.tma.intern.common.base.BaseService;
import org.tma.intern.common.contract.event.BookingCreated;
import org.tma.intern.common.contract.event.BookingItemCreated;
import org.tma.intern.common.dto.PageResponse;
import org.tma.intern.common.exception.AppError;
import org.tma.intern.common.exception.HttpException;
import org.tma.intern.common.type.BookingStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingServiceImpl extends BaseService implements BookingService {

    BookingItemService bookingItemService;

    BookingRepository bookingRepository;

    BookingMapper bookingMapper;

    BookingItemMapper bookingItemMapper;

    @NonFinal
    @Inject
    @Channel("booking.created-out")
    private Emitter<BookingCreated> bookingEventBus;

    Faker faker;

    static public String ROLLBACK_FAILED_MESSAGE = "Rollback failed: could not {} {}";

    @Override
    public Uni<List<String>> seedData(int count) {
//        List<Booking> bookings = IntStream.range(0, count)
//            .mapToObj(i -> createRandom())
//            .toList();
//
//        return bookingRepository.persist(bookings)
//            .replaceWith(bookings)
//            .flatMap(concertList ->
//                Multi.createFrom().iterable(concertList)
//                    .onItem().transformToUniAndMerge(concert ->
//                        bookingItemService.generateSeatsForConcert(concert.getId())
//                            .onFailure().call(throwable -> {
//                                log.error("Failed to generate seats for concert {}", concert.getId(), throwable);
//                                return bookingRepository.deleteById(concert.getId());
//                            })
//                            .replaceWith(Void.TYPE)
//                    )
//                    .collect().asList()
//                    .replaceWith(concertList.stream().map(c -> c.getId().toHexString()).toList())
//            )
//            .onFailure().transform(throwable -> new HttpException(AppError.ACTION_FAILED,
//                Response.Status.NOT_IMPLEMENTED, throwable, "Seed", "concerts"
//            ));
        return null;
    }

    @Override
    public Uni<String> create(BookingRequest.Body request) {
        Booking booking = bookingMapper.toEntity(request);
        booking.setCreatedAt(Instant.now());
        booking.setCreatedBy(identityContext.getClaim("sub"));

        List<BookingItem> bookingItems = request.items().stream().distinct().map(bookingItemMapper::toEntity).toList();

        List<BookingItemCreated> eventItems = request.items().stream().map(item ->
                BookingItemCreated.newBuilder()
                    .setSeatId(item.seatId())
                    .setPrice(item.price())
                    .build())
            .toList();

        return bookingItemService.isAnyExisted(request.items().stream().map(BookingItemRequest.Body::seatId).toList())
            .onItem().transformToUni(isExisted -> {
                if (isExisted) throw new HttpException(AppError.ACTION_FAILED, Response.Status.NOT_IMPLEMENTED,
                    new IllegalAccessException("Any item is already existed !!!"), "Create", "booking");

                return bookingRepository.persist(booking)
                    .flatMap(createdBooking ->
                        bookingItemService.create(createdBooking.getId(), bookingItems)
                            .invoke(() -> sendBookingCreatedEvent(request, eventItems))
                            .replaceWith(createdBooking.getId().toHexString())
                            .onFailure().call(throwable ->
                                rollbackCreateBooking(createdBooking.getId(), throwable))
                    );
            });

    }

    @Override
    public Uni<String> update(String id, BookingRequest.Update request) {
        AtomicReference<List<BookingItem>> deletedItemsRef = new AtomicReference<>();
        return bookingRepository.findById(new ObjectId(id))
            .onItem().ifNull().failWith(() -> new HttpException(AppError.RESOURCE_NOT_FOUND,
                Response.Status.NOT_FOUND, new NullPointerException(), "booking"))
            .onItem().transformToUni(existingBooking -> {
                // Step 1: Create new booking items
                return createNewItems(existingBooking, request)
                    .flatMap(createdItemIds ->
                        // Step 2: Delete requested items
                        deleteOldItems(request, deletedItemsRef, createdItemIds)
                            .flatMap(__ -> {
                                updateAuditing(existingBooking);
                                return bookingRepository.update(existingBooking)
                                    .onFailure().call(updateFailure -> rollbackUpdate(
                                        existingBooking.getId(), deletedItemsRef.get(), createdItemIds, updateFailure)
                                    );
                            }).replaceWith(id)
                    );
            })
            .onFailure().transform(throwable -> new HttpException(AppError.ACTION_FAILED,
                Response.Status.NOT_IMPLEMENTED, throwable, "Update", "booking"));
    }

    @Override
    public Uni<String> update(String id, BookingStatus status) {
        return bookingRepository.findById(new ObjectId(id)).onItem().ifNotNull().transformToUni(booking -> {
            updateAuditing(booking);
            return bookingRepository.update(booking)
                .onItem().transform(result -> result.getId().toHexString())
                .onFailure().transform(throwable -> new HttpException(
                    AppError.ACTION_FAILED, Response.Status.NOT_IMPLEMENTED, throwable, "Update", "booking"));
        });
    }

    @Override
    public Uni<String> softDelete(String id) {
        return bookingRepository.findById(new ObjectId(id)).onItem().ifNotNull().transformToUni(booking -> {
            updateAuditing(booking);
            booking.setDeleted(true);
            return bookingRepository.update(booking)
                .onItem().transform(result -> result.getId().toHexString())
                .onFailure().transform(throwable -> new HttpException(
                    AppError.ACTION_FAILED, Response.Status.NOT_IMPLEMENTED, throwable, "Soft delete", "booking"));
        });
    }

    @Override
    public Uni<String> delete(String id) {
        return bookingRepository.findById(new ObjectId(id)).onItem().ifNotNull().transformToUni(booking ->
            bookingRepository.deleteById(new ObjectId(id))
                .onItem().transformToUni(isDeleted -> {
                    if (!isDeleted) throw new HttpException(AppError.ACTION_FAILED,
                        Response.Status.NOT_IMPLEMENTED, null, "Delete", "booking");

                    return bookingItemService.findAllByBookingId(id).flatMap(items ->
                        bookingItemService.delete(items.stream().distinct().map(BookingItemResponse.Detail::getId).toList())
                            .replaceWith(id)
                            .onFailure().call(throwable -> bookingRepository.persist(booking)
                                .onFailure().invoke(rollbackFailure ->
                                    log.warn(ROLLBACK_FAILED_MESSAGE, "create", "deleted booking"))));
                })
                .onFailure().transform(throwable -> new HttpException(AppError.ACTION_FAILED,
                    Response.Status.NOT_IMPLEMENTED, throwable, "Delete", "booking"
                )));
    }

    @Override
    public Uni<BookingResponse.Detail> findById(String id) {
        return bookingRepository.findById(new ObjectId(id))
            .onItem().ifNull().failWith(() ->
                new HttpException(AppError.RESOURCE_NOT_FOUND,
                    Response.Status.NOT_FOUND, new NullPointerException(), "booking"))
            .flatMap(entity ->
                bookingItemService.findAllByBookingId(id)
                    .map(details -> {
                        BookingResponse.Detail dto = bookingMapper.toDto(entity);
                        dto.setItems(details);
                        return dto;
                    })
            )
            .onFailure().transform(throwable -> new HttpException(AppError.RESOURCE_NOT_FOUND,
                Response.Status.NOT_FOUND, throwable, "Booking"
            ));
    }

    @Override
    public Uni<PageResponse<BookingResponse.Detail>> findAll(int index, int limit) {
        return Uni.combine().all().unis(
                bookingRepository.find("is_deleted", false).page(Page.of(index, limit)).list(),
                bookingRepository.count()
            ).asTuple().flatMap(tuple -> {
                List<Booking> bookings = tuple.getItem1();
                Long total = tuple.getItem2();

                List<Uni<BookingResponse.Detail>> detailUnis = bookings.stream()
                    .map(booking -> bookingItemService.findAllByBookingId(booking.getId().toHexString())
                        .map(items -> {
                            BookingResponse.Detail dto = bookingMapper.toDto(booking);
                            dto.setItems(items);
                            return dto;
                        })
                    ).toList();

                return Uni.combine().all().unis(detailUnis).with(BookingServiceImpl::castDetails)
                    .map(details -> PageResponse.of(details, index, limit, total));
            })
            .onFailure().invoke(throwable -> log.warn("Fail to fetch bookings !!!"))
            .replaceWith(PageResponse.of(new ArrayList<>(), index, limit, 0));
    }

    private void sendBookingCreatedEvent(BookingRequest.Body request, List<BookingItemCreated> items) {
        BookingCreated event = BookingCreated.newBuilder()
            .setConcertId(request.concertId())
            .setConcertOwnerId(request.concertOwnerId())
            .setItems(items).build();

        bookingEventBus.send(event).thenAccept(ignored -> log.info("Booking created event sent successfully!"));
    }

    private Uni<Void> rollbackCreateBooking(ObjectId bookingId, Throwable throwable) {
        return bookingRepository.deleteById(bookingId)
            .onItem().invoke(deleted -> {
                if (!deleted) log.warn(ROLLBACK_FAILED_MESSAGE, "delete", "created booking");
            }).replaceWithVoid().invoke(() -> {
                throw new HttpException(AppError.ACTION_FAILED,
                    Response.Status.NOT_IMPLEMENTED, throwable, "Create", "booking");
            });
    }

    private Uni<List<String>> createNewItems(Booking booking, BookingRequest.Update request) {
        return bookingItemService.create(booking.getId(),
                request.newItems().stream().distinct().map(bookingItemMapper::toEntity).toList())
            .onFailure().transform(throwable -> new HttpException(AppError.ACTION_FAILED,
                Response.Status.NOT_IMPLEMENTED, throwable, "Create", "booking items"));
    }

    private Uni<Void> deleteOldItems(BookingRequest.Update request, AtomicReference<List<BookingItem>> deletedBookingRef, List<String> createdItemIds) {
        return bookingItemService.delete(request.deletedItems())
            .onItem().transformToUni(deletedIds ->
                bookingItemService.findByIds(deletedIds)
                    .collect().asList()
                    .invoke(deletedBookingRef::set)
                    .invoke(deletedItems -> deletedItems.forEach(item ->
                        log.info("Deleted booking item: {}", item.getId())))
            )
            .onFailure().call(deleteFailure ->
                rollbackCreateItems(createdItemIds, deleteFailure)).replaceWithVoid();
    }

    private Uni<Void> rollbackCreateItems(List<String> createdItemIds, Throwable deleteFailure) {
        return bookingItemService.delete(createdItemIds)
            .map(__ -> {
                throw new HttpException(AppError.ACTION_FAILED,
                    Response.Status.NOT_IMPLEMENTED, deleteFailure, "Update", "booking");
            })
            .onFailure().invoke(rollbackFailure ->
                log.warn(ROLLBACK_FAILED_MESSAGE, "delete", "created booking items")).replaceWithVoid();
    }

    private Uni<Void> rollbackUpdate(ObjectId bookingId, List<BookingItem> deletedItems, List<String> createdItemIds, Throwable updateFailure) {
        return bookingItemService.delete(createdItemIds)
            .flatMap(deletedIds ->
                bookingItemService.create(bookingId, deletedItems)
                    .map(__ -> {
                        throw new HttpException(AppError.ACTION_FAILED,
                            Response.Status.NOT_IMPLEMENTED, updateFailure, "Update", "booking");
                    })
                    .onFailure().invoke(rollbackFailure ->
                        log.warn(ROLLBACK_FAILED_MESSAGE, "create", "deleted booking items")))
            .onFailure().invoke(rollbackFailure ->
                log.warn(ROLLBACK_FAILED_MESSAGE, "delete", "created booking items")).replaceWithVoid();
    }

    public void updateAuditing(Booking booking) {
        booking.setVersion(booking.getVersion() + 1);
        booking.setUpdatedAt(Instant.now());
        booking.setUpdatedBy(identityContext.getClaim("sub"));
    }

    private static List<BookingResponse.Detail> castDetails(List<?> results) {
        return results.stream()
            .map(BookingResponse.Detail.class::cast)
            .toList();
    }

    private Booking createRandom() {
        return Booking.builder()
            .id(new ObjectId())
            .build();
    }

}
