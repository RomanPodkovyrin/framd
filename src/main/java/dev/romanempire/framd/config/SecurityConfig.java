//package dev.romanempire.framd.config;
//
//import dev.romanempire.framd.acl.Roles;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.Customizer;
//import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
//import org.springframework.security.core.userdetails.User;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.provisioning.InMemoryUserDetailsManager;
//import org.springframework.security.web.SecurityFilterChain;
//
//@Configuration
//@EnableMethodSecurity // allows to define roles on methods directly
//public class SecurityConfig {
//
//    @Value("${admin.password}")
//    private String adminPassword;
//
//    @Bean
//    public UserDetailsService userDetailsService() {
//        UserDetails userDetails = User
//                .withUsername("admin")
//                .password(passwordEncoder().encode(adminPassword))
//                .roles(Roles.ADMIN)
//                .build();
//        return new InMemoryUserDetailsManager(userDetails);
//
//    }
//
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//
////    @Bean
////    UrlBasedCorsConfigurationSource corsConfigurationSource() {
////        CorsConfiguration corsConfiguration = new CorsConfiguration();
////        corsConfiguration.setAllowedOrigins(List.of("http://localhost:7878/"));
////        corsConfiguration.setAllowedMethods(List.of("GET","POST"));
////        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
////        source.registerCorsConfiguration("/**", corsConfiguration);
////        return source;
////    }
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
//        return http.authorizeHttpRequests(auth -> auth
////                        .requestMatchers("/").permitAll()
//                        .requestMatchers("/scan").permitAll()
////                        .requestMatchers("/admin").hasRole("ADMIN")
//                        .anyRequest()
//                        .authenticated())
//                .formLogin(Customizer.withDefaults()) // default login page
//                .cors(AbstractHttpConfigurer::disable)
//                .csrf(csrf -> csrf.ignoringRequestMatchers("/scan"))
//                .build();
////        return http.authorizeHttpRequests(authorizeRequests -> authorizeRequests.anyRequest()
////                .permitAll()).build();
//    }
//}
