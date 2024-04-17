package pe.gob.reniec.rrcc.plataformaelectronica.security;

import lombok.*;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class UserInfo {
  private String dni;
  private String primerApellido;
  private String segundoApellido;
  private String preNombre;
  private String codigoOrec;
  private List<String> permisos;
  private BaseModel perfil;
  private BaseModel grupo;
}
