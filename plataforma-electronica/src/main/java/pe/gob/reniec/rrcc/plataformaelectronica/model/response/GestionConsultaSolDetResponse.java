package pe.gob.reniec.rrcc.plataformaelectronica.model.response;

import lombok.Getter;
import lombok.Setter;
import pe.gob.reniec.rrcc.plataformaelectronica.model.bean.DetalleSolicitudArchivoFirmaBean;
import pe.gob.reniec.rrcc.plataformaelectronica.model.bean.TipoSolicitudRegFirmaBean;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class GestionConsultaSolDetResponse {
    //DATOS DE CABECERA O SOLICITUD
    private String numeroSolicitud;
    private String fechaRecepcion;
    private String fechaAsignacion;
    private String fechaAtencion;
    private String analistaAsignado;
    private String codigoAnalistaAsignado;

    //DATOS DE DETALLE
    private String numeroDocumento;
    private String primerApellido;
    private String segundoApellido;
    private String preNombres;
    private String celular;
    private String email;
    private String detalleRegistro;
    private String nombreDepartamento;
    private String nombreProvincia;
    private String nombreDistrito;
    private String nombreCentroPoblado;
}
