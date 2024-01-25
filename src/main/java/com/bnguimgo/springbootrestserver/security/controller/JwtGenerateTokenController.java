package com.bnguimgo.springbootrestserver.security.controller;

import com.bnguimgo.springbootrestserver.model.User;
import com.bnguimgo.springbootrestserver.security.component.JwtTokenUtil;
import com.bnguimgo.springbootrestserver.util.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;


@RestController
@CrossOrigin
@RequestMapping("/token")
public class JwtGenerateTokenController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @PostMapping(value = "/generateToken")
    @CacheEvict(value="userCache", allEntries=true)//On vide d'abord le cache au début
    public ResponseEntity<TokenUtils> generateToken(@RequestBody User user) throws Exception {

        final String token = jwtTokenUtil.generateToken(user.getLogin(), user.getPassword());

        TokenUtils tokenUtils = TokenUtils.builder()
                .token(token)
                .owner(jwtTokenUtil.getUsernameFromToken(token))
                .tokenExpirationDate(jwtTokenUtil.getExpDate(token))
                .build();
        return new ResponseEntity<>(tokenUtils, HttpStatus.OK);
    }

}
