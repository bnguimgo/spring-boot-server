package com.bnguimgo.springbootrestserver.service;

import com.bnguimgo.springbootrestserver.dao.RoleRepository;
import com.bnguimgo.springbootrestserver.dao.UserRepository;
import com.bnguimgo.springbootrestserver.exception.BusinessResourceException;
import com.bnguimgo.springbootrestserver.model.Role;
import com.bnguimgo.springbootrestserver.model.User;
import org.apache.commons.collections4.IteratorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private PasswordEncoder bCryptPasswordEncoder;

    public UserServiceImpl() {
        super();
    }

    @Autowired
    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder bCryptPasswordEncoder) {
        super();
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Override
    public User findByLogin(String login) throws BusinessResourceException {

        return userRepository.findByLogin(login).orElseThrow(() -> new BusinessResourceException("User Not Found", "L'utilisateur avec ce login n'existe pas: "+ login, HttpStatus.NOT_FOUND));
    }

    @Override
    public Collection<User> getAllUsers() {
        return IteratorUtils.toList(userRepository.findAll().iterator());
    }

    @Override
    public User findUserById(Long id) throws BusinessResourceException {

        return userRepository.findById(id).orElseThrow(() -> new BusinessResourceException("User Not Found", "Aucun utilisateur avec l'identifiant :" + id, HttpStatus.NOT_FOUND));
    }

    @Override
    @Transactional
    public User saveOrUpdateUser(User user) throws BusinessResourceException {
        try {
            if (null == user.getId()) {//pas d'Id --> création d'un user
                addUserRole(user);//Ajout d'un rôle par défaut
                user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
            } else {//sinon, mise à jour d'un user

                User userFromDB = findUserById(user.getId());
                if (!bCryptPasswordEncoder.matches(user.getPassword(), userFromDB.getPassword())) {
                    user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));//MAJ du mot de passe s'il a été modifié
                } else {

                    user.setPassword(userFromDB.getPassword());//Sinon, on remet le password déjà haché
                }
                updateUserRole(user);//On extrait le rôle en cas de mise à jour
            }

            return userRepository.save(user);

        } catch (DataIntegrityViolationException ex) {
            logger.error("Utilisateur non existant", ex);
            throw new BusinessResourceException("DataIntegrityViolationException", ex.getMessage() + " for " + user.getLogin(), HttpStatus.CONFLICT);
        } catch (BusinessResourceException e) {
            logger.error("Utilisateur non existant", e);
            throw new BusinessResourceException("UserNotFound", "Aucun utilisateur avec l'identifiant: " + user.getId(), HttpStatus.NOT_FOUND);
        } catch (Exception ex) {
            logger.error("Erreur technique de création ou de mise à jour de l'utilisateur", ex);
            throw new BusinessResourceException("SaveOrUpdateUserError", "Erreur technique de création ou de mise à jour de l'utilisateur: " + user.getLogin(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public void deleteUser(Long id) throws BusinessResourceException {
        try {
            userRepository.deleteById(id);
        } catch (EmptyResultDataAccessException ex) {
            logger.error(String.format("Aucun utilisateur n'existe avec l'identifiant {}", id), ex);
            throw new BusinessResourceException("DeleteUserError", "Erreur de suppression de l'utilisateur avec l'identifiant: "+ id, HttpStatus.NOT_FOUND);
        } catch (Exception ex) {
            throw new BusinessResourceException("DeleteUserError", "Erreur de suppression de l'utilisateur avec l'identifiant: "+ id, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    //@Cacheable(value = "userCache", key = "#login", unless = "#result == null")
    @Cacheable(value = "userCache", key ="#p0")
    public User findByLoginAndPassword(String login, String password) throws BusinessResourceException {
        try {
            User userFound = this.findByLogin(login);
            if (bCryptPasswordEncoder.matches(password, userFound.getPassword())) {
                return userFound;
            } else {
                throw new BusinessResourceException("UserNotFound", "Mot de passe incorrect", HttpStatus.NOT_FOUND);
            }
        } catch (BusinessResourceException ex) {
            logger.error("Login ou mot de passe incorrect", ex);
            throw new BusinessResourceException("UserNotFound", "Login ou mot de passe incorrect", HttpStatus.NOT_FOUND);
        } catch (Exception ex) {
            logger.error("Une erreur technique est survenue", ex);
            throw new BusinessResourceException("TechnicalError", "Une erreur technique est survenue", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void addUserRole(User user) {
/*        Set<Role> roles = new HashSet<>();
        Role roleUser = new Role("ROLE_USER");//initialisation du rôle ROLE_USER
        roleUser.addUser(user);
        //roles.add(roleUser);
        user.setActive(Integer.valueOf(0));
        Role roleUserDB = roleRepository.findByRoleName("ROLE_USER");
        //user.setRoles(Set.of(roleUser));
        user.addRole(roleUser); */

/*        Set<Role> roleFromDB = extractRole_Java8(roles, roleRepository.getAllRolesStream());
        user.setRoles(roleFromDB);*/

        //Role roleUserDB = roleRepository.findByRoleName("ROLE_USER");
        //user.setRoles(Set.of(roleUserDB));

        Set<String> names = user.getRoles().stream().map(Role::getRoleName).collect(Collectors.toSet());
        Set<Role> roleUserDB = roleRepository.findAllByRoleNameIn(names);
        user.getRoles().clear();
        //roleUserDB.forEach(role -> role.addUser(user));
        roleUserDB.forEach(user::addRole);

        //user.setRoles(roleUserDB);
    }

    private void updateUserRole(User user) {

        Set<Role> roleFromDB = extractRole_Java8(user.getRoles(), roleRepository.getAllRolesStream());
        user.setRoles(roleFromDB);
    }

    private Set<Role> extractRole_Java8(Set<Role> rolesSetFromUser, Stream<Role> roleStreamFromDB) {
        // Collect UI role names
        Set<String> uiRoleNames = rolesSetFromUser.stream()
                .map(Role::getRoleName)
                .collect(Collectors.toCollection(HashSet::new));
        // Filter DB roles
        return roleStreamFromDB
                .filter(role -> uiRoleNames.contains(role.getRoleName()))
                .collect(Collectors.toSet());
    }

    @SuppressWarnings("unused")
    private Set<Role> extractRoleUsingCompareTo_Java8(Set<Role> rolesSetFromUser, Stream<Role> roleStreamFromDB) {
        return roleStreamFromDB
                .filter(roleFromDB -> rolesSetFromUser.stream()
                        .anyMatch(roleFromUser -> roleFromUser.compareTo(roleFromDB) == 0))
                .collect(Collectors.toCollection(HashSet::new));
    }

    @SuppressWarnings("unused")
    private Set<Role> extractRole_BeforeJava8(Set<Role> rolesSetFromUser, Collection<Role> rolesFromDB) {
        Set<Role> rolesToAdd = new HashSet<>();
        for (Role roleFromUser : rolesSetFromUser) {
            for (Role roleFromDB : rolesFromDB) {
                if (roleFromDB.compareTo(roleFromUser) == 0) {
                    rolesToAdd.add(roleFromDB);
                    break;
                }
            }
        }
        return rolesToAdd;
    }
}