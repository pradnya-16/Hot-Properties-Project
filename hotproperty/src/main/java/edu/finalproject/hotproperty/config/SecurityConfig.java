package edu.finalproject.hotproperty.config;

import edu.finalproject.hotproperty.services.CustomUserDetailsService;
import edu.finalproject.hotproperty.utils.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

  @Autowired private CustomUserDetailsService customUserDetailsService;

  @Autowired private JwtAuthenticationFilter jwtAuthenticationFilter;

  @Autowired private CustomAuthenticationEntryPoint unauthorizedHandler;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public DaoAuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(customUserDetailsService);
    authProvider.setPasswordEncoder(passwordEncoder());
    return authProvider;
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig)
      throws Exception {
    return authConfig.getAuthenticationManager();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(
                        "/",
                        "/home",
                        "/login",
                        "/register",
                        "/css/**",
                        "/images/**",
                        "/js/**",
                        "/webjars/**",
                        "/error",
                        "/properties/**")
                    .permitAll()
                    .requestMatchers("/users/admin/**")
                    .hasRole("ADMIN")
                    .requestMatchers(
                        "/agent/**",
                        "/properties/add",
                        "/properties/manage/**",
                        "/properties/edit/**")
                    .hasRole("AGENT")
                    .requestMatchers(
                        "/buyer/**",
                        "/favorites",
                        "/favorites/**",
                        "/messages/buyer",
                        "/messages/send/**",
                        "/messages/delete/**",
                        "/properties/list",
                        "/properties/view/**")
                    .hasRole("BUYER")
                    .anyRequest()
                    .authenticated())
        .logout(
            logout ->
                logout
                    .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                    .logoutSuccessUrl("/login?logout")
                    .invalidateHttpSession(true)
                    .deleteCookies("jwtTokenHotProperties")
                    .permitAll());

    http.authenticationProvider(authenticationProvider());
    http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}
