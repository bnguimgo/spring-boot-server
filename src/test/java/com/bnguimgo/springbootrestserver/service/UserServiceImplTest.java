package com.bnguimgo.springbootrestserver.service;

import com.bnguimgo.springbootrestserver.cache.CachingConfig;
import com.bnguimgo.springbootrestserver.dao.RoleRepository;
import com.bnguimgo.springbootrestserver.dao.UserRepository;
import com.bnguimgo.springbootrestserver.exception.BusinessResourceException;
import com.bnguimgo.springbootrestserver.model.Role;
import com.bnguimgo.springbootrestserver.model.User;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@Import({CachingConfig.class})
@ExtendWith(SpringExtension.class)
@EnableCaching
@ImportAutoConfiguration(classes = {
    CacheAutoConfiguration.class,
    RedisAutoConfiguration.class
})
@ContextConfiguration
public class UserServiceImplTest {

    private UserService userService;
    private RoleRepository roleRepository;
    private UserRepository userRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    User userInCache = null;

    @Configuration
    @EnableCaching
    static class Config {

/*        @Bean
        public MyCacheableInterface myCacheableInterface() {
            return myCacheableInterfaceMock;
        }*/

        @Bean
        public CacheManager cacheManager() {
            return new ConcurrentMapCacheManager("userCache");
        }
    }

    @BeforeEach
    public void setup() {
        userRepository = Mockito.mock(UserRepository.class);
        roleRepository = Mockito.mock(RoleRepository.class);
        bCryptPasswordEncoder = Mockito.mock(BCryptPasswordEncoder.class);
        userService = new UserServiceImpl(userRepository, roleRepository, bCryptPasswordEncoder);
        userInCache = new User("Dupont", "password", "1");
    }

    @Test
    public void testFindAllUsers() {
        User user = new User("Dupont", "password", "1");
        Role role = new Role("USER_ROLE");
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);
        List<User> allUsers = List.of(user);
        when(userRepository.findAll()).thenReturn(allUsers);
        Collection<User> users = userService.getAllUsers();
        assertNotNull(users);
        assertEquals(users, allUsers);
        assertEquals(users.size(), allUsers.size());
        verify(userRepository).findAll();
    }

    @Test
    public void testSaveUser() {
        User user = new User("Dupont", "password", "1");
        Role role = new Role("USER_ROLE");
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        User userMock = new User(1L, "Dupont", "password", "1");
        userMock.setRoles(roles);
        when(roleRepository.getAllRolesStream()).thenReturn(roles.stream());
        when(userRepository.save((user))).thenReturn(userMock);
        User userSaved = userService.saveOrUpdateUser(user);
        Assertions.assertNotNull(userSaved);
        assertEquals(userMock.getId(), userSaved.getId());
        assertEquals(userMock.getLogin(), userSaved.getLogin());
        assertEquals(userMock.getLogin(), userSaved.getLogin());
        assertEquals(userMock.getRoles(), userSaved.getRoles());
        verify(userRepository).save(any(User.class));
    }

    @Test
    public void testSaveUser_existing_login_throws_error() {
        User user = new User("Dupont", "password", "1");
        Role role = new Role("USER_ROLE");
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        when(roleRepository.getAllRolesStream()).thenReturn(roles.stream());
        when(userRepository.save((user))).thenThrow(new DataIntegrityViolationException("Duplicate Login"));
        BusinessResourceException thrown = assertThrows(BusinessResourceException.class, () -> userService.saveOrUpdateUser(user));
        verify(userRepository).save(any(User.class));
        assertNotNull(thrown);
        assertEquals("Duplicate Login for " + user.getLogin(), thrown.getMessage());
    }

    @Test
    public void testFindUserByLogin() {
        User user = new User("Dupont", "password", "1");
        when(userRepository.findByLogin(user.getLogin())).thenReturn(Optional.of(user));
        User userFromDB = userService.findByLogin(user.getLogin());
        assertNotNull(userFromDB);
        MatcherAssert.assertThat(userFromDB.getLogin(), is(user.getLogin()));
        verify(userRepository).findByLogin(any(String.class));
    }

    @Test
    public void testUpdateUser() {
        User userToUpdate = new User(1L, "NewDupont", "newPassword", "1");
        Role role = new Role("USER_ROLE");
        Set<Role> roles = new HashSet<>();
        roles.add(role);

        User userFoundById = new User(1L, "OldDupont", "oldpassword", "1");
        userFoundById.setRoles(roles);

        User userUpdated = new User(1L, "NewDupont", "newPassword", "1");
        userUpdated.setRoles(roles);

        when(userRepository.findById(1L)).thenReturn(Optional.of(userFoundById));
        when(bCryptPasswordEncoder.matches(any(String.class), any(String.class))).thenReturn(false);
        when(bCryptPasswordEncoder.encode(any(String.class))).thenReturn("newPassword");
        when(userRepository.save((userToUpdate))).thenReturn(userUpdated);
        User userFromDB = userService.saveOrUpdateUser(userToUpdate);
        assertNotNull(userFromDB);
        verify(userRepository).save(any(User.class));
        assertEquals(Long.valueOf(1), userFromDB.getId());
        assertEquals("NewDupont", userFromDB.getLogin());
        assertEquals("newPassword", userFromDB.getPassword());
        assertEquals("1", userFromDB.getActive());
        assertEquals(roles, userFromDB.getRoles());
    }

    @Test
    public void testDelete() {
        User userTodelete = new User(1L, "Dupont", "password", "1");
        Mockito.doNothing().when(userRepository).deleteById(userTodelete.getId());
        userService.deleteUser(userTodelete.getId());

        verify(userRepository).deleteById(any(Long.class));
    }

    @Test
    public void testFindByLoginAndPassword() {
        String login = "login";
        String password = "password";
        User userExpected = new User();
        userExpected.setLogin(login);
        userExpected.setPassword(password);

        when(userRepository.findByLogin(login)).thenReturn(Optional.of(userInCache));
        when(bCryptPasswordEncoder.matches(anyString(), anyString())).thenReturn(true);

        User firstCallResultService = userService.findByLoginAndPassword(login, password);
        User nextCallResultService = userService.findByLoginAndPassword(login, password);
        assertNotNull(firstCallResultService);
        assertNotNull(nextCallResultService);

        assertEquals("Dupont",firstCallResultService.getLogin());
        assertEquals("Dupont",nextCallResultService.getLogin());
        assertEquals(firstCallResultService,nextCallResultService);

        verify(userRepository, times(2)).findByLogin(login);//FIXME Should times(1) --> check @Cacheable(xxx)


    }
}
