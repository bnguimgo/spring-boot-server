package com.bnguimgo.springbootrestserver.model;

import jakarta.persistence.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "UTILISATEUR")
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    //GenerationType strategy: https://blog.stackademic.com/avoid-using-generationtype-auto-strategy-for-id-generation-in-spring-boot-a67c6239eb7d
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "UserSequenceParam")
    @SequenceGenerator(name = "UserSequenceParam", sequenceName = "UserSequenceName", initialValue = 4, allocationSize = 1)
    @Column(name = "USER_ID", unique = true, updatable = false, nullable = false)
    private Long id;

    @Column(name = "LOGIN", unique = true, nullable = false)
    private String login;

    @Column(name = "USER_PASSWORD", nullable = false)
    private String password;

    @Column(name = "USER_ACTIVE", nullable = false)
    private String active;

    @ManyToMany//(cascade = CascadeType.DETACH)
    @JoinTable(
        name = "USER_ROLE",
        joinColumns = @JoinColumn(name = "USER_ID", nullable = false, updatable = false, insertable = false),
        inverseJoinColumns = @JoinColumn(name = "ROLE_ID", nullable = false, updatable = false, insertable = false))
    private Set<Role> roles = new HashSet<>();

    public User() {
        super();
    }

    public User(String login, String password, String active) {
        this.login = login;
        this.password = password;
        this.active = active;
    }

    public User(Long id, String login, String password, String active) {
        this.id = id;
        this.login = login;
        this.password = password;
        this.active = active;
    }

    public User(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public void addRole(Role role) {
        this.roles.add(role);
        role.getUsers().add(this);
    }

    public void removeRole(Role role) {
        this.roles.remove(role);
        role.getUsers().remove(this);
    }


    @Override
    public String toString() {
        return "User [id=" + id + ", login=" + login + ", password=XXXXXXX, active=" + active + ", roles="
            + roles + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return Objects.equals(getActive(), user.getActive()) && Objects.equals(getId(), user.getId()) && getLogin().equals(user.getLogin()) && Objects.equals(getPassword(), user.getPassword()) && Objects.equals(getRoles(), user.getRoles());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getLogin(), getPassword(), getActive(), getRoles());
    }
}