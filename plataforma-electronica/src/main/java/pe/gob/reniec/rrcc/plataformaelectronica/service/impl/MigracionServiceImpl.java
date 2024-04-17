package pe.gob.reniec.rrcc.plataformaelectronica.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import pe.gob.reniec.rrcc.plataformaelectronica.exception.ApiValidateException;
import pe.gob.reniec.rrcc.plataformaelectronica.service.MigracionService;

import javax.sql.DataSource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.UUID;


import pe.gob.reniec.rrcc.plataformaelectronica.utility.ConstantUtil;
import java.io.InputStream;
import java.sql.*;
import java.util.Date;


@Slf4j
@Service
@AllArgsConstructor
public class MigracionServiceImpl implements MigracionService {

    private DataSource dataSourceOrigen;
    private DataSource dataSourceDestino;

    @Autowired
    public void setDataSourceOrigen(@Qualifier("dataSourceOrigen")DataSource dataSourceOrigen) {
        this.dataSourceOrigen = dataSourceOrigen;
    }
    @Autowired
    public void setDataSourceDestino(@Qualifier("dataSourceDestino")DataSource dataSourceDestino) {
        this.dataSourceDestino = dataSourceDestino;
    }
    @Override
    public String eliminar() {
        try {
            Connection conexionDestino = dataSourceDestino.getConnection();

            PreparedStatement stmtDeleteReg = null;
            PreparedStatement stmtDeletArc = null;
            PreparedStatement stmtDeletRel = null;
            ResultSet resultadoValidacion = null;
            PreparedStatement stmtValidarDestino = null;
            PreparedStatement stmtDeleteEdtc = null;

            String consultaDestinoSQL = "SELECT * FROM EDTC_FICHA_MIGRA";
            PreparedStatement stmtRelacion = conexionDestino.prepareStatement(consultaDestinoSQL);
            ResultSet resultadoRelacion = stmtRelacion.executeQuery();
            String deleteEdtc = "DELETE FROM IDO_PLATAFORMA_EXPE.EDTC_REG_CIVIL WHERE NU_DOC_IDENTIDAD = ?";
            String deleteRegCivil = "DELETE FROM IDO_PLATAFORMA_EXPE.EDTM_REG_CIVIL WHERE ID_REG_CIVIL = ?";
            String deleteArc = "DELETE FROM IDO_PLATAFORMA_EXPE.EDTR_ARCHIVO WHERE ID_ARCHIVO = ?";
            String deleteRel = "DELETE FROM EDTC_FICHA_MIGRA WHERE NU_DOC_IDENTIDAD = ?";

            Long id_reg_civil;
            Long id_archivo;
            String dni_migra;
            while (resultadoRelacion.next()) {

                dni_migra = resultadoRelacion.getString("NU_DOC_IDENTIDAD");
                System.out.println("\nValidando Data Migrada del DNI: "+dni_migra);
                id_reg_civil = resultadoRelacion.getLong("ID_REG_CIVIL");
                id_archivo = resultadoRelacion.getLong("ID_ARCHIVO");
                dni_migra = resultadoRelacion.getString("NU_DOC_IDENTIDAD");


                System.out.println("\n Eliminando Data Migrada de la tabla EDTC_REG_CIVIL: "+dni_migra);
                stmtDeleteEdtc = conexionDestino.prepareStatement(deleteEdtc);
                stmtDeleteEdtc.setString(1, dni_migra);
                int rowsAffected4 = stmtDeleteEdtc.executeUpdate();
                System.out.println("Cantidad de filas eliminadas de la tabla EDTC_REG_CIVIL: " + rowsAffected4);

                System.out.println("\n Eliminando Data Migrada de la tabla EDTM_REG_CIVIL: "+id_reg_civil);
                stmtDeleteReg = conexionDestino.prepareStatement(deleteRegCivil);
                stmtDeleteReg.setLong(1, id_reg_civil);
                int rowsAffected = stmtDeleteReg.executeUpdate();
                System.out.println("Cantidad de filas eliminadas de la tabla EDTM_REG_CIVIL: " + rowsAffected);

                System.out.println("\n Eliminando Data Migrada de la tabla EDTR_ARCHIVO: "+id_archivo);
                stmtDeletArc = conexionDestino.prepareStatement(deleteArc);
                stmtDeletArc.setLong(1, id_archivo);
                int rowsAffected2 = stmtDeletArc.executeUpdate();
                System.out.println("Cantidad de filas eliminadas de la tabla EDTR_ARCHIVO: " + rowsAffected2);

                System.out.println("\n Eliminando Data Migrada de la tabla Relacion, DNI: "+dni_migra);
                stmtDeletRel = conexionDestino.prepareStatement(deleteRel);
                stmtDeletRel.setString(1, dni_migra);
                int rowsAffected3 = stmtDeletRel.executeUpdate();
                System.out.println("Cantidad de filas eliminadas de la tabla Relacion: " + rowsAffected3);

                if (stmtDeleteEdtc != null) stmtDeleteEdtc.close();
                if (stmtDeleteReg != null) stmtDeleteReg.close();
                if (stmtDeletArc != null) stmtDeletArc.close();
                if (stmtDeletRel != null) stmtDeletRel.close();

                Thread.sleep(25);

            }
            // Cerrar conexiones
            resultadoRelacion.close();
            stmtRelacion.close();
            if (stmtDeleteEdtc != null) stmtDeleteEdtc.close();
            if (stmtDeleteReg != null) stmtDeleteReg.close();
            if (stmtDeletArc != null) stmtDeletArc.close();
            if (stmtDeletRel != null) stmtDeletRel.close();
            if (resultadoValidacion != null) resultadoValidacion.close();
            if (stmtValidarDestino != null) stmtValidarDestino.close();
            conexionDestino.close();

            System.out.println("Eliminación de Data Migrada completada con éxito.");
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error durante la eliminacion de data migrada: " + e.getMessage());
            throw new ApiValidateException(e.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);

        }
        return "Eliminación de Data Migrada completada con éxito.";
     }

