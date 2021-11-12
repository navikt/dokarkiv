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
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.flyway.FlywayConfigurationCustomizer;
import org.springframework.boot.autoconfigure.flyway.FlywayDataSource;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.boot.autoconfigure.flyway.FlywayProperties;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.jdbc.DatabaseDriver;
import org.springframework.boot.sql.init.dependency.DatabaseInitializationDependencyConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ConfigurationCondition;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.MetaDataAccessException;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import javax.sql.DataSource;
import java.sql.DatabaseMetaData;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.jdbc.support.JdbcUtils.extractDatabaseMetaData;
import static org.springframework.util.StringUtils.hasText;

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
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(Flyway.class)
@Conditional(Flyway42AutoConfiguration.FlywayDataSourceCondition.class)
@ConditionalOnProperty(prefix = "spring.flyway", name = "enabled", matchIfMissing = true)
@AutoConfigureAfter({DataSourceAutoConfiguration.class,
		JdbcTemplateAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
@Import(DatabaseInitializationDependencyConfigurer.class)
@Profile("nais")
public class Flyway42AutoConfiguration {

	@Bean
	@ConfigurationPropertiesBinding
	public StringOrNumberToMigrationVersionConverter stringOrNumberMigrationVersionConverter() {
		return new StringOrNumberToMigrationVersionConverter();
	}

	@Bean
	public FlywaySchemaManagementProvider flywayDefaultDdlModeProvider(ObjectProvider<Flyway> flyways) {
		return new FlywaySchemaManagementProvider(flyways);
	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass(JdbcUtils.class)
	@ConditionalOnMissingBean(Flyway.class)
	@EnableConfigurationProperties(FlywayProperties.class)
	@Profile("nais")
	public static class FlywayConfiguration {

		private final FlywayProperties properties;

		private final ResourceLoader resourceLoader;

		private final DataSource dataSource;

		private final DataSource flywayDataSource;

		private final FlywayMigrationStrategy migrationStrategy;

		private List<Callback> callbacks;

		private Stream<FlywayConfigurationCustomizer> flywayConfigurationCustomizer;


		public FlywayConfiguration(FlywayProperties properties, ResourceLoader resourceLoader,
								   ObjectProvider<DataSource> dataSource,
								   @FlywayDataSource ObjectProvider<DataSource> flywayDataSource,
								   ObjectProvider<FlywayMigrationStrategy> migrationStrategy,
								   ObjectProvider<Callback> callbacks,
								   ObjectProvider<FlywayConfigurationCustomizer> fluentConfigurationCustomizers) {
			this.properties = properties;
			this.resourceLoader = resourceLoader;
			this.dataSource = dataSource.getIfUnique();
			this.flywayDataSource = flywayDataSource.getIfAvailable();
			this.migrationStrategy = migrationStrategy.getIfAvailable();
			this.callbacks = callbacks.orderedStream().collect(Collectors.toList());
			this.flywayConfigurationCustomizer = fluentConfigurationCustomizers.orderedStream();
		}

		@Bean
		@ConfigurationProperties(prefix = "spring.flyway")
		public Flyway flyway() {
			FluentConfiguration configuration = new FluentConfiguration(resourceLoader.getClassLoader());
			configureDataSource(configuration);
			configureProperties(configuration);
			configureCallbacks(configuration);
			flywayConfigurationCustomizer.forEach(customizer -> customizer.customize(configuration));
			configureFlywayCallbacks(configuration);

			return configuration.load();
		}

		private void configureDataSource(FluentConfiguration configuration) {
			DataSource migrationDataSource = getMigrationDataSource();
			configuration.dataSource(migrationDataSource);
		}

		private DataSource getMigrationDataSource() {
			if (flywayDataSource != null) {
				return flywayDataSource;
			}
			if (properties.getUrl() != null) {
				DataSourceBuilder<?> builder = DataSourceBuilder.create().type(SimpleDriverDataSource.class);
				builder.url(properties.getUrl());
				applyCommonBuilderProperties(builder);
				return builder.build();
			}
			if (properties.getUser() != null && dataSource != null) {
				DataSourceBuilder<?> builder = DataSourceBuilder.derivedFrom(dataSource)
						.type(SimpleDriverDataSource.class);
				applyCommonBuilderProperties(builder);
				return builder.build();
			}
			Assert.state(dataSource != null, "Flyway migration DataSource missing");
			return dataSource;
		}

		private void applyCommonBuilderProperties(DataSourceBuilder<?> builder) {
			builder.username(properties.getUser());
			builder.password(properties.getPassword());
			if (hasText(properties.getDriverClassName())) {
				builder.driverClassName(properties.getDriverClassName());
			}
		}

		private void configureProperties(FluentConfiguration configuration) {
			PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
			String[] locations = new LocationResolver(configuration.getDataSource())
					.resolveLocations(properties.getLocations()).toArray(new String[0]);
			checkLocationExists(locations);
			map.from(locations).to(configuration::locations);
			map.from(properties.getEncoding()).to(configuration::encoding);
			map.from(properties.getConnectRetries()).to(configuration::connectRetries);
		}

		private void configureCallbacks(FluentConfiguration configuration) {
			if (!callbacks.isEmpty()) {
				configuration.callbacks(callbacks.toArray(new Callback[0]));
			}
		}

		private void configureFlywayCallbacks(FluentConfiguration flyway) {
			if (!callbacks.isEmpty()) {
				flyway.callbacks(callbacks.toArray(new Callback[0]));
			}
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

	}

	private static class LocationResolver {

		private static final String VENDOR_PLACEHOLDER = "{vendor}";

		private final DataSource dataSource;

		LocationResolver(DataSource dataSource) {
			this.dataSource = dataSource;
		}

		List<String> resolveLocations(List<String> locations) {
			if (usesVendorLocation(locations)) {
				DatabaseDriver databaseDriver = getDatabaseDriver();
				return replaceVendorLocations(locations, databaseDriver);
			}
			return locations;
		}

		private List<String> replaceVendorLocations(List<String> locations, DatabaseDriver databaseDriver) {
			if (databaseDriver == DatabaseDriver.UNKNOWN) {
				return locations;
			}
			String vendor = databaseDriver.getId();
			return locations.stream().map((location) -> location.replace(VENDOR_PLACEHOLDER, vendor))
					.collect(Collectors.toList());
		}

		private DatabaseDriver getDatabaseDriver() {
			try {
				String url = extractDatabaseMetaData(this.dataSource, DatabaseMetaData::getURL);
				return DatabaseDriver.fromJdbcUrl(url);
			} catch (MetaDataAccessException ex) {
				throw new IllegalStateException(ex);
			}

		}

		private boolean usesVendorLocation(Collection<String> locations) {
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

	static final class FlywayDataSourceCondition extends AnyNestedCondition {

		FlywayDataSourceCondition() {
			super(ConfigurationCondition.ConfigurationPhase.REGISTER_BEAN);
		}

		@ConditionalOnBean(DataSource.class)
		private static final class DataSourceBeanCondition {

		}

		@ConditionalOnProperty(prefix = "spring.flyway", name = "url")
		private static final class FlywayUrlCondition {

		}

	}

}
