package com.bnguimgo.springbootrestserver.controller;

import com.bnguimgo.springbootrestserver.exception.BusinessResourceException;
import com.bnguimgo.springbootrestserver.model.User;
import com.bnguimgo.springbootrestserver.service.UserService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/auth/user/*")
public class UserController {//pas de logique métier dans le contrôleur, mais, uniquement l'appel des services

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    @Autowired
    private UserService userService;

    @GetMapping(value = "/users", produces = "application/json")
    @ApiResponse(description = "Successful Operation", responseCode = "200", content = @Content(mediaType = "application/json"))
    public ResponseEntity<Collection<User>> getAllUsers() {
        Collection<User> users = userService.getAllUsers();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @PostMapping(value = "/users", consumes = "application/json", produces = "application/json")
    @Transactional
    public ResponseEntity<User> saveUser(@RequestBody User user) {

        User userSaved = userService.saveOrUpdateUser(user);
        return new ResponseEntity<>(userSaved, HttpStatus.CREATED);
    }

    @PutMapping(value = "/users", consumes = "application/json", produces = "application/json")
    public ResponseEntity<User> updateUser(@RequestBody User user) {
        User userUpdated = userService.saveOrUpdateUser(user);
        return new ResponseEntity<>(userUpdated, HttpStatus.OK);
    }

    @DeleteMapping(value = "/users")
    public ResponseEntity<Void> deleteUser(@RequestParam(value = "id") Long id) throws BusinessResourceException {
        try {
            userService.deleteUser(id);
            return new ResponseEntity<>(HttpStatus.GONE);
        } catch (BusinessResourceException ex) {
            // log the error message
            logger.error(String.format("Aucun utilisateur n'existe avec l'identifiant: " + id, ex));
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (RuntimeException ex) {
            // log the error message
            logger.error(String.format("erreur inattendue avec l'identifiant: " + id, ex));
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

    }

    @PostMapping(value = "/users/login", consumes = "application/json", produces = "application/json")
    public ResponseEntity<User> findUserByLoginAndPassword(@RequestBody User user) {
        User userFound = userService.findByLoginAndPassword(user.getLogin(), user.getPassword());
        return new ResponseEntity<>(userFound, HttpStatus.OK);
    }

    @GetMapping(value = "/users/{id}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<User> findUserById(@PathVariable(value = "id") Long id) {
        User userFound = userService.findUserById(id);
        return new ResponseEntity<>(userFound, HttpStatus.OK);
    }
}
