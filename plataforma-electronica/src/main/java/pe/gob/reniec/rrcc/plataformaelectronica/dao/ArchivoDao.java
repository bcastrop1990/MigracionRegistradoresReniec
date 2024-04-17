package pe.gob.reniec.rrcc.plataformaelectronica.dao;

import java.util.List;
import java.util.Optional;
import pe.gob.reniec.rrcc.plataformaelectronica.model.bean.ArchivoBean;

public interface ArchivoDao {
  void registrar(ArchivoBean archivo);
  void eliminarArchivoDet(String idArchivoDetalle);
  void eliminarArchivoSust(String idArchivoSustento);

  void registrarDocAtencion(ArchivoBean archivo);
  Optional<ArchivoBean> obtener(Long id);

  List<ArchivoBean> obtenerPorIdSolicitud(Long idSolicitud);
  List<ArchivoBean> obtenerRespuestaPorIdSol(Long idSolicitud);
  Optional<ArchivoBean> obtenerPorCodigo(String nombre);

  void actualizarEstado(Long id, String estado);
  void actualizarIdSolicitud(Long idArchivo, Long idSolicitud, String estado, String tipoArchivo);
  void actualizarIdSolicitudDetalle(Long idArchivo, String estado);
  void eliminarArchivo(Long idArchivo);

}
