package no.nav.dokarkiv.core.repository;

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

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@EntityScan(basePackages = {
		"no.nav.dokarkiv.core.domain.entities",
		"no.nav.dokarkiv.core.domain.codes"
})
@EnableJpaRepositories(basePackageClasses = {
		JoarkRepository.class,
		DokumentFilRepository.class,
		DokumentinfoRepository.class,
		JournalpostDokumentInfoRelasjonRepository.class,
		BidragMellomlagringRepository.class,
		BidragMellomlagringDokumentRepository.class,
		JoarkDeleteRepository.class, AksjonsLoggRepository.class,
		SakRepository.class
})
@EnableTransactionManagement
@EnableConfigurationProperties(DataSourceProperties.class)
@Configuration
@Import(value = {JoarkRepositorySkjermet.class, DokumentFilSkjermetRepository.class})
public class RepositoryConfig {
	@Bean
	@Primary
	DataSource dataSource(final DataSourceProperties dataSourceProperties) throws SQLException {
		PoolDataSource poolDataSource = PoolDataSourceFactory.getPoolDataSource();
		poolDataSource.setURL(dataSourceProperties.getUrl());
		poolDataSource.setUser(dataSourceProperties.getUsername());
		poolDataSource.setPassword(dataSourceProperties.getPassword());
		poolDataSource.setConnectionFactoryClassName(dataSourceProperties.getDriverClassName());
		poolDataSource.registerConnectionInitializationCallback(connection -> connection.setSchema("joark"));

		Properties connProperties = new Properties();
		connProperties.setProperty(SQLnetDef.TCP_CONNTIMEOUT_STR, "3000");
		connProperties.setProperty("oracle.jdbc.thinForceDNSLoadBalancing", "true");
		// Optimizing UCP behaviour https://docs.oracle.com/database/121/JJUCP/optimize.htm#JJUCP8143
		// About Optimizing Real-World Performance with Static Connection Pools
		// https://docs.oracle.com/en/database/oracle/oracle-database/19/jjucp/optimizing-real-world-performance.html
		// select STAT_NAME, to_char(VALUE) as VALUE, COMMENTS from v$osstat where stat_name IN ('NUM_CPUS','NUM_CPU_CORES','NUM_CPU_SOCKETS');
		// NUM_CPU i Joark produksjon er 96.
		// Anbefalt av Oracle: 1-10 koblinger / CPU.
		// Max connections: 960
		//
		// Joark. https://github.com/navikt/joark/blob/master/layers/config/joark-appconfig/src/main/resources/app-config.xml
		// Joark reservert koblinger: 50 + 50 (XADS) = 100
		//
		// Sak. https://github.com/navikt/sak/blob/master/src/main/java/no/nav/sak/SakApplication.java#L226
		// Sak reservert koblinger: 12 pods * 10 (default maximumPoolSize i HikariCP) = 120
		//
		// Rest koblinger: 960 (max) - 100 (Joark) - 120 (Sak) = 740
		// Dokarkiv statisk pool (denne appen) er max 740 koblinger.
		// dokarkiv pods: 12 (naiserator.yaml)
		// dokarkiv koblinger / pod = 740 / 12 = ~61. La oss si 60.
		poolDataSource.setInitialPoolSize(60);
		poolDataSource.setMinPoolSize(60);
		poolDataSource.setMaxPoolSize(60);
		poolDataSource.setMaxConnectionReuseTime(300); // 5min
		poolDataSource.setMaxConnectionReuseCount(1000);
		poolDataSource.setConnectionProperties(connProperties);
		return poolDataSource;
	}

	@Bean
	@Primary
	NamedParameterJdbcTemplate namedParameterJdbcTemplate(final DataSource dataSource) {
		return new NamedParameterJdbcTemplate(dataSource);
	}
}


