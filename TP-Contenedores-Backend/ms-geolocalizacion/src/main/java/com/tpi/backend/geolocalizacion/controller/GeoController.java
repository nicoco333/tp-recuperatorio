package com.tpi.backend.geolocalizacion.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.tpi.backend.geolocalizacion.dto.distanciageo.DistanciaDTO;
import com.tpi.backend.geolocalizacion.service.GeoService;
import com.tpi.backend.geolocalizacion.service.RutaService;
import entities.Ruta;
import entities.Tramo;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/geo")
@RequiredArgsConstructor
public class GeoController {

    private final GeoService servicioGeo;
    private final RutaService rutaService;

    /**
     * Endpoint existente: cálculo directo con parámetros inicio y fin (formato "lon,lat")
     */
    @GetMapping("/calculo-distancia")
    public DistanciaDTO calcular(@RequestParam("inicio") String inicio, @RequestParam("fin") String fin) {
        try {
            return servicioGeo.calcularDistancia(inicio, fin);
        } catch (Exception ex) {
            throw new RuntimeException("Fallo en cálculo de coordenadas: " + ex.getMessage(), ex);
        }
    }

    /**
     * Nuevo endpoint: calcula distancia usando las coordenadas guardadas en la base de datos
     * para la primera tramo asociado a la solicitud indicada por nro_solicitud (snake_case).
     *
     * Uso:
     *  GET /api/geo/calculo-distancia/por-solicitud?nro_solicitud=1
     */
    @GetMapping("/calculo-distancia/por-solicitud")
    public DistanciaDTO calcularPorSolicitud(@RequestParam("nro_solicitud") Integer nroSolicitud) {
        if (nroSolicitud == null) {
            throw new IllegalArgumentException("El parámetro nro_solicitud es obligatorio");
        }

        // 1) Buscar ruta por nro_solicitud recorriendo las rutas disponibles.
        //    Como la entidad Ruta parece tener una relación a Solicitud, comprobamos eso.
        List<Ruta> rutas = rutaService.listarRutas(null);
        Optional<Ruta> optRuta = rutas.stream()
                .filter(r -> r.getSolicitud() != null
                        && r.getSolicitud().getNroSolicitud() != null
                        && r.getSolicitud().getNroSolicitud().equals(nroSolicitud))
                .findFirst();

        if (optRuta.isEmpty()) {
            throw new IllegalArgumentException("No se encontró ruta asociada a la solicitud " + nroSolicitud);
        }

        Ruta ruta = optRuta.get();

        // 2) Obtener tramos de esa ruta (orden ascendente) y tomar el primer tramo
        List<Tramo> tramos = rutaService.listarTramosPorRuta(ruta.getIdRuta());
        if (tramos == null || tramos.isEmpty()) {
            throw new IllegalArgumentException("No se encontraron tramos para la ruta asociada a la solicitud " + nroSolicitud);
        }

        Tramo primerTramo = tramos.stream()
                .sorted((t1, t2) -> Integer.compare(
                        t1.getOrden() == null ? Integer.MAX_VALUE : t1.getOrden(),
                        t2.getOrden() == null ? Integer.MAX_VALUE : t2.getOrden()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No hay tramos válidos para la ruta"));

        // 3) Reutilizar el método que ya calcula distancia por id de tramo
        return rutaService.obtenerDistanciaDeTramo(primerTramo.getIdTramo());
    }
}