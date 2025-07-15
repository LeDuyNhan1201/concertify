package org.tma.intern.booking.service;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.tma.intern.booking.dto.BookingItemResponse;
import org.tma.intern.booking.entity.BookingItem;
import org.tma.intern.booking.repository.BookingItemRepository;
import org.tma.intern.booking.dto.BookingItemRequest;
import org.tma.intern.booking.dto.BookingMapper;
import org.tma.intern.common.base.BaseService;
import org.tma.intern.common.exception.AppError;
import org.tma.intern.common.exception.HttpException;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingItemServiceImpl extends BaseService implements BookingItemService {

    BookingItemRepository bookingItemRepository;

    BookingMapper bookingMapper;

    @Override
    public Uni<List<String>> create(String bookingId, List<BookingItemRequest.Body> request) {
        List<BookingItem> entities = request.stream().map(body -> {
            BookingItem entity = bookingMapper.toEntity(body);
            entity.setBookingId(bookingId);
            return entity;
        }).toList();
        return bookingItemRepository.persist(entities)
            .replaceWith(
                entities.stream().map(item -> item.getId().toHexString()).toList()
            )
            .onFailure().transform(throwable -> new HttpException(AppError.ACTION_FAILED,
                Response.Status.NOT_IMPLEMENTED, throwable, "Create", "booking item"
            ));
    }

    @Override
    public Uni<String> delete(String id) {
        return bookingItemRepository.deleteById(new ObjectId(id))
            .onItem().transform(result -> {
                if (!result) throw new HttpException(AppError.ACTION_FAILED,
                    Response.Status.NOT_IMPLEMENTED, null, "Delete", "booking item"
                );
                return id;
            })
            .onFailure().transform(throwable -> new HttpException(AppError.ACTION_FAILED,
                Response.Status.NOT_IMPLEMENTED, throwable, "Delete", "booking item"
            ));
    }

    @Override
    public Uni<List<String>> delete(List<String> ids) {
        return Multi.createFrom().iterable(ids)
            .onItem().transformToUniAndMerge(this::delete)
            .collect().asList();
    }

    @Override
    public Uni<List<BookingItemResponse.Detail>> findAllByBookingId(String bookingId) {
        return bookingItemRepository.find("booking_id", bookingId).list()
            .onItem().transform(bookingItems -> bookingItems.stream().map(bookingMapper::toDto).toList())
            .onFailure().invoke(throwable -> log.error("Fail to fetch items for booking {} !!!", bookingId))
            .replaceWith(new ArrayList<>());
    }

}
