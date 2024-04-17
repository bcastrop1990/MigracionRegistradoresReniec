package pe.gob.reniec.rrcc.plataformaelectronica.model.bean;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DetalleSolicitudArchivoFirmaActualizarBean {

  private Long idDetalleSolicitudArchivo;
  private Long idDetalleSolicitud;
  private Long idArchivo;
  private ArchivoBean archivo;
  private String codigoTipoArchivo;
  private TipoArchivoBean tipoArchivo;
  private String codigoUsoArchivo;
  private String idCrea;
  private LocalDateTime fechaCrea;
  private LocalDateTime fechaActualiza;
  private String idActualiza;
  private String estado;
  private Long id_solicitud;
}
