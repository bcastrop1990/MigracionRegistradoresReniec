package pe.gob.reniec.rrcc.plataformaelectronica.dao;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pe.gob.reniec.rrcc.plataformaelectronica.model.bean.*;
import pe.gob.reniec.rrcc.plataformaelectronica.model.request.DetalleSolicitudRegFirmaRequest;

public interface SolicitudDao {
  Long registrar(SolicitudBean solicitud);
  Long registrarUsuarioInterno(SolicitudBean solicitud);
  void registrarHistorial(SolicitudBean solicitud);
  void registrarDetalleFirma(DetalleSolicitudFirmaBean detalle);
  void actualizarDetalleFirma(DetalleSolicitudFirmaBean detalle);
  void registrarDetalleLibro(DetalleSolicitudLibroBean detalle);
  void actualizarDetalleLibro(DetalleSolicitudLibroBean detalle);
  void registrarDetalleArchivoFirma(DetalleSolicitudArchivoFirmaBean archivo);
  void actualizarIdSolicitud(DetalleSolicitudArchivoFirmaBean archivo);
  Optional<SolicitudBean> obtenerPorNumero(String numero);
  Optional<SolicitudBean> obtenerSolFirmaByDniReg(String dni);
  List<DetalleSolicitudFirmaBean> listarByIdSolicitud(Long idSolicitud);
  List<DetalleSolicitudFirmaBean> listarByDni(String dni);
  boolean validarArchivoDetalle(Long idArchivo);
  boolean validarArchivoSustento(Long idArchivo);

  Page<SolicitudBean> consultarSeguimiento(String nroSolicitud,Pageable pageable, String dni, String fechaIni, String fechaFin);
  Page<SolicitudBean> consultar(SolicitudBean bean, Pageable pageable, String fechaIni, String fechaFin);
  Page<SolicitudBean> consultarAnalista(SolicitudBean bean, Pageable pageable, String fechaIni, String fechaFin);

  Page<SolicitudBean> consultarExpediente(SolicitudBean bean, Pageable pageable, String fechaIni, String fechaFin);
  void recepcionar(String nroSolicitud, String codigoEstado, String codigoUsuario);
  void recepcionarHistorial(String nroSolicitud, String codigoEstado, String codigoUsuario);

  void eliminarSolicitud(String nroSolicitud);
  void eliminarDetalleFirma(Long idDetalle);
  void eliminarDetalleLibro(Long idDetalle);

  void asignarAnalista(String nroSolicitud, String codigoAnalista, String codigoEstado);

  void asignarAnalistaHistorial(String nroSolicitud, String codigoAnalista, String codigoEstado, String dniCoordinador);

  void reasignarAnalista(String nroSolicitud, String codigoAnalista, String codigoEstado);
  void reasignarAnalistaHistorial(String nroSolicitud, String codigoAnalista, String codigoEstado, String dniCoordinador);

  List<DetalleSolicitudLibroBean> listarLibrosFullBySolicitud(Long idSolicitud);
  void actualizarArchivoRespuesta(Long idSolicitud, String codigoTipoArchivo, Long idArchivoSustento);
  void actualizarEstadoDetalleSolLibroBySol(Long idSolicitud, String estado);
  void actualizarEstadoSolicitud(Long idSolicitud, String estado);
  void actualizarEstadoSolicitudHistorial(Long idSolicitud, String estado, String numeroSolicitud);

  List<DetalleSolicitudArchivoFirmaBean> listarArchivoFirmaByDetalleId(Long idDetalle);

  void actualizarEstadoFirmaDetSolById(Long idDetalleSolicitud, String codEstadoFirma);


}
