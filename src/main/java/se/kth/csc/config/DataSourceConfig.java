package se.kth.csc.config;

import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Provider of a JDBC database source.
 */
@Configuration
public interface DataSourceConfig {
    /**
     * Creates the data source that this configuration provides.
     */
    DataSource dataSource();
}
