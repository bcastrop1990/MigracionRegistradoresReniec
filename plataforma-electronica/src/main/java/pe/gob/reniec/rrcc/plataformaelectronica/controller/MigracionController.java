package pe.gob.reniec.rrcc.plataformaelectronica.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.gob.reniec.rrcc.plataformaelectronica.model.response.ApiResponse;
import pe.gob.reniec.rrcc.plataformaelectronica.service.MigracionService;
import pe.gob.reniec.rrcc.plataformaelectronica.utility.ConstantUtil;

@RestController
@RequestMapping("migracion")
@AllArgsConstructor
public class MigracionController {

    private MigracionService migracionService;
    @PostMapping(value = "eliminar")
    public ResponseEntity<ApiResponse<String>> eliminar() {
        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .code(ConstantUtil.OK_CODE)
                        .message(ConstantUtil.OK_MESSAGE)
                        .data(migracionService.eliminar())
                        .build());
    }


    @PostMapping(value = "migrar")
    public ResponseEntity<ApiResponse<String>> migrar() {
        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .code(ConstantUtil.OK_CODE)
                        .message(ConstantUtil.OK_MESSAGE)
                        .data(migracionService.migrar())
                        .build()
        );
    }
}
