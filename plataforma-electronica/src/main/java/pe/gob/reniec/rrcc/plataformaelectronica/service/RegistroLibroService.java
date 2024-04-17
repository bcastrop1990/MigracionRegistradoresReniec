package pe.gob.reniec.rrcc.plataformaelectronica.service;

import pe.gob.reniec.rrcc.plataformaelectronica.model.bean.SolicitudBean;
import pe.gob.reniec.rrcc.plataformaelectronica.model.request.*;

public interface RegistroLibroService {
    String registrar(SolicitudBean solicitudBean);
    String actualizar(SolicitudBean solicitudBean);
    String registrarUsuarioInterno(SolicitudBean solicitudBean);

    String validarDatos(ValidarDatosRegLibroRequest datosRequest);
    String validarDatosUsuarioInterno(ValidarDatosRegLibroUsuarioInternoRequest datosRequest);

}
