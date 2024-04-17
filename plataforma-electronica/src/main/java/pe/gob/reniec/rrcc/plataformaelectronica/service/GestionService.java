package pe.gob.reniec.rrcc.plataformaelectronica.service;

import pe.gob.reniec.rrcc.plataformaelectronica.model.request.*;
import pe.gob.reniec.rrcc.plataformaelectronica.model.response.*;

public interface GestionService {
    ApiPageResponse<GestionConsultaSolResponse> consultaSolicitud(GestionConsultaSolRequest request, int page, int size);

    ApiPageResponse<GestionConsultaSolDetResponse> consultaDetalleSolicitud(GestionConsultaSolRequest request, int page, int size);
    String recepcionarSolicitud(RecepcionSolicitudRequest request);
    //String elimnarDocumentoSolicitud(EliminarDocumentoSolicitudRequest request);
    String asignarSolicitud(AsignacionSolicitudRequest request);

    String reasignarSolicitud(ReasignacionSolicitudRequest request);

    DetalleSolLibroResponse consultarSolicitudLibro(String nroSolicitud);

    SolicitudLibroResponse consultarSolicitudAtencion(String nroSolicitud);

    Boolean atenderSolicitud(AtencionSolLibroRequest request);
    DetalleSolFirmaResponse consultarSolicitudFirma(String nroSolicitud);
    void eliminarSolicitud(String nroSolicitud);
    void eliminarDetalleFirma(Long idDetalle);
    void eliminarDetalleLibro(Long idDetalle);

    ApiPageResponse<ExpedienteConsultaResponse> consultarExpediente(ExpedienteConsultaRequest request, int page, int size);
}
