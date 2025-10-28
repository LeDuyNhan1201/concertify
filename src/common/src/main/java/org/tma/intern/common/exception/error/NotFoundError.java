package org.tma.intern.common.exception.error;

public final class NotFoundError {

    public final SubError Resource = new SubError("not-found/resource", "NotFound.Resource");

    public final SubError Group = new SubError("not-found/group", "NotFound.User");

    public final SubError User = new SubError("not-found/user", "NotFound.User");

    public final SubError Role = new SubError("not-found/role", "NotFound.Role");

    public final SubError Concert = new SubError("not-found/concert", "NotFound.Concert");

    public final SubError Seat = new SubError("not-found/seat", "NotFound.Seat");

    public final SubError Booking = new SubError("not-found/booking", "NotFound.Booking");

    public final SubError BookingItem = new SubError("not-found/booking-item", "NotFound.BookingItem");

}
