package com.lcjian.spunsugar.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@EnableWebSecurity
class SecurityContext {

    @Configuration
    @Order(1)
    public static class ApiWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {
        
        @Autowired
        private AuthenticationProvider authenticationProvider;
        
        @Override
        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
            auth.authenticationProvider(authenticationProvider);
        }
        
        protected void configure(HttpSecurity http) throws Exception {
            http.antMatcher("/api/**")
                    .authorizeRequests()
                        .anyRequest().authenticated()
                        .and()
                    .httpBasic()
                        .and()
                    .csrf().disable()
                    .sessionManagement().disable();
        }
    }

    @Configuration
    public static class FormLoginWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {
        
        @Override
        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
            auth.inMemoryAuthentication()
                .withUser("user").password("9a11b00c").roles("USER").and()
                .withUser("admin").password("9a11b00c").roles("USER", "ADMIN");
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.authorizeRequests()
                    .anyRequest().authenticated()
                    .and()
                .formLogin()
                    .and()
                .csrf().disable();
        }
    }
}
