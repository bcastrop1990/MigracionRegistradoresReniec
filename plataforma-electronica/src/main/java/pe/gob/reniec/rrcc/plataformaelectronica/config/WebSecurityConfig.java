package pe.gob.reniec.rrcc.plataformaelectronica.config;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import pe.gob.reniec.rrcc.plataformaelectronica.security.JWTAuthorizationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@AllArgsConstructor
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

  private final SecurityProperties securityProperties;

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
        .cors()
        .and()
        .csrf().disable()
        .authorizeRequests()
        .antMatchers(HttpMethod.POST, "/migracion/migrar").permitAll()
        .antMatchers(HttpMethod.POST, "/migracion/eliminar").permitAll()
        .antMatchers("/swagger-resources/**",
            "/swagger-ui/**",
            "/v3/api-docs").permitAll()
        .anyRequest().authenticated()
        .and()
        .addFilter(new JWTAuthorizationFilter(authenticationManager(), securityProperties))
        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
  }
}



