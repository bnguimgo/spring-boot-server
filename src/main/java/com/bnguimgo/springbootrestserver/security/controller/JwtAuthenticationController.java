package com.bnguimgo.springbootrestserver.security.controller;

import com.bnguimgo.springbootrestserver.exception.BusinessResourceException;
import com.bnguimgo.springbootrestserver.model.User;
import com.bnguimgo.springbootrestserver.security.component.JwtTokenUtil;
import com.bnguimgo.springbootrestserver.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.web.bind.annotation.*;


@RestController
@CrossOrigin
@RequestMapping("/auth/*")
public class JwtAuthenticationController {

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private JwtTokenUtil jwtTokenUtil;

	@Autowired
	private UserService userService;

	@PostMapping(value = "/authenticate")
	public ResponseEntity<User> findUserByLoginAndPassword(@RequestBody User user) throws Exception {

		//authenticate (xx, xx) est une méthode qui  appelle implicitement l'implémentation de UserDetailsService de Spring security
		//Pour valider ou non le login et le mot de passe
		//Dans notre cas, c'est la classe JwtUserDetailsService qui implémente UserDetailsService

		authenticate(user.getLogin(), user.getPassword());

		User result = userService.findByLoginAndPassword(user.getLogin(), user.getPassword());
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	/**
	 * This method is used to set authenticate to false
	 * @param username user name
	 * @param password password
	 */
	public void authenticate(String username, String password) throws RuntimeException {
		try {
			authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
		} catch (DisabledException ex) {
			//throw new Exception("USER_DISABLED", ex);
			throw new BusinessResourceException("USER_DISABLED", "Login ou mot de passe incorrect", HttpStatus.UNAUTHORIZED);
		} catch (BadCredentialsException ex) {
			throw new BusinessResourceException("INVALID_CREDENTIALS", "Login ou mot de passe incorrect", HttpStatus.UNAUTHORIZED);
		} catch (AccountExpiredException ex){
			throw new BusinessResourceException("ACCOUNT_EXPIRATION_EXCEPTION","Connexion expirée", HttpStatus.UNAUTHORIZED);
		} catch (Exception ex){
			throw new BusinessResourceException("UNKNOW_ERROR_EXCEPTION","Erreur inconnue", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
