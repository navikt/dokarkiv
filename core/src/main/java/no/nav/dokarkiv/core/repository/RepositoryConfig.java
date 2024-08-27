package no.nav.dokarkiv.core.repository;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.properties.DataSourceAdditionalProperties;
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
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@EntityScan(basePackages = {
		"no.nav.dokarkiv.core.domain.entities",
		"no.nav.dokarkiv.core.domain.codes"
})
@EnableJpaRepositories
@EnableTransactionManagement
@EnableConfigurationProperties({
		DataSourceProperties.class,
		DataSourceAdditionalProperties.class
})
@Configuration
@Import({
		JournalpostRepositorySkjermet.class,
		DokumentFilSkjermetRepository.class
})
public class RepositoryConfig {
	@Bean
	@Primary
	DataSource dataSource(final DataSourceProperties dataSourceProperties,
						  final DataSourceAdditionalProperties dataSourceAdditionalProperties,
						  final DokarkivProperties dokarkivProperties) throws SQLException {
		PoolDataSource poolDataSource = PoolDataSourceFactory.getPoolDataSource();
		poolDataSource.setURL(dataSourceProperties.getUrl());
		poolDataSource.setUser(dataSourceProperties.getUsername());
		poolDataSource.setPassword(dataSourceProperties.getPassword());
		poolDataSource.setConnectionFactoryClassName(OracleDataSource.class.getName());
		poolDataSource.registerConnectionInitializationCallback(connection -> connection.setSchema("joark"));
		poolDataSource.setMaxConnectionReuseTime(MINUTES.toSeconds(5));
		// Behøver ikke sette setSQLForValidateConnection pga UCP gjør intern ping mot Oracle
		poolDataSource.setValidateConnectionOnBorrow(true);
		poolDataSource.setSecondsToTrustIdleConnection((int) MINUTES.toSeconds(3));

		if (isOracleFastConnectionFailoverSupported(dataSourceProperties.getUrl(), dataSourceAdditionalProperties.onshosts())) {
			poolDataSource.setFastConnectionFailoverEnabled(true);
			String onsConfiguration = "nodes=" + dataSourceAdditionalProperties.onshosts();
			poolDataSource.setONSConfiguration(onsConfiguration);
			log.info("RepositoryConfig - Skrur på FCF/FAN. onsConfiguration={}", onsConfiguration);
		} else {
			// Har ikke fått system property -Doracle.jdbc.fanEnabled=false til å fungere med programmatisk oppsett av Oracle UCP.
			// Derfor er denne else blokken her
			poolDataSource.setFastConnectionFailoverEnabled(false);
			poolDataSource.setONSConfiguration("");
			log.info("RepositoryConfig - FCF/FAN er skrudd av");
		}

		Properties properties = new Properties();
		properties.setProperty(SQLnetDef.TCP_CONNTIMEOUT_STR, "3000");
		properties.setProperty("oracle.jdbc.thinForceDNSLoadBalancing", "true");
		properties.setProperty("oracle.jdbc.implicitStatementCacheSize", "200");
		// Statisk poolsize. Se DokarkivProperties.java
		int poolsize = dokarkivProperties.getDatabase().getPoolsize();
		log.info("Setter opp Oracle UCP med statisk poolsize={}", poolsize);
		poolDataSource.setInitialPoolSize(poolsize);
		poolDataSource.setMinPoolSize(poolsize);
		poolDataSource.setMaxPoolSize(poolsize);
		poolDataSource.setConnectionProperties(properties);
		return poolDataSource;
	}

	@Bean
	@Primary
	NamedParameterJdbcTemplate namedParameterJdbcTemplate(final DataSource dataSource) {
		return new NamedParameterJdbcTemplate(dataSource);
	}

	private static boolean isOracleFastConnectionFailoverSupported(String jdbcurl, String onshosts) {
		return jdbcurl.toLowerCase().contains("failover") && isNotBlank(onshosts);
	}
}


