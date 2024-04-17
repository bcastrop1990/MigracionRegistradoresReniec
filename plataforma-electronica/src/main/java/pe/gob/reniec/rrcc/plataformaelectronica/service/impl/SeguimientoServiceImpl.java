package pe.gob.reniec.rrcc.plataformaelectronica.service.impl;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import pe.gob.reniec.rrcc.plataformaelectronica.dao.OficinaDao;
import pe.gob.reniec.rrcc.plataformaelectronica.dao.PersonaDao;
import pe.gob.reniec.rrcc.plataformaelectronica.dao.RegistradorCivilDao;
import pe.gob.reniec.rrcc.plataformaelectronica.dao.SolicitudDao;
import pe.gob.reniec.rrcc.plataformaelectronica.exception.ApiValidateException;
import pe.gob.reniec.rrcc.plataformaelectronica.model.bean.*;
import pe.gob.reniec.rrcc.plataformaelectronica.model.request.*;
import pe.gob.reniec.rrcc.plataformaelectronica.model.response.*;
import pe.gob.reniec.rrcc.plataformaelectronica.security.JWTUtil;
import pe.gob.reniec.rrcc.plataformaelectronica.security.UserInfo;
import pe.gob.reniec.rrcc.plataformaelectronica.security.SecurityUtil;
import pe.gob.reniec.rrcc.plataformaelectronica.service.PersonaService;
import pe.gob.reniec.rrcc.plataformaelectronica.service.RegistroFirmaService;
import pe.gob.reniec.rrcc.plataformaelectronica.service.SeguimientoService;
import pe.gob.reniec.rrcc.plataformaelectronica.utility.ConstantUtil;
import pe.gob.reniec.rrcc.plataformaelectronica.utility.SolicitudConstant;

@Service
@AllArgsConstructor
@Slf4j
public class SeguimientoServiceImpl implements SeguimientoService {

  private SolicitudDao solicitudDao;
  private PersonaDao personaDao;

  private OficinaDao oficinaDao;

  private RegistradorCivilDao registradorCivilDao;
  private RegistroFirmaService registroFirmaService;

  private PersonaService personaService;
  private JWTUtil jwtUtil;

  @Override
  public String validarDatos(ValidarDatosSeguimientoRequest request) {

    PersonaBean personaValida = PersonaBean.builder()
            .dni(request.getDni())
            .digitoVerifica(request.getDigitoVerifica())
            .fechaEmision(request.getFechaEmision())
            .build();

    PersonaBean persona = null;
    if(request.getDni() != null && StringUtils.isEmpty(request.getDigitoVerifica())
            && StringUtils.isEmpty(request.getFechaEmision()) ) {
      persona = personaService.buscarByDni(request.getDni());
    } else {
      persona = personaService.validarDatos(personaValida);
    }

/*
    SolicitudBean solicitudBean = solicitudDao.obtenerPorNumero(request.getNumeroSolicitud())
        .orElseThrow(() -> new ApiValidateException(ConstantUtil.MSG_DATOS_INVALIDO));
    */
    Page<SolicitudBean> solicitudes = solicitudDao.consultarSeguimiento(
            request.getNumeroSolicitud(),
            PageRequest.of(0, 1),
            request.getDni(),
            request.getFechaIni(),
            request.getFechaFin());

    if (solicitudes == null || solicitudes.isEmpty() ||
            solicitudes.getTotalElements() == 0 || !solicitudes.stream().findAny().isPresent()) {
      throw new ApiValidateException(ConstantUtil.MSG_DATOS_INVALIDO);
    }

    SolicitudBean solicitudBean = solicitudes.stream().findFirst().get();

    if (solicitudBean.getIdTipoRegistro().equals(SolicitudConstant.TIPO_LIBRO)) {
      if(!solicitudBean.getNumeroDocumentoSolicitante().equals(request.getDni())) {
        throw new ApiValidateException(ConstantUtil.MSG_DATOS_INVALIDO);
      }
    } else {
      if (!solicitudBean.getNumeroDocumentoSolicitante().equals(request.getDni())) {
        solicitudDao.obtenerSolFirmaByDniReg(request.getDni())
                .orElseThrow(() -> new ApiValidateException(ConstantUtil.MSG_DATOS_INVALIDO));
      }
    }
    UserInfo userInfo = this.mapToPersonInfo(persona, solicitudBean.getCodigoOrec());
    return jwtUtil.createExternalToken(userInfo);
  }

