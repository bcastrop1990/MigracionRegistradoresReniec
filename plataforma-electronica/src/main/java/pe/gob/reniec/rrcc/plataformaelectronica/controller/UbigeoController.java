package pe.gob.reniec.rrcc.plataformaelectronica.controller;

import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.gob.reniec.rrcc.plataformaelectronica.controller.mapper.UbigeoMapper;
import pe.gob.reniec.rrcc.plataformaelectronica.model.response.ApiResponse;
import pe.gob.reniec.rrcc.plataformaelectronica.model.response.OficinaPorDatosResponse;
import pe.gob.reniec.rrcc.plataformaelectronica.model.response.UbigeoResponse;
import pe.gob.reniec.rrcc.plataformaelectronica.model.response.UbigeoCoDenoResponse;
import pe.gob.reniec.rrcc.plataformaelectronica.model.request.*;
import pe.gob.reniec.rrcc.plataformaelectronica.service.SeguimientoService;
import pe.gob.reniec.rrcc.plataformaelectronica.service.UbigeoService;
import pe.gob.reniec.rrcc.plataformaelectronica.utility.ConstantUtil;
/*
@RestController
@RequestMapping("ubigeos")
@AllArgsConstructor*/
public class UbigeoController {
  private UbigeoService ubigeoService;
  private UbigeoMapper ubigeoMapper;
  private SeguimientoService seguimientoService;
  @GetMapping("departamentos")
  public ResponseEntity<ApiResponse<List<UbigeoResponse>>> listarDepartamentos() {
    return ResponseEntity.ok(
        ApiResponse.<List<UbigeoResponse>>builder()
            .code(ConstantUtil.OK_CODE)
            .message(ConstantUtil.OK_MESSAGE)
            .data(ubigeoService.listarDepartamentos()
                .stream()
                .map(ubigeoMapper::ubigeoBeanToUbigeoRes)
                .collect(Collectors.toList()))
            .build());
  }

  @GetMapping("provincias")
  public ResponseEntity<ApiResponse<List<UbigeoResponse>>> listarProvincias(@RequestParam String idDepartamento) {
    return ResponseEntity.ok(ApiResponse.<List<UbigeoResponse>>builder()
        .code(ConstantUtil.OK_CODE)
        .message(ConstantUtil.OK_MESSAGE)
        .data(ubigeoService
            .listarProvincias(idDepartamento)
            .stream()
            .map(ubigeoMapper::ubigeoBeanToUbigeoRes)
            .collect(Collectors.toList()))
        .build());
  }

  @GetMapping("distritos")
  public ResponseEntity<ApiResponse<List<UbigeoResponse>>> listarDistritos(@RequestParam String idDepartamento,
                                                                           @RequestParam String idProvincia) {
    return ResponseEntity.ok(ApiResponse.<List<UbigeoResponse>>builder()
        .code(ConstantUtil.OK_CODE)
        .message(ConstantUtil.OK_MESSAGE)
        .data(ubigeoService
            .listarDistritos(idDepartamento, idProvincia)
            .stream()
            .map(ubigeoMapper::ubigeoBeanToUbigeoRes)
            .collect(Collectors.toList()))
        .build());
  }

  @GetMapping("centro-poblados")
  public ResponseEntity<ApiResponse<List<UbigeoCoDenoResponse>>> listarCentroPoblados(@RequestParam String idDepartamento,
                                                                                @RequestParam String idProvincia,
                                                                                @RequestParam String idDistrito) {
    return ResponseEntity.ok(ApiResponse.<List<UbigeoCoDenoResponse>>builder()
        .code(ConstantUtil.OK_CODE)
        .message(ConstantUtil.OK_MESSAGE)
        .data(ubigeoService
            .listarCentroPoblado(idDepartamento, idProvincia, idDistrito)
            .stream()
            .map(ubigeoMapper::ubigeoBeanToUbigeoCoDenoRes)
            .collect(Collectors.toList()))
        .build());
  }

  @GetMapping("unidad-organica")
  public ResponseEntity<ApiResponse<List<UbigeoResponse>>> listarUnidadOrganica(@RequestParam String idDepartamento,
                                                                                      @RequestParam String idProvincia,
                                                                                      @RequestParam String idDistrito) {
    return ResponseEntity.ok(ApiResponse.<List<UbigeoResponse>>builder()
            .code(ConstantUtil.OK_CODE)
            .message(ConstantUtil.OK_MESSAGE)
            .data(ubigeoService
                    .listarUnidadOrganica(idDepartamento, idProvincia, idDistrito)
                    .stream()
                    .map(ubigeoMapper::ubigeoBeanToUbigeoRes)
                    .collect(Collectors.toList()))
            .build());
  }

  @GetMapping("oficina-por-datos")
  public ResponseEntity<ApiResponse<OficinaPorDatosResponse>> oficinaPorDatos(@RequestParam(name = "dni") String dni,
                                                                              @RequestParam(name = "digitoVerifica") String digitoVerifica,
                                                                              @RequestParam(name = "fechaEmision") String fechaEmision) {
    BusqOficinaRequest request = new BusqOficinaRequest();
    request.setDni(dni);
    request.setDigitoVerifica(digitoVerifica);
    request.setFechaEmision(fechaEmision);
    return ResponseEntity.ok(
            ApiResponse.<OficinaPorDatosResponse>builder()
                    .code(ConstantUtil.OK_CODE)
                    .message(ConstantUtil.OK_MESSAGE)
                    .data(seguimientoService.buscarOficinaPorDatos(request))
                    .build());
  }
  @GetMapping("oficina-por-dni/{dni}")
  public ResponseEntity<ApiResponse<OficinaPorDatosResponse>> oficinaPorDni(@PathVariable String dni) {
    return ResponseEntity.ok(
            ApiResponse.<OficinaPorDatosResponse>builder()
                    .code(ConstantUtil.OK_CODE)
                    .message(ConstantUtil.OK_MESSAGE)
                    .data(seguimientoService.buscarOficinaPorDni(dni))
                    .build());
  }
}
