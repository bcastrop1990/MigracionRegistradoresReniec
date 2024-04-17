package pe.gob.reniec.rrcc.plataformaelectronica.controller;

import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import pe.gob.reniec.rrcc.plataformaelectronica.controller.mapper.RegistroFirmaMapper;
import pe.gob.reniec.rrcc.plataformaelectronica.model.bean.PersonaBean;
import pe.gob.reniec.rrcc.plataformaelectronica.model.bean.SolicitudBean;
import pe.gob.reniec.rrcc.plataformaelectronica.model.request.*;
import pe.gob.reniec.rrcc.plataformaelectronica.model.response.*;
import pe.gob.reniec.rrcc.plataformaelectronica.service.RegistroFirmaService;
import pe.gob.reniec.rrcc.plataformaelectronica.utility.ConstantUtil;
/*
@RestController
@RequestMapping("registro-firmas")
@AllArgsConstructor*/
public class RegistroFirmaController {

  private RegistroFirmaService registroFirmaService;
  private RegistroFirmaMapper registroFirmaMapper;

  @PostMapping("")
  public ResponseEntity<ApiResponse<String>> registrar(@Valid @RequestBody SolicitudRegFirmaRequest request) {
    System.out.print("inicio - RegistroFirmaController - registrar ");
    return ResponseEntity.ok(
        ApiResponse.<String>builder()
            .code(ConstantUtil.OK_CODE)
            .message(ConstantUtil.OK_MESSAGE)
            .data(registroFirmaService.registrar(registroFirmaMapper.RegFirmaReqToSolBean(request)))
            .build()
    );
  }
  @PostMapping("actualizar")
  public ResponseEntity<ApiResponse<String>> actualizar(@Valid @RequestBody SolicitudRegFirmaRequest request) {
    return ResponseEntity.ok(
            ApiResponse.<String>builder()
                    .code(ConstantUtil.OK_CODE)
                    .message(ConstantUtil.OK_MESSAGE)
                    .data(registroFirmaService.actualizar(registroFirmaMapper.RegFirmaReqToSolBean(request)))
                    .build()
    );
  }

  @PostMapping("usuario-interno")
  public ResponseEntity<ApiResponse<String>> registrarUsuarioInterno(@Valid @RequestBody SolRegFirmaUsuarioInterRequest request) {
    SolicitudBean solicitudBean = registroFirmaMapper.UserRegFirmaReqToSolBean(request);
    solicitudBean.setNumeroDocumentoSolicitante(request.getDniSolicitante());
    solicitudBean.setPreNombres(request.getPreNombreSolicitante());
    solicitudBean.setPrimerApellido(request.getPrimerApeSolicitante());
    solicitudBean.setSegundoApellido(request.getSegundoApeSolicitante());
    solicitudBean.setCodigoOrec(request.getCodigoOrecSolicitante());
    return ResponseEntity.ok(
            ApiResponse.<String>builder()
                    .code(ConstantUtil.OK_CODE)
                    .message(ConstantUtil.OK_MESSAGE)
                    .data(registroFirmaService.registrarUsuarioInterno(solicitudBean))
                    .build()
    );
  }


  @PostMapping("validar-datos")
  public ResponseEntity<ApiResponse<String>> validarDatos(@Valid @RequestBody ValidarDatosRegFirmaRequest request) {
     return ResponseEntity.ok(
        ApiResponse.<String>builder()
            .code(ConstantUtil.OK_CODE)
            .message(ConstantUtil.OK_MESSAGE)
            .data(registroFirmaService.validarDatos(request)) //PROBANDO
            .build()
    );
  }
  @PostMapping("validar-datos-firma-usuario-interno")
  public ResponseEntity<ApiResponse<String>> validarDatosInterno(@Valid @RequestBody ValidarDatosRegFirmaInternoRequest request) {
    return ResponseEntity.ok(
            ApiResponse.<String>builder()
                    .code(ConstantUtil.OK_CODE)
                    .message(ConstantUtil.OK_MESSAGE)
                    .data(registroFirmaService.validarDatosInterno(request)) //PROBANDO
                    .build()
    );
  }
  @GetMapping("tipo-solicitud")
  public ResponseEntity<ApiResponse<List<TipoSolicitudRegFirmaResponse>>> listarTipoSolicitud() {
     return ResponseEntity.ok(
        ApiResponse.<List<TipoSolicitudRegFirmaResponse>>builder()
            .code(ConstantUtil.OK_CODE)
            .message(ConstantUtil.OK_MESSAGE)
            .data(registroFirmaService
                .listarTipoSolicitud()
                .stream()
                .map(registroFirmaMapper::tipoSolBeanToTipoSolResponse)
                .collect(Collectors.toList()))
            .build()
    );
  }

  //SERVICIO EJEMPLO
  @GetMapping("consultar-personas")
  public ResponseEntity<ApiResponse<BusqPersonaResponse>> consultarPersonaPorDni(@RequestParam("dni") String dni) {
     return ResponseEntity.ok(
            ApiResponse.<BusqPersonaResponse>builder()
                    .code(ConstantUtil.OK_CODE)
                    .message(ConstantUtil.OK_MESSAGE)
                    .data(registroFirmaMapper.personaBeanToBusqPersonaResponse(
                            registroFirmaService.consultarPersonaPorDni(dni)
                    )).build()
    );
  }

  @PostMapping("consultar-por-datos-registrados")
  public ResponseEntity<ApiPageResponse<BusqRegCivilResponse>> consultarPorDatos(@RequestBody BusqPorDatosRegCivilRequest request,
                                                                                 @RequestParam(defaultValue = "0") int page,
                                                                                 @RequestParam(defaultValue = "10") int size) {
     return ResponseEntity.ok(registroFirmaService.consultarRegCivilPorDatos(request, page, size));
  }

  @PostMapping("consultar-por-datos-ruipin")
  public ResponseEntity<ApiResponse<PersonaBean>> consultarPorDatosRuipin(@RequestBody BusqPorDatosRegCivilRuipinRequest request) {
    return ResponseEntity.ok(registroFirmaService.consultarRegCivilPorDatosRuipin(request));
  }
}
