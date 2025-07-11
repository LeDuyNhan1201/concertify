package org.tma.intern.auth.dto;

public class UserRequest {

    public record Creation(
            String email,
            String password
    ){};

}
