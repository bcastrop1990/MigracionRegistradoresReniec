package pe.gob.reniec.rrcc.plataformaelectronica.service;

import java.util.List;

import pe.gob.reniec.rrcc.plataformaelectronica.model.request.*;
import pe.gob.reniec.rrcc.plataformaelectronica.model.response.ApiPageResponse;
import pe.gob.reniec.rrcc.plataformaelectronica.model.response.ConsultaSolSegResponse;
import pe.gob.reniec.rrcc.plataformaelectronica.model.response.OficinaPorDatosResponse;
import pe.gob.reniec.rrcc.plataformaelectronica.model.response.SegSolDetFirmaResponse;

public interface SeguimientoService {
  String validarDatos(ValidarDatosSeguimientoRequest request);

  OficinaPorDatosResponse buscarOficinaPorDatos(BusqOficinaRequest request);
  OficinaPorDatosResponse buscarOficinaPorDni(String dni);

  ApiPageResponse<ConsultaSolSegResponse> consultarSeguimiento(ConsultaSolSegRequest request, int page, int size);

  List<SegSolDetFirmaResponse> consultarSeguimientoSolFirma(String nroSolicitud);
}
