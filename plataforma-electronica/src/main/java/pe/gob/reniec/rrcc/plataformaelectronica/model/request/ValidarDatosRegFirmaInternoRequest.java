package pe.gob.reniec.rrcc.plataformaelectronica.model.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
public class ValidarDatosRegFirmaInternoRequest {
 @NotNull(message = "El DNI es requerido")
 private String dni;
 @NotNull(message = "Datos de oficinas es requerido")
 @Valid
 private DatosOficinaRegFirmaRequest datosOficina;
}
