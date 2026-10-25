package es.medoncalls.guardias.support;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
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

    /**
     * Base nueva en el mismo contenedor, solo con el esquema legacy y sin migraciones: para
     * comparar el legacy antes y después de aplicar Liquibase (SC-006).
     */
    public static DataSource baseSinMigrar(String nombre) throws SQLException {
        String servidor = "jdbc:mysql://" + MYSQL.getHost() + ":" + MYSQL.getMappedPort(MySQLContainer.MYSQL_PORT) + "/";
        try (Connection conexion = DriverManager.getConnection(servidor, "root", MYSQL.getPassword());
                Statement sql = conexion.createStatement()) {
            sql.execute("DROP DATABASE IF EXISTS " + nombre);
            sql.execute("CREATE DATABASE " + nombre + " CHARACTER SET utf8");
        }
        DataSource base = new DriverManagerDataSource(
                servidor + nombre + "?useSSL=false&allowPublicKeyRetrieval=true", "root", MYSQL.getPassword());
        new ResourceDatabasePopulator(new ClassPathResource("legacy-schema.sql")).execute(base);
        return base;
    }

    public static void registrar(DynamicPropertyRegistry registro) {
        registro.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registro.add("spring.datasource.username", MYSQL::getUsername);
        registro.add("spring.datasource.password", MYSQL::getPassword);
    }
}
