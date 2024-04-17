package pe.gob.reniec.rrcc.plataformaelectronica.service.impl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.reniec.rrcc.plataformaelectronica.config.NotificationProperties;
import pe.gob.reniec.rrcc.plataformaelectronica.dao.*;
import pe.gob.reniec.rrcc.plataformaelectronica.exception.ApiValidateException;
import pe.gob.reniec.rrcc.plataformaelectronica.model.bean.*;
import pe.gob.reniec.rrcc.plataformaelectronica.model.request.*;
import pe.gob.reniec.rrcc.plataformaelectronica.model.response.ApiPageResponse;
import pe.gob.reniec.rrcc.plataformaelectronica.model.response.ApiResponse;
import pe.gob.reniec.rrcc.plataformaelectronica.model.response.BusqRegCivilResponse;
import pe.gob.reniec.rrcc.plataformaelectronica.model.response.BusqRegCivilRuipinResponse;
import pe.gob.reniec.rrcc.plataformaelectronica.model.thirdparty.NotificationDto;
import pe.gob.reniec.rrcc.plataformaelectronica.security.JWTUtil;
import pe.gob.reniec.rrcc.plataformaelectronica.security.UserInfo;
import pe.gob.reniec.rrcc.plataformaelectronica.security.SecurityUtil;
import pe.gob.reniec.rrcc.plataformaelectronica.service.*;
import pe.gob.reniec.rrcc.plataformaelectronica.utility.ArchivoConstant;
import pe.gob.reniec.rrcc.plataformaelectronica.utility.ConstantUtil;
import pe.gob.reniec.rrcc.plataformaelectronica.utility.SolicitudConstant;
import pe.gob.reniec.rrcc.plataformaelectronica.utility.Utilitario;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Service
@AllArgsConstructor
@Slf4j
public class RegistroFirmaServiceImpl implements RegistroFirmaService {

  private RegistradorCivilDao registradorCivilDao;
  private PersonaDao personaDao;
  private TipoSolicitudRegFrimaDao tipoSolicitudRegFrimaDao;
  private JWTUtil jwtUtil;
  private SolicitudNumeracionService solicitudNumeracionService;
  private SolicitudDao solicitudDao;
  private OficinaDao oficinaDao;
  private ArchivoDao archivoDao;
  private NotificationService notificationService;
  private NotificationProperties notificationProperties;
  private ArchivoService archivoService;

  private AuditoriaService auditoriaService;

