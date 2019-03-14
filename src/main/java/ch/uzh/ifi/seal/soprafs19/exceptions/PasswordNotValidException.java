package ch.uzh.ifi.seal.soprafs19.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class PasswordNotValidException extends RuntimeException {
    public PasswordNotValidException(String name) {
        super("Password for user " + name + " is not valid!");
    }
}