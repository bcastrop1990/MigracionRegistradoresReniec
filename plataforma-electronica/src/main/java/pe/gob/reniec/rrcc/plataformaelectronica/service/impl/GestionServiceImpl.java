package pe.gob.reniec.rrcc.plataformaelectronica.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.reniec.rrcc.plataformaelectronica.config.ApiGestionUsuarioProperties;
import pe.gob.reniec.rrcc.plataformaelectronica.dao.ArchivoDao;
import pe.gob.reniec.rrcc.plataformaelectronica.dao.OficinaDao;
import pe.gob.reniec.rrcc.plataformaelectronica.dao.PersonaDao;
import pe.gob.reniec.rrcc.plataformaelectronica.dao.SolicitudDao;
import pe.gob.reniec.rrcc.plataformaelectronica.exception.ApiValidateException;
import pe.gob.reniec.rrcc.plataformaelectronica.model.bean.*;
import pe.gob.reniec.rrcc.plataformaelectronica.model.request.*;
import pe.gob.reniec.rrcc.plataformaelectronica.model.response.*;
import pe.gob.reniec.rrcc.plataformaelectronica.security.UserInfo;
import pe.gob.reniec.rrcc.plataformaelectronica.service.ArchivoService;
import pe.gob.reniec.rrcc.plataformaelectronica.service.GestionService;
import pe.gob.reniec.rrcc.plataformaelectronica.service.SeguridadService;
import pe.gob.reniec.rrcc.plataformaelectronica.utility.ArchivoConstant;
import pe.gob.reniec.rrcc.plataformaelectronica.utility.ConstantUtil;
import pe.gob.reniec.rrcc.plataformaelectronica.utility.SolicitudConstant;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class GestionServiceImpl implements GestionService {
  private SolicitudDao solicitudDao;
  private OficinaDao oficinaDao;
  private PersonaDao personaDao;
  private ArchivoDao archivoDao;
  private ApiGestionUsuarioProperties ApiGUproperties;
  private SeguridadService seguridadService;

  private ArchivoService archivoService;

  @Override
  public ApiPageResponse<GestionConsultaSolResponse> consultaSolicitud(GestionConsultaSolRequest request,
                                                                  int page, int size) {
    UserInfo userInfo = seguridadService.getUserAuth();
    Page<SolicitudBean> solicitudes = Page.empty();
    String codigoEstado = request.getCodigoEstado();
    String dni_solicitante = request.getDniSolicitante();
    String dni_user = userInfo.getDni();

    if (userInfo.getPerfil().getCodigo().equals(ApiGUproperties.getCodigoPerfilAnalista())){
      System.out.print("\nEs Analista");
      System.out.print("\nEste es el codigo estado = "+ codigoEstado);

      if(codigoEstado.equals("1") || codigoEstado.equals("2")){
        System.out.print("\nEstado Registrado o Recepcionado");
        if(dni_solicitante == null || dni_solicitante == ""){
          System.out.print("\nNo tiene dni solicitante");

          request.setCodigoAnalistaAsignado(null);
          request.setDniCrea(dni_user);
          request.setDniSolicitante(dni_user);
          solicitudes = solicitudDao.consultarAnalista(buildSolicitudBean(request),
                  PageRequest.of(page - 1, size),
                  request.getFechaIni(),
                  request.getFechaFin());
        }else{
          System.out.print("\nSi tiene dni solicitante");

          request.setCodigoAnalistaAsignado(null);
          request.setDniCrea(dni_user);
          solicitudes = solicitudDao.consultar(buildSolicitudBean(request),
                  PageRequest.of(page - 1, size),
                  request.getFechaIni(),
                  request.getFechaFin());
        }


      } else if (codigoEstado.equals("3") || codigoEstado.equals("4") || codigoEstado.equals("5")) {
        System.out.print("\nEstado Asignado o Reasignado o Atendido");

        request.setCodigoAnalistaAsignado(userInfo.getDni());
        request.setDniCrea(null);

        solicitudes = solicitudDao.consultar(buildSolicitudBean(request),
                PageRequest.of(page - 1, size),
                request.getFechaIni(),
                request.getFechaFin());

      } else if (codigoEstado.equals("")) {
        System.out.print("\nEstado Todos");

          if(dni_solicitante == null || dni_solicitante == "") {
            System.out.print("\nNo tiene dni solicitante");
            request.setDniCrea(userInfo.getDni());
            request.setCodigoAnalistaAsignado(userInfo.getDni());
            request.setDniSolicitante(userInfo.getDni());

            solicitudes = solicitudDao.consultarAnalista(buildSolicitudBean(request),
                    PageRequest.of(page - 1, size),
                    request.getFechaIni(),
                    request.getFechaFin());
          }else{
            System.out.print("\nSi tiene dni solicitante");
            request.setDniCrea(null);
            request.setCodigoAnalistaAsignado(userInfo.getDni());
            solicitudes = solicitudDao.consultar(buildSolicitudBean(request),
                    PageRequest.of(page - 1, size),
                    request.getFechaIni(),
                    request.getFechaFin());
          }
      }
    }else {
      System.out.print("\nEs coordinador");
      System.out.print("\nEste es el codigo estado = "+ codigoEstado);

      solicitudes = solicitudDao.consultar(buildSolicitudBean(request),
              PageRequest.of(page - 1, size),
              request.getFechaIni(),
              request.getFechaFin());
    }
    ApiPageResponse<GestionConsultaSolResponse> response = new ApiPageResponse<>();
    response.setCode(ConstantUtil.OK_CODE);
    response.setMessage(ConstantUtil.OK_MESSAGE);
    response.setData(solicitudes.getContent().stream().map(this::mapper).collect(Collectors.toList()));
    response.setPage(solicitudes.getNumber());
    response.setSize(solicitudes.getSize());
    response.setTotalPage(solicitudes.getTotalPages());
    response.setTotalElements(solicitudes.getTotalElements());
    response.setNumberOfElements(solicitudes.getNumberOfElements());
    return response;
  }

  @Override
  public ApiPageResponse<GestionConsultaSolDetResponse> consultaDetalleSolicitud(GestionConsultaSolRequest request,
                                                                       int page, int size) {

    UserInfo userInfo = seguridadService.getUserAuth();

    List<GestionConsultaSolDetResponse> listaTotal = new ArrayList<>();
    Page<SolicitudBean> solicitudes = Page.empty();
    String codigoEstado = request.getCodigoEstado();
    String dni_solicitante = request.getDniSolicitante();
    String dni_user = userInfo.getDni();

    if (userInfo.getPerfil().getCodigo().equals(ApiGUproperties.getCodigoPerfilAnalista())){
      System.out.print("\nEs Analista");
      System.out.print("\nEste es el codigo estado = "+ codigoEstado);

      if(codigoEstado.equals("1") || codigoEstado.equals("2")){
        System.out.print("\nEstado Registrado o Recepcionado");
        if(dni_solicitante == null || dni_solicitante == ""){
          System.out.print("\nNo tiene dni solicitante");

          request.setCodigoAnalistaAsignado(null);
          request.setDniCrea(dni_user);
          request.setDniSolicitante(dni_user);
          solicitudes = solicitudDao.consultarAnalista(buildSolicitudBean(request),
                  PageRequest.of(page - 1, size),
                  request.getFechaIni(),
                  request.getFechaFin());
        }else{
          System.out.print("\nSi tiene dni solicitante");

          request.setCodigoAnalistaAsignado(null);
          request.setDniCrea(dni_user);
          solicitudes = solicitudDao.consultar(buildSolicitudBean(request),
                  PageRequest.of(page - 1, size),
                  request.getFechaIni(),
                  request.getFechaFin());
        }


      } else if (codigoEstado.equals("3") || codigoEstado.equals("4") || codigoEstado.equals("5")) {
        System.out.print("\nEstado Asignado o Reasignado o Atendido");

        request.setCodigoAnalistaAsignado(userInfo.getDni());
        request.setDniCrea(null);

        solicitudes = solicitudDao.consultar(buildSolicitudBean(request),
                PageRequest.of(page - 1, size),
                request.getFechaIni(),
                request.getFechaFin());

      } else if (codigoEstado.equals("")) {
        System.out.print("\nEstado Todos");

        if(dni_solicitante == null || dni_solicitante == "") {
          System.out.print("\nNo tiene dni solicitante");
          request.setDniCrea(userInfo.getDni());
          request.setCodigoAnalistaAsignado(userInfo.getDni());
          request.setDniSolicitante(userInfo.getDni());

          solicitudes = solicitudDao.consultarAnalista(buildSolicitudBean(request),
                  PageRequest.of(page - 1, size),
                  request.getFechaIni(),
                  request.getFechaFin());
        }else{
          System.out.print("\nSi tiene dni solicitante");
          request.setDniCrea(null);
          solicitudes = solicitudDao.consultar(buildSolicitudBean(request),
                  PageRequest.of(page - 1, size),
                  request.getFechaIni(),
                  request.getFechaFin());
        }
      }
    }else {
      System.out.print("\nEs coordinador");
      System.out.print("\nEste es el codigo estado = "+ codigoEstado);

      solicitudes = solicitudDao.consultar(buildSolicitudBean(request),
              PageRequest.of(page - 1, size),
              request.getFechaIni(),
              request.getFechaFin());
    }
    for (SolicitudBean solicitud : solicitudes) {
      SolicitudBean solicitudBean = solicitudDao.obtenerPorNumero(solicitud.getNumeroSolicitud())
              .orElseThrow(() -> new ApiValidateException("El numero de solicitud no existe"));
      List<DetalleSolicitudFirmaBean> listaDetalle = solicitudDao.listarByIdSolicitud(solicitudBean.getIdSolicitud());

      for (DetalleSolicitudFirmaBean detalle : listaDetalle) {
        GestionConsultaSolDetResponse responseTotal = new GestionConsultaSolDetResponse();
        if(solicitudBean.getCodigoOrec() != null && solicitudBean.getCodigoOrec() != "" && solicitudBean.getCodigoOrec() != " "){
          if (solicitudBean.getCodigoOrec().length() != 6) {
            OficinaBean oficina = oficinaDao.obtenerPorSucursal(solicitudBean.getCodigoOrec())
                    .orElseThrow(() -> new ApiValidateException("La oficina no existe"));
            System.out.print("\nEntro aqui:::::::::::::::::::::: 11111");
            System.out.print("\nEntro aqui:::::::::::::::::::::: 1111111_"+ solicitudBean.getCodigoOrec() + "_");

            responseTotal.setNombreDepartamento(oficina.getNombreDepartamento());
            responseTotal.setNombreProvincia(oficina.getNombreProvincia());
            responseTotal.setNombreDistrito(oficina.getNombreDistrito());
            responseTotal.setNombreCentroPoblado(oficina.getDescripcionCentroPoblado() !=null ? oficina.getDescripcionCentroPoblado() : "");

          }else{

            OficinaBean oficina = oficinaDao.obtener(solicitudBean.getCodigoOrec())
                    .orElseThrow(() -> new ApiValidateException("La oficina no existe"));
            System.out.print("\nEntro aqui:::::::::::::::::::::: 22222");
            System.out.print("\nEntro aqui:::::::::::::::::::::: 222222_"+ solicitudBean.getCodigoOrec() + "_");

            responseTotal.setNombreDepartamento(oficina.getNombreDepartamento());
            responseTotal.setNombreProvincia(oficina.getNombreProvincia());
            responseTotal.setNombreDistrito(oficina.getNombreDistrito());
            responseTotal.setNombreCentroPoblado(oficina.getDescripcionCentroPoblado() !=null ? oficina.getDescripcionCentroPoblado() : "");

          }
            }
        responseTotal.setNumeroDocumento(detalle.getNumeroDocumento());
        responseTotal.setPrimerApellido(detalle.getPrimerApellido());
        responseTotal.setSegundoApellido(detalle.getSegundoApellido());
        responseTotal.setPreNombres(detalle.getPreNombres());
        responseTotal.setCelular(detalle.getCelular());
        responseTotal.setEmail(detalle.getEmail());
        responseTotal.setDetalleRegistro(detalle.getTipoSolicitud().getDescripcion());
        responseTotal.setNumeroSolicitud(solicitud.getNumeroSolicitud());

        if (Objects.nonNull(solicitud.getFechaRecepcion())) {
          responseTotal.setFechaRecepcion(solicitud.getFechaRecepcion().format(DateTimeFormatter.ofPattern(ConstantUtil.DATE_FORMAT)));
        }
        if (Objects.nonNull(solicitud.getFechaAsignacion())) {
          responseTotal.setFechaAsignacion(solicitud.getFechaAsignacion().format(DateTimeFormatter.ofPattern(ConstantUtil.DATE_FORMAT)));
        }
        if (Objects.nonNull(solicitud.getFechaAtencion())) {
          responseTotal.setFechaAtencion(solicitud.getFechaAtencion().format(DateTimeFormatter.ofPattern(ConstantUtil.DATE_FORMAT)));
        }
        if (Objects.nonNull(solicitud.getAnalistaAsignado().getPreNombres())) {
          responseTotal.setAnalistaAsignado(solicitud.getAnalistaAsignado().getPreNombres());
        }
        if (Objects.nonNull(solicitud.getAnalistaAsignado().getPrimerApellido())) {
          responseTotal.setAnalistaAsignado(String.format("%s %s",
                  responseTotal.getAnalistaAsignado(),
                  solicitud.getAnalistaAsignado().getPrimerApellido()));
        }
        if (Objects.nonNull(solicitud.getAnalistaAsignado().getSegundoApellido())) {
          responseTotal.setAnalistaAsignado(String.format("%s %s",
                  responseTotal.getAnalistaAsignado(),
                  solicitud.getAnalistaAsignado().getSegundoApellido()));
        }
        responseTotal.setCodigoAnalistaAsignado(solicitud.getCodigoAnalistaAsignado());
        listaTotal.add(responseTotal);
      }
    }

    Page<GestionConsultaSolDetResponse> listaTotalPage = new PageImpl<>(listaTotal, PageRequest.of(page - 1, size), listaTotal.size());
    ApiPageResponse<GestionConsultaSolDetResponse> response = new ApiPageResponse<>();
    response.setCode(ConstantUtil.OK_CODE);
    response.setMessage(ConstantUtil.OK_MESSAGE);
    response.setData(listaTotal);
    response.setPage(page - 1);
    response.setSize(size);
    response.setTotalPage(listaTotalPage.getTotalPages());
    response.setTotalElements(listaTotalPage.getTotalElements());
    response.setNumberOfElements(size);
    return response;
  }

  @Override
  @Transactional
  public String recepcionarSolicitud(RecepcionSolicitudRequest request) {
    AtomicInteger total = new AtomicInteger();
    UserInfo userInfo = seguridadService.getUserAuth();
    request.getSolicitudes().forEach(nroSol -> {
      Optional<SolicitudBean> solicitudBean = solicitudDao.obtenerPorNumero(nroSol);
      if (solicitudBean.isPresent()
              && solicitudBean.get().getCodigoEstado().equals(SolicitudConstant.ESTADO_REGISTRADO)) {
        solicitudDao.recepcionar(nroSol, SolicitudConstant.ESTADO_RECEPCIONADO, userInfo.getDni());
        solicitudDao.recepcionarHistorial(nroSol, SolicitudConstant.ESTADO_RECEPCIONADO, userInfo.getDni());
        total.getAndIncrement();
      }
    });
    return String.format("Se recepcionaron %s solicitud(es).", total);
  }

  @Override
  @Transactional
  public String asignarSolicitud(AsignacionSolicitudRequest request) {
    AtomicInteger total = new AtomicInteger();
    request.getSolicitudes().forEach(nroSol -> {
      Optional<SolicitudBean> solicitudBean = solicitudDao.obtenerPorNumero(nroSol);
      if (solicitudBean.isPresent()
              && solicitudBean.get().getCodigoEstado().equals(SolicitudConstant.ESTADO_RECEPCIONADO)) {
        solicitudDao.asignarAnalista(nroSol, request.getCodigoAnalista(), SolicitudConstant.ESTADO_ASIGNADO);
        solicitudDao.asignarAnalistaHistorial(nroSol, request.getCodigoAnalista(), SolicitudConstant.ESTADO_ASIGNADO, request.getDniCoordinador());
        total.getAndIncrement();
      }
    });
    return String.format("Se asignaron %s solicitud(es).", total);
  }
  @Override
  @Transactional
  public String reasignarSolicitud(ReasignacionSolicitudRequest request) {
    AtomicInteger total = new AtomicInteger();
    request.getSolicitudes().forEach(nroSol -> {
      Optional<SolicitudBean> solicitudBean = solicitudDao.obtenerPorNumero(nroSol);
      if (solicitudBean.isPresent()
              && (solicitudBean.get().getCodigoEstado().equals(SolicitudConstant.ESTADO_ASIGNADO)  || solicitudBean.get().getCodigoEstado().equals(SolicitudConstant.ESTADO_REASIGNADO))) {
        solicitudDao.reasignarAnalista(nroSol, request.getCodigoAnalista(), SolicitudConstant.ESTADO_REASIGNADO);
        solicitudDao.reasignarAnalistaHistorial(nroSol, request.getCodigoAnalista(), SolicitudConstant.ESTADO_REASIGNADO, request.getDniCoordinador());
        total.getAndIncrement();
      }
    });
    return String.format("Se reasignaron %s solicitud(es).", total);
  }

  @Override
  public DetalleSolLibroResponse consultarSolicitudLibro(String nroSolicitud) {
    SolicitudBean solicitudBean = solicitudDao.obtenerPorNumero(nroSolicitud)
            .orElseThrow(() -> new ApiValidateException("El numero de solicitud no existe"));
    List<DetalleSolicitudLibroBean> libros = solicitudDao.listarLibrosFullBySolicitud(solicitudBean.getIdSolicitud());
    OficinaBean oficinaBean = oficinaDao.obtener(solicitudBean.getCodigoOrec())
            .orElseThrow(() -> new ApiValidateException("La oficina no existe"));
    List<ArchivoBean> listaArchivoSustento = archivoDao.obtenerPorIdSolicitud(solicitudBean.getIdSolicitud());
    List<ArchivoBean> listaArchivoRespuesta = archivoDao.obtenerRespuestaPorIdSol(solicitudBean.getIdSolicitud());

    DetalleSolLibroResponse detSolResp = new DetalleSolLibroResponse();
    detSolResp.setCodigoOrec(solicitudBean.getCodigoOrec());
    detSolResp.setDescripcionOrecLarga(solicitudBean.getDescripcionOrecLarga());
    detSolResp.setUbigeo(String.format("%s-%s-%s",
            oficinaBean.getNombreDepartamento(),
            oficinaBean.getNombreProvincia(),
            oficinaBean.getNombreDistrito()));
    List<ArchivoResponse> listArchivoSustento2 = new ArrayList<>();
    listaArchivoSustento.forEach(archivo->{
      ArchivoResponse archivoResponse = new ArchivoResponse();
      archivoResponse.setIdArchivo(archivo.getIdArchivo());
      archivoResponse.setIdTipoArchivo(archivo.getTipoCodigoNombre());
      archivoResponse.setCodigo(archivo.getCodigoNombre());
      archivoResponse.setNombreOriginal(String.format("%s.%s",
              archivo.getNombreOriginal(),
              archivo.getExtension()));
      listArchivoSustento2.add(archivoResponse);

    });

    List<ArchivoResponse> listaArchivoRespuesta2 = new ArrayList<>();
    listaArchivoRespuesta.forEach(archivo->{
      ArchivoResponse archivoRespuestaResponse = new ArchivoResponse();
      archivoRespuestaResponse.setIdArchivo(archivo.getIdArchivo());
      archivoRespuestaResponse.setIdTipoArchivo(archivo.getTipoCodigoNombre());
      archivoRespuestaResponse.setCodigo(archivo.getCodigoNombre());
      archivoRespuestaResponse.setNombreOriginal(String.format("%s.%s",
              archivo.getNombreOriginal(),
              archivo.getExtension()));
      listaArchivoRespuesta2.add(archivoRespuestaResponse);

    });

    detSolResp.setArchivoSustento(listArchivoSustento2);
    detSolResp.setArchivoRespuesta(listaArchivoRespuesta2);
    libros.forEach(libro -> detSolResp.getDetalleSolicitudLibro().add(
            new DetalleSolDetLibroResponse(
                    libro.getIdDetalleSolLibro(),
                    libro.getArticuloBean().getDescripcion(),
                    libro.getCodigoArticulo(),
                    libro.getLenguaBean().getCodigo(),
                    libro.getLenguaBean().getDescripcion(),
                    libro.getCantidad(),
                    libro.getNumeroUltimaActa()
            )));
    return detSolResp;
  }

  @Override
  public SolicitudLibroResponse consultarSolicitudAtencion(String nroSolicitud) {
    SolicitudBean solicitudBean = solicitudDao.obtenerPorNumero(nroSolicitud)
            .orElseThrow(() -> new ApiValidateException("El numero de solicitud no existe"));
    List<DetalleSolicitudLibroBean> libros = solicitudDao.listarLibrosFullBySolicitud(solicitudBean.getIdSolicitud());
    OficinaBean oficinaBean = oficinaDao.obtener(solicitudBean.getCodigoOrec())
            .orElseThrow(() -> new ApiValidateException("La oficina no existe"));

    SolicitudLibroResponse solResponse = new SolicitudLibroResponse();
    solResponse.setCodigoOrec(oficinaBean.getCodigoOrec());
    solResponse.setDescripcionOrecLarga(oficinaBean.getDescripcionLocalLarga());
    solResponse.setUbigeo(String.format("%s-%s-%s",
            oficinaBean.getNombreDepartamento(),
            oficinaBean.getNombreProvincia(),
            oficinaBean.getNombreDistrito()));

    libros.forEach(libro -> solResponse.getDetalleSolicitudLibro().add(
            new SolDetalleLibroResponse(
                    libro.getIdDetalleSolLibro(),
                    libro.getCodigoArticulo(),
                    libro.getCodigoLengua(),
                    libro.getCantidad(),
                    libro.getNumeroUltimaActa()
            )));
    return solResponse;
  }

  @Override
  @Transactional
  public Boolean atenderSolicitud(AtencionSolLibroRequest request) {
    SolicitudBean solicitudBean = solicitudDao.obtenerPorNumero(request.getNumeroSolicitud())
            .orElseThrow(() -> new ApiValidateException(SolicitudConstant.SOLICITUD_NO_EXISTE));

    if ((!solicitudBean.getCodigoEstado().equals(SolicitudConstant.ESTADO_ASIGNADO) && (!solicitudBean.getCodigoEstado().equals(SolicitudConstant.ESTADO_REASIGNADO)))) {
      throw new ApiValidateException("La solicitud debe estar asignada");
    }
    Long idArchivo = archivoService.getIdByCodigo(request.getArchivoRespuesta().getCodigoNombre());
    solicitudDao.actualizarArchivoRespuesta(solicitudBean.getIdSolicitud(),
            request.getCodigoTipoArchivoRespuesta(),
            idArchivo);
    archivoDao.actualizarEstado(idArchivo, ArchivoConstant.ESTADO_ASIGNADO);
    solicitudDao.actualizarEstadoSolicitud(solicitudBean.getIdSolicitud(),
            SolicitudConstant.ESTADO_ATENTIDO);
    solicitudDao.actualizarEstadoSolicitudHistorial(solicitudBean.getIdSolicitud(),
            SolicitudConstant.ESTADO_ATENTIDO, request.getNumeroSolicitud());
    solicitudDao.actualizarEstadoDetalleSolLibroBySol(solicitudBean.getIdSolicitud(), SolicitudConstant.INACTIVO);
    UserInfo userInfo = seguridadService.getUserAuth();
    request.getDetalleSolicitud().forEach(detalle -> {
      DetalleSolicitudLibroBean detalleLibro = DetalleSolicitudLibroBean.builder()
              .idSolicitud(solicitudBean.getIdSolicitud())
              .cantidad(detalle.getCantidad())
              .codigoArticulo(detalle.getCodigoArticulo())
              .codigoLengua(detalle.getCodigoLengua())
              .numeroUltimaActa(detalle.getNumeroUltimaActa())
              .idCrea(userInfo.getDni())
              .build();
      solicitudDao.registrarDetalleLibro(detalleLibro);
    });

    return Boolean.TRUE;
  }

  @Override
  @Transactional
  public void eliminarSolicitud(String nroSolicitud) {
    solicitudDao.eliminarSolicitud(nroSolicitud);
  }
  @Override
  @Transactional
  public void eliminarDetalleFirma(Long idDetalle) {

    solicitudDao.eliminarDetalleFirma(idDetalle);
  }
  @Override
  @Transactional
  public void eliminarDetalleLibro(Long idDetalle) {
    System.out.print("\nLLEGO DELETE DETALLE LIBRO 2");

    solicitudDao.eliminarDetalleLibro(idDetalle);
  }
  @Override
  public DetalleSolFirmaResponse consultarSolicitudFirma(String nroSolicitud) {
    SolicitudBean solicitudBean = solicitudDao.obtenerPorNumero(nroSolicitud)
            .orElseThrow(() -> new ApiValidateException("El numero de solicitud no existe"));
    List<DetalleSolicitudFirmaBean> firmas = solicitudDao.listarByIdSolicitud(solicitudBean.getIdSolicitud());



    List<ArchivoBean> listaArchivoSustento = archivoDao.obtenerPorIdSolicitud(solicitudBean.getIdSolicitud());
    if(listaArchivoSustento!= null){
      ApiValidateException apiValidateException = new ApiValidateException(ArchivoConstant.MSG_ARCHIVO_NO_EXISTE);
    }
            //.set(() -> new ApiValidateException(ArchivoConstant.MSG_ARCHIVO_NO_EXISTE));

    DetalleSolFirmaResponse detSolResp = new DetalleSolFirmaResponse();
    detSolResp.setCodigoOrec(solicitudBean.getCodigoOrec());
    detSolResp.setDescripcionOrecLarga(solicitudBean.getDescripcionOrecLarga());
    if (solicitudBean.getCodigoOrec().length() != 6){
      OficinaBean oficinaBean = oficinaDao.obtenerPorSucursal(solicitudBean.getCodigoOrec())
              .orElseThrow(() -> new ApiValidateException(ConstantUtil.MSG_OREC_NO_EXISTE));

      detSolResp.setUbigeo(String.format("%s-%s-%s",
              oficinaBean.getNombreDepartamento(),
              oficinaBean.getNombreProvincia(),
              oficinaBean.getNombreDistrito()));
    }else{
      OficinaBean oficinaBean = oficinaDao.obtener(solicitudBean.getCodigoOrec())
              .orElseThrow(() -> new ApiValidateException(ConstantUtil.MSG_OREC_NO_EXISTE));

      detSolResp.setUbigeo(String.format("%s-%s-%s",
              oficinaBean.getNombreDepartamento(),
              oficinaBean.getNombreProvincia(),
              oficinaBean.getNombreDistrito()));
    }
    List<ArchivoResponse> listArchivoSustento2 = new ArrayList<>();
    listaArchivoSustento.forEach(archivo->{
      ArchivoResponse archivoResponse = new ArchivoResponse();
      archivoResponse.setIdTipoArchivo(archivo.getTipoCodigoNombre());
      archivoResponse.setCodigo(archivo.getCodigoNombre());
      archivoResponse.setIdArchivo(archivo.getIdArchivo());
      archivoResponse.setNombreOriginal(String.format("%s.%s",
              archivo.getNombreOriginal(),
              archivo.getExtension()));
      listArchivoSustento2.add(archivoResponse);
    });
     detSolResp.setArchivoSustento(listArchivoSustento2);


    firmas.forEach(firma -> {

      List<DetalleSolicitudArchivoFirmaBean> detalleArchivos =
              solicitudDao.listarArchivoFirmaByDetalleId(firma.getIdDetalleSolicitud())
                      .stream()
                      .filter(archivo -> archivo.getCodigoUsoArchivo().equals(SolicitudConstant.USO_ARCH_SUSTENTO))
                      .collect(Collectors.toList());


      detSolResp.getDetalleSolicitudFirma().add(
              DetalleSolDetFirmaResponse.builder()
                      .idDetalleSolicitud(firma.getIdDetalleSolicitud())
                      .idTipoSolicitud(firma.getIdTipoSolicitud())
                      .numeroDocumento(firma.getNumeroDocumento())
                      .primerApellido(firma.getPrimerApellido())
                      .segundoApellido(firma.getSegundoApellido())
                      .preNombres(firma.getPreNombres())
                      .celular(firma.getCelular())
                      .email(firma.getEmail())
                      .archivos(this.mapperArchivosLibro(detalleArchivos))
                      .tipoSolicitud(firma.getTipoSolicitud().getDescripcion())
                      .codigoEstadoFirma(firma.getCodigoEstadoFirma())
                      .build());
    });
    return detSolResp;
  }

  private List<ArchivoResponse> mapperArchivosLibro(List<DetalleSolicitudArchivoFirmaBean> detalleArchivos) {
    List<ArchivoResponse> archivos = new ArrayList<>();
    detalleArchivos.forEach(archivo ->
            archivos.add(ArchivoResponse.builder()
                    .tipoArchivo(archivo.getTipoArchivo().getNombreArchivo())
                    .idTipoArchivo(archivo.getCodigoTipoArchivo())
                    .idArchivo(archivo.getArchivo().getIdArchivo())
                    .nombreOriginal(String.format("%s.%s",
                            archivo.getArchivo().getNombreOriginal(),
                            archivo.getArchivo().getExtension()))
                    .codigo(archivo.getArchivo().getCodigoNombre())
                    .build()));

    return archivos;
  }

  private SolicitudBean buildSolicitudBean(GestionConsultaSolRequest request) {
    return SolicitudBean.builder()
            .numeroSolicitud(request.getNumeroSolicitud())
            .idTipoRegistro(request.getCodigoTipoRegistro())
            .codigoEstado(request.getCodigoEstado())
            .codigoOrec(request.getCodigoOrec())
            .codigoDepartamentoOrec(request.getCodigoDepartamento())
            .codigoDistritoOrec(request.getCodigoDistrito())
            .codigoProvinciaOrec(request.getCodigoProvincia())
            .codigoCentroPobladoOrec(request.getCodigoCentroPoblado())
            .codigoAnalistaAsignado(request.getCodigoAnalistaAsignado())
            .numeroDocumentoSolicitante(request.getDniSolicitante())
            .idCrea(request.getDniCrea())
            .build();
  }


  private GestionConsultaSolResponse mapper(SolicitudBean bean) {
    GestionConsultaSolResponse response = new GestionConsultaSolResponse();
    response.setNumeroSolicitud(bean.getNumeroSolicitud());
    response.setFechaSolicitud(bean.getFechaSolicitud().format(DateTimeFormatter.ofPattern(ConstantUtil.DATE_FORMAT)));
    response.setTipoRegistro(bean.getTipoRegistro().getDescripcion());
    response.setEstadoSolicitud(bean.getSolicitudEstado().getDescripcion());
    if (Objects.nonNull(bean.getFechaRecepcion())) {
      response.setFechaRecepcion(bean.getFechaRecepcion().format(DateTimeFormatter.ofPattern(ConstantUtil.DATE_FORMAT)));
    }
    if (Objects.nonNull(bean.getFechaAsignacion())) {
      response.setFechaAsignacion(bean.getFechaAsignacion().format(DateTimeFormatter.ofPattern(ConstantUtil.DATE_FORMAT)));
    }
    if (Objects.nonNull(bean.getFechaAtencion())) {
      response.setFechaAtencion(bean.getFechaAtencion().format(DateTimeFormatter.ofPattern(ConstantUtil.DATE_FORMAT)));
    }
    response.setOficinaAutorizada(bean.getDescripcionOrecLarga());
    response.setDniSolicitante(bean.getNumeroDocumentoSolicitante());
    if (Objects.nonNull(bean.getAnalistaAsignado().getPreNombres())) {
      response.setAnalistaAsignado(bean.getAnalistaAsignado().getPreNombres());
    }
    if (Objects.nonNull(bean.getAnalistaAsignado().getPrimerApellido())) {
      response.setAnalistaAsignado(String.format("%s %s",
              response.getAnalistaAsignado(),
              bean.getAnalistaAsignado().getPrimerApellido()));
    }
    if (Objects.nonNull(bean.getAnalistaAsignado().getSegundoApellido())) {
      response.setAnalistaAsignado(String.format("%s %s",
              response.getAnalistaAsignado(),
              bean.getAnalistaAsignado().getSegundoApellido()));
    }
    if (Objects.nonNull(bean.getCodigoAnalistaAsignado())) {
      response.setCodigoAnalistaAsignado(bean.getCodigoAnalistaAsignado());
    }

    return response;
  }


  @Override
  public ApiPageResponse<ExpedienteConsultaResponse> consultarExpediente(ExpedienteConsultaRequest request, int page, int size) {

    SolicitudBean solicitudBean = SolicitudBean.builder()
            .numeroSolicitud(request.getNumeroSolicitud())
            .idTipoRegistro(request.getCodigoTipoRegistro())
            .codigoEstado(request.getCodigoEstado())
            .codigoOrec(request.getCodigoOrec())
            .codigoDepartamentoOrec(request.getCodigoDepartamento())
            .codigoDistritoOrec(request.getCodigoDistrito())
            .codigoProvinciaOrec(request.getCodigoProvincia())
            .codigoCentroPobladoOrec(request.getCodigoCentroPoblado())
            .codigoAnalistaAsignado(request.getCodigoAnalistaAsignado())
            .build();

    Page<SolicitudBean> solicitudes = solicitudDao.consultarExpediente(
            solicitudBean,
            PageRequest.of(page - 1, size),
            request.getFechaIni(),
            request.getFechaFin());

    ApiPageResponse<ExpedienteConsultaResponse> response = new ApiPageResponse<>();
    response.setCode(ConstantUtil.OK_CODE);
    response.setMessage(ConstantUtil.OK_MESSAGE);
    response.setData(solicitudes.getContent()
            .stream().map(this::mapSolicitudToExpResponse)
            .collect(Collectors.toList()));
    response.setPage(solicitudes.getNumber());
    response.setSize(solicitudes.getSize());
    response.setTotalPage(solicitudes.getTotalPages());
    response.setTotalElements(solicitudes.getTotalElements());
    response.setNumberOfElements(solicitudes.getNumberOfElements());
    return response;
  }

  private ExpedienteConsultaResponse mapSolicitudToExpResponse(SolicitudBean bean) {
    ExpedienteConsultaResponse response = new ExpedienteConsultaResponse();
    response.setNumeroSolicitud(bean.getNumeroSolicitud());
    response.setFechaSolicitud(bean.getFechaSolicitud().format(DateTimeFormatter.ofPattern(ConstantUtil.DATE_FORMAT)));
    response.setTipoRegistro(bean.getTipoRegistro().getDescripcion());
    response.setEstadoSolicitud(bean.getSolicitudEstado().getDescripcion());
    if (Objects.nonNull(bean.getFechaRecepcion())) {
      response.setFechaRecepcion(bean.getFechaRecepcion().format(DateTimeFormatter.ofPattern(ConstantUtil.DATE_FORMAT)));
    }
    if (Objects.nonNull(bean.getFechaAsignacion())) {
      response.setFechaAsignacion(bean.getFechaAsignacion().format(DateTimeFormatter.ofPattern(ConstantUtil.DATE_FORMAT)));
    }
    if (Objects.nonNull(bean.getFechaAtencion())) {
      response.setFechaAtencion(bean.getFechaAtencion().format(DateTimeFormatter.ofPattern(ConstantUtil.DATE_FORMAT)));
    }
    response.setOficinaAutorizada(bean.getDescripcionOrecLarga());
    if (Objects.nonNull(bean.getAnalistaAsignado().getPreNombres())) {
      response.setAnalistaAsignado(bean.getAnalistaAsignado().getPreNombres());
    }
    if (Objects.nonNull(bean.getAnalistaAsignado().getPrimerApellido())) {
      response.setAnalistaAsignado(String.format("%s %s",
              response.getAnalistaAsignado(),
              bean.getAnalistaAsignado().getPrimerApellido()));
    }
    if (Objects.nonNull(bean.getAnalistaAsignado().getSegundoApellido())) {
      response.setAnalistaAsignado(String.format("%s %s",
              response.getAnalistaAsignado(),
              bean.getAnalistaAsignado().getSegundoApellido()));
    }

    return response;
  }


  //@Override
  //@Transactional
  /*public String elimnarDocumentoSolicitud(EliminarDocumentoSolicitudRequest request) {
    AtomicInteger total = new AtomicInteger();
    UserInfo userInfo = seguridadService.getUserAuth();

    Optional<SolicitudBean> solicitudBean = solicitudDao.obtenerPorNumero(codDocumento);
    request.getCodigoDocumento().forEach(codDocumento -> {
      if (solicitudBean.isPresent()
              && solicitudBean.get().getCodigoEstado().equals(SolicitudConstant.ESTADO_REGISTRADO)) {
        solicitudDao.eliminar(codDocumento, SolicitudConstant.ESTADO_RECEPCIONADO, userInfo.getDni());
        total.getAndIncrement();
      }
    });
    return String.format("Se recepcionaron %s solicitudes.", total);
  } */
}


