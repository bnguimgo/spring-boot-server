package com.bnguimgo.springbootrestserver.model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "ROLE")
public class Role implements Serializable{

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "RoleSequenceParam")//IDENTITY ==> c'est la base de données qui va générer la clé primaire afin d'éviter les doublons, car cette table contient déjà les données à l'initialisation
	@SequenceGenerator(name = "RoleSequenceParam", sequenceName = "RoleSequenceName", initialValue = 3, allocationSize = 1)
	@Column(name = "ROLE_ID", unique = true, updatable = false, nullable = false)
	private Long id;
	
	@Column(name="ROLE_NAME", nullable = false)
	private String roleName;

	@ManyToMany(fetch = FetchType.LAZY, mappedBy = "roles")
	@JsonIgnore
	private Set<User> users = new HashSet<>();

	public Role(){
		super();
	}
	public Role(String roleName){
		super();
		this.roleName = roleName;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getRoleName() {
		return roleName;
	}
	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}
	public Set<User> getUsers() {
		return users;
	}

	public void setUsers(Set<User> users) {
		this.users = users;
	}

	public void addUser(User user) {
		this.users.add(user);
		user.getRoles().add(this);
	}
	public void removeUser(User user) {
		this.users.remove(user);
		user.getRoles().remove(this);
	}

	@Override
	public String toString() {
		return "Role [id=" + id + ", role=" + roleName + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, roleName);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Role other = (Role) obj;
		if (!Objects.equals(id, other.id))
			return false;
		if (roleName == null) {
			return other.roleName == null;
		} else return roleName.equals(other.roleName);
	}
	
	public int compareTo(Role role){
		return this.roleName.compareTo(role.getRoleName());
		
	}
}