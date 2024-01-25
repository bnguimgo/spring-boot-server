/**
 * 
 */
package com.bnguimgo.springbootrestserver.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * @author bnguimgo
 * Classe permettant de créer des mots de passe hachés pour des besoin de tests
 *
 */
public class PasswordEncoder {

	private static BCryptPasswordEncoder bCryptPasswordEncoder;

	public static void main(String[] args) {
		bCryptPasswordEncoder = new BCryptPasswordEncoder();
		String password ="password2";
		String encodedPassword = bCryptPasswordEncoder.encode(password);
		System.out.println("Mot de passe en clair : "+password);
		System.out.println("Mot de passe haché : "+encodedPassword);
		//Pour vérifier que le mot de passe haché correspond bien au mot de passe initial, il utiliser la méthode bCryptPasswordEncoder.matches(x, y)
		System.out.println("Le mot de passe est bien haché : "+bCryptPasswordEncoder.matches(password, encodedPassword));
	}

}
