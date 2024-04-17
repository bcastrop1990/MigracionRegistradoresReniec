package pe.gob.reniec.rrcc.plataformaelectronica.dao.impl;

import java.sql.PreparedStatement;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import lombok.AllArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import pe.gob.reniec.rrcc.plataformaelectronica.dao.ArchivoDao;
import pe.gob.reniec.rrcc.plataformaelectronica.model.bean.ArchivoBean;
import pe.gob.reniec.rrcc.plataformaelectronica.security.SecurityUtil;

@Repository
@AllArgsConstructor
public class ArchivoDaoImpl implements ArchivoDao {

  private JdbcTemplate jdbcTemplate;

  @Override
  public void registrar(ArchivoBean archivo) {
    String sql = "INSERT INTO IDO_PLATAFORMA_EXPE.EDTR_ARCHIVO(ID_ARCHIVO, CO_NOMBRE," +
        "DE_NOMBRE_ORIGINAL, CO_EXTENSION, IM_ARCHIVO, CO_ESTADO, FE_CREA, ID_CREA) \n" +
        "VALUES(IDO_PLATAFORMA_EXPE.EDSE_ARCHIVO.nextval,?,?,?,?,?,SYSDATE, ?)";
    KeyHolder keyHolder = new GeneratedKeyHolder();
      jdbcTemplate.update(connection -> {
          PreparedStatement ps = connection.prepareStatement(sql, new String[]{"ID_ARCHIVO"});
          ps.setString(1, archivo.getCodigoNombre());
          ps.setString(2, archivo.getNombreOriginal());
          ps.setString(3, archivo.getExtension());
          ps.setBytes(4, archivo.getArchivo());
          ps.setString(5, archivo.getEstado());
                  ps.setString(6, archivo.getIdCrea());
                  return ps;
        },
        keyHolder);
    archivo.setIdArchivo(keyHolder.getKey().longValue());
  }

    @Override
    public void eliminarArchivoDet(String idArchivoDetalle) {
        String sql = "UPDATE IDO_PLATAFORMA_EXPE.EDTD_DET_ARCH_SOL_FIRMA \n" +
                " SET CO_ESTADO = '0' \n" +
                "WHERE ID_ARCHIVO = ?";
        jdbcTemplate.update(sql, idArchivoDetalle);
    }
    @Override
    public void eliminarArchivoSust(String idArchivoSustento) {
        String sql = "UPDATE IDO_PLATAFORMA_EXPE.EDTR_ARCHIVO \n" +
                " SET CO_ESTADO = 3, ID_SOLICITUD = NULL  \n" +
                "WHERE ID_ARCHIVO = ?";
        jdbcTemplate.update(sql, idArchivoSustento);
    }
    @Override
  public void registrarDocAtencion(ArchivoBean archivo) {
    String sql = "INSERT INTO IDO_PLATAFORMA_EXPE.EDTR_ARCHIVO(ID_ARCHIVO, CO_NOMBRE," +
            "DE_NOMBRE_ORIGINAL, CO_EXTENSION, IM_ARCHIVO, CO_ESTADO, FE_CREA, ID_CREA, CO_TIPO_ARCHIVO) \n" +
            "VALUES(IDO_PLATAFORMA_EXPE.EDSE_ARCHIVO.nextval,?,?,?,?,?,SYSDATE, ?, ?)";
    KeyHolder keyHolder = new GeneratedKeyHolder();
    jdbcTemplate.update(connection -> {
              PreparedStatement ps = connection.prepareStatement(sql, new String[]{"ID_ARCHIVO"});
              ps.setString(1, archivo.getCodigoNombre());
              ps.setString(2, archivo.getNombreOriginal());
              ps.setString(3, archivo.getExtension());
              ps.setBytes(4, archivo.getArchivo());
              ps.setString(5, archivo.getEstado());
              ps.setString(6, archivo.getIdCrea());
              ps.setString(7, "99");

              return ps;
            },
            keyHolder);
    archivo.setIdArchivo(keyHolder.getKey().longValue());
  }

