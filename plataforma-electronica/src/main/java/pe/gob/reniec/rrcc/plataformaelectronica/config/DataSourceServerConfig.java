package pe.gob.reniec.rrcc.plataformaelectronica.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;

import javax.sql.DataSource;

@Configuration
@Profile("!local")
public class DataSourceServerConfig {
  @Value("${spring.datasource.jndi-name-origen}")
  private String jndiNameOrigen;

  @Value("${spring.datasource.jndi-name-destino}")
  private String jndiNameDestino;

  @Bean(name = "dataSourceOrigen")
  public DataSource dataSourceOrigen() {
    JndiDataSourceLookup lookup = new JndiDataSourceLookup();
    return lookup.getDataSource(jndiNameOrigen);
  }

  @Bean(name = "dataSourceDestino")
  public DataSource dataSourceDestino() {
    JndiDataSourceLookup lookup = new JndiDataSourceLookup();
    return lookup.getDataSource(jndiNameDestino);
  }

  @Bean
  public JdbcTemplate jdbcTemplate(@Qualifier("dataSourceDestino") DataSource dataSourceDestino) {
    return new JdbcTemplate(dataSourceDestino);
  }

  @Bean
  public DataSourceTransactionManager transactionManager(@Qualifier("dataSourceDestino") DataSource dataSourceDestino) {
    return new DataSourceTransactionManager(dataSourceDestino);
  }

}
