package pe.gob.reniec.rrcc.plataformaelectronica.model.response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OficinaPorDatosResponse {
    private String codigo;

    private String nombreDepartamento;
    private String coNombreDepartamento;
    private String nombreProvincia;
    private String coNombreProvincia;
    private String nombreDistrito;
    private String coNombreDistrito;
    private String nombreCentroPoblado;
    private String coNombreCentroPoblado;
    private String nombreOficina;
    private String coNombreOficina;

}