    @Override
    public Optional<ArchivoBean> obtener(Long id) {
        String sql = "SELECT ID_ARCHIVO AS id,CO_NOMBRE as codigoNombre,DE_NOMBRE_ORIGINAL as nombreOriginal, " +
                " CO_EXTENSION extension, CO_ESTADO as estado, IM_ARCHIVO as archivo " +
                " FROM IDO_PLATAFORMA_EXPE.EDTR_ARCHIVO WHERE ID_ARCHIVO = ?";
        try {
            return Optional.of(jdbcTemplate.queryForObject(sql,
                    BeanPropertyRowMapper.newInstance(ArchivoBean.class),
                    id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
  @Override
  public List<ArchivoBean> obtenerPorIdSolicitud(Long idSolicitud) {
    String sql = "SELECT ID_ARCHIVO AS idArchivo,CO_NOMBRE as codigoNombre,DE_NOMBRE_ORIGINAL as nombreOriginal, " +
            " CO_EXTENSION extension, CO_ESTADO as estado, IM_ARCHIVO as archivo, CO_TIPO_ARCHIVO as tipoCodigoNombre " +
            " FROM IDO_PLATAFORMA_EXPE.EDTR_ARCHIVO WHERE ID_SOLICITUD = ?";
    try {
      return jdbcTemplate.query(sql,
              BeanPropertyRowMapper.newInstance(ArchivoBean.class),
              idSolicitud);
    } catch (EmptyResultDataAccessException e) {
      return (List<ArchivoBean>) Collections.emptyMap();
    }
  }

    @Override
    public List<ArchivoBean> obtenerRespuestaPorIdSol(Long idSolicitud) {
        String sql = "SELECT EDTR_ARCHIVO.ID_ARCHIVO AS idArchivo, EDTR_ARCHIVO.CO_NOMBRE as codigoNombre, EDTR_ARCHIVO.DE_NOMBRE_ORIGINAL as nombreOriginal, " +
                " EDTC_SOLICITUD.CO_TIPO_ARCHIVO_RESPUESTA as tipoCodigoNombre, EDTR_ARCHIVO.CO_EXTENSION extension " +
                " FROM IDO_PLATAFORMA_EXPE.EDTR_ARCHIVO " +
                " INNER JOIN IDO_PLATAFORMA_EXPE.EDTC_SOLICITUD ON EDTR_ARCHIVO.ID_ARCHIVO = EDTC_SOLICITUD.ID_ARCHIVO_RESPUESTA WHERE EDTC_SOLICITUD.ID_SOLICITUD = ?";
        try {
            return jdbcTemplate.query(sql,
                    BeanPropertyRowMapper.newInstance(ArchivoBean.class),
                    idSolicitud);
        } catch (EmptyResultDataAccessException e) {
            return (List<ArchivoBean>) Collections.emptyMap();
        }
    }
  @Override
  public Optional<ArchivoBean> obtenerPorCodigo(String codigo) {
    String sql = "SELECT ID_ARCHIVO AS idArchivo,CO_NOMBRE as codigoNombre,DE_NOMBRE_ORIGINAL as nombreOriginal, " +
        " CO_EXTENSION extension, CO_ESTADO as estado, IM_ARCHIVO as archivo " +
            " FROM IDO_PLATAFORMA_EXPE.EDTR_ARCHIVO WHERE CO_NOMBRE = ?";
    try {
      return Optional.of(jdbcTemplate.queryForObject(sql,
          BeanPropertyRowMapper.newInstance(ArchivoBean.class),
          codigo));
    } catch (EmptyResultDataAccessException e) {
      return Optional.empty();
    }
  }
  @Override
  public void actualizarEstado(Long idArchivo, String estado) {
    String sql = "UPDATE IDO_PLATAFORMA_EXPE.EDTR_ARCHIVO SET CO_ESTADO = ?, ID_ACTUALIZA = ?, FE_ACTUALIZA = SYSDATE" +
        " WHERE ID_ARCHIVO = ?";
    jdbcTemplate.update(sql, estado, SecurityUtil.getUserInfo().getDni(), idArchivo);
  }
    @Override
    public void actualizarIdSolicitud(Long idArchivo, Long idSolicitud, String estado, String tipoArchivo) {
        String sql = "UPDATE IDO_PLATAFORMA_EXPE.EDTR_ARCHIVO SET CO_ESTADO = ?, ID_ACTUALIZA = ?, FE_ACTUALIZA = SYSDATE, ID_SOLICITUD = ?, CO_TIPO_ARCHIVO = ?" +
                " WHERE ID_ARCHIVO = ?";
        jdbcTemplate.update(sql, estado, SecurityUtil.getUserInfo().getDni(), idSolicitud, tipoArchivo, idArchivo);
    }
  @Override
  public void actualizarIdSolicitudDetalle(Long idArchivo, String estado) {
    String sql = "UPDATE IDO_PLATAFORMA_EXPE.EDTR_ARCHIVO SET CO_ESTADO = ?, ID_ACTUALIZA = ?, FE_ACTUALIZA = SYSDATE" +
            " WHERE ID_ARCHIVO = ?";
    jdbcTemplate.update(sql, estado, SecurityUtil.getUserInfo().getDni(), idArchivo);
  }
    @Override
    public void eliminarArchivo(Long idArchivo) {
        String sql = "UPDATE IDO_PLATAFORMA_EXPE.EDTR_ARCHIVO SET CO_ESTADO = ?, ID_ACTUALIZA = ?, FE_ACTUALIZA = SYSDATE" +
                " WHERE ID_ARCHIVO = ?";
        jdbcTemplate.update(sql, idArchivo);
    }

}
