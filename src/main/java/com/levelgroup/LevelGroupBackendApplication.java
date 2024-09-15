package com.levelgroup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
//import org.springframework.context.annotation.Bean;
//import org.springframework.http.HttpMethod;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.web.SecurityFilterChain;
//
//import static org.springframework.security.config.Customizer.withDefaults;

@SpringBootApplication
//@EnableWebSecurity
public class LevelGroupBackendApplication {


//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//                .csrf(option -> option.disable())
//                .authorizeHttpRequests(auth -> {
//                    auth.requestMatchers("/test").hasRole("ADMIN");
//                    auth.requestMatchers("/").permitAll();
//                    auth.requestMatchers(HttpMethod.GET, "/{type}").permitAll();
//                    auth.requestMatchers(HttpMethod.GET, "/{type}/{category}").permitAll();
//                    auth.requestMatchers(HttpMethod.GET, "/{type}/{category}/{id}").permitAll();
//                    auth.requestMatchers(HttpMethod.POST, "/submit-form").permitAll();
//                })
//                .httpBasic(withDefaults());
//
//        return http.build();
//    }


    public static void main(String[] args) {
        SpringApplication.run(LevelGroupBackendApplication.class, args);
    }
}
