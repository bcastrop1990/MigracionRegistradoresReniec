package pe.gob.reniec.rrcc.plataformaelectronica.model.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class SolRegFirmaUsuarioInterRequest {
  //@NotNull(message = "El campo celular es requerido.")
  //@Size(min = 7, max = 12, message = "el campo celular debe ser de 7 a 12 caracteres.")
  private String celular;
  //@Pattern(regexp = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$", message = "El email no es válido.")
  private String email;
  private String codigoTipoArchivoSustento;
  @NotNull(message = "El campo archivoSustento es requerido")
  @Valid
  private List<ArchivoRequest> listArchivoSustento;
  @NotNull(message = "El campo codigoModoRegistro es requerido.")
  private String codigoModoRegistro;
  @NotNull(message = "El campo detalleSolicitud es requerido")
  @Size(min = 1, message = "La solicitud no tiene detalle.")
  @Valid
  private List<DetalleSolicitudRegFirmaRequest> detalleSolicitud;
  private String codigoOrecSolicitante;
  private String dniSolicitante;
  private String preNombreSolicitante;
  private String primerApeSolicitante;
  private String segundoApeSolicitante;
}
