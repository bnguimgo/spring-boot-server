package com.bnguimgo.springbootrestserver.integrationtest;

import com.bnguimgo.springbootrestserver.SpringbootRestserverApplication;
import com.bnguimgo.springbootrestserver.model.Role;
import com.bnguimgo.springbootrestserver.model.User;
import com.bnguimgo.springbootrestserver.security.component.JwtTokenUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = SpringbootRestserverApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT)
//@SqlGroup({ @Sql(value = "classpath:data.sql")})
//@Sql(value = { "classpath:data.sql" })
@TestMethodOrder(MethodOrderer.OrderAnnotation.class) //permet de définir l'ordre d'exécution des tests
public class UserControllerIntegrationTest {

    @LocalServerPort
    private int port;
    @Autowired
    private TestRestTemplate restTemplate;
    private static final String host = "http://localhost";
    private String url = "";// URL doit être identique au profil integrationTest dans le pom.xml, cette url du est celle du serveur REST.
    // Cette url peut être celle d'un serveur distant

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    private HttpHeaders httpHeaders;
    //private RestTemplate restTemplate;

    private String getURLWithPort(String path) {
        return url + path;
    }

    @BeforeEach
    public void setUp() {

        url = host + ":"+ port;

        String token = jwtTokenUtil.generateToken("login2", "password2");
        assertNotNull(token);
        httpHeaders = new HttpHeaders();
        httpHeaders.set("Authorization", "Bearer " + token);//token for security
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    }

    @Test
    @Order(1)//Ce test s'exécute en premier
    public void testFindAllUsers() {

        HttpEntity<Void> requestEntity = new HttpEntity<>(httpHeaders);

        ResponseEntity<Object> responseEntity = restTemplate.exchange(
            getURLWithPort("/springboot-restserver/auth/user/users"), HttpMethod.GET, requestEntity, Object.class);
        assertNotNull(responseEntity);
        @SuppressWarnings("unchecked")
        Collection<User> userCollections = (Collection<User>) responseEntity.getBody();
        assertEquals(HttpStatus.OK.value(), responseEntity.getStatusCode().value(), "Réponse inattendue");
        assertNotNull(userCollections);
        assertEquals(3, userCollections.size());
        // on a bien 3 utilisateurs initialisés par les scripts datas.sql + un nouvel
        // utilisateur crée dans testSaveUser
    }

    @Test
    @Order(2)
    public void testFindByLoginAndPassword() {

        User user = new User("admin@admin.com", "admin");
        HttpEntity<User> request = new HttpEntity<>(user, httpHeaders);

        ResponseEntity<User> responseEntity = restTemplate
            .exchange(getURLWithPort("/springboot-restserver/auth/user/users/login"), HttpMethod.POST, request, User.class);

        assertNotNull(responseEntity);
        User userFound = responseEntity.getBody();
        ObjectMapper mapper = new ObjectMapper();
        mapper.convertValue(userFound, User.class);

        assertEquals(HttpStatus.OK.value(), responseEntity.getStatusCode().value(), "Réponse inattendue");
        assertNotNull(userFound);
        assertEquals(Long.valueOf(1), userFound.getId());
    }

    @Test
    @Order(3)
    public void testFindByLoginAndPassword_notFound() {

        User user = new User("unknowUser", "password3");
        HttpEntity<User> request = new HttpEntity<>(user, httpHeaders);

        ResponseEntity<User> responseEntity = restTemplate
            .exchange(getURLWithPort("/springboot-restserver/auth/user/users/login"), HttpMethod.POST, request, User.class);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.NOT_FOUND.value(), responseEntity.getStatusCode().value(), "Réponse inattendue");
    }

    @Test
    @Order(4)
    public void testUpdateUser() {

        User user = new User("login3", "password3");
        HttpEntity<User> request = new HttpEntity<>(user, httpHeaders);

        ResponseEntity<User> responseEntityToUpdate = restTemplate
            .exchange(getURLWithPort("/springboot-restserver/auth/user/users/login"), HttpMethod.POST, request, User.class);

        User userFromDBtoUpdate = responseEntityToUpdate.getBody();
        // on met à jour l'utilisateur en lui attribuant le role admin, nouveau login et mot de passe
        assertNotNull(userFromDBtoUpdate);
        userFromDBtoUpdate.setLogin("newLogin");
        userFromDBtoUpdate.setPassword("newPassword");
        userFromDBtoUpdate.setActive("1");
        Role role = new Role("ROLE_ADMIN");
        userFromDBtoUpdate.getRoles().add(role);

        HttpEntity<User> requestEntity = new HttpEntity<>(userFromDBtoUpdate, httpHeaders);

        ResponseEntity<User> responseEntity = restTemplate.exchange(getURLWithPort("/springboot-restserver/auth/user/users"), HttpMethod.PUT, requestEntity, User.class);
        assertNotNull(responseEntity);
        User userUpdated = responseEntity.getBody();
        assertNotNull(userUpdated);
        assertEquals(HttpStatus.OK.value(), responseEntity.getStatusCode().value(), "Réponse inattendue");
        assertEquals(userFromDBtoUpdate.getLogin(), userUpdated.getLogin());
    }

    @Test
    @Order(5)
    public void testSaveUser() {

        User user = new User("PIPO", "newPassword", "1");
        Role role_user = new Role("ROLE_USER");
        Role role_admin = new Role("ROLE_ADMIN");
        user.setRoles(Set.of(role_user, role_admin));
        HttpEntity<User> request = new HttpEntity<>(user, httpHeaders);

        ResponseEntity<User> userEntitySaved = restTemplate
            .exchange(getURLWithPort("/springboot-restserver/auth/user/users"), HttpMethod.POST, request, User.class);

        assertNotNull(userEntitySaved);
        // On vérifie le code de réponse HTTP est celui attendu
        assertEquals(HttpStatus.CREATED.value(), userEntitySaved.getStatusCode().value(), "Réponse inattendue");
        User userSaved = userEntitySaved.getBody();
        assertNotNull(userSaved);
        assertEquals(user.getLogin(), userSaved.getLogin());
        Set<String> roles = userSaved.getRoles().stream().map(Role::getRoleName).collect(Collectors.toSet());
        assertTrue(roles.containsAll(List.of("ROLE_USER", "ROLE_ADMIN")));
    }

    @Test
    @Order(6)
    public void testDeleteUser() {

        URI uri = UriComponentsBuilder.fromHttpUrl(url).path("/springboot-restserver/auth/user/users")
            .queryParam("id", 2L).build().toUri();
        HttpEntity<User> request = new HttpEntity<>(httpHeaders);

        ResponseEntity<Void> responseEntity = restTemplate.exchange(uri, HttpMethod.DELETE, request, Void.class);
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.GONE.value(), responseEntity.getStatusCode().value(), "Réponse inattendue");
    }
}
