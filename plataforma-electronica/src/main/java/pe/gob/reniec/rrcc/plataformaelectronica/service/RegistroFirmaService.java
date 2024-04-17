package pe.gob.reniec.rrcc.plataformaelectronica.service;

import java.util.List;

import pe.gob.reniec.rrcc.plataformaelectronica.model.bean.PersonaBean;
import pe.gob.reniec.rrcc.plataformaelectronica.model.bean.SolicitudBean;
import pe.gob.reniec.rrcc.plataformaelectronica.model.bean.TipoSolicitudRegFirmaBean;
import pe.gob.reniec.rrcc.plataformaelectronica.model.request.*;
import pe.gob.reniec.rrcc.plataformaelectronica.model.response.ApiPageResponse;
import pe.gob.reniec.rrcc.plataformaelectronica.model.response.ApiResponse;
import pe.gob.reniec.rrcc.plataformaelectronica.model.response.BusqRegCivilResponse;
import pe.gob.reniec.rrcc.plataformaelectronica.model.response.BusqRegCivilRuipinResponse;

public interface RegistroFirmaService {
    String registrar(SolicitudBean solicitudBean);
    String actualizar(SolicitudBean solicitudBean);
    String registrarUsuarioInterno(SolicitudBean solicitudBean);
    List<TipoSolicitudRegFirmaBean> listarTipoSolicitud();
    String validarDatos(ValidarDatosRegFirmaRequest datosRequest);
    String validarDatosInterno(ValidarDatosRegFirmaInternoRequest datosRequest);

    PersonaBean consultarPersonaPorDni(String dni);
    PersonaBean obtenerDatosPersonaPorDni(String dni);

    ApiPageResponse<BusqRegCivilResponse> consultarRegCivilPorDatos(BusqPorDatosRegCivilRequest request, int page, int size);

    ApiResponse<PersonaBean> consultarRegCivilPorDatosRuipin(BusqPorDatosRegCivilRuipinRequest request);
}
