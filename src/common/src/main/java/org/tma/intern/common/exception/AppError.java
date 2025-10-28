package org.tma.intern.common.exception;

import org.tma.intern.common.exception.error.AuthError;
import org.tma.intern.common.exception.error.FailureError;
import org.tma.intern.common.exception.error.InvalidError;
import org.tma.intern.common.exception.error.NotFoundError;

public interface AppError {

    NotFoundError NotFound = new NotFoundError();

    InvalidError Invalid = new InvalidError();

    FailureError Failure = new FailureError();

    AuthError Auth = new AuthError();

}
