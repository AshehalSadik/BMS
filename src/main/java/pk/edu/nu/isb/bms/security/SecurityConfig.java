package pk.edu.nu.isb.bms.security;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import pk.edu.nu.isb.bms.models.MyUserService;

@Configuration
@AllArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private final MyUserService userService;

    @Bean
    public UserDetailsService userDetailsService() {
        return userService;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider(userService);
        return daoAuthenticationProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(httpForm -> {
                    httpForm.loginPage("/login").permitAll();
                })

                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/signup", "/css/**", "/js/**").permitAll();
                    auth.anyRequest().authenticated();
                })
                .build();
    }
}
