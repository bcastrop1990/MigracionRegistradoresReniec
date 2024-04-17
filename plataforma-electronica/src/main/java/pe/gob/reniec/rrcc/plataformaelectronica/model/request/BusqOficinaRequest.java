package pe.gob.reniec.rrcc.plataformaelectronica.model.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
public class BusqOficinaRequest {
  @NotNull(message = "El campo dni es requerido")
  private String dni;
  private String digitoVerifica;
  private String fechaEmision;
}
