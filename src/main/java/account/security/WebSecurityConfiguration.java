package account.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration {
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return new CustomAccessDeniedHandler();
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return new RestAuthenticationEntryPoint();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.httpBasic()
                .authenticationEntryPoint(authenticationEntryPoint())
                .and()
                .csrf().disable().headers().frameOptions().disable()
                .and()
                .authorizeRequests()
                .antMatchers("/api/admin/**").hasRole("ADMINISTRATOR")
                .antMatchers("/api/empl/payment").hasAnyRole("USER", "ACCOUNTANT")
                .antMatchers("/api/acct/**").hasRole("ACCOUNTANT")
                .antMatchers("/api/auth/signup").permitAll()
                .antMatchers("/h2/**").permitAll()
                .anyRequest().authenticated()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and().exceptionHandling().accessDeniedHandler(accessDeniedHandler());

        return http.build();
    }

}
