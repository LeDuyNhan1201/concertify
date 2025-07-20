package org.tma.intern.booking.service.impl;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.tma.intern.booking.dto.response.BookingItemResponse;
import org.tma.intern.booking.entity.BookingItem;
import org.tma.intern.booking.mapper.BookingItemMapper;
import org.tma.intern.booking.repository.BookingItemRepository;
import org.tma.intern.booking.service.BookingItemService;
import org.tma.intern.common.base.BaseService;
import org.tma.intern.common.exception.AppError;
import org.tma.intern.common.exception.HttpException;
import org.tma.intern.common.helper.StringHelper;

import java.util.List;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingItemServiceImpl extends BaseService implements BookingItemService {

    BookingItemRepository bookingItemRepository;

    BookingItemMapper bookingItemMapper;

    @Override
    public Uni<Boolean> isAnyExisted(List<String> seatIds) {
        return bookingItemRepository.find("seat_id in ?1", seatIds).list()
            .map(items -> !items.isEmpty());
    }

    @Override
    public Uni<List<String>> create(ObjectId bookingId, List<BookingItem> items) {
        List<BookingItem> newItems = items.stream().peek(bookingItem -> {
            bookingItem.setId(new ObjectId());
            bookingItem.setBookingId(bookingId.toHexString());
        }).toList();
        return bookingItemRepository.persist(newItems).replaceWith(
            newItems.stream().map(item -> item.getId().toHexString()).toList());
    }

    @Override
    public Uni<String> delete(ObjectId id) {
        return bookingItemRepository.deleteById(id)
            .onItem().transform(isDeleted -> {
                if (!isDeleted)
                    throw new HttpException(AppError.ACTION_FAILED, Response.Status.NOT_IMPLEMENTED, null, "Delete", "booking item");
                return id.toHexString();
            });
    }

    @Override
    public Uni<List<String>> delete(List<String> ids) {
        return Multi.createFrom().iterable(ids).map(ObjectId::new).onItem().transformToUniAndMerge(this::delete).collect().asList();
    }

    @Override
    public Uni<List<BookingItemResponse.Detail>> findAllByBookingId(String bookingId) {
        return bookingItemRepository.find("booking_id", bookingId).list().map(items ->
            items.stream().map(bookingItemMapper::toDto).toList()
        );
    }

    @Override
    public Multi<BookingItem> findByIds(List<String> ids) {
        return bookingItemRepository.find("_id in ?1", ids.stream().map(StringHelper::safeParse).toList()).stream();
    }

}
