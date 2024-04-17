package pe.gob.reniec.rrcc.plataformaelectronica.dao;

import java.util.Optional;
import pe.gob.reniec.rrcc.plataformaelectronica.model.bean.PersonaBean;

public interface PersonaDao {
  Optional<PersonaBean> validarPersona(String dni, String digitoVerifica, String fechaEmision);
  Optional<PersonaBean> buscarByDni(String dni);
  Optional<PersonaBean> buscarByApellidos(String apePrimer, String apeSegundo);
  Optional<PersonaBean> obetenerByDni(String dni);
}
