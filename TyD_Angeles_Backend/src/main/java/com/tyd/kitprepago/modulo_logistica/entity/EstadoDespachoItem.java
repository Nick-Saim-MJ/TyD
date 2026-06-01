package com.tyd.kitprepago.modulo_logistica.entity;

public enum EstadoDespachoItem {
    /** Kit incluido en el despacho, aún no confirmado */
    ENVIADO,
    /** Recibido en buen estado → kit pasa a DISPONIBLE en destino */
    RECIBIDO_OK,
    /** Llegó con daños → kit pasa a DEFECTUOSO */
    RECIBIDO_DEFECTUOSO,
    /** No llegó físicamente → kit vuelve a DISPONIBLE en origen */
    NO_RECIBIDO
}
