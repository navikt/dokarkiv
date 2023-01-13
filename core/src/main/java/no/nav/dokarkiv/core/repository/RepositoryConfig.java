package no.nav.dokarkiv.core.repository;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.properties.DokarkivProperties;
import oracle.jdbc.pool.OracleDataSource;
import oracle.net.ns.SQLnetDef;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Properties;

import static java.util.concurrent.TimeUnit.MINUTES;

@Slf4j
@EntityScan(basePackages = {
		"no.nav.dokarkiv.core.domain.entities",
		"no.nav.dokarkiv.core.domain.codes"
})
@EnableJpaRepositories
@EnableTransactionManagement
@EnableConfigurationProperties(DataSourceProperties.class)
@Configuration
@Import(value = {JournalpostRepositorySkjermet.class, DokumentFilSkjermetRepository.class})
public class RepositoryConfig {
	@Bean
	@Primary
	DataSource dataSource(final DataSourceProperties dataSourceProperties,
						  final DokarkivProperties dokarkivProperties) throws SQLException {
		PoolDataSource poolDataSource = PoolDataSourceFactory.getPoolDataSource();
		poolDataSource.setURL(dataSourceProperties.getUrl());
		poolDataSource.setUser(dataSourceProperties.getUsername());
		poolDataSource.setPassword(dataSourceProperties.getPassword());
		poolDataSource.setConnectionFactoryClassName(OracleDataSource.class.getName());
		poolDataSource.registerConnectionInitializationCallback(connection -> connection.setSchema("joark"));
		poolDataSource.setMaxConnectionReuseTime(MINUTES.toSeconds(30));
		// Behøver ikke sette setSQLForValidateConnection pga UCP gjør intern ping mot Oracle
		poolDataSource.setValidateConnectionOnBorrow(true);
		poolDataSource.setSecondsToTrustIdleConnection((int) MINUTES.toSeconds(3));

		Properties connProperties = new Properties();
		connProperties.setProperty(SQLnetDef.TCP_CONNTIMEOUT_STR, "3000");
		connProperties.setProperty("oracle.jdbc.thinForceDNSLoadBalancing", "true");
		// Statisk poolsize. Se DokarkivProperties.java
		int poolsize = dokarkivProperties.getDatabase().getPoolsize();
		log.info("Setter opp Oracle UCP med statisk poolsize={}", poolsize);
		poolDataSource.setInitialPoolSize(poolsize);
		poolDataSource.setMinPoolSize(poolsize);
		poolDataSource.setMaxPoolSize(poolsize);
		poolDataSource.setConnectionProperties(connProperties);
		return poolDataSource;
	}

	@Bean
	@Primary
	NamedParameterJdbcTemplate namedParameterJdbcTemplate(final DataSource dataSource) {
		return new NamedParameterJdbcTemplate(dataSource);
	}
}


