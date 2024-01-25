package com.bnguimgo.springbootrestserver.security.config;

import com.bnguimgo.springbootrestserver.security.component.JwtAuthenticationEntryPoint;
import com.bnguimgo.springbootrestserver.security.component.JwtRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Classe chargée dès le démarrage du serveur d'application "/swagger-ui/**"
 */

//Sources:
//https://docs.spring.io/spring-security/reference/5.8/migration/servlet/session-management.html
//For deprecated explanation:
//https://stackoverflow.com/questions/76993442/deprecated-csrf-and-requireschannel-methods-after-spring-boot-v3-migration#:~:text=However%2C%20the%20.-,csrf()%20and%20.,longer%20work%20because%20of%20deprecations.
@Configuration
@EnableWebSecurity
@EnableMethodSecurity()
@ComponentScan({"com.bnguimgo.springbootrestserver.security"})
public class WebSecurityConfig {

	@Autowired
	private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

/*	@Autowired
	private UserDetailsService jwtUserDetailsService;*/

	@Autowired
	private JwtRequestFilter jwtRequestFilter;

	@Autowired
	private PasswordEncoder passwordEncoder;

/*	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		// configure AuthenticationManager so that it knows from where to load
		// user for matching credentials
		// Use BCryptPasswordEncoder
		auth.userDetailsService(jwtUserDetailsService).passwordEncoder(passwordEncoder);
	}*/

/*	@Bean
	public PasswordEncoder passwordEncoderBean() {
		return new BCryptPasswordEncoder();
	}*/

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
		httpSecurity.csrf(AbstractHttpConfigurer::disable);
		//httpSecurity.requiresChannel(c -> c.requestMatchers("/actuator/**").requiresInsecure());
		httpSecurity.authorizeHttpRequests(request -> {
			request.requestMatchers(
				"/token/**",
				"/auth/user/**",
				"/auth/user/**.*",
				"/swagger-ui/**",
				"/docs/**",
				"/api/v*/registration/**",
				"/register*",
				"/login",
				"/actuator/**").permitAll();
			request.anyRequest().authenticated();
		});
		//httpSecurity.exceptionHandling((exception)-> exception.authenticationEntryPoint(jwtAuthenticationEntryPoint).accessDeniedPage("/error/accedd-denied"));
		httpSecurity.exceptionHandling((exception)-> exception.authenticationEntryPoint(jwtAuthenticationEntryPoint));
		httpSecurity.sessionManagement((sessions) -> sessions.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		//httpSecurity.sessionManagement((sessions) -> sessions.requireExplicitAuthenticationStrategy(false));

		//TODO for login form
/*		httpSecurity.formLogin(fL -> fL.loginPage("/login")
			.usernameParameter("email").permitAll()
			.defaultSuccessUrl("/", true)
			.failureUrl("/login-error"));*/

		//TODO for logOut form
/*		httpSecurity.logout(logOut -> logOut.logoutUrl("/logout")
			.clearAuthentication(true)
			.invalidateHttpSession(true)
			.deleteCookies("JSESSIONID","Idea-2e8e7cee")
			.logoutSuccessUrl("/login"));*/

		httpSecurity.httpBasic(withDefaults());

		// Add a filter to validate the tokens with every request
		httpSecurity.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
		return httpSecurity.build();
	}

/*	@Bean
	public WebSecurityCustomizer webSecurityCustomizer() {
		return (web) -> web.ignoring().requestMatchers("auth/user", "/ignore2");
	}*/
}