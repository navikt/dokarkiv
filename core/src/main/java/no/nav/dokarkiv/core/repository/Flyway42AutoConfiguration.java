/*
 * Copyright 2012-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package no.nav.dokarkiv.core.repository;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.callback.Callback;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AbstractDependsOnBeanFactoryPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.flyway.FlywayDataSource;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.boot.autoconfigure.flyway.FlywayProperties;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.EntityManagerFactoryDependsOnPostProcessor;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DatabaseDriver;
import org.springframework.boot.jdbc.SchemaManagement;
import org.springframework.boot.jdbc.SchemaManagementProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.support.MetaDataAccessException;
import org.springframework.orm.jpa.AbstractEntityManagerFactoryBean;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static java.util.Objects.nonNull;
import static org.springframework.jdbc.support.JdbcUtils.extractDatabaseMetaData;

/**
 * Kopiert  fra spring-boot 2.0.9.RELEASE og modifisert for kompatibilitet med flyway 4.2.0.
 * <p>
 * {@link EnableAutoConfiguration Auto-configuration} for Flyway database migrations.
 *
 * @author Dave Syer
 * @author Phillip Webb
 * @author Vedran Pavic
 * @author Stephane Nicoll
 * @author Jacques-Etienne Beaudet
 * @author Eddú Meléndez
 * @author Dominic Gunn
 * @since 1.1.0
 */
