package pe.gob.reniec.rrcc.plataformaelectronica.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DetalleSolDetLibroResponse {
  private Long idDetalleSolicitud;
  private String articulo;
  private String codigoArticulo;
  private String idLengua;
  private String lengua;
  private int cantidad;
  private String numeroUltimaActa;
}
