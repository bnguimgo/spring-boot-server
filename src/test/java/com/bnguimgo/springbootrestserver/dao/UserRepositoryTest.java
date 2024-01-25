package com.bnguimgo.springbootrestserver.dao;

import com.bnguimgo.springbootrestserver.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * source: https://www.baeldung.com/spring-boot-testing
 * DataJpaTest annotation provides some standard setup needed for testing the persistence layer:
 *
 * configuring H2, an in-memory database, initialize database
 * setting Hibernate, Spring Data, and the DataSource
 * performing an @EntityScan
 * turning on SQL logging
 */
@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private UserRepository userRepository;
    User user = new User("Dupont", "password", "1");

    @BeforeEach
    public void setup() {
        entityManager.persist(user);//on sauvegarde l'objet user au début de chaque test
        entityManager.flush();
    }

    @Test
    public void testFindAllUsers() {

        List<User> users = userRepository.findAll();
        assertThat(4, is(users.size()));//on a 3 Users dans le fichier d'initialisation xxx.sql et un utilisateur ajouté lors du setup du test
    }

    @Test
    public void testSaveUser() {
        User user = new User("Paul", "password", "1");
        User userSaved = userRepository.save(user);
        assertNotNull(userSaved.getId());
        assertThat("Paul", is(userSaved.getLogin()));
    }

    @Test
    public void testFindByLogin() {
        Optional<User> userFromDB = userRepository.findByLogin("login2");
        assertTrue(userFromDB.isPresent());
        assertThat("login2", is(userFromDB.get().getLogin()));//login2 a été crée lors de l'initialisation du fichier xxx.sql
    }

    @Test
    public void testFindById() {
        Optional<User> userFromDB = userRepository.findById(user.getId());
        assertTrue(userFromDB.isPresent());
        assertThat(user.getLogin(), is(userFromDB.get().getLogin()));//user a été crée lors du setup
    }

    @Test
    public void testFindBy_Unknow_Id() {
        Optional<User> userFromDB = userRepository.findById(50L);
        assertEquals(Optional.empty(), Optional.of(userFromDB).get());
    }

    @Test
    public void testDeleteUser() {
        userRepository.deleteById(user.getId());
        Optional<User> userFromDB = userRepository.findByLogin(user.getLogin());
        assertEquals(Optional.empty(), Optional.of(userFromDB).get());
    }

    @Test
    public void testUpdateUser() {//Test si le compte utilisateur est désactivé
        Optional<User> userToUpdate = userRepository.findByLogin(user.getLogin());
        assertTrue(userToUpdate.isPresent());
        userToUpdate.get().setActive("0");
        userRepository.save(userToUpdate.get());
        Optional<User> userUpdatedFromDB = userRepository.findByLogin(userToUpdate.get().getLogin());
        assertTrue(userUpdatedFromDB.isPresent());
        assertThat("0", is(userUpdatedFromDB.get().getActive()));
    }
}
