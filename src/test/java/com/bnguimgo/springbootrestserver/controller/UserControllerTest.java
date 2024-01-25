package com.bnguimgo.springbootrestserver.controller;

import com.bnguimgo.springbootrestserver.SpringbootRestserverApplication;
import com.bnguimgo.springbootrestserver.dao.UserRepository;
import com.bnguimgo.springbootrestserver.model.Role;
import com.bnguimgo.springbootrestserver.model.User;
import com.bnguimgo.springbootrestserver.security.component.JwtTokenUtil;
import com.bnguimgo.springbootrestserver.service.UserService;
import com.bnguimgo.springbootrestserver.util.TokenUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringbootRestserverApplication.class)
//@WebMvcTest(controllers = UserController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@AutoConfigureMockMvc() // disable all filters (especially security filters)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @MockBean
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    User user;
    private ObjectMapper objectMapper;
    HttpHeaders httpHeaders;

    String token ="";

    public UserControllerTest() {
    }

    @BeforeEach
    public void setUp() throws Exception {
        // Initialisation du setup avant chaque test
        Role role = new Role("USER_ROLE");// initialisation du role utilisateur
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user = new User(1L, "Dupont", "password", "1");
        //user.setRoles(roles);
        user.addRole(role);
        List<User> allUsers = List.of(user);
        objectMapper = new ObjectMapper();

        // Mock de la couche de service
        when(userService.getAllUsers()).thenReturn(allUsers);
        when(userService.findUserById(any(Long.class))).thenReturn(user);

        //token = jwtTokenUtil.generateToken("Dupont", "password");
        token =  generateToken();
        httpHeaders = new HttpHeaders();
        httpHeaders.add("Authorization", "Bearer " + token);
        assertNotNull(token);

    }

    @Test
    public void testFindAllUsers() throws Exception {

        User connectedUser = new User("Dupont", "password", "1");
        when(userService.findByLogin(connectedUser.getLogin())).thenReturn(user);

        MvcResult result = mockMvc.perform(get("/auth/user/users")
                        .headers(httpHeaders)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        // ceci est une redondance, car déjà vérifié par: isOk())
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus(), "Réponse incorrecte");
        verify(userService).getAllUsers();
        assertNotNull(result);
        Collection<User> users = objectMapper.readValue(result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
        assertNotNull(users);
        assertEquals(1, users.size());
        User userResult = users.iterator().next();
        assertEquals(user.getLogin(), userResult.getLogin());
        assertEquals(user.getPassword(), userResult.getPassword());

    }

    @Test
    public void testSaveUser() throws Exception {

        User connectedUser = new User("Dupont", "password", "1");
        String jsonContent = objectMapper.writeValueAsString(connectedUser);
        when(userService.findByLogin(connectedUser.getLogin())).thenReturn(user);
        when(userService.saveOrUpdateUser(any(User.class))).thenReturn(user);

        MvcResult result = mockMvc
                .perform(post("/auth/user/users")
                        .headers(httpHeaders)
                        .contentType(MediaType.APPLICATION_JSON).content(jsonContent))
                .andExpect(status().isCreated()).andReturn();

        assertEquals(HttpStatus.CREATED.value(), result.getResponse().getStatus(), "Erreur de sauvegarde");
        verify(userService).saveOrUpdateUser(any(User.class));
        User userResult = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
        });
        assertNotNull(userResult);
        assertEquals(connectedUser.getLogin(), userResult.getLogin());
        assertEquals(connectedUser.getPassword(), userResult.getPassword());

    }

    @Test
    public void testFindUserByLogin() throws Exception {
        when(userService.findByLoginAndPassword("Dupont", "password")).thenReturn(user);
        User connectedUser = new User("Dupont", "password", "1");
        String jsonContent = objectMapper.writeValueAsString(connectedUser);
        when(userService.findByLogin(connectedUser.getLogin())).thenReturn(user);
        // on execute la requête
        MvcResult result = mockMvc
                .perform(post("/auth/user/users/login").headers(httpHeaders)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isOk()).andReturn();

        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus(), "Erreur de sauvegarde");
        verify(userService).findByLoginAndPassword(any(String.class), any(String.class));
        User userResult = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
        });
        assertNotNull(userResult);
        assertEquals(Long.valueOf(1), userResult.getId());
        assertEquals(connectedUser.getLogin(), userResult.getLogin());
        assertEquals(connectedUser.getPassword(), userResult.getPassword());
    }

    @Test
    public void testDeleteUser() throws Exception {

        User connectedUser = new User("Dupont", "password", "1");
        User userFromDB = new User("Dupont", "password", "1");
        userFromDB.setId(1L);
        userFromDB.setRoles(Set.of());
        when(userService.findByLogin(connectedUser.getLogin())).thenReturn(userFromDB);
        //when(userRepository.findByLogin(connectedUser.getLogin())).thenReturn(Optional.of(user));

        MvcResult result = mockMvc.perform(delete("/auth/user/users")
                        .headers(httpHeaders)
                        .param("id", String.valueOf(1L)))
                .andExpect(status().isGone())
                .andReturn();
        assertEquals(HttpStatus.GONE.value(), result.getResponse().getStatus(), "Erreur de suppression");
        verify(userService).deleteUser(any(Long.class));
    }

    @Test
    public void testUpdateUser() throws Exception {

        User userToUpdate = new User("Toto", "password", "0");
        User userUpdated = new User(2L, "Toto", "password", "0");
        String jsonContent = objectMapper.writeValueAsString(userToUpdate);
        when(userService.saveOrUpdateUser(userToUpdate)).thenReturn(userUpdated);
        User connectedUser = new User("Dupont", "password", "1");
        when(userService.findByLogin(connectedUser.getLogin())).thenReturn(user);
        // on execute la requête
        MvcResult result = mockMvc
                .perform(MockMvcRequestBuilders.put("/auth/user/users")
                        .headers(httpHeaders)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON).content(jsonContent))
                .andExpect(status().isOk()).andReturn();

        verify(userService).saveOrUpdateUser(any(User.class));
        User userResult = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
        });
        assertNotNull(userResult);
        assertEquals(Long.valueOf(2), userResult.getId());
        assertEquals(userToUpdate.getLogin(), userResult.getLogin());
        assertEquals(userToUpdate.getPassword(), userResult.getPassword());
        assertEquals(userToUpdate.getActive(), userResult.getActive());
    }

    private String generateToken() throws Exception {

        User connectedUser = new User("Dupont", "password", "1");
        String jsonContent = objectMapper.writeValueAsString(connectedUser);
        //when(userService.findByLogin(connectedUser.getLogin())).thenReturn(user);
        //when(userService.saveOrUpdateUser(any(User.class))).thenReturn(user);

        MvcResult result = mockMvc
            .perform(post("/token/generateToken")
                .headers(new HttpHeaders())
                .contentType(MediaType.APPLICATION_JSON).content(jsonContent))
            .andExpect(status().isOk()).andReturn();

        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus(), "Erreur de sauvegarde");
        //verify(userService).saveOrUpdateUser(any(User.class));

        TokenUtils tokenUtilsResult = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
        });
        //assertNotNull(tokenUtilsResult);
        //assertEquals(connectedUser.getLogin(), tokenUtilsResult.getOwner());
        return  tokenUtilsResult.getToken();
    }
}
