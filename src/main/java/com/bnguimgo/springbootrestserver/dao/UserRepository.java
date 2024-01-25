package com.bnguimgo.springbootrestserver.dao;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bnguimgo.springbootrestserver.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
	
	//@Query(value = "select u from User u where u.login = ?1")
	Optional<User> findByLogin(String loginParam);

//	@Query(value = "select user from User user where user.login = ?1 and user.password = ?2")
	User findByLoginAndPassword(String login, String password); //méthode non utilisée pour le moment

}