@Configuration
@ConditionalOnClass(Flyway.class)
@ConditionalOnBean(DataSource.class)
@ConditionalOnProperty(prefix = "spring.flyway", name = "enabled", matchIfMissing = true)
@AutoConfigureAfter({DataSourceAutoConfiguration.class,
		JdbcTemplateAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
@Profile("nais")
public class Flyway42AutoConfiguration {

	@Bean
	@ConfigurationPropertiesBinding
	public StringOrNumberToMigrationVersionConverter stringOrNumberMigrationVersionConverter() {
		return new StringOrNumberToMigrationVersionConverter();
	}

	@Bean
	public SchemaManagementProvider flywayDefaultDdlModeProvider(
			ObjectProvider<List<Flyway>> flyways) {
		return dataSource -> SchemaManagement.MANAGED;
	}

	@Configuration
	@ConditionalOnMissingBean(Flyway.class)
	@EnableConfigurationProperties({DataSourceProperties.class, FlywayProperties.class})
	@Profile("nais")
	public static class FlywayConfiguration {

		private final FlywayProperties properties;

		private final DataSourceProperties dataSourceProperties;

		private final ResourceLoader resourceLoader;

		private final DataSource dataSource;

		private final DataSource flywayDataSource;

		private final FlywayMigrationStrategy migrationStrategy;

		private List<Callback> flywayCallbacks;

		public FlywayConfiguration(FlywayProperties properties,
								   DataSourceProperties dataSourceProperties, ResourceLoader resourceLoader,
								   ObjectProvider<DataSource> dataSource,
								   @FlywayDataSource ObjectProvider<DataSource> flywayDataSource,
								   ObjectProvider<FlywayMigrationStrategy> migrationStrategy,
								   ObjectProvider<List<Callback>> flywayCallbacks) {
			this.properties = properties;
			this.dataSourceProperties = dataSourceProperties;
			this.resourceLoader = resourceLoader;
			this.dataSource = dataSource.getIfUnique();
			this.flywayDataSource = flywayDataSource.getIfAvailable();
			this.migrationStrategy = migrationStrategy.getIfAvailable();
			this.flywayCallbacks = flywayCallbacks.getIfAvailable(Collections::emptyList);
		}

		@Bean
		@ConfigurationProperties(prefix = "spring.flyway")
		public Flyway flyway() {
			return getFluentConfiguration().load();
		}

		private FluentConfiguration getFluentConfiguration() {
			FluentConfiguration configure = Flyway.configure();
			if (nonNull(this.properties.getUrl()) || nonNull(this.properties.getUser())) {
				String url = getProperty(this.properties::getUrl,
						this.dataSourceProperties::getUrl);
				String user = getProperty(this.properties::getUser,
						this.dataSourceProperties::getUsername);
				String password = getProperty(this.properties::getPassword,
						this.dataSourceProperties::getPassword);
				configure.dataSource(url, user, password);
			} else if (this.flywayDataSource != null) {
				configure.dataSource(this.flywayDataSource);
			} else {
				configure.dataSource(this.dataSource);
			}
			if (!this.flywayCallbacks.isEmpty()) {
				configure.callbacks(this.flywayCallbacks.toArray(new Callback[0]));
			}
			String[] locations = new LocationResolver(configure.load().getConfiguration().getDataSource())
					.resolveLocations(this.properties.getLocations());
			checkLocationExists(locations);
			configure.locations(locations);
			return configure;
		}

		private String getProperty(Supplier<String> property,
								   Supplier<String> defaultValue) {
			String value = property.get();
			return (value != null) ? value : defaultValue.get();
		}

		private void checkLocationExists(String... locations) {
			Assert.state(locations.length != 0,
					"Migration script locations not configured");
			boolean exists = hasAtLeastOneLocation(locations);
			Assert.state(exists, () -> "Cannot find migrations location in: "
					+ Arrays.asList(locations)
					+ " (please add migrations or check your Flyway configuration)");
		}

		private boolean hasAtLeastOneLocation(String... locations) {
			for (String location : locations) {
				if (this.resourceLoader.getResource(normalizePrefix(location)).exists()) {
					return true;
				}
			}
			return false;
		}

		private String normalizePrefix(String location) {
			return location.replace("filesystem:", "file:");
		}

		@Bean
		@ConditionalOnMissingBean
		public FlywayMigrationInitializer flywayInitializer(Flyway flyway) {
			return new FlywayMigrationInitializer(flyway, this.migrationStrategy);
		}

		/**
		 * Additional configuration to ensure that {@link EntityManagerFactory} beans
		 * depend on the {@code flywayInitializer} bean.
		 */
		@Configuration
		@ConditionalOnClass(LocalContainerEntityManagerFactoryBean.class)
		@ConditionalOnBean(AbstractEntityManagerFactoryBean.class)
		protected static class FlywayInitializerJpaDependencyConfiguration
				extends EntityManagerFactoryDependsOnPostProcessor {

			public FlywayInitializerJpaDependencyConfiguration() {
				super("flywayInitializer");
			}

		}

		/**
		 * Additional configuration to ensure that {@link JdbcOperations} beans depend on
		 * the {@code flywayInitializer} bean.
		 */
		@Configuration
		@ConditionalOnClass(JdbcOperations.class)
		@ConditionalOnBean(JdbcOperations.class)
		protected static class FlywayInitializerJdbcOperationsDependencyConfiguration
				extends AbstractDependsOnBeanFactoryPostProcessor {

			public FlywayInitializerJdbcOperationsDependencyConfiguration() {
				super(JdbcOperations.class, "flywayInitializer");
			}

		}

	}

	/**
	 * Additional configuration to ensure that {@link EntityManagerFactory} beans depend
	 * on the {@code flyway} bean.
	 */
	@Configuration
	@ConditionalOnClass(LocalContainerEntityManagerFactoryBean.class)
	@ConditionalOnBean(AbstractEntityManagerFactoryBean.class)
	protected static class FlywayJpaDependencyConfiguration
			extends EntityManagerFactoryDependsOnPostProcessor {

		public FlywayJpaDependencyConfiguration() {
			super("flyway");
		}

	}

	/**
	 * Additional configuration to ensure that {@link JdbcOperations} beans depend on the
	 * {@code flyway} bean.
	 */
	@Configuration
	@ConditionalOnClass(JdbcOperations.class)
	@ConditionalOnBean(JdbcOperations.class)
	protected static class FlywayJdbcOperationsDependencyConfiguration
			extends EntityManagerFactoryDependsOnPostProcessor {

		public FlywayJdbcOperationsDependencyConfiguration() {
			super("flyway");
		}

	}

	private static class LocationResolver {

		private static final String VENDOR_PLACEHOLDER = "{vendor}";

		private final DataSource dataSource;

		LocationResolver(DataSource dataSource) {
			this.dataSource = dataSource;
		}

		public String[] resolveLocations(Collection<String> locations) {
			return resolveLocations(StringUtils.toStringArray(locations));
		}

		public String[] resolveLocations(String[] locations) {
			if (usesVendorLocation(locations)) {
				DatabaseDriver databaseDriver = getDatabaseDriver();
				return replaceVendorLocations(locations, databaseDriver);
			}
			return locations;
		}

		private String[] replaceVendorLocations(String[] locations,
												DatabaseDriver databaseDriver) {
			if (databaseDriver == DatabaseDriver.UNKNOWN) {
				return locations;
			}
			String vendor = databaseDriver.getId();
			return Arrays.stream(locations)
					.map((location) -> location.replace(VENDOR_PLACEHOLDER, vendor))
					.toArray(String[]::new);
		}

		private DatabaseDriver getDatabaseDriver() {
			try {
				String url = extractDatabaseMetaData(this.dataSource, databaseMetaData -> databaseMetaData.getURL());
				return DatabaseDriver.fromJdbcUrl(url);
			} catch (MetaDataAccessException ex) {
				throw new IllegalStateException(ex);
			}
		}

		private boolean usesVendorLocation(String... locations) {
			for (String location : locations) {
				if (location.contains(VENDOR_PLACEHOLDER)) {
					return true;
				}
			}
			return false;
		}

	}

	/**
	 * Convert a String or Number to a {@link MigrationVersion}.
	 */
	private static class StringOrNumberToMigrationVersionConverter
			implements GenericConverter {

		private static final Set<ConvertiblePair> CONVERTIBLE_TYPES;

		static {
			Set<ConvertiblePair> types = new HashSet<>(2);
			types.add(new ConvertiblePair(String.class, MigrationVersion.class));
			types.add(new ConvertiblePair(Number.class, MigrationVersion.class));
			CONVERTIBLE_TYPES = Collections.unmodifiableSet(types);
		}

		@Override
		public Set<ConvertiblePair> getConvertibleTypes() {
			return CONVERTIBLE_TYPES;
		}

		@Override
		public Object convert(Object source, TypeDescriptor sourceType,
							  TypeDescriptor targetType) {
			String value = ObjectUtils.nullSafeToString(source);
			return MigrationVersion.fromVersion(value);
		}

	}

}
