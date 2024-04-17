package pe.gob.reniec.rrcc.plataformaelectronica.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

@Configuration
@Profile("local")
public class DataSourceLocalConfig {

    @Bean(name = "dataSourceOrigen")
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource dataSourceOrigen() {
        return DataSourceBuilder.create().build();
    }
    @Bean(name = "dataSourceDestino")
    @ConfigurationProperties(prefix = "spring.datasource2")
    public DataSource dataSourceDestino() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public JdbcTemplate jdbcTemplate(@Qualifier("dataSourceDestino")DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    DataSourceTransactionManager dataSourceTransactionManager(@Qualifier("dataSourceDestino")DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
