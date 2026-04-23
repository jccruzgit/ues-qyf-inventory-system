package sv.edu.ues.qyf.inventory.config;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.StringUtils;

public class DatabaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String PROPERTY_SOURCE_NAME = "normalizedDatabaseUrl";
    private static final String JDBC_POSTGRESQL_PREFIX = "jdbc:postgresql://";
    private static final String POSTGRESQL_PREFIX = "postgresql://";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String dbUrl = environment.getProperty("DB_URL");
        if (!StringUtils.hasText(dbUrl)) {
            return;
        }

        NormalizedDatabaseUrl normalizedDatabaseUrl = normalize(dbUrl.trim());
        if (normalizedDatabaseUrl == null) {
            return;
        }

        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("spring.datasource.url", normalizedDatabaseUrl.jdbcUrl());

        if (!StringUtils.hasText(environment.getProperty("DB_USERNAME"))
                && StringUtils.hasText(normalizedDatabaseUrl.username())) {
            properties.put("spring.datasource.username", normalizedDatabaseUrl.username());
        }

        if (!StringUtils.hasText(environment.getProperty("DB_PASSWORD"))
                && StringUtils.hasText(normalizedDatabaseUrl.password())) {
            properties.put("spring.datasource.password", normalizedDatabaseUrl.password());
        }

        environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, properties));
    }

    private NormalizedDatabaseUrl normalize(String dbUrl) {
        String uriValue = dbUrl;
        if (dbUrl.startsWith(JDBC_POSTGRESQL_PREFIX)) {
            uriValue = dbUrl.substring("jdbc:".length());
        }

        if (!uriValue.startsWith(POSTGRESQL_PREFIX)) {
            return dbUrl.startsWith(JDBC_POSTGRESQL_PREFIX)
                    ? new NormalizedDatabaseUrl(dbUrl, null, null)
                    : null;
        }

        URI uri = URI.create(uriValue);
        if (!StringUtils.hasText(uri.getHost())) {
            return null;
        }

        StringBuilder jdbcUrl = new StringBuilder(JDBC_POSTGRESQL_PREFIX)
                .append(uri.getHost());

        if (uri.getPort() > 0) {
            jdbcUrl.append(':').append(uri.getPort());
        }

        if (StringUtils.hasText(uri.getRawPath())) {
            jdbcUrl.append(uri.getRawPath());
        }

        if (StringUtils.hasText(uri.getRawQuery())) {
            jdbcUrl.append('?').append(uri.getRawQuery());
        }

        Credentials credentials = extractCredentials(uri.getRawUserInfo());
        return new NormalizedDatabaseUrl(jdbcUrl.toString(), credentials.username(), credentials.password());
    }

    private Credentials extractCredentials(String rawUserInfo) {
        if (!StringUtils.hasText(rawUserInfo)) {
            return new Credentials(null, null);
        }

        String[] parts = rawUserInfo.split(":", 2);
        String username = decode(parts[0]);
        String password = parts.length > 1 ? decode(parts[1]) : null;
        return new Credentials(username, password);
    }

    private String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private record NormalizedDatabaseUrl(String jdbcUrl, String username, String password) {
    }

    private record Credentials(String username, String password) {
    }
}
