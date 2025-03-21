package com.paralelogram.user.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserException extends RuntimeException {

    private HttpStatus status;
    private String message;
    private Throwable ex;

    public UserException(HttpStatus status, String message) {
        this.message = message;
    }
}
