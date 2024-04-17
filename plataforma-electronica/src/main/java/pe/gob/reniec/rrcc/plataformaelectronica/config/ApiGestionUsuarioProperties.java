package pe.gob.reniec.rrcc.plataformaelectronica.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@ConfigurationProperties("application.ws-gestion-usuarios")
@Getter
@Setter
public class ApiGestionUsuarioProperties {
  private String urlIdentificarDni;
  private String urlCambioClave;
  private String urlObtenerPermisos;
  private String urlObtenerGrupoPerfiles;
  private String codigoAplicativo;
  private String ip;
  private String tiCodigo;
  //private Map<String, String> permisos;
  private Map<String, String> perfiles;
  private List<String> grupos;
  private String codigoPerfilAnalista="03"; //borrar para pase a produccion
}