  @Override
  @Transactional(rollbackFor = Exception.class)
  public String registrar(SolicitudBean solicitudBean) {
    System.out.print("\nINGRESO A RegistroFirmaServiceImpl - REGISTRAR NORMA.");

    for (DetalleSolicitudFirmaBean detalleSolicitudFirma : solicitudBean.getDetalleSolicitudFirma()){
      System.out.print("ESTE ES EL dni: "+detalleSolicitudFirma.getNumeroDocumento());

      PersonaBean datos = consultarPersonaPorDni(detalleSolicitudFirma.getNumeroDocumento());
      Optional<RegistradorCivilBean> registradorCivil = registradorCivilDao.validarPorDni(detalleSolicitudFirma.getNumeroDocumento());

      if(!detalleSolicitudFirma.getPreNombres().trim().equals(datos.getPreNombre().trim()) || !detalleSolicitudFirma.getPrimerApellido().trim().equals(datos.getPrimerApellido().trim()) || !detalleSolicitudFirma.getSegundoApellido().trim().equals(datos.getSegundoApellido().trim())){
          throw new ApiValidateException("Los datos ingresados del DNI: "+detalleSolicitudFirma.getNumeroDocumento()+" no son correctos.");
        }
        if(detalleSolicitudFirma.getIdTipoSolicitud().equals("1")){
          System.out.print("ESTE ES EL TIPO: "+(detalleSolicitudFirma.getIdTipoSolicitud()));
          if (registradorCivil.isPresent()) {
             throw new ApiValidateException("Al ser una solicitud de ALTA, no se puede continuar, ya que la persona con el DNI: " + detalleSolicitudFirma.getNumeroDocumento() + " ya se encuentra registrada. ");
          }
        }
        if(detalleSolicitudFirma.getIdTipoSolicitud().equals("2")){
          System.out.print("ESTE ES EL TIPO: "+(detalleSolicitudFirma.getIdTipoSolicitud()));
          if (!registradorCivil.isPresent()) {
            throw new ApiValidateException("La persona con el DNI: " + detalleSolicitudFirma.getNumeroDocumento() + " no se encuentra registrada. ");
          }else {
            RegistradorCivilBean aaa = registradorCivil.get();
            if(!"1".equals(aaa.getEstado().trim()) && !"3".equals(aaa.getEstado().trim())) {
              throw new ApiValidateException("Al ser una solicitud de Baja, no se puede continuar, ya que la persona con el DNI: " + detalleSolicitudFirma.getNumeroDocumento() + " debe de estar de alta. ");
            }else{
            }
          }
        }
        if(detalleSolicitudFirma.getIdTipoSolicitud().equals("3")) {
          System.out.print("ESTE ES EL TIPO: "+(detalleSolicitudFirma.getIdTipoSolicitud()));
          if (!registradorCivil.isPresent()) {
                throw new ApiValidateException("Al ser una solicitud de Actualizar, no se puede continuar ya que la persona con el DNI: " + detalleSolicitudFirma.getNumeroDocumento() + " no se encuentra registrada. ");
            }
        }
    }


    SolicitudNumeracionBean numeracionBean = solicitudNumeracionService.obtener();
    String numeroSolicitud = Utilitario.generateNumeracion(numeracionBean.getPeriodo(),
        numeracionBean.getLongitud(), numeracionBean.getCorrelativo());

    UserInfo userInfo = (UserInfo) SecurityUtil.getAuthentication().getPrincipal();
    OficinaBean oficinaBean = oficinaDao.obtener(userInfo.getCodigoOrec())
        .orElseThrow(() -> new ApiValidateException(ConstantUtil.MSG_OREC_NO_EXISTE));

    Long Id_Solicitud = solicitudBean.getIdSolicitud();
    System.out.print("\nESTE ES EL ID_SOLICITUD 104: "+Id_Solicitud);

    solicitudBean.setIdTipoRegistro(SolicitudConstant.TIPO_FIRMA);
    solicitudBean.setCodigoOrec(oficinaBean.getCodigoOrec());
    solicitudBean.setDescripcionOrecCorta(oficinaBean.getDescripcionLocalCorta());
    solicitudBean.setDescripcionOrecLarga(oficinaBean.getDescripcionLocalLarga());
    solicitudBean.setCodigoDepartamentoOrec(oficinaBean.getCodigoDepartamento());
    solicitudBean.setCodigoProvinciaOrec(oficinaBean.getCodigoProvincia());
    solicitudBean.setCodigoDistritoOrec(oficinaBean.getCodigoDistrito());
    solicitudBean.setCodigoCentroPobladoOrec(oficinaBean.getCodigoCentroPoblado());
    solicitudBean.setNumeroDocumentoSolicitante(userInfo.getDni());
    solicitudBean.setPrimerApellido(userInfo.getPrimerApellido());
    solicitudBean.setSegundoApellido(userInfo.getSegundoApellido());
    solicitudBean.setPreNombres(userInfo.getPreNombre());
    solicitudBean.setNumeroSolicitud(numeroSolicitud);
    solicitudBean.setIdCrea(userInfo.getDni());
    solicitudBean.setCodigoEstado(SolicitudConstant.ESTADO_REGISTRADO);
    solicitudBean.setIdArchivoSustento(archivoService.getIdByCodigo(solicitudBean.getListArchivoSustento().get(0).getCodigoNombre()));
    solicitudBean.setIdTipoDocumentoSolicitante(SolicitudConstant.TIPO_DOC_DNI);
    Long id_Solicitud = solicitudDao.registrar(solicitudBean);
    System.out.print("\nESTE ES EL ID_SOLICITUD 125: "+id_Solicitud);
    System.out.print("\nESTE ES EL ID_SOLICITUD 135: "+ solicitudBean.getIdSolicitud());

    solicitudDao.registrarHistorial(solicitudBean);

    for (int i = solicitudBean.getListArchivoSustento().size(); i > 0; i --) {
      Long idArchivoSustento= (archivoService.getIdByCodigo(solicitudBean.getListArchivoSustento().get(i-1).getCodigoNombre()));
      String tipoArchivo = solicitudBean.getListArchivoSustento().get(i-1).getTipoCodigoNombre();
      archivoDao.actualizarIdSolicitud(idArchivoSustento, id_Solicitud, ArchivoConstant.ESTADO_ASIGNADO, tipoArchivo );
    }
      solicitudBean.getDetalleSolicitudFirma().forEach(detalle -> {
      detalle.setIdTipoDocumento(SolicitudConstant.TIPO_DOC_DNI);
      detalle.setIdCrea(userInfo.getDni());
      detalle.setIdSolicitud(id_Solicitud);
      solicitudDao.registrarDetalleFirma(detalle);
      detalle.getDetalleArchivo().forEach(archivo -> {
        archivo.setIdArchivo(archivoService.getIdByCodigo(archivo.getArchivo().getCodigoNombre()));
        archivo.setIdDetalleSolicitud(detalle.getIdDetalleSolicitud());
        archivo.setIdCrea(userInfo.getDni());
        archivo.setCodigoUsoArchivo(SolicitudConstant.USO_ARCH_SUSTENTO);
        archivo.setId_solicitud(Id_Solicitud);
        solicitudDao.registrarDetalleArchivoFirma(archivo);
        archivoDao.actualizarIdSolicitudDetalle(archivo.getIdArchivo(), ArchivoConstant.ESTADO_ASIGNADO);
      });
    });

    NotificationDto notificationDto = NotificationDto.builder()
            .from(notificationProperties.getFrom())
            .subject(notificationProperties.getSubject())
            .to(solicitudBean.getEmail())
            .message(String.format(notificationProperties.getBodyTemplate(),
                    buildFullName(solicitudBean),
                    SolicitudConstant.SOLICITUD_FIRMA,
                    LocalDate.now().format(DateTimeFormatter.ofPattern(ConstantUtil.DATE_FORMAT)),
                    solicitudBean.getNumeroSolicitud()))
            .build();
    notificationService.send(notificationDto);

    return numeroSolicitud;
  }
  @Override
  @Transactional(rollbackFor = Exception.class)
  public String actualizar(SolicitudBean solicitudBean) {

    for (DetalleSolicitudFirmaBean detalleSolicitudFirma : solicitudBean.getDetalleSolicitudFirma()){
      System.out.print("ESTE ES EL dni: "+detalleSolicitudFirma.getNumeroDocumento());

      PersonaBean datos = consultarPersonaPorDni(detalleSolicitudFirma.getNumeroDocumento());
      Optional<RegistradorCivilBean> registradorCivil = registradorCivilDao.validarPorDni(detalleSolicitudFirma.getNumeroDocumento());

      if(!detalleSolicitudFirma.getPreNombres().trim().equals(datos.getPreNombre().trim()) || !detalleSolicitudFirma.getPrimerApellido().trim().equals(datos.getPrimerApellido().trim()) || !detalleSolicitudFirma.getSegundoApellido().trim().equals(datos.getSegundoApellido().trim())){
        throw new ApiValidateException("Los datos ingresados del DNI: "+detalleSolicitudFirma.getNumeroDocumento()+" no son correctos.");
      }
      if(detalleSolicitudFirma.getIdTipoSolicitud().equals("1")){
        System.out.print("ESTE ES EL TIPO: "+(detalleSolicitudFirma.getIdTipoSolicitud()));
        if (registradorCivil.isPresent()) {
          throw new ApiValidateException("Al ser una solicitud de ALTA, no se puede continuar, ya que la persona con el DNI: " + detalleSolicitudFirma.getNumeroDocumento() + " ya se encuentra registrada. ");
        }
      }
      if(detalleSolicitudFirma.getIdTipoSolicitud().equals("2")){
        System.out.print("ESTE ES EL TIPO: "+(detalleSolicitudFirma.getIdTipoSolicitud()));
        if (!registradorCivil.isPresent()) {
          throw new ApiValidateException("La persona con el DNI: " + detalleSolicitudFirma.getNumeroDocumento() + " no se encuentra registrada. ");
        }else {
          RegistradorCivilBean aaa = registradorCivil.get();
          if(!"1".equals(aaa.getEstado().trim()) && !"3".equals(aaa.getEstado().trim())) {
            throw new ApiValidateException("Al ser una solicitud de Baja, no se puede continuar, ya que la persona con el DNI: " + detalleSolicitudFirma.getNumeroDocumento() + " debe de estar de alta. ");
          }else{
          }
        }
      }
      if(detalleSolicitudFirma.getIdTipoSolicitud().equals("3")) {
        System.out.print("ESTE ES EL TIPO: "+(detalleSolicitudFirma.getIdTipoSolicitud()));
        if (!registradorCivil.isPresent()) {
          throw new ApiValidateException("Al ser una solicitud de Actualizar, no se puede continuar ya que la persona con el DNI: " + detalleSolicitudFirma.getNumeroDocumento() + " no se encuentra registrada. ");
        }
      }
    }
    SolicitudBean aaaa = solicitudDao.obtenerPorNumero(solicitudBean.getNumeroSolicitud())
            .orElseThrow(() -> new ApiValidateException(SolicitudConstant.SOLICITUD_NO_EXISTE));


    UserInfo userInfo = (UserInfo) SecurityUtil.getAuthentication().getPrincipal();

    for (int i = solicitudBean.getListArchivoSustento().size(); i > 0; i --) {
      String codigoNombre = solicitudBean.getListArchivoSustento().get(i - 1).getCodigoNombre();
      Long idArchivoSustento = (archivoService.getIdByCodigo(codigoNombre));
      if (solicitudDao.validarArchivoSustento(idArchivoSustento) == false) {
        System.out.print("\n NUEVO ARCHIVO SUSTENTO.");
        System.out.print("\n ESTE ES EL ARCHIVO SUSTENTO: "+codigoNombre);
        archivoDao.actualizarIdSolicitud(idArchivoSustento, aaaa.getIdSolicitud(), ArchivoConstant.ESTADO_ASIGNADO,
                solicitudBean.getListArchivoSustento().get(i - 1).getTipoCodigoNombre());
      }
    }

    solicitudBean.getDetalleSolicitudFirma().forEach(detalle -> {
      detalle.setIdSolicitud(aaaa.getIdSolicitud());
      if(detalle.getIdDetalleSolicitud() == -1L){
        System.out.print("\n NUEVO DETALLE.");
        solicitudDao.registrarDetalleFirma(detalle);
        detalle.getDetalleArchivo().forEach(archivo -> {
          System.out.print("\n ESTE ES EL CODIGO NOMBRE." + archivo.getArchivo().getCodigoNombre());
          Long idArchivo = archivoService.getIdByCodigo(archivo.getArchivo().getCodigoNombre());
          System.out.print("\n ESTE ES EL ID ARCHIVO." + idArchivo);
          if(solicitudDao.validarArchivoDetalle(idArchivo) == false){
              System.out.print("\n NUEVO ARCHIVO DETALLE.");
              archivo.setIdArchivo(idArchivo);
              archivo.setIdDetalleSolicitud(detalle.getIdDetalleSolicitud());
              archivo.setCodigoUsoArchivo(SolicitudConstant.USO_ARCH_SUSTENTO);
              archivo.setId_solicitud(aaaa.getIdSolicitud());
              archivo.setIdCrea(userInfo.getDni());
              solicitudDao.registrarDetalleArchivoFirma(archivo);
              archivoDao.actualizarIdSolicitudDetalle(archivo.getIdArchivo(), ArchivoConstant.ESTADO_ASIGNADO);
            }
        });
      }else{
        System.out.print("\n DETALLE YA EXISTENTE.");

        solicitudDao.actualizarDetalleFirma(detalle);
        detalle.getDetalleArchivo().forEach(archivo -> {
          Long idArchivo = archivoService.getIdByCodigo(archivo.getArchivo().getCodigoNombre());
          System.out.print("\n ESTE ES EL CODIGO NOMBRE." + archivo.getArchivo().getCodigoNombre());

          System.out.print("\n ESTE ES EL ID ARCHIVO." + idArchivo);

          if(solicitudDao.validarArchivoDetalle(idArchivo) == false){
            System.out.print("\n NUEVO ARCHIVO DETALLE, PARA DETALLE YA EXISTENTE.");

            archivo.setIdArchivo(idArchivo);
            archivo.setIdDetalleSolicitud(detalle.getIdDetalleSolicitud());
            archivo.setCodigoUsoArchivo(SolicitudConstant.USO_ARCH_SUSTENTO);
            archivo.setId_solicitud(aaaa.getIdSolicitud());
            archivo.setIdCrea(userInfo.getDni());
            solicitudDao.registrarDetalleArchivoFirma(archivo);
            archivoDao.actualizarIdSolicitudDetalle(archivo.getIdArchivo(), ArchivoConstant.ESTADO_ASIGNADO);
          }
        });
      }

    });

    return "aaaa";
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public String registrarUsuarioInterno(SolicitudBean solicitudBean) {
    int x = 1;

    for (DetalleSolicitudFirmaBean detalleSolicitudFirma : solicitudBean.getDetalleSolicitudFirma()){
      System.out.print("\nENTRO AL FORRRRR" + x);
      PersonaBean datos = consultarPersonaPorDni(detalleSolicitudFirma.getNumeroDocumento());
      Optional<RegistradorCivilBean> registradorCivil = registradorCivilDao.validarPorDni(detalleSolicitudFirma.getNumeroDocumento());
      System.out.print("\nESTE ES EL DNI" +x+" "+detalleSolicitudFirma.getNumeroDocumento());
      if(!detalleSolicitudFirma.getPreNombres().trim().equals(datos.getPreNombre().trim())
              || !detalleSolicitudFirma.getPrimerApellido().trim().equals(datos.getPrimerApellido().trim()) ||
              !detalleSolicitudFirma.getSegundoApellido().trim().equals(datos.getSegundoApellido().trim())){
        throw new ApiValidateException("Los datos ingresados del DNI: "+detalleSolicitudFirma.getNumeroDocumento()+" no son correctos.");
      }if(detalleSolicitudFirma.getIdTipoSolicitud().equals("1")){
        if (registradorCivil.isPresent()) {
          System.out.print("\nENTRO ALTA");

          throw new ApiValidateException("Al ser una solicitud de ALTA, no se puede continuar," +
                  " ya que la persona con el DNI: " + detalleSolicitudFirma.getNumeroDocumento() + " ya se encuentra registrada. ");
        }
      }if(detalleSolicitudFirma.getIdTipoSolicitud().equals("2")){
        System.out.print("ESTE ES EL TIPO: "+(detalleSolicitudFirma.getIdTipoSolicitud()));
        if (!registradorCivil.isPresent()) {
          throw new ApiValidateException("La persona con el DNI: " + detalleSolicitudFirma.getNumeroDocumento() + " no se encuentra registrada. ");
        }else {
          RegistradorCivilBean aaa = registradorCivil.get();
          if(!"1".equals(aaa.getEstado().trim()) && !"3".equals(aaa.getEstado().trim())) {
            throw new ApiValidateException("Al ser una solicitud de Baja, no se puede continuar, ya que la persona con el DNI: " + detalleSolicitudFirma.getNumeroDocumento() + " debe de estar de alta. ");
          }else{
          }
        }
      }
      if(detalleSolicitudFirma.getIdTipoSolicitud().equals("3")) {
        System.out.print("\nENTRO actualizar");

        if (!registradorCivil.isPresent()) {
          throw new ApiValidateException("Al ser una solicitud de Actualizar," +
                  " no se puede continuar ya que la persona con el DNI: " + detalleSolicitudFirma.getNumeroDocumento() + " no se encuentra registrada. ");
        }
      }
      System.out.print("\nSALIO DEL FORRRRR" + x);
      x++;
    }
    SolicitudNumeracionBean numeracionBean = solicitudNumeracionService.obtener();
    String numeroSolicitud = Utilitario.generateNumeracion(numeracionBean.getPeriodo(),
            numeracionBean.getLongitud(), numeracionBean.getCorrelativo());
    UserInfo userInfo = (UserInfo) SecurityUtil.getAuthentication().getPrincipal();


    if (solicitudBean.getCodigoOrec().length() != 6){
      OficinaBean oficinaBean = oficinaDao.obtenerPorSucursal(solicitudBean.getCodigoOrec())
            .orElseThrow(() -> new ApiValidateException(ConstantUtil.MSG_OREC_NO_EXISTE));
      solicitudBean.setCodigoOrec(oficinaBean.getCodigoOrec());
      solicitudBean.setDescripcionOrecCorta(oficinaBean.getDescripcionLocalCorta());
      solicitudBean.setDescripcionOrecLarga(oficinaBean.getDescripcionLocalLarga());
      solicitudBean.setCodigoDepartamentoOrec(oficinaBean.getCodigoDepartamento());
      solicitudBean.setCodigoProvinciaOrec(oficinaBean.getCodigoProvincia());
      solicitudBean.setCodigoDistritoOrec(oficinaBean.getCodigoDistrito());
    }else{
      OficinaBean oficinaBean = oficinaDao.obtener(solicitudBean.getCodigoOrec())
              .orElseThrow(() -> new ApiValidateException(ConstantUtil.MSG_OREC_NO_EXISTE));

      solicitudBean.setCodigoOrec(oficinaBean.getCodigoOrec());
      solicitudBean.setDescripcionOrecCorta(oficinaBean.getDescripcionLocalCorta());
      solicitudBean.setDescripcionOrecLarga(oficinaBean.getDescripcionLocalLarga());
      solicitudBean.setCodigoDepartamentoOrec(oficinaBean.getCodigoDepartamento());
      solicitudBean.setCodigoProvinciaOrec(oficinaBean.getCodigoProvincia());
      solicitudBean.setCodigoDistritoOrec(oficinaBean.getCodigoDistrito());
      solicitudBean.setCodigoCentroPobladoOrec(oficinaBean.getCodigoCentroPoblado());
    }

    Long Id_Solicitud = solicitudBean.getIdSolicitud();
    solicitudBean.setIdTipoRegistro(SolicitudConstant.TIPO_FIRMA);
    solicitudBean.setNumeroDocumentoSolicitante(solicitudBean.getNumeroDocumentoSolicitante());
    solicitudBean.setPrimerApellido(solicitudBean.getPrimerApellido());
    solicitudBean.setSegundoApellido(solicitudBean.getSegundoApellido());
    solicitudBean.setPreNombres(solicitudBean.getPreNombres());
    solicitudBean.setNumeroSolicitud(numeroSolicitud);
    solicitudBean.setIdCrea(userInfo.getDni());
    solicitudBean.setCodigoEstado(SolicitudConstant.ESTADO_REGISTRADO);
    //solicitudBean.setIdArchivoSustento(Long.valueOf(1));
    solicitudBean.setIdArchivoSustento(archivoService.getIdByCodigo(solicitudBean.getListArchivoSustento().get(0).getCodigoNombre()));
    solicitudBean.setIdTipoDocumentoSolicitante(SolicitudConstant.TIPO_DOC_DNI);
    Long id_Solicitud = solicitudDao.registrarUsuarioInterno(solicitudBean);
    solicitudDao.registrarHistorial(solicitudBean);
    for (int i = solicitudBean.getListArchivoSustento().size(); i > 0; i --) {
      Long idArchivoSustento= (archivoService.getIdByCodigo(solicitudBean.getListArchivoSustento().get(i-1).getCodigoNombre()));
      archivoDao.actualizarIdSolicitud(idArchivoSustento, id_Solicitud, ArchivoConstant.ESTADO_ASIGNADO, solicitudBean.getListArchivoSustento().get(i-1).getTipoCodigoNombre());
    }
    solicitudBean.getDetalleSolicitudFirma().forEach(detalle -> {
      detalle.setIdSolicitud(solicitudBean.getIdSolicitud());
      detalle.setIdTipoDocumento(SolicitudConstant.TIPO_DOC_DNI);
      detalle.setIdCrea(userInfo.getDni());
      detalle.setIdSolicitud(id_Solicitud);
      solicitudDao.registrarDetalleFirma(detalle);
      detalle.getDetalleArchivo().forEach(archivo -> {
        archivo.setIdArchivo(archivoService.getIdByCodigo(archivo.getArchivo().getCodigoNombre()));
        archivo.setIdDetalleSolicitud(detalle.getIdDetalleSolicitud());
        archivo.setIdCrea(userInfo.getDni());
        archivo.setCodigoUsoArchivo(SolicitudConstant.USO_ARCH_SUSTENTO);
        archivo.setId_solicitud(Id_Solicitud);
        solicitudDao.registrarDetalleArchivoFirma(archivo);
        archivoDao.actualizarIdSolicitudDetalle(archivo.getIdArchivo(), ArchivoConstant.ESTADO_ASIGNADO);
      });
    });

    NotificationDto notificationDto = NotificationDto.builder()
            .from(notificationProperties.getFrom())
            .subject(notificationProperties.getSubject())
            .to(solicitudBean.getEmail())
            .message(String.format(notificationProperties.getBodyTemplate(),
                    buildFullName(solicitudBean),
                    SolicitudConstant.SOLICITUD_FIRMA,
                    LocalDate.now().format(DateTimeFormatter.ofPattern(ConstantUtil.DATE_FORMAT)),
                    solicitudBean.getNumeroSolicitud()))
            .build();
    notificationService.send(notificationDto);
    return numeroSolicitud;
  }


  @Override
  public List<TipoSolicitudRegFirmaBean> listarTipoSolicitud() {
    return tipoSolicitudRegFrimaDao.listar();
  }

  @Override
  public String validarDatos(ValidarDatosRegFirmaRequest datosRequest) {

    DatosPersonaRegFirmaRequest datosPersona = datosRequest.getDatosPersona();
    DatosOficinaRegFirmaRequest datosOficina = datosRequest.getDatosOficina();
    if(datosOficina.getCodigoOrec() == null || datosOficina.getCodigoOrec().isEmpty()){
      throw new ApiValidateException("La oficina es requerida.");
    }
    PersonaBean persona = personaDao.validarPersona(datosPersona.getDni(),
            datosPersona.getDigitoVerifica(), datosPersona.getFechaEmision())
        .orElseThrow(() -> new ApiValidateException(ConstantUtil.MSG_PERSONA_NO_ENCONTRADA));
    UserInfo userInfo = this.mapToPersonInfo(persona, datosOficina.getCodigoOrec());
    return jwtUtil.createExternalToken(userInfo);
  }
  @Override
  public String validarDatosInterno(ValidarDatosRegFirmaInternoRequest datosRequest) {
    DatosOficinaRegFirmaRequest datosOficina = datosRequest.getDatosOficina();

    if(datosOficina.getCodigoOrec() == null || datosOficina.getCodigoOrec().isEmpty()){
      throw new ApiValidateException("La oficina es requerida.");
    }
    PersonaBean datos = obtenerDatosPersonaPorDni(datosRequest.getDni());
    PersonaBean persona = personaDao.validarPersona(datos.getDni(),
                    datos.getDigitoVerifica(), datos.getFechaEmision())
            .orElseThrow(() -> new ApiValidateException(ConstantUtil.MSG_PERSONA_NO_ENCONTRADA));


    UserInfo userInfo = this.mapToPersonInfo(persona, datosOficina.getCodigoOrec());
    return jwtUtil.createExternalToken(userInfo);
  }

  @Override
  public PersonaBean consultarPersonaPorDni(String dni) {

    if (dni.length() != ConstantUtil.INT_MAX_DIG_DNI || !StringUtils.isNumeric(dni)) {
      throw new ApiValidateException(ConstantUtil.MSG_FORMATO_DNI_INCORRECTO);
    }
    return personaDao.buscarByDni(dni)
            .orElseThrow(() -> new ApiValidateException(ConstantUtil.MSG_PERSONA_NO_ENCONTRADA));
  }
  @Override
  public PersonaBean obtenerDatosPersonaPorDni(String dni) {

    if (dni.length() != ConstantUtil.INT_MAX_DIG_DNI || !StringUtils.isNumeric(dni)) {
      throw new ApiValidateException(ConstantUtil.MSG_FORMATO_DNI_INCORRECTO);
    }
    return personaDao.obetenerByDni(dni)
            .orElseThrow(() -> new ApiValidateException(ConstantUtil.MSG_PERSONA_NO_ENCONTRADA));
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

   private String buildFullName(SolicitudBean solicitudBean) {
    return Stream.of(solicitudBean.getPreNombres(),
                    solicitudBean.getPrimerApellido(),
                    solicitudBean.getSegundoApellido())
            .filter(Objects::nonNull)
            .filter(name -> !StringUtils.isEmpty(name))
            .collect(Collectors.joining(" "));
  }
  @Override
  public ApiResponse<PersonaBean> consultarRegCivilPorDatosRuipin(BusqPorDatosRegCivilRuipinRequest request) {
    ApiResponse<PersonaBean> response = new ApiResponse<>();
    PersonaBean datos =consultarPersonaPorDni(request.getDni());
    if (datos.getPrimerApellido().trim().equals(request.getPrimerApellido().trim())) {
      UserInfo userInfo = (UserInfo) SecurityUtil.getAuthentication().getPrincipal();
      AuditoriaBean auditoriaBean = AuditoriaBean.builder()
              .usuario(userInfo.getDni())
              .tipoDoc("01") //Colocar el tipo de documento para DNI
              .nuDoc(datos.getDni())
              .nombres(datos.getPreNombre())
              .apellidoPaterno(datos.getPrimerApellido())
              .apellidoMaterno(datos.getSegundoApellido())
              .build();
      auditoriaService.registrarAuditoria(auditoriaBean);
      response.setData(datos);
      response.setCode(ConstantUtil.OK_CODE);
      response.setMessage(ConstantUtil.OK_MESSAGE);
    }else{
      throw new ApiValidateException("El Apellido Paterno no coincide con el DNI.");
    }
    return response;
  }

  private BusqRegCivilRuipinResponse mapSolicitudToRegCivilRuipinResponse(RegistradorCivilRuipinBean regCivil) {
    BusqRegCivilRuipinResponse regCivilResponse = new BusqRegCivilRuipinResponse();

    regCivilResponse.setPrimerApellido(regCivil.getPrimerApellido());
    regCivilResponse.setSegundoApellido(regCivil.getSegundoApellido());
    regCivilResponse.setPreNombres(regCivil.getPreNombre());
    regCivilResponse.setDni(regCivil.getNumeroDocIdentidad());
    return regCivilResponse;
  }
  @Override
  public ApiPageResponse<BusqRegCivilResponse> consultarRegCivilPorDatos(BusqPorDatosRegCivilRequest request, int page, int size) {
    RegistradorCivilBean registradorCivilBean = RegistradorCivilBean.builder()
            .numeroDocIdentidad(request.getDni())
            .primerApellido(request.getPrimerApellido())
            .segundoApellido(request.getSegundoApellido())
            .preNombre(request.getPreNombres())
            .build();

    Page<RegistradorCivilBean> solicitudes = registradorCivilDao.consultarRegCivilPorDatos(
            registradorCivilBean,
            PageRequest.of(page - 1, size));

    List<BusqRegCivilResponse> solResponse = solicitudes.getContent()
            .stream().map(this::mapSolicitudToRegCivilResponse)
            .collect(Collectors.toList());

    ApiPageResponse<BusqRegCivilResponse> response = new ApiPageResponse<>();
    response.setCode(ConstantUtil.OK_CODE);
    response.setMessage(ConstantUtil.OK_MESSAGE);
    response.setData(solResponse);
    response.setPage(solicitudes.getNumber());
    response.setSize(solicitudes.getSize());
    response.setTotalPage(solicitudes.getTotalPages());
    response.setTotalElements(solicitudes.getTotalElements());
    response.setNumberOfElements(solicitudes.getNumberOfElements());


    return response;
  }

  private BusqRegCivilResponse mapSolicitudToRegCivilResponse(RegistradorCivilBean regCivil) {
    BusqRegCivilResponse regCivilResponse = new BusqRegCivilResponse();

    regCivilResponse.setPrimerApellido(regCivil.getPrimerApellido());
    regCivilResponse.setSegundoApellido(regCivil.getSegundoApellido());
    regCivilResponse.setPreNombres(regCivil.getPreNombre());
    regCivilResponse.setCelular(regCivil.getCelular());
    regCivilResponse.setEmail(regCivil.getEmail());
    regCivilResponse.setNombreDepartamento(regCivil.getOficina().getNombreDepartamento());
    regCivilResponse.setNombreProvincia(regCivil.getOficina().getNombreProvincia());
    regCivilResponse.setNombreDistrito(regCivil.getOficina().getNombreDistrito());
    regCivilResponse.setDescripcionOrec(regCivil.getDescripcionOrecCorta());
    regCivilResponse.setDni(regCivil.getNumeroDocIdentidad());
    return regCivilResponse;
  }

}
