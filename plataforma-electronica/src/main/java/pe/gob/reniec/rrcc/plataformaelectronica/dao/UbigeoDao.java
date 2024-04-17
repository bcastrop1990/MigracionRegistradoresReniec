package pe.gob.reniec.rrcc.plataformaelectronica.dao;

import java.util.List;
import pe.gob.reniec.rrcc.plataformaelectronica.model.bean.UbigeoBean;

public interface UbigeoDao {
  List<UbigeoBean> listarUbigeo(String codigo);
  List<UbigeoBean> listarCoDenoUbigeo(String codigo);
  List<UbigeoBean> listarUnidadOrganica(String idDepartamento, String idProvincia, String idDistrito);


}
