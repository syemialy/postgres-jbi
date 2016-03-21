package com.jetbi.postgresftsapp.spring.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Configuration class to initialize MVC part of the application
 * <p/>
 * @author Sergei.Emelianov on 19.03.2016.
 */
@Configuration
@ComponentScan(basePackages = {"com.jetbi.postgresftsapp.spring.stereotype"})
@PropertySource("classpath:application.properties")
@EnableWebMvc
public class PostgresFtsSpringMVCConfig extends WebMvcConfigurerAdapter {

    @Autowired
    private Environment env;

    /**
     * Adds CORS support
     * <p/>
     * Note: by default all origins are allowed, there was no specific requirements addressing CORS
     *
     * @param registry
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {

        registry.addMapping("/**");
    }

    /**
     * Initializing  datasource using application properties
     *
     * @return
     */
    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(env.getProperty("database.url"));
        dataSource.setUsername(env.getProperty("database.user"));
        dataSource.setPassword(env.getProperty("database.pwd"));
        Properties props = new Properties();
        props.setProperty("ssl", "true");
        props.setProperty("sslfactory", "org.postgresql.ssl.NonValidatingFactory");
        dataSource.setConnectionProperties(props);
        return dataSource;
    }

    /**
     * Initialises transaction manager for the datasource in context
     *
     * @return
     */
    @Bean
    public DataSourceTransactionManager txManager() {
        DataSourceTransactionManager txManager = new DataSourceTransactionManager();
        txManager.setDataSource(dataSource());
        return txManager;
    }

    /**
     * Initializes instance of JDBCTemplate, which is thread-safe by default and may be referenced across
     * different beans
     *
     * @return
     */
    @Bean
    public JdbcTemplate jdbcTemplate() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource());
        return jdbcTemplate;
    }

    /**
     * Gson serialization, this is where all custom type serializers may be used. The gson
     * is used within the application to aid JSON transformation. The application has no
     * strict data model and all the REST service requests and response may be changed any time
     * without affecting too many classes.
     *
     * @return
     */
    @Bean
    public Gson gson() {
        return new GsonBuilder().create();
    }

}
