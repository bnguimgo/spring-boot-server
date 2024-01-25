package com.bnguimgo.springbootrestserver.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.bnguimgo.springbootrestserver.dao.RoleRepository;
import com.bnguimgo.springbootrestserver.model.Role;

public class RoleServiceImplTest {

	RoleRepository roleRepository = Mockito.mock(RoleRepository.class);
	
    @Test
    public void test_getAllRoles(){

        RoleServiceImpl roleService = new  RoleServiceImpl (roleRepository );
 
        Role role = new Role("USER_ROLE");
    	List<Role> roles = Arrays.asList(role);
    	
        Mockito.when(roleRepository.findAll()).thenReturn(roles);
        Collection<Role> result = roleService.getAllRoles();
 
        Assertions.assertThat(result).isNotNull().hasSize(1);
    }
    
    @Test
    public void test_getAllRolesStream(){

        RoleServiceImpl roleService = new  RoleServiceImpl (roleRepository );
 
        Role role = new Role("USER_ROLE");
    	List<Role> roles = Arrays.asList(role);
    	
        Mockito.when(roleRepository.findAll()).thenReturn(roles);
        Collection<Role> result = roleService.getAllRoles();
 
        Assertions.assertThat(result).isNotNull().hasSize(1);
    }
}
