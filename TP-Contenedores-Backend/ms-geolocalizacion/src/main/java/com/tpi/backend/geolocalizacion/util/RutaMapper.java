package com.tpi.backend.geolocalizacion.util;

import com.tpi.backend.msrutas.dto.RutaDTO;
import com.tpi.backend.msrutas.dto.TramoDTO;
import com.tpi.backend.msrutas.dto.DepositoDTO;
import entities.Ruta;
import entities.Tramo;
import entities.Deposito;
import entities.Geolocalizacion;
import entities.TipoTramo;
import entities.Estado;
import entities.Camion;
import entities.Solicitud;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class RutaMapper {

    // ------- RUTA -------
    public RutaDTO toRutaDTO(Ruta e) {
        RutaDTO dto = new RutaDTO();
        dto.setIdRuta(e.getIdRuta());
        dto.setNroSolicitud(e.getSolicitud() != null ? e.getSolicitud().getNroSolicitud() : null);
        dto.setCantTramos(e.getCantidadTramos());
        dto.setCantDepositos(e.getCantidadDepositos());

        if (e.getTramos() != null) {
            dto.setTramos(e.getTramos()
                    .stream()
                    .map(this::toTramoDTO)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    public Ruta toRutaEntity(RutaDTO dto) {
        Ruta e = new Ruta();
        e.setIdRuta(dto.getIdRuta());
        if (dto.getNroSolicitud() != null) {
            Solicitud s = new Solicitud();
            s.setNroSolicitud(dto.getNroSolicitud());
            e.setSolicitud(s);
        }
        e.setCantidadTramos(dto.getCantTramos());
        e.setCantidadDepositos(dto.getCantDepositos());
        return e;
    }

    // ------- TRAMO -------
    public TramoDTO toTramoDTO(Tramo e) {
        TramoDTO dto = new TramoDTO();
        dto.setIdTramo(e.getIdTramo());
        dto.setIdRuta(e.getRuta() != null ? e.getRuta().getIdRuta() : null);
        dto.setOrigenGeo(e.getOrigenGeo() != null ? e.getOrigenGeo().getIdGeo() : null);
        dto.setDestinoGeo(e.getDestinoGeo() != null ? e.getDestinoGeo().getIdGeo() : null);
        dto.setOrigenDepositoId(e.getOrigenDeposito() != null ? (e.getOrigenDeposito().getIdDeposito() != null ? e.getOrigenDeposito().getIdDeposito().intValue() : null) : null);
        dto.setDestinoDepositoId(e.getDestinoDeposito() != null ? (e.getDestinoDeposito().getIdDeposito() != null ? e.getDestinoDeposito().getIdDeposito().intValue() : null) : null);
        dto.setTipoTramo(e.getTipoTramo() != null ? e.getTipoTramo().getIdTipoTramo() : null);
        dto.setIdEstado(e.getEstado() != null ? e.getEstado().getIdEstado() : null);
        dto.setOrden(e.getOrden());
        dto.setFechaHoraInicioEstimada(e.getFechaHoraInicioEstimada());
        dto.setFechaHoraFinEstimada(e.getFechaHoraFinEstimada());
        dto.setFechaHoraInicioReal(e.getFechaHoraInicioReal());
        dto.setFechaHoraFinReal(e.getFechaHoraFinReal());
        dto.setCostoAproximado(e.getCostoAproximado());
        dto.setCostoReal(e.getCostoReal());
    dto.setDominioCamion(e.getCamion() != null ? e.getCamion().getDominioCamion() : null);
        return dto;
    }

    public Tramo toTramoEntity(TramoDTO dto) {
        Tramo e = new Tramo();
        e.setIdTramo(dto.getIdTramo());
        if (dto.getIdRuta() != null) {
            Ruta r = new Ruta();
            r.setIdRuta(dto.getIdRuta());
            e.setRuta(r);
        }
        if (dto.getOrigenGeo() != null) {
            Geolocalizacion g = new Geolocalizacion();
            g.setIdGeo(dto.getOrigenGeo());
            e.setOrigenGeo(g);
        }
        if (dto.getDestinoGeo() != null) {
            Geolocalizacion g2 = new Geolocalizacion();
            g2.setIdGeo(dto.getDestinoGeo());
            e.setDestinoGeo(g2);
        }
        if (dto.getOrigenDepositoId() != null) {
            Deposito d = new Deposito();
            d.setIdDeposito(dto.getOrigenDepositoId().longValue());
            e.setOrigenDeposito(d);
        }
        if (dto.getDestinoDepositoId() != null) {
            Deposito d2 = new Deposito();
            d2.setIdDeposito(dto.getDestinoDepositoId().longValue());
            e.setDestinoDeposito(d2);
        }
        if (dto.getTipoTramo() != null) {
            TipoTramo tt = new TipoTramo();
            tt.setIdTipoTramo(dto.getTipoTramo());
            e.setTipoTramo(tt);
        }
        if (dto.getIdEstado() != null) {
            Estado est = new Estado();
            est.setIdEstado(dto.getIdEstado());
            e.setEstado(est);
        }
        e.setOrden(dto.getOrden());
        e.setFechaHoraInicioEstimada(dto.getFechaHoraInicioEstimada());
        e.setFechaHoraFinEstimada(dto.getFechaHoraFinEstimada());
        e.setFechaHoraInicioReal(dto.getFechaHoraInicioReal());
        e.setFechaHoraFinReal(dto.getFechaHoraFinReal());
        e.setCostoAproximado(dto.getCostoAproximado());
        e.setCostoReal(dto.getCostoReal());
        if (dto.getDominioCamion() != null) {
            Camion c = new Camion();
            c.setDominioCamion(dto.getDominioCamion());
            e.setCamion(c);
        }
        return e;
    }

    // ------- DEPOSITO -------
    public DepositoDTO toDepositoDTO(Deposito e) {
        DepositoDTO dto = new DepositoDTO();
    dto.setIdDeposito(e.getIdDeposito() != null ? e.getIdDeposito().intValue() : null);
        dto.setNombre(e.getNombre());
        dto.setDireccion(e.getGeolocalizacion() != null ? e.getGeolocalizacion().getDireccion() : null);
        dto.setLatitud(e.getGeolocalizacion() != null ? e.getGeolocalizacion().getLatitud() : null);
        dto.setLongitud(e.getGeolocalizacion() != null ? e.getGeolocalizacion().getLongitud() : null);
        return dto;
    }

    public Deposito toDepositoEntity(DepositoDTO dto) {
        Deposito e = new Deposito();
        if (dto.getIdDeposito() != null) e.setIdDeposito(dto.getIdDeposito().longValue());
        e.setNombre(dto.getNombre());
        e.setCostoEstadiaDiaria(dto.getCostoEstadiaDiaria());
        if (dto.getDireccion() != null || dto.getLatitud() != null || dto.getLongitud() != null) {
            Geolocalizacion g = new Geolocalizacion();
            g.setDireccion(dto.getDireccion());
            g.setLatitud(dto.getLatitud());
            g.setLongitud(dto.getLongitud());
            e.setGeolocalizacion(g);
        }
        return e;
    }
}
