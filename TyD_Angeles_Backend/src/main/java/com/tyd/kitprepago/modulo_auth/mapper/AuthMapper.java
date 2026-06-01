package com.tyd.kitprepago.modulo_auth.mapper;

import com.tyd.kitprepago.modulo_auth.dto.response.SucursalResponse;
import com.tyd.kitprepago.modulo_auth.dto.response.UsuarioResponse;
import com.tyd.kitprepago.modulo_auth.dto.response.ZonaResponse;
import com.tyd.kitprepago.modulo_auth.entity.Sucursal;
import com.tyd.kitprepago.modulo_auth.entity.Usuario;
import com.tyd.kitprepago.modulo_auth.entity.Zona;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct genera la implementación en tiempo de compilación.
 * No escribir código manual de mapeo — solo declarar los métodos.
 *
 * componentModel = "spring" (configurado en pom.xml con -Amapstruct.defaultComponentModel=spring)
 * hace que la implementación sea un @Component inyectable con @Autowired / @RequiredArgsConstructor.
 */
@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AuthMapper {

    // ── USUARIO ──────────────────────────────────────────────────────────────

    @Mapping(source = "zona.id",        target = "zonaId")
    @Mapping(source = "zona.nombre",    target = "zonaNombre")
    @Mapping(source = "sucursal.id",    target = "sucursalId")
    @Mapping(source = "sucursal.nombre",target = "sucursalNombre")
    UsuarioResponse toUsuarioResponse(Usuario u);

    List<UsuarioResponse> toUsuarioResponseList(List<Usuario> usuarios);

    // ── SUCURSAL ─────────────────────────────────────────────────────────────

    @Mapping(source = "zona.id",                   target = "zonaId")
    @Mapping(source = "zona.nombre",               target = "zonaNombre")
    @Mapping(source = "ubicacionFisica.id",        target = "ubicacionFisicaId")
    @Mapping(source = "ubicacionFisica.nombre",    target = "ubicacionFisicaNombre")
    SucursalResponse toSucursalResponse(Sucursal s);

    List<SucursalResponse> toSucursalResponseList(List<Sucursal> sucursales);

    // ── ZONA ─────────────────────────────────────────────────────────────────

    @Mapping(target = "totalSucursales", expression = "java(z.getSucursales() != null ? z.getSucursales().size() : 0)")
    ZonaResponse toZonaResponse(Zona z);

    List<ZonaResponse> toZonaResponseList(List<Zona> zonas);
}