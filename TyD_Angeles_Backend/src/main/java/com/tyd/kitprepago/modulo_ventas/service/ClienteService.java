package com.tyd.kitprepago.modulo_ventas.service;

import com.tyd.kitprepago.modulo_ventas.dto.request.CrearClienteRequest;
import com.tyd.kitprepago.modulo_ventas.dto.response.ClienteResponse;
import com.tyd.kitprepago.modulo_ventas.entity.Cliente;
import com.tyd.kitprepago.modulo_ventas.entity.TipoCliente;
import com.tyd.kitprepago.modulo_ventas.mapper.VentasMapper;
import com.tyd.kitprepago.modulo_ventas.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClienteService {

    private final ClienteRepository clienteRepo;
    private final VentasMapper mapper;

    /**
     * Autocomplete: busca por DNI, nombre, apellido o RUC.
     * Límite de 10 resultados — suficiente para el dropdown del formulario.
     */
    @Transactional(readOnly = true)
    public List<ClienteResponse> buscar(String q) {
        if (q == null || q.trim().length() < 2) return List.of();
        return mapper.toClienteResponseList(clienteRepo.buscarParaAutocomplete(q.trim()));
    }

    /**
     * Lista completa con filtro por tipo — solo ADMIN y CONTADOR
     */
    @Transactional(readOnly = true)
    public List<ClienteResponse> listar(TipoCliente tipo) {
        List<Cliente> clientes = tipo != null
                ? clienteRepo.findByTipoOrderByApellidosAscNombresAsc(tipo)
                : clienteRepo.findAll();
        return mapper.toClienteResponseList(clientes);
    }

    @Transactional
    public ClienteResponse crear(CrearClienteRequest req) {
        if (clienteRepo.existsByDni(req.dni())) {
            throw new IllegalArgumentException("Ya existe un cliente con DNI: " + req.dni());
        }
        if (req.ruc() != null && clienteRepo.existsByRuc(req.ruc())) {
            throw new IllegalArgumentException("Ya existe un cliente con RUC: " + req.ruc());
        }
        Cliente c = Cliente.builder()
                .dni(req.dni())
                .nombres(req.nombres())
                .apellidos(req.apellidos())
                .telefono(req.telefono())
                .tipo(req.tipo() != null ? req.tipo() : TipoCliente.GENERAL)
                .razonSocial(req.razonSocial())
                .ruc(req.ruc())
                .build();
        return mapper.toClienteResponse(clienteRepo.save(c));
    }
}
