package com.tyd.kitprepago.modulo_ventas.service;

import com.tyd.kitprepago.modulo_auth.entity.Usuario;
import com.tyd.kitprepago.modulo_ventas.dto.request.ConfirmarActivacionRequest;
import com.tyd.kitprepago.modulo_ventas.dto.response.ActivacionResponse;
import com.tyd.kitprepago.modulo_ventas.entity.Activacion;
import com.tyd.kitprepago.modulo_ventas.entity.EstadoActivacion;
import com.tyd.kitprepago.modulo_ventas.mapper.VentasMapper;
import com.tyd.kitprepago.modulo_ventas.repository.ActivacionRepository;
import com.tyd.kitprepago.shared.exception.RecursoNoEncontradoException;
import com.tyd.kitprepago.shared.security.UsuarioPrincipal;
import com.tyd.kitprepago.shared.zona.ZonaContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivacionService {

    private final ActivacionRepository activacionRepo;
    private final VentasMapper mapper;
    private final ZonaContextHolder zonaCtx;
    private final JpaRepository<Usuario, Long> usuarioRepo;

    /**
     * Lista activaciones PENDIENTE filtradas por zona del usuario
     */
    @Transactional(readOnly = true)
    public List<ActivacionResponse> listarPendientes() {
        Long zonaId = zonaCtx.getZonaIdFiltro().orElse(null);
        return mapper.toActivacionResponseList(activacionRepo.findPendientesPorZona(zonaId));
    }

    /**
     * Confirma la activación cuando DirecTV procesa la señal.
     * Cambia estado → ACTIVO y registra fecha_activacion y registrado_por.
     */
    @Transactional
    public ActivacionResponse confirmar(Long id, ConfirmarActivacionRequest req,
                                        UsuarioPrincipal confirmador) {
        Activacion activacion = activacionRepo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Activacion", id));

        if (EstadoActivacion.ACTIVO.equals(activacion.getEstado())) {
            throw new IllegalStateException("La activación " + id + " ya fue confirmada");
        }

        Usuario usuario = usuarioRepo.findById(confirmador.getId()).orElseThrow();

        activacion.setEstado(EstadoActivacion.ACTIVO);
        activacion.setFechaActivacion(Instant.now());
        activacion.setRegistradoPor(usuario);
        if (req.montoRecargaInicial() != null) {
            activacion.setMontoRecargaInicial(req.montoRecargaInicial());
        }
        if (req.comentarios() != null) {
            activacion.setComentarios(req.comentarios());
        }

        log.info("Activación {} confirmada por {}", id, confirmador.getUsername());
        return mapper.toActivacionResponse(activacionRepo.save(activacion));
    }
}
