package no.nav.dokarkiv.core.properties;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Konfigurasjon for dokarkiv
 */
@Data
@ConfigurationProperties("dokarkiv")
@Validated
public class DokarkivProperties {

	private final Database database = new Database();
	private final Endpoints endpoints = new Endpoints();

	@Data
	@Validated
	public static class Database {
		/**
		 * Statisk pool verdi for dokarkiv databasen.
		 * <p>
		 * Optimizing UCP behaviour https://docs.oracle.com/database/121/JJUCP/optimize.htm#JJUCP8143
		 * About Optimizing Real-World Performance with Static Connection Pools
		 * https://docs.oracle.com/en/database/oracle/oracle-database/19/jjucp/optimizing-real-world-performance.html
		 * select STAT_NAME, to_char(VALUE) as VALUE, COMMENTS from v$osstat where stat_name IN ('NUM_CPUS','NUM_CPU_CORES','NUM_CPU_SOCKETS');
		 * NUM_CPU i Joark produksjon er 96.
		 * Anbefalt av Oracle: 1-10 koblinger / CPU.
		 * Max connections: 960
		 * //
		 * Joark. https://github.com/navikt/joark/blob/master/layers/config/joark-appconfig/src/main/resources/app-config.xml
		 * Joark reservert koblinger: 50 + 50 (XADS) = 100
		 * //
		 * Sak. https://github.com/navikt/sak/blob/master/src/main/java/no/nav/sak/SakApplication.java#L226
		 * Sak reservert koblinger: 12 pods * 10 (default maximumPoolSize i HikariCP) = 120
		 * //
		 * Rest koblinger: 960 (max) - 100 (Joark) - 120 (Sak) = 740
		 * Dokarkiv statisk pool (denne appen) er max 740 koblinger.
		 * dokarkiv pods: 12 (naiserator.yaml)
		 * dokarkiv koblinger / pod = 740 / 12 = ~61. La oss si 60. (p-config.json)
		 *
		 * @see no.nav.dokarkiv.core.repository.RepositoryConfig
		 */
		@Positive
		private int poolsize = 60;
		/**
		 * true hvis miljøet er konfigurert og har ressurser til å kjøre spørringer med Oracle PARALLEL hint
		 */
		private boolean parallelHintSupport;
	}

	@Data
	@Validated
	public static class Endpoints {
		private String overrideMsGraphServiceRoot;

		/**
		 * URL til PDL (Persondataløsningen).
		 */
		@NotNull
		private AzureEndpoint pdl;

		/**
		 * URL til saf API.
		 */
		@NotNull
		private AzureEndpoint saf;
	}

	@Data
	@Validated
	public static class AzureEndpoint {
		/**
		 * Url til tjeneste som har azure autorisasjon
		 */
		@NotEmpty
		private String url;

		/**
		 * Scope til azure client credential flow
		 */
		@NotEmpty
		private String scope;
	}
}
