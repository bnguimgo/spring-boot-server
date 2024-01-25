package com.bnguimgo.springbootrestserver.component;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
 
// ce filtre permet d'autoriser les requêtes Cross Domain (requêtes entre deux réseaux distincts)
//plus d'explications ici: http://sqli.developpez.com/tutoriels/spring/construire-backend-springboot/
@Component
public class CrossDomainFilter extends OncePerRequestFilter {
	@Override
	protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain)
			throws ServletException, IOException {
		httpServletResponse.addHeader("Access-Control-Allow-Origin", "*");//toutes les URI sont autorisées
   		httpServletResponse.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
   		httpServletResponse.addHeader("Access-Control-Allow-Headers", "origin, content-type, accept, x-req");
   		
   		filterChain.doFilter(httpServletRequest, httpServletResponse);		
	}

}