  @Override
  public OficinaPorDatosResponse buscarOficinaPorDatos(BusqOficinaRequest request) {
    OficinaPorDatosResponse response = new OficinaPorDatosResponse();
    Optional<RegistradorCivilBean> registradorCivil = registradorCivilDao.buscarPorDni(request.getDni());

    PersonaBean persona = personaDao.validarPersona(request.getDni(),
                    request.getDigitoVerifica(), request.getFechaEmision())
            .orElseThrow(() -> new ApiValidateException(ConstantUtil.MSG_PERSONA_NO_ENCONTRADA));

    if (registradorCivil.isPresent()) {
        RegistradorCivilBean solicitudBean = registradorCivil.get();
      if("1".equals(solicitudBean.getEstado().trim()) || "3".equals(solicitudBean.getEstado().trim())) {
        OficinaBean oficina = oficinaDao.obtener(solicitudBean.getCodigoOrec())
                .orElseThrow(() -> new ApiValidateException("La oficina no existe"));
        response.setCodigo("1");
        response.setNombreDepartamento(oficina.getNombreDepartamento());
        response.setNombreProvincia(oficina.getNombreProvincia());
        response.setNombreDistrito(oficina.getNombreDistrito());
        response.setNombreCentroPoblado(oficina.getDescripcionCentroPoblado());
        response.setNombreOficina(oficina.getDescripcionLocalLarga());
        response.setCoNombreDepartamento(oficina.getCodigoDepartamento());
        response.setCoNombreProvincia(oficina.getCodigoProvincia());
        response.setCoNombreDistrito(oficina.getCodigoDistrito());
        response.setCoNombreCentroPoblado(oficina.getCodigoCentroPoblado());
        response.setCoNombreOficina(solicitudBean.getCodigoOrec());
      }else if("2".equals(solicitudBean.getEstado().trim())) {
        throw new ApiValidateException(ConstantUtil.MSG_FIRMA_INHABILITADA);
      }
    }else{
      throw new ApiValidateException(ConstantUtil.MSG_PERSONA_NO_REGISTRADOR);
    }
    return response;
  }
  @Override
  public OficinaPorDatosResponse buscarOficinaPorDni(String dni) {
    //prueba
    OficinaPorDatosResponse response = new OficinaPorDatosResponse();
    Optional<RegistradorCivilBean> registradorCivil = registradorCivilDao.buscarPorDni(dni);

    PersonaBean datos = registroFirmaService.obtenerDatosPersonaPorDni(dni);

    PersonaBean personaValida = PersonaBean.builder()
            .dni(datos.getDni())
            .digitoVerifica(datos.getDigitoVerifica())
            .fechaEmision(datos.getFechaEmision())
            .build();

    PersonaBean persona =  personaService.validarDatos(personaValida);

    if (registradorCivil.isPresent()) {
      RegistradorCivilBean solicitudBean = registradorCivil.get();
      if("1".equals(solicitudBean.getEstado().trim()) || "3".equals(solicitudBean.getEstado().trim())) {
        OficinaBean oficina = oficinaDao.obtener(solicitudBean.getCodigoOrec())
                .orElseThrow(() -> new ApiValidateException("La oficina no existe"));
        response.setCodigo("1");
        response.setNombreDepartamento(oficina.getNombreDepartamento());
        response.setNombreProvincia(oficina.getNombreProvincia());
        response.setNombreDistrito(oficina.getNombreDistrito());
        response.setNombreCentroPoblado(oficina.getDescripcionCentroPoblado());
        response.setNombreOficina(oficina.getDescripcionLocalLarga());
        response.setCoNombreDepartamento(oficina.getCodigoDepartamento());
        response.setCoNombreProvincia(oficina.getCodigoProvincia());
        response.setCoNombreDistrito(oficina.getCodigoDistrito());
        response.setCoNombreCentroPoblado(oficina.getCodigoCentroPoblado());
        response.setCoNombreOficina(solicitudBean.getCodigoOrec());
      }else if("2".equals(solicitudBean.getEstado().trim())) {
        throw new ApiValidateException(ConstantUtil.MSG_FIRMA_INHABILITADA);
      }
      }else{
        throw new ApiValidateException(ConstantUtil.MSG_PERSONA_NO_REGISTRADOR);
      }
    return response;
  }
  @Override
  public ApiPageResponse<ConsultaSolSegResponse> consultarSeguimiento(ConsultaSolSegRequest request, int page, int size) {
    UserInfo userInfo = (UserInfo) SecurityUtil.getAuthentication().getPrincipal();
    Page<SolicitudBean> solicitudes = solicitudDao.consultarSeguimiento(
        request.getNumeroSolicitud(),
        PageRequest.of(page - 1, size),
        userInfo.getDni(),
        request.getFechaIni(),
        request.getFechaFin());
    ApiPageResponse<ConsultaSolSegResponse> response = new ApiPageResponse<>();
    response.setCode(ConstantUtil.OK_CODE);
    response.setMessage(ConstantUtil.OK_MESSAGE);
    response.setData(solicitudes.getContent().stream().map(this::solBeanToSolSegResponse).collect(Collectors.toList()));
    response.setPage(solicitudes.getNumber());
    response.setSize(solicitudes.getSize());
    response.setTotalPage(solicitudes.getTotalPages());
    response.setTotalElements(solicitudes.getTotalElements());
    response.setNumberOfElements(solicitudes.getNumberOfElements());
    return response;
  }
  private ConsultaSolSegResponse solBeanToSolSegResponse(SolicitudBean bean) {
    ConsultaSolSegResponse response = new ConsultaSolSegResponse();
    response.setTipoRegistro(bean.getTipoRegistro().getDescripcion());
    response.setEstadoSolicitud(bean.getSolicitudEstado().getDescripcion());
    response.setFechaSolicitud(bean.getFechaSolicitud().format(DateTimeFormatter.ofPattern(ConstantUtil.DATE_FORMAT)));
    response.setCodigoArchivoSustento(bean.getArchivoSustento().getCodigoNombre());
    response.setCodigoArchivoRespuesta(bean.getArchivoRespuesta().getCodigoNombre());
    response.setNumeroSolicitud(bean.getNumeroSolicitud());
    return response;
  }

