package no.nav.dokarkiv.core.exceptions;

import java.util.ArrayList;
import java.util.List;

public class FlywayMigrationScriptMissingException extends RuntimeException{

	private final List<String> locations;

	public FlywayMigrationScriptMissingException(List<String> locations) {
		super(locations.isEmpty() ? "Migration script locations not configured" : "Cannot find migration scripts in: "
				+ locations + " (please add migration scripts or check your Flyway configuration)");
		this.locations = new ArrayList<>(locations);
	}

	public List<String> getLocations() {
		return this.locations;
	}

}
