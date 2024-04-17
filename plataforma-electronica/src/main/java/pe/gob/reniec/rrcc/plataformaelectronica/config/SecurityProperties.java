package pe.gob.reniec.rrcc.plataformaelectronica.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("application.security")
@Getter
@Setter
public class SecurityProperties {
    private String secretKey;
    private Long expirationTime;
}
