package com.tyd.kitprepago.modulo_ventas.dto.response;
import com.tyd.kitprepago.modulo_ventas.entity.TipoCliente;
import java.time.Instant;
public record ClienteResponse(
    Long id, String dni, String nombres, String apellidos,
    String nombreCompleto, String telefono, TipoCliente tipo,
    String razonSocial, String ruc, Instant createdAt
) {}
