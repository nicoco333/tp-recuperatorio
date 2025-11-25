package com.tpi.backend.msflota;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Test inicial del microservicio.
 * Verifica que el contexto de Spring Boot cargue correctamente.
 * Si este test pasa, significa que el proyecto está bien configurado.
 */
@SpringBootTest
class MsFlotaApplicationTests {

    @Test
    void contextLoads() {
        // No hace falta código. Si el contexto de Spring se levanta sin errores,
        // este test pasa exitosamente ✅
    }
}
