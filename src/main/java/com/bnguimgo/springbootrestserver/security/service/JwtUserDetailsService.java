package com.bnguimgo.springbootrestserver.security.service;

import com.bnguimgo.springbootrestserver.exception.BusinessResourceException;
import com.bnguimgo.springbootrestserver.model.User;
import com.bnguimgo.springbootrestserver.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Source: https://www.baeldung.com/spring-cache-tutorial
 * Cette classe charge les infos de l'utilisateur au démarrage de l'application
 */
@Service
public class JwtUserDetailsService implements UserDetailsService {

    @Autowired
    private final UserService userService;

    public JwtUserDetailsService(UserService userService) {
        this.userService = userService;
    }

    @Override
    //@Cacheable(value = "userAuthCache", key = "#username", unless = "#result == null")//Mise en cache du résultat: UserDetails
    @Cacheable(value = "userAuthCache", key ="#p0")
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, BusinessResourceException {
        //Connexion à la base de données pour récupérer le user, puis mise en cache si aucune erreur
        User userFromDB = null;
        try {
            userFromDB = userService.findByLogin(username);
        } catch (Exception ex){
            throw new BusinessResourceException("User Auth Not Found", "L'utilisateur avec ce login n'existe pas: "+ username, HttpStatus.NOT_FOUND);
        }

        if (username.equals(userFromDB.getLogin())) {
            return new UserDetailsWrapper(userFromDB);//UserDetailsWrapper implémente UserDetails juste pour transformer User en UserDetails
        } else {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
    }
}