create or replace PROCEDURE EDSP_SOL_FIRMA_OBT_X_DNI (
P_VNUM_DOC VARCHAR2,
P_CRRESULT OUT SYS_REFCURSOR
)
AS

BEGIN
    OPEN P_CRRESULT FOR
    SELECT SOL.ID_SOLICITUD,
    SOL.ID_TIPO_REGISTRO,
    SOL.CO_TIPO_DOC_IDENTI_SOLICITANTE,
    SOL.NU_DOC_IDENTIDAD_SOLICITANTE,
    SOL.AP_PRIMER_APELLIDO,
    SOL.AP_SEGUNDO_APELLIDO,
    SOL.NO_PRENOMBRES,
    SOL.NU_CELULAR,
    SOL.DE_MAIL,
    SOL.DE_DETALLE_OREC_LARGA,
    SOL.DE_DETALLE_OREC_CORTA,
    SOL.CO_DEPARTAMENTO_OREC,
    SOL.CO_PROVINCIA_OREC,
    SOL.CO_DISTRITO_OREC,
    SOL.CO_CP_OREC,
    SOL.FE_FECHA_SOLICITUD,
    SOL.CO_ESTADO_SOLICITUD,
    SOL.CO_TIPO_ARCHIVO_SUSTENTO,
    SOL.ID_ARCHIVO_SUSTENTO,
    SOL.CO_MOD_REGISTRO,
    SOL.ID_CREA,
    SOL.FE_CREA,
    SOL.CO_OREC_SOLICITUD,
    SOL.NU_SOLICITUD_NUMERO,
    TAR.DE_NOMBRE_ARCHIVO DE_TIPO_ARCHIVO_SUSTENTO 
    FROM IDO_PLATAFORMA_EXPE.EDTC_SOLICITUD SOL
    INNER JOIN IDO_PLATAFORMA_EXPE.EDTV_TIPO_ARCHIVO TAR ON SOL.CO_TIPO_ARCHIVO_SUSTENTO = TAR.ID_TIPO_ARCHIVO
    INNER JOIN IDO_PLATAFORMA_EXPE.EDTD_DET_SOL_FIRMA DSF ON SOL.ID_SOLICITUD = DSF.ID_SOLICITUD        
    WHERE DSF.NU_DOC_IDENTIDAD = P_VNUM_DOC AND SOL.CO_ESTADO = '1';
    
END;