    @Override
    public String migrar(){

        long idArchivo = 0;
        long idReg = 0;
        long idEdtc = 0;
        String localCorta;
        String detalleUbigeo;
        String co_Oficina;
        String Cod_Cont;
        String Cod_Pais;
        String Cod_Dep;
        String Cod_Dist;
        String Cod_Prov;
        String Cod_CentP;
        String tipo_reg;
        String nu_doc_alta;
        String dniActualizacion;
        String nombre;
        String primerSeg;
        String primerApe;
        String dni_crea;
        String dni;
        String hoActualizacion;
        String feActualizacion;
        String feCrea;
        String hoCrea;
        String fechaActFormateada;
        String fechaCreaFormateada;
        String feAlta;
        PreparedStatement stmtDestino = null;
        PreparedStatement stmtDestinoReg = null;
        PreparedStatement stmtValidarDestino = null;
        PreparedStatement stmtRelacionDestino = null;
        PreparedStatement stmNextIdReg = null;
        PreparedStatement stmNextIdEdtc = null;
        PreparedStatement stmNextIdArc = null;
        PreparedStatement stmtGevw_UbigeosOrigen = null;
        PreparedStatement stmtGetrLoalOrigen = null;
        PreparedStatement stmtedtc = null;
        ResultSet resultadoIdReg = null;
        ResultSet resultadoIdEdtc = null;
        ResultSet resultadoGevw_UbigeosOrigen = null;
        ResultSet resultadoIdArc = null;
        ResultSet resultadoGetrLocalOrigen = null;
        Date fechaCreaMigracion = null;
        Date fechaActMigracion = null;
        ResultSet resultadoValidacion = null;
        Date horaActualizacion;
        Date fechaActualizacion;
        Date horaCreacion;
        Date fechaCreacion;
        SimpleDateFormat formatoHoraOrigen = new SimpleDateFormat("HHmmss");
        SimpleDateFormat formatoHoraDestino = new SimpleDateFormat("HH:mm:ss");
        SimpleDateFormat formatoFechaOrigen = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat formatoFechaDestino = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat formatoFinal = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");


        try {
            Connection conexionOrigen = dataSourceOrigen.getConnection();
            Connection conexionDestino = dataSourceDestino.getConnection();

            // Consulta SQL para seleccionar datos de la tabla de origen:
            String consultaSQL = "SELECT * FROM DBALEM01.RCTV_REGISTRADOR_CIVIL WHERE ES_REGISTRADOR_CIVIL = '1'";
            PreparedStatement stmtOrigen = conexionOrigen.prepareStatement(consultaSQL);

            // Consulta SQL para insertar datos en la tabla de destino:
            String insercionRegSQL = "INSERT INTO IDO_PLATAFORMA_EXPE.EDTM_REG_CIVIL(ID_REG_CIVIL, ID_TIPO_SOLICITUD_FIRMA, " +
                    "CO_TIPO_DOC_IDENTIDAD, NU_DOC_IDENTIDAD, AP_PRIMER_APELLIDO, AP_SEGUNDO_APELLIDO, NO_PRENOMBRES, " +
                    "CO_OREC_REG_CIVIL, DE_DETALLE_OREC_CORTA, CO_ESTADO_REGISTRADOR, ID_CREA, FE_CREA, ID_ACTUALIZA, FE_ACTUALIZA, " +
                    "FE_CAMBIO_ESTADO) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            // Consulta SQL para insertar datos en la tabla de destino:
            String insercionSQL = "INSERT INTO IDO_PLATAFORMA_EXPE.EDTR_ARCHIVO (ID_ARCHIVO, CO_NOMBRE, DE_NOMBRE_ORIGINAL, CO_EXTENSION, " +
                    "IM_ARCHIVO, CO_ESTADO, FE_CREA, FE_ACTUALIZA, ID_CREA, ID_ACTUALIZA) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            String insercionRelacionSQL = "INSERT INTO IDO_PLATAFORMA_EXPE.EDTC_FICHA_MIGRA(ID_MIGRA, NU_DOC_IDENTIDAD, ID_REG_CIVIL, " +
                    "ID_ARCHIVO, ID_REGISTRO, FECHA_MIGRA) VALUES (IDO_PLATAFORMA_EXPE.EDSE_FICHA_MIGRA.nextval, ?, ?, ?, ?, SYSDATE)";

            String nextIdArchivo = "SELECT IDO_PLATAFORMA_EXPE.EDSE_ARCHIVO.nextval FROM DUAL";
            String nextIdReg = "SELECT IDO_PLATAFORMA_EXPE.EDSE_REG_CIVIL.nextval FROM DUAL";
            String nextIdEdtc = "SELECT IDO_PLATAFORMA_EXPE.EDSE_CREG_CIVIL.nextval FROM DUAL";

            String consultaDestinoSQL = "SELECT * FROM EDTC_FICHA_MIGRA WHERE NU_DOC_IDENTIDAD = ?";
            //EJECUTAR TABLA DE ORIGEN:
            ResultSet resultadoOrigen = stmtOrigen.executeQuery();

            String consultaGetr_LocalSQL = "SELECT * FROM GETR_LOCAL WHERE CO_LOCAL = ? ";
            String consultaGevw_UbigeosSQL = "SELECT DE_UBIGEO_DETALLE FROM IDOTABMAESTRA.GEVW_UBIGEOS WHERE CO_CONTINENTE=? AND CO_PAIS=? AND CO_DEPARTAMENTO=? AND CO_DISTRITO=? AND CO_PROVINCIA=? AND CO_CENTRO_POBLADO_O=?";

            String insercion_edct = "INSERT INTO IDO_PLATAFORMA_EXPE.EDTC_REG_CIVIL (ID_REGISTRO, ID_TIPO_SOLICITUD_FIRMA, CO_TIPO_DOC_IDENTIDAD, NU_DOC_IDENTIDAD," +
                    " AP_PRIMER_APELLIDO, AP_SEGUNDO_APELLIDO, NO_PRENOMBRES, NU_CELULAR, DE_MAIL, DE_DETALLE_OREC_CORTA, DE_UBIGEO_DETALLE, CO_CARGO_REGISTRADOR," +
                    " FE_FECHA_ALTA, FE_FECHA_BAJA, FE_FECHA_ACTUALIZACION, CO_ESTADO_REGISTRADOR, CO_CONDICION, CO_MOTIVO_ACTUALIZA, FE_FECHA_INICIO, FE_FECHA_FIN," +
                    " CO_ESTADO_FORMATO, DE_OBSERVACION, ID_CREA, FE_CREA, ID_ACTUALIZA, FE_ACTUALIZA, CO_OREC_REG_CIVIL, ID_DET_SOL_FIRMA)" +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1)";


            String consultaLast = "SELECT * FROM EDTC_FICHA_MIGRA WHERE ID_MIGRA IN (SELECT MAX(ID_MIGRA) FROM EDTC_FICHA_MIGRA)";
            PreparedStatement stmtObtenerLast = conexionDestino.prepareStatement(consultaLast);
            ResultSet resultadoRelacion = stmtObtenerLast.executeQuery();
            String deleteEdtc = "DELETE FROM IDO_PLATAFORMA_EXPE.EDTC_REG_CIVIL WHERE NU_DOC_IDENTIDAD = ?";
            String deleteRegCivil = "DELETE FROM IDO_PLATAFORMA_EXPE.EDTM_REG_CIVIL WHERE ID_REG_CIVIL = ?";
            String deleteArc = "DELETE FROM IDO_PLATAFORMA_EXPE.EDTR_ARCHIVO WHERE ID_ARCHIVO = ?";
            String deleteRel = "DELETE FROM EDTC_FICHA_MIGRA WHERE NU_DOC_IDENTIDAD = ?";

            if (resultadoRelacion.next()) {
                Long id_reg_civil;
                Long id_archivo;
                String dni_migra;

                dni_migra = resultadoRelacion.getString("NU_DOC_IDENTIDAD");
                id_reg_civil = resultadoRelacion.getLong("ID_REG_CIVIL");
                id_archivo = resultadoRelacion.getLong("ID_ARCHIVO");

                System.out.println("\n Eliminando Data Migrada de la tabla EDTC_REG_CIVIL: " + dni_migra);
                PreparedStatement stmtDeleteEdtc = conexionDestino.prepareStatement(deleteEdtc);
                stmtDeleteEdtc.setString(1, dni_migra);
                int rowsAffected4 = stmtDeleteEdtc.executeUpdate();
                System.out.println("Cantidad de filas eliminadas de la tabla EDTC_REG_CIVIL: " + rowsAffected4);

                System.out.println("\n Eliminando Data Migrada de la tabla EDTM_REG_CIVIL: " + id_reg_civil);
                PreparedStatement stmtDeleteReg = conexionDestino.prepareStatement(deleteRegCivil);
                stmtDeleteReg.setLong(1, id_reg_civil);
                int rowsAffected = stmtDeleteReg.executeUpdate();
                System.out.println("Cantidad de filas eliminadas de la tabla EDTM_REG_CIVIL: " + rowsAffected);

                System.out.println("\n Eliminando Data Migrada de la tabla EDTR_ARCHIVO: " + id_archivo);
                PreparedStatement stmtDeletArc = conexionDestino.prepareStatement(deleteArc);
                stmtDeletArc.setLong(1, id_archivo);
                int rowsAffected2 = stmtDeletArc.executeUpdate();
                System.out.println("Cantidad de filas eliminadas de la tabla EDTR_ARCHIVO: " + rowsAffected2);

                System.out.println("\n Eliminando Data Migrada de la tabla Relacion, DNI: " + dni_migra);
                PreparedStatement stmtDeletRel = conexionDestino.prepareStatement(deleteRel);
                stmtDeletRel.setString(1, dni_migra);
                int rowsAffected3 = stmtDeletRel.executeUpdate();
                System.out.println("Cantidad de filas eliminadas de la tabla Relacion: " + rowsAffected3);

                if (stmtDeleteEdtc != null) stmtDeleteEdtc.close();
                if (stmtDeleteReg != null) stmtDeleteReg.close();
                if (stmtDeletArc != null) stmtDeletArc.close();
                if (stmtDeletRel != null) stmtDeletRel.close();
                if (stmtObtenerLast != null) stmtObtenerLast.close();
            }
            if (stmtObtenerLast != null) stmtObtenerLast.close();
            if (resultadoRelacion != null) resultadoRelacion.close();

            while (resultadoOrigen.next()) {
                co_Oficina = resultadoOrigen.getString("CO_OFICINA_RC");
                dni = resultadoOrigen.getString("NU_DNI_REGISTRADOR_CIVIL");
                System.out.println("\nRealizando insercion del DNI: " + dni);

                stmtValidarDestino = conexionDestino.prepareStatement(consultaDestinoSQL);
                stmtValidarDestino.setString(1, dni);
                resultadoValidacion = stmtValidarDestino.executeQuery();
                if (resultadoValidacion.next()) {
                    System.out.println("Registro duplicado encontrado para DNI: " + dni);
                    resultadoValidacion.close();
                    stmtValidarDestino.close();

                } else {
                    System.out.println("No se encontro registro duplicado para DNI: " + dni + ".");
                    stmtDestino = conexionDestino.prepareStatement(insercionSQL);
                    stmtDestinoReg = conexionDestino.prepareStatement(insercionRegSQL);
                    stmtRelacionDestino = conexionDestino.prepareStatement(insercionRelacionSQL);
                    stmtedtc = conexionDestino.prepareStatement(insercion_edct);

                    //Consulta SQL a GETR_LOCAL:
                    stmtGetrLoalOrigen = conexionOrigen.prepareStatement(consultaGetr_LocalSQL);
                    stmtGetrLoalOrigen.setString(1, co_Oficina);
                    resultadoGetrLocalOrigen = stmtGetrLoalOrigen.executeQuery();

                    if (resultadoGetrLocalOrigen.next()) {
                        // OBTENER CAMPOS DE GETR_LOCAL:
                        Cod_Cont = resultadoGetrLocalOrigen.getString("CO_CONTINENTE");
                        Cod_Pais = resultadoGetrLocalOrigen.getString("CO_PAIS");
                        Cod_Dep = resultadoGetrLocalOrigen.getString("CO_DEPARTAMENTO");
                        Cod_Dist = resultadoGetrLocalOrigen.getString("CO_DISTRITO");
                        Cod_Prov = resultadoGetrLocalOrigen.getString("CO_PROVINCIA");
                        Cod_CentP = resultadoGetrLocalOrigen.getString("CO_CENTRO_POBLADO");

                        localCorta = resultadoGetrLocalOrigen.getString("DE_LOCAL_CORTA");

                        stmtGevw_UbigeosOrigen = conexionDestino.prepareStatement(consultaGevw_UbigeosSQL);
                        stmtGevw_UbigeosOrigen.setString(1, Cod_Cont);
                        stmtGevw_UbigeosOrigen.setString(2, Cod_Pais);
                        stmtGevw_UbigeosOrigen.setString(3, Cod_Dep);
                        stmtGevw_UbigeosOrigen.setString(4, Cod_Dist);
                        stmtGevw_UbigeosOrigen.setString(5, Cod_Prov);
                        stmtGevw_UbigeosOrigen.setString(6, Cod_CentP);

                        // EJECUTAR TABLA DE ORIGEN:
                        resultadoGevw_UbigeosOrigen = stmtGevw_UbigeosOrigen.executeQuery();

                        if (resultadoGevw_UbigeosOrigen.next()) {
                            detalleUbigeo = resultadoGevw_UbigeosOrigen.getString("DE_UBIGEO_DETALLE");
                        } else {
                            detalleUbigeo = "  ";
                        }
                    } else {
                        detalleUbigeo = "  ";
                        localCorta = "  ";
                    }
                    if (resultadoGevw_UbigeosOrigen != null) resultadoGevw_UbigeosOrigen.close();
                    if (resultadoGetrLocalOrigen != null) resultadoGetrLocalOrigen.close();
                    if (stmtGetrLoalOrigen != null) stmtGetrLoalOrigen.close();
                    if (stmtGevw_UbigeosOrigen != null) stmtGevw_UbigeosOrigen.close();


                    stmNextIdArc = conexionDestino.prepareStatement(nextIdArchivo);
                    resultadoIdArc = stmNextIdArc.executeQuery();
                    if (resultadoIdArc.next()) {
                        idArchivo = resultadoIdArc.getLong(1);
                    }
                    if (resultadoIdArc != null) resultadoIdArc.close();
                    if (stmNextIdArc != null) stmNextIdArc.close();

                    stmNextIdReg = conexionDestino.prepareStatement(nextIdReg);
                    resultadoIdReg = stmNextIdReg.executeQuery();
                    if (resultadoIdReg.next()) {
                        idReg = resultadoIdReg.getLong(1);
                    }
                    if (resultadoIdReg != null) resultadoIdReg.close();
                    if (stmNextIdReg != null) stmNextIdReg.close();

                    stmNextIdEdtc = conexionDestino.prepareStatement(nextIdEdtc);
                    resultadoIdEdtc = stmNextIdEdtc.executeQuery();
                    if (resultadoIdEdtc.next()) {
                        idEdtc = resultadoIdEdtc.getLong(1);
                    }
                    if (resultadoIdEdtc != null) resultadoIdEdtc.close();
                    if (stmNextIdEdtc != null) stmNextIdEdtc.close();

                    tipo_reg = resultadoOrigen.getString("TI_REGISTRADOR");
                    dni_crea = resultadoOrigen.getString("NU_DNI_CREACION");
                    primerApe = resultadoOrigen.getString("AP_PATERNO_REGISTRADOR");
                    primerSeg = resultadoOrigen.getString("AP_MATERNO_REGISTRADOR");
                    nombre = resultadoOrigen.getString("NO_REGISTRADOR");
                    dniActualizacion = resultadoOrigen.getString("NU_DNI_ACTUALIZACION");
                    nu_doc_alta = resultadoOrigen.getString("NU_DOC_ALTA");
                    feAlta = resultadoOrigen.getString("FE_ALTA_REGISTRADOR");

                    hoCrea = resultadoOrigen.getString("HO_CREACION");
                    feCrea = resultadoOrigen.getString("FE_CREACION");

                    if (hoCrea != null && !hoCrea.trim().isEmpty() && feCrea != null && !feCrea.trim().isEmpty()) {
                        horaCreacion = formatoHoraOrigen.parse(hoCrea);
                        fechaCreacion = formatoFechaOrigen.parse(feCrea);
                        fechaCreaFormateada = formatoFechaDestino.format(fechaCreacion) + " " + formatoHoraDestino.format(horaCreacion);
                        fechaCreaMigracion = formatoFinal.parse(fechaCreaFormateada);
                    }
                    hoActualizacion = resultadoOrigen.getString("HO_ACTUALIZACION");
                    feActualizacion = resultadoOrigen.getString("FE_ACTUALIZACION");

                    if (hoActualizacion != null && !hoActualizacion.trim().isEmpty() && feActualizacion != null && !feActualizacion.trim().isEmpty()) {
                        horaActualizacion = formatoHoraOrigen.parse(hoActualizacion);
                        fechaActualizacion = formatoFechaOrigen.parse(feActualizacion);
                        fechaActFormateada = formatoFechaDestino.format(fechaActualizacion) + " " + formatoHoraDestino.format(horaActualizacion);
                        fechaActMigracion = formatoFinal.parse(fechaActFormateada);
                    }

                    stmtDestino.setLong(1, idArchivo);
                    stmtDestino.setString(2, UUID.randomUUID().toString());
                    stmtDestino.setString(3, nu_doc_alta != null ? nu_doc_alta : ConstantUtil.EMPTY_VALUE);
                    stmtDestino.setString(4, ConstantUtil.PDF_FORMAT);
                    InputStream input = resultadoOrigen.getBinaryStream("BL_FICHA_REGISTRADOR");
                    stmtDestino.setBinaryStream(5, input);
                    stmtDestino.setInt(6, ConstantUtil.ASIGNADO_CODE);
                    if (fechaCreaMigracion != null) {
                        stmtDestino.setDate(7, new java.sql.Date(fechaCreaMigracion.getTime()));
                        stmtDestinoReg.setDate(12, new java.sql.Date(fechaCreaMigracion.getTime()));
                        stmtedtc.setDate(24, new java.sql.Date(fechaCreaMigracion.getTime()));
                    } else {
                        stmtDestino.setNull(7, java.sql.Types.DATE);
                        stmtDestinoReg.setNull(12, java.sql.Types.DATE);
                        stmtedtc.setNull(24, java.sql.Types.DATE);
                    }
                    if (fechaActMigracion != null) {
                        stmtDestino.setDate(8, new java.sql.Date(fechaActMigracion.getTime()));
                        stmtDestinoReg.setDate(14, new java.sql.Date(fechaActMigracion.getTime()));
                        stmtedtc.setDate(26, new java.sql.Date(fechaActMigracion.getTime()));
                    } else {
                        stmtDestino.setNull(8, java.sql.Types.DATE);
                        stmtDestinoReg.setNull(14, java.sql.Types.DATE);
                        stmtedtc.setNull(26, java.sql.Types.DATE);
                    }
                    stmtDestino.setString(9, dni_crea != null ? dni_crea : ConstantUtil.EMPTY_VALUE);
                    stmtDestino.setString(10, dniActualizacion != null ? dniActualizacion : ConstantUtil.EMPTY_VALUE);

                    stmtDestinoReg.setLong(1, idReg);
                    stmtDestinoReg.setString(2, ConstantUtil.COD_ESTADO_ACTIVO);
                    stmtDestinoReg.setString(3, ConstantUtil.CO_DNI);
                    stmtDestinoReg.setString(4, dni != null ? dni : ConstantUtil.EMPTY_VALUE);
                    stmtDestinoReg.setString(5, primerApe != null ? primerApe : ConstantUtil.EMPTY_VALUE);
                    stmtDestinoReg.setString(6, primerSeg != null ? primerSeg : ConstantUtil.EMPTY_VALUE);
                    stmtDestinoReg.setString(7, nombre != null ? nombre : ConstantUtil.EMPTY_VALUE);
                    stmtDestinoReg.setString(8, co_Oficina != null ? co_Oficina : ConstantUtil.EMPTY_VALUE);
                    stmtDestinoReg.setString(9, localCorta != null ? localCorta : ConstantUtil.EMPTY_VALUE);
                    stmtDestinoReg.setString(10, ConstantUtil.COD_ESTADO_ACTIVO);
                    stmtDestinoReg.setString(11, dni_crea != null ? dni_crea : ConstantUtil.EMPTY_VALUE);
                    stmtDestinoReg.setString(13, dniActualizacion != null ? dniActualizacion : ConstantUtil.EMPTY_VALUE);
                    stmtDestinoReg.setNull(15, java.sql.Types.DATE);

                    stmtedtc.setLong(1, idEdtc);
                    stmtedtc.setString(2, ConstantUtil.COD_ESTADO_ACTIVO);
                    stmtedtc.setString(3, ConstantUtil.CO_DNI);
                    stmtedtc.setString(4, dni != null ? dni : ConstantUtil.EMPTY_VALUE);
                    stmtedtc.setString(5, primerApe != null ? primerApe : ConstantUtil.EMPTY_VALUE);
                    stmtedtc.setString(6, primerSeg != null ? primerSeg : ConstantUtil.EMPTY_VALUE);
                    stmtedtc.setString(7, nombre != null ? nombre : ConstantUtil.EMPTY_VALUE);
                    stmtedtc.setNull(8, java.sql.Types.VARCHAR);
                    stmtedtc.setNull(9, java.sql.Types.VARCHAR);
                    stmtedtc.setString(10, localCorta != null ? localCorta : ConstantUtil.EMPTY_VALUE);

                    if (detalleUbigeo != null) {
                        stmtedtc.setString(11, detalleUbigeo);
                    } else {
                        stmtedtc.setString(11, ConstantUtil.EMPTY_VALUE);
                    }
                    if (tipo_reg != null) {
                        if (tipo_reg.equals("8") || tipo_reg.equals("2") || tipo_reg.equals("3")) {
                            stmtedtc.setString(12, "1");
                        } else {
                            stmtedtc.setString(12, "2");
                        }
                    } else {
                        stmtedtc.setString(12, ConstantUtil.EMPTY_VALUE);
                    }
                    if (feAlta == null || feAlta.trim().isEmpty()) {
                        stmtedtc.setNull(13, java.sql.Types.DATE);
                    } else {
                        Date fechaAlta = formatoFechaOrigen.parse(feAlta);
                        String fechaFormateada = formatoFechaDestino.format(fechaAlta);
                        Date fechaAltaMigracion = formatoFechaDestino.parse(fechaFormateada);
                        stmtedtc.setDate(13, new java.sql.Date(fechaAltaMigracion.getTime()));
                    }
                    stmtedtc.setNull(14, java.sql.Types.DATE);
                    stmtedtc.setNull(15, java.sql.Types.DATE);
                    stmtedtc.setString(16, ConstantUtil.COD_ESTADO_ACTIVO);
                    stmtedtc.setString(17, ConstantUtil.EMPTY_VALUE);
                    stmtedtc.setString(18, ConstantUtil.EMPTY_VALUE);
                    stmtedtc.setNull(19, java.sql.Types.DATE);
                    stmtedtc.setNull(20, java.sql.Types.DATE);
                    stmtedtc.setString(21, ConstantUtil.EMPTY_VALUE);
                    if ((resultadoOrigen.getString("DE_OBSERVA_ALTA")) != null) {
                        stmtedtc.setString(22, resultadoOrigen.getString("DE_OBSERVA_ALTA"));
                    } else {
                        stmtedtc.setString(22, ConstantUtil.EMPTY_VALUE);
                    }
                    stmtedtc.setString(23, dni_crea != null ? dni_crea : ConstantUtil.EMPTY_VALUE);
                    stmtedtc.setString(25, dniActualizacion != null ? dniActualizacion : ConstantUtil.EMPTY_VALUE);
                    stmtedtc.setString(27, co_Oficina != null ? co_Oficina : ConstantUtil.EMPTY_VALUE);

                    stmtRelacionDestino.setString(1, dni);
                    stmtRelacionDestino.setLong(2, idReg);
                    stmtRelacionDestino.setLong(3, idArchivo);
                    stmtRelacionDestino.setLong(4, idEdtc);

                    System.out.println("Realizando Relacion del registrador y su ficha del DNI: " + dni + ".");
                    stmtRelacionDestino.executeQuery();

                    System.out.println("Migrando a tabla Cabezera Registradores: : " + idEdtc + ".");
                    stmtedtc.executeQuery();

                    System.out.println("Migrando a tabla Registradores: " + idReg + ".");
                    stmtDestinoReg.executeUpdate();

                    System.out.println("Migrando la ficha del registrador a tabla Archivos: " + idArchivo + ".");
                    stmtDestino.executeUpdate();

                    if (stmtRelacionDestino != null) stmtRelacionDestino.close();
                    if (stmtedtc != null) stmtedtc.close();
                    if (stmtDestinoReg != null) stmtDestinoReg.close();
                    if (stmtDestino != null) stmtDestino.close();
                    if (stmtValidarDestino != null) stmtValidarDestino.close();
                    resultadoValidacion.close();


                    Thread.sleep(50);
                }
            }

            // Cerrar conexiones
            if (resultadoIdArc != null) resultadoIdArc.close();
            if (stmNextIdArc != null) stmNextIdArc.close();
            if (resultadoIdReg != null) resultadoIdReg.close();
            if (stmNextIdReg != null) stmNextIdReg.close();
            if (resultadoIdEdtc != null) resultadoIdEdtc.close();
            if (stmNextIdEdtc != null) stmNextIdEdtc.close();
            if (stmtRelacionDestino != null) stmtRelacionDestino.close();
            if (stmtDestinoReg != null) stmtDestinoReg.close();
            if (stmtDestino != null) stmtDestino.close();
            if (stmtValidarDestino != null) stmtValidarDestino.close();
            if (stmtedtc != null) stmtedtc.close();
            resultadoOrigen.close();
            stmtOrigen.close();
            conexionOrigen.close();
            conexionDestino.close();

            System.out.println("Migración completada con éxito.");
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error durante la migración: " + e.getMessage());
            throw new ApiValidateException(e.getMessage());
        } catch (ParseException e) {
            e.printStackTrace();
            System.err.println("Error de Parseo: " + e.getMessage());
            throw new ApiValidateException(e.getMessage());
        } catch (InterruptedException e) {
            System.err.println("Error: " + e.getMessage());
            throw new RuntimeException(e);
        }

        return "Migración completada con éxito.";
    }
}
