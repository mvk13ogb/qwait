package se.kth.csc.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import se.kth.csc.Application;

import javax.inject.Singleton;

import static org.springframework.context.annotation.ComponentScan.Filter;

/**
 * Configures the whole application; scans for components in the entire package tree and sets up DI accordingly.
 * <p/>
 * This does NOT include set-up of web application components such as controllers.
 */
@Configuration
@ComponentScan(basePackageClasses = Application.class, excludeFilters = @Filter(Controller.class))
@PropertySource("classpath:/settings.properties")
class ApplicationConfig {
    private static final Logger log = LoggerFactory.getLogger(ApplicationConfig.class);

    /**
     * The provider for placeholders that lets us use {@link org.springframework.beans.factory.annotation.Value}
     * annotations elsewhere in the application.
     */
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer(Environment environment) {
        PropertySourcesPlaceholderConfigurer ppc = new PropertySourcesPlaceholderConfigurer();

        ppc.setEnvironment(environment);

        log.info("Creating placeholder configurer");
        return ppc;
    }

    /**
     * Provider for Jackson serialization
     */
    @Bean(name = "objectMapper")
    public ObjectMapper objectMapper() {
        ObjectMapper result = new ObjectMapper();
        result.registerModule(new JodaModule());

        log.info("Creating object mapper with Joda support");
        return result;
    }
}
