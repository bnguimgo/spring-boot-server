package com.bnguimgo.springbootrestserver.dao;
import java.util.Set;
import java.util.stream.Stream;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.bnguimgo.springbootrestserver.model.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
	
	Role findByRoleName(String roleName);

	@Query("select role from Role role")
	Stream<Role> getAllRolesStream();// Java8 Stream

	Set<Role> findAllByRoleNameIn(Set<String> names);
}