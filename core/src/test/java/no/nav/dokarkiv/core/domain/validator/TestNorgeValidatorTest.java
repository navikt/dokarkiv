package no.nav.dokarkiv.core.domain.validator;

import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

import javax.annotation.concurrent.NotThreadSafe;

import static no.nav.dokarkiv.core.domain.builder.BrukerBuilder.getBrukerBuilder;
import static org.junit.jupiter.api.parallel.ResourceAccessMode.READ_WRITE;
import static org.junit.jupiter.api.parallel.Resources.SYSTEM_PROPERTIES;

@NotThreadSafe
@ResourceLock(value = SYSTEM_PROPERTIES, mode = READ_WRITE)
public class TestNorgeValidatorTest {

	private static final String TESTNORGE_INDENT = "27857798800";

	@BeforeClass
	public static void setUp() {
		System.setProperty("NAIS_CLUSTER_NAME", "dev-fss");

	}

	@AfterClass
	public static void tearDown() {
		System.clearProperty("NAIS_CLUSTER_NAME");

	}

	@Test
	public void shouldValidateValidTestNorgePerson() {
		Bruker bruker = getBrukerBuilder()
				.brukerId(TESTNORGE_INDENT)
				.brukerType(BrukerTypeCode.PERSON)
				.build();

		BrukerValidator.validate(bruker);
	}
}
