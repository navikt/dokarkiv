package no.nav.dokarkiv.core.repository;

import org.flywaydb.core.Flyway;
import org.springframework.boot.jdbc.SchemaManagement;
import org.springframework.boot.jdbc.SchemaManagementProvider;

import javax.sql.DataSource;
import java.util.stream.StreamSupport;

public class FlywaySchemaManagementProvider implements SchemaManagementProvider {

	private final Iterable<Flyway> flywayInstances;

	public FlywaySchemaManagementProvider(Iterable<Flyway> flywayInstances) {
		this.flywayInstances = flywayInstances;
	}

	@Override
	public SchemaManagement getSchemaManagement(DataSource dataSource) {
		return StreamSupport.stream(this.flywayInstances.spliterator(), false)
				.map((flyway) -> flyway.getConfiguration().getDataSource()).filter(dataSource::equals).findFirst()
				.map((managedDataSource) -> SchemaManagement.MANAGED).orElse(SchemaManagement.UNMANAGED);
	}
}
