package com.springbootfproject.firstWebApp.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.springbootfproject.firstWebApp.service.CustomUserDetailsServices;

@Configuration
@EnableWebSecurity
public class SpringSecurityConfigration {

    @Autowired
    CustomUserDetailsServices customUserDetailsServices;

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @SuppressWarnings("removal")
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
            .authorizeHttpRequests()
            .requestMatchers(
                "/userDetails/**",
                "/todo/**"
            //    "/todo/list-todos",
            //    "/todo/add-todo",
             //   "/todo/delete-todo",
            //    "/todo/update-todo",
             //   "/todo/filter-todos"
            ).permitAll()
            .and()
            .formLogin()
            .loginPage("/userDetails/login")
            .loginProcessingUrl("/userDetails/login")
            .defaultSuccessUrl("/userDetails/home", true)
            .permitAll()
            .and()
            .logout()
            .invalidateHttpSession(true)
            .clearAuthentication(true)
            .logoutRequestMatcher(new AntPathRequestMatcher("/userDetails/logout"))
            .logoutSuccessUrl("/userDetails/login?logout")
            .permitAll();
        return http.build();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(customUserDetailsServices).passwordEncoder(passwordEncoder());
    }
}
