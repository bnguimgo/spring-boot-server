package com.bnguimgo.springbootrestserver.service;
import java.util.Collection;
import java.util.stream.Stream;

import org.apache.commons.collections4.IteratorUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bnguimgo.springbootrestserver.dao.RoleRepository;
import com.bnguimgo.springbootrestserver.model.Role;


@Service
public class RoleServiceImpl implements RoleService {
	
	
	private RoleRepository roleRepository;
	
	public RoleServiceImpl() {
		super();
	}
	@Autowired //autowired par constructeur pour bénéficier des tests unitaires
	public RoleServiceImpl(RoleRepository roleRepository) {
		super();
		this.roleRepository = roleRepository;
	}

	@Override
	public Collection<Role> getAllRoles() {
		return IteratorUtils.toList(roleRepository.findAll().iterator());
	}
	
	@Override
	public Stream<Role> getAllRolesStream() {
		return roleRepository.getAllRolesStream();
	}
	
	@Override
	public Role findByRoleName(String roleName) {
		return roleRepository.findByRoleName(roleName);
	}
}