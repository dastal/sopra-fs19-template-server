package ch.uzh.ifi.seal.soprafs19.service;

import ch.uzh.ifi.seal.soprafs19.constant.UserStatus;
import ch.uzh.ifi.seal.soprafs19.entity.User;
import ch.uzh.ifi.seal.soprafs19.exceptions.AuthenticationException;
import ch.uzh.ifi.seal.soprafs19.exceptions.UserExistingException;
import ch.uzh.ifi.seal.soprafs19.exceptions.UserNotFound;
import ch.uzh.ifi.seal.soprafs19.exceptions.PasswordNotValidException;

import ch.uzh.ifi.seal.soprafs19.repository.UserRepository;
import ch.uzh.ifi.seal.soprafs19.exceptions.*;


import org.apache.tomcat.util.http.parser.Authorization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpClientErrorException.Conflict;

import java.util.Calendar;
import java.util.UUID;

import static java.rmi.server.LogStream.log;

@Service
@Transactional
public class UserService {

    private final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;


    @Autowired
    public UserService(@Qualifier("userRepository") UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Iterable<User> getUsers() {
        return this.userRepository.findAll();
    }

    public User loginUser(String username, String password) {
        User temp = this.userRepository.findByUsername(username);
        if (temp == null) throw new UserNotFound(username);
        if (temp.getPassword().equals(password)) {
            temp.setStatus(UserStatus.ONLINE);
            temp.setToken(UUID.randomUUID().toString());
            log.debug("User {} logged in!", username);
            return temp;
        }
        else throw new PasswordNotValidException(username);
    }

    public String logoutUser(String token) {
        User temp = this.userRepository.findByToken(token);
        if (temp == null) {
            throw new AuthenticationException("Token invalid");
        }
        temp.setStatus(UserStatus.OFFLINE);
        temp.setToken(null);
        return "logout successful!";
    }

    public User createUser(User newUser) {
        if (userRepository.findByUsername(newUser.getUsername()) != null) throw new UserExistingException(newUser.getUsername());
        newUser.setStatus(UserStatus.OFFLINE);
        newUser.setCreationDate(Long.toString(System.currentTimeMillis()));
        userRepository.save(newUser);
        log.debug("Created Information for User: {}", newUser);
        return newUser;
    }

    public User getUser(String id) {
        User temp = this.userRepository.findById(Long.parseLong(id)).orElse(null);
        if (temp == null) throw new UserNotFound("User not found!");
        return temp;
    }

    public Boolean validateToken(String token) {
        return this.userRepository.findByToken(token) != null;
    }

    public void updateUser(String id, User updatedUser, String token) {
        User localByUsername = this.getUser(id);
        User local = this.userRepository.findByToken(token);
        if (local == null || !local.equals(localByUsername)) throw new AuthenticationException("Token not valid!");
        User temp = userRepository.findByUsername(updatedUser.getUsername());
        if (temp != null && temp.getUsername() != local.getUsername()) throw new UserExistingException(updatedUser.getUsername());
        local.setName(updatedUser.getName());
        local.setUsername(updatedUser.getUsername());
        local.setBirthDay(updatedUser.getBirthDay());
    }
}