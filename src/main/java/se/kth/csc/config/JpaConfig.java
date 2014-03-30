package se.kth.csc.config;

import org.hibernate.cfg.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;
import se.kth.csc.Application;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Configures JPA to run on top of our data source.
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackageClasses = Application.class)
class JpaConfig implements TransactionManagementConfigurer {
    private static final Logger log = LoggerFactory.getLogger(JpaConfig.class);

    /**
     * Creates a new entity manager factory bean using the provided settings.
     *
     * @param dataSource  The JDBC data source to use.
     * @param hbm2ddlAuto The Hibernate setting controlling how database schemas should be automatically migrated.
     * @param dialect     The SQL dialect that Hibernate should use.
     */
    @Autowired
    @Bean
    public LocalContainerEntityManagerFactoryBean configureEntityManagerFactory(
            DataSource dataSource, @Value("${hibernate.hbm2ddl.auto}") String hbm2ddlAuto, @Value("${hibernate.dialect}") String dialect) {
        LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactoryBean.setDataSource(dataSource);
        entityManagerFactoryBean.setPackagesToScan(Application.class.getPackage().getName());
        entityManagerFactoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        Properties jpaProperties = new Properties();
        jpaProperties.put(Environment.DIALECT, dialect);
        jpaProperties.put(Environment.HBM2DDL_AUTO, hbm2ddlAuto);
        entityManagerFactoryBean.setJpaProperties(jpaProperties);

        log.info("Creating entity manager factory bean with automatic \"{}\" schema management and the {} SQL dialect",
                hbm2ddlAuto, dialect);
        return entityManagerFactoryBean;
    }

    @Bean
    public PlatformTransactionManager annotationDrivenTransactionManager() {
        log.info("Creating JPA transaction manager");
        return new JpaTransactionManager();
    }
}
