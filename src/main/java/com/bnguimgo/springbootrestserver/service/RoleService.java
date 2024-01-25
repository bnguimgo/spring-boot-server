package com.bnguimgo.springbootrestserver.service;
import java.util.Collection;
import java.util.stream.Stream;

import com.bnguimgo.springbootrestserver.model.Role;

public interface RoleService {
	
	Role findByRoleName(String roleName);
	
	Collection<Role> getAllRoles();
	
	Stream<Role> getAllRolesStream();
}