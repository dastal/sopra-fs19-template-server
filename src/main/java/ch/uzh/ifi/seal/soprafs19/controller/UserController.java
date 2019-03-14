package ch.uzh.ifi.seal.soprafs19.controller;

import ch.uzh.ifi.seal.soprafs19.entity.User;
import ch.uzh.ifi.seal.soprafs19.exceptions.AuthenticationException;
import ch.uzh.ifi.seal.soprafs19.exceptions.UserNotFound;
import ch.uzh.ifi.seal.soprafs19.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import java.io.Console;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;


@RestController
public class UserController {

    private final UserService service;

    UserController(UserService service) {
        this.service = service;
    }

    //FOR DEBUGGING PURPOSES
    @GetMapping("/debug/users")
    Iterable<User> all() {
        return service.getUsers();
    }

    @GetMapping("/users")
    Iterable<User> all(@RequestParam() String token) {
        if (service.validateToken(token)) {
            return service.getUsers();
        }
        else {
            throw new AuthenticationException("token invalid");
        }
    }

    @GetMapping("/users/{id}")
    User one(@PathVariable String id,  @RequestParam() String token) {
        if (service.validateToken(token)) {
            return service.getUser(id);
        }
        else {
            throw new AuthenticationException("token invalid");
        }
    }

    //dto -> for updates
    @CrossOrigin
    @PutMapping("/users/{id}")
    ResponseEntity update(@PathVariable String id, @RequestBody User updateUser, @RequestParam() String token) {
        this.service.updateUser(id, updateUser, token);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/users/login")
    AuthorizationCredentials login(@RequestBody LoginCredentials cred) {
        AuthorizationCredentials acred = new AuthorizationCredentials();
        User local = this.service.loginUser(cred.username, cred.password);
        acred.token = local.getToken();
        acred.id = local.getId();
        return acred;
    }

    @PostMapping("/users/logout")
    @ResponseStatus(HttpStatus.OK)
    String logout(@RequestBody AuthorizationCredentials cred) {
        return this.service.logoutUser(cred.token);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/users")
    String createUser(@RequestBody User newUser, HttpServletRequest request) throws UnknownHostException {
        User local = this.service.createUser(newUser);
        String host = InetAddress.getLocalHost().getHostAddress();
        return String.format("http://%s:%s/users/%s", host, request.getLocalPort(), local.getId());
    }
}


class AuthorizationCredentials implements Serializable {
    public String token;
    public Long id;
}

class LoginCredentials implements Serializable {
    public String username;
    public String password;
}