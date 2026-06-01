package com.tyd.kitprepago.modulo_ventas.dto.request;

import com.tyd.kitprepago.modulo_ventas.entity.TipoCliente;
import jakarta.validation.constraints.*;

public record CrearClienteRequest(
    @NotBlank @Size(max = 20) String dni,
    @NotBlank String nombres,
    @NotBlank String apellidos,
    @Size(max = 20) String telefono,
    TipoCliente tipo,         // null = GENERAL por defecto
    String razonSocial,       // solo si tipo = PDV
    @Size(max = 15) String ruc // solo si tipo = PDV
) {}
