package org.tma.intern.booking.service.implementation;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.tma.intern.booking.model.BookingItem;
import org.tma.intern.booking.repository.BookingItemRepository;
import org.tma.intern.booking.service.BookingItemService;
import org.tma.intern.common.base.BaseService;
import org.tma.intern.common.helper.StringHelper;
import org.tma.intern.common.type.Action;

import java.util.List;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BaseBookingItemService extends BaseService implements BookingItemService {

    @Inject
    BookingItemRepository bookingItemRepository;

    @Override
    public Uni<Boolean> isAnyExisted(List<String> seatIds) {
        return bookingItemRepository.find(BookingItem.FIELD_SEAT_ID + " in ?1", seatIds)
            .list().map(items -> !items.isEmpty());
    }

    @Override
    public Uni<List<String>> create(ObjectId bookingId, List<BookingItem> items) {
        List<BookingItem> newItems = items.stream().peek(bookingItem -> {
            bookingItem.setId(new ObjectId());
            bookingItem.setBookingId(bookingId.toHexString());
        }).toList();

        return bookingItemRepository.persist(newItems).replaceWith(
            newItems.stream().map(item -> item.getId().toHexString()).toList()
        );
    }

    @Override
    public Uni<String> delete(ObjectId id) {
        return bookingItemRepository.deleteById(id)
            .onItem().transform(isDeleted -> {
                if (!isDeleted) {
                    throw actionFailed(Action.DELETE, BookingItem.class, null);
                }
                return id.toHexString();
            });
    }

    @Override
    public Uni<List<String>> delete(List<String> ids) {
        return Multi.createFrom().iterable(ids)
            .map(ObjectId::new).onItem().transformToUniAndMerge(this::delete).collect().asList();
    }

    @Override
    public Uni<List<BookingItem>> findAllByBookingId(String bookingId) {
        return bookingItemRepository.find(BookingItem.FIELD_BOOKING_ID, bookingId).list();
    }

    @Override
    public Multi<BookingItem> findByIds(List<String> ids) {
        return bookingItemRepository.find(
            BookingItem.FIELD_ID + " in ?1",
            ids.stream().map(StringHelper::safeParse).toList()
        ).stream();
    }

}
