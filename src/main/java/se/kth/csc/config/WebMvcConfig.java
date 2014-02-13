package se.kth.csc.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.thymeleaf.extras.springsecurity3.dialect.SpringSecurityDialect;
import org.thymeleaf.messageresolver.StandardMessageResolver;
import org.thymeleaf.spring3.SpringTemplateEngine;
import org.thymeleaf.spring3.messageresolver.SpringMessageResolver;
import org.thymeleaf.spring3.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;
import org.thymeleaf.templateresolver.TemplateResolver;
import se.kth.csc.Application;

import static org.springframework.context.annotation.ComponentScan.Filter;

@Configuration
@ComponentScan(basePackageClasses = Application.class, includeFilters = @Filter(Controller.class))
class WebMvcConfig extends WebMvcConfigurationSupport {
    private static final Logger log = LoggerFactory.getLogger(WebMvcConfig.class);

    /**
     * Resource path for where to store localized messages for Thymeleaf.
     */
    public static final String MESSAGE_SOURCE = "/WEB-INF/i18n/messages";

    /**
     * The location of views in general.
     */
    public static final String VIEWS = "/WEB-INF/views/";

    /**
     * The location of static resources that should be public.
     */
    public static final String RESOURCES_HANDLER = "/resources/";

    /**
     * The Servlet pattern matching static resources.
     */
    public static final String RESOURCES_LOCATION = RESOURCES_HANDLER + "**";

    @Override
    public RequestMappingHandlerMapping requestMappingHandlerMapping() {
        RequestMappingHandlerMapping requestMappingHandlerMapping = super.requestMappingHandlerMapping();

        // Only do exact handler dispatch, so /homerun does not get handled by /home
        requestMappingHandlerMapping.setUseSuffixPatternMatch(false);

        // Make sure that /home/ is different from /home
        requestMappingHandlerMapping.setUseTrailingSlashMatch(false);

        return requestMappingHandlerMapping;
    }

    @Bean(name = "messageSource")
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();

        // Configure the location of messages
        messageSource.setBasename(MESSAGE_SOURCE);

        // Cache messages for a while, but make sure that they can be edited at run-time
        messageSource.setCacheSeconds(5);

        log.info("Creating message source reading from {} that caches for 5 seconds", MESSAGE_SOURCE);
        return messageSource;
    }

    @Bean
    public TemplateResolver thymeleafTemplateResolver() {
        // Use Thymeleaf for templating
        TemplateResolver templateResolver = new ServletContextTemplateResolver();

        // Configure the location of templates
        templateResolver.setPrefix(VIEWS);

        // The templates should follow Thymeleaf convention and be fully compliant HTML files on their own
        templateResolver.setSuffix(".html");

        // Modern web standards!
        templateResolver.setTemplateMode("HTML5");

        // Do not cache templates
        // TODO: change this when going into production
        templateResolver.setCacheable(false);

        log.info("Creating Thymeleaf template resolver in HTML5 mode");
        return templateResolver;
    }

    @Autowired
    @Bean
    public SpringTemplateEngine templateEngine(
            TemplateResolver templateResolver,
            @Value("${product.name}") String productName,
            @Value("${product.version}") String productVersion) {
        // Use default Thymeleaf Spring 3 template engine
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();

        // Use our configured template resolver
        templateEngine.setTemplateResolver(templateResolver);

        StandardMessageResolver messageResolver = new StandardMessageResolver();
        messageResolver.addDefaultMessage("product.name", productName);
        messageResolver.addDefaultMessage("product.version", productVersion);
        templateEngine.addMessageResolver(messageResolver);

        // Add support for security stuff
        templateEngine.addDialect(new SpringSecurityDialect());

        log.info("Creating Thymeleaf template engine with security support");
        return templateEngine;
    }

    @Bean
    public ThymeleafViewResolver viewResolver(SpringTemplateEngine springTemplateEngine) {
        // Use default Thymeleaf view resolver
        ThymeleafViewResolver thymeleafViewResolver = new ThymeleafViewResolver();

        // Hook it up with our template engine
        thymeleafViewResolver.setTemplateEngine(springTemplateEngine);

        // Override platform encoding
        thymeleafViewResolver.setCharacterEncoding("UTF-8");

        log.info("Creating Thymeleaf view resolver");
        return thymeleafViewResolver;
    }

    @Override
    public Validator getValidator() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();

        // Use our message source when validating beans
        validator.setValidationMessageSource(messageSource());

        log.info("Setting up validation factory using a custom message source");
        return validator;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Set up static resources
        registry.addResourceHandler(RESOURCES_HANDLER).addResourceLocations(RESOURCES_LOCATION);
    }

    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }
}
