package es.medoncalls.guardias.support;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * MySQL 5.7 con el esquema legacy sin datos (`legacy-schema.sql`, T004). Las migraciones de
 * Liquibase se aplican al arrancar el contexto de Spring. Un único contenedor para todas las
 * clases de prueba de la JVM.
 */
public final class MySqlLegacyContainer {

    private static final MySQLContainer<?> MYSQL = new MySQLContainer<>(DockerImageName.parse("mysql:5.7"))
            .withDatabaseName("guardias")
            .withInitScript("legacy-schema.sql")
            .withCommand("--character-set-server=utf8", "--collation-server=utf8_general_ci");

    static {
        MYSQL.start();
    }

    private MySqlLegacyContainer() {
    }

    public static void registrar(DynamicPropertyRegistry registro) {
        registro.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registro.add("spring.datasource.username", MYSQL::getUsername);
        registro.add("spring.datasource.password", MYSQL::getPassword);
    }
}
