package pe.gob.reniec.rrcc.plataformaelectronica.controller;

import javax.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.gob.reniec.rrcc.plataformaelectronica.controller.mapper.RegistroLibroMapper;
import pe.gob.reniec.rrcc.plataformaelectronica.model.bean.PersonaBean;
import pe.gob.reniec.rrcc.plataformaelectronica.model.bean.SolicitudBean;
import pe.gob.reniec.rrcc.plataformaelectronica.model.request.*;
import pe.gob.reniec.rrcc.plataformaelectronica.model.response.ApiResponse;
import pe.gob.reniec.rrcc.plataformaelectronica.model.response.BusqRegCivilRuipinResponse;
import pe.gob.reniec.rrcc.plataformaelectronica.model.response.LenguaResponse;
import pe.gob.reniec.rrcc.plataformaelectronica.service.RegistroFirmaService;
import pe.gob.reniec.rrcc.plataformaelectronica.service.RegistroLibroService;
import pe.gob.reniec.rrcc.plataformaelectronica.utility.ConstantUtil;

import java.util.List;
import java.util.stream.Collectors;
/*
@RestController
@RequestMapping("registro-libros")
@AllArgsConstructor*/
public class RegistroLibroController {

  private RegistroLibroService registroLibroService;
  private RegistroLibroMapper registroLibroMapper;
  private RegistroFirmaService registroFirmaService;

  @PostMapping("")
  public ResponseEntity<ApiResponse<String>> registrar(@Valid @RequestBody SolicitudRegLibroRequest request) {
    return ResponseEntity.ok(
        ApiResponse.<String>builder()
            .code(ConstantUtil.OK_CODE)
            .message(ConstantUtil.OK_MESSAGE)
            .data(registroLibroService
                .registrar(registroLibroMapper
                    .RegLibroReqToSolLibroBean(request)))
            .build()
    );
  }/*
  @PostMapping("actualizar")
  public ResponseEntity<ApiResponse<String>> actualizar(@Valid @RequestBody SolicitudRegLibroRequest request) {
    return ResponseEntity.ok(
        ApiResponse.<String>builder()
            .code(ConstantUtil.OK_CODE)
            .message(ConstantUtil.OK_MESSAGE)
            .data(registroLibroService.actualizar(registroLibroMapper.RegLibroReqToSolLibroBean(request)))
            .build()
    );
  }*/
  @PostMapping("actualizar")
  public ResponseEntity<ApiResponse<Void>> actualizar(@Valid @RequestBody SolicitudRegLibroRequest request) {
    System.out.print("SI FUNCIONO....");

    SolicitudBean aaa = registroLibroMapper.RegLibroReqToSolLibroBean(request);

    registroLibroService.actualizar(aaa);


    return ResponseEntity.ok(
            ApiResponse.<Void>builder()
                    .code(ConstantUtil.OK_CODE)
                    .message(ConstantUtil.OK_MESSAGE)
                    .build()
    );
  }
  @PostMapping("usuario-interno")
  public ResponseEntity<ApiResponse<String>> registrarUsuarioInterno(@Valid @RequestBody SolicitudRegLibroRequest request) {
    SolicitudBean solicitudBean = registroLibroMapper.RegLibroReqToSolLibroBean(request);
    solicitudBean.setNumeroDocumentoSolicitante(request.getDniSolicitante());
    solicitudBean.setPreNombres(request.getPreNombreSolicitante());
    solicitudBean.setPrimerApellido(request.getPrimerApeSolicitante());
    solicitudBean.setSegundoApellido(request.getSegundoApeSolicitante());
    solicitudBean.setCodigoOrec(request.getCodigoOrecSolicitante());
    return ResponseEntity.ok(
            ApiResponse.<String>builder()
                    .code(ConstantUtil.OK_CODE)
                    .message(ConstantUtil.OK_MESSAGE)
                    .data(registroLibroService
                            .registrarUsuarioInterno(solicitudBean))
                    .build()
    );
  }

  @PostMapping("validar-datos")
  public ResponseEntity<ApiResponse<String>> validarDatos(@Valid @RequestBody ValidarDatosRegLibroRequest request) {
     return ResponseEntity.ok(
        ApiResponse.<String>builder()
            .code(ConstantUtil.OK_CODE)
            .message(ConstantUtil.OK_MESSAGE)
            .data(registroLibroService.validarDatos(request))
            .build()
    );
  }
  @PostMapping("validar-datos-usuario-interno")
  public ResponseEntity<ApiResponse<String>> validarDatosUsarioInterno(@Valid @RequestBody ValidarDatosRegLibroUsuarioInternoRequest request) {
    return ResponseEntity.ok(
            ApiResponse.<String>builder()
                    .code(ConstantUtil.OK_CODE)
                    .message(ConstantUtil.OK_MESSAGE)
                    .data(registroLibroService.validarDatosUsuarioInterno(request))
                    .build()
    );
  }
  @PostMapping("consultar-por-datos-ruipin")
  public ResponseEntity<ApiResponse<PersonaBean>> consultarPorDatosRuipin(@RequestBody BusqPorDatosRegCivilRuipinRequest request) {
    return ResponseEntity.ok(registroFirmaService.consultarRegCivilPorDatosRuipin(request));
  }
}