  @Override
  public List<SegSolDetFirmaResponse> consultarSeguimientoSolFirma(String nroSolicitud) {
    SolicitudBean solicitudBean = solicitudDao.obtenerPorNumero(nroSolicitud)
        .orElseThrow(() -> new ApiValidateException(SolicitudConstant.SOLICITUD_NO_EXISTE));
    List<SegSolDetFirmaResponse> response = new ArrayList<>();
    List<DetalleSolicitudFirmaBean> detalle = solicitudDao.listarByIdSolicitud(solicitudBean.getIdSolicitud());

    detalle.forEach(det -> {
      Optional<DetalleSolicitudArchivoFirmaBean> detalleArchivo =
      solicitudDao.listarArchivoFirmaByDetalleId(det.getIdDetalleSolicitud())
          .stream().filter(archivo -> archivo.getCodigoUsoArchivo().equals(SolicitudConstant.USO_ARCH_RESPUESTA))
          .findFirst();

      response.add(SegSolDetFirmaResponse.builder()
              .tipoSolicitud(det.getTipoSolicitud().getDescripcion())
              .numeroDocumento(det.getNumeroDocumento())
              .primerApellido(det.getPrimerApellido())
              .segundoApellido(det.getSegundoApellido())
              .preNombres(det.getPreNombres())
              .archivoRespuesta(this.mapArchivoRespuesta(detalleArchivo))
          .build());
    });

    return response;
  }

  private UserInfo mapToPersonInfo(PersonaBean personaBean, String orec) {
    return UserInfo.builder()
            .dni(personaBean.getDni())
            .primerApellido(personaBean.getPrimerApellido())
            .segundoApellido(personaBean.getSegundoApellido())
            .preNombre(personaBean.getPreNombre())
            .codigoOrec(orec)
            .build();
  }

  private ArchivoResponse mapArchivoRespuesta(Optional<DetalleSolicitudArchivoFirmaBean> archivo) {
    if (!archivo.isPresent())
      return null;


    return ArchivoResponse.builder()
        .tipoArchivo(archivo.get().getTipoArchivo().getNombreArchivo())
        .nombreOriginal(String.format("%s.%s",
            archivo.get().getArchivo().getNombreOriginal(),
            archivo.get().getArchivo().getExtension()))
        .codigo(archivo.get().getArchivo().getCodigoNombre())
        .build();
  }

}
