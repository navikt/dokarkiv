package no.nav.dokarkiv.innsynjournal.v2.security.urn;

import static no.nav.dokarkiv.innsynjournal.v2.security.urn.ResourceAttributeIds.ATTR_ALLE_HISTORISKE_PERSONNUMMER;
import static no.nav.dokarkiv.innsynjournal.v2.security.urn.ResourceAttributeIds.ATTR_RESOURCE_TARGET;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/**
 * Unit tests for {@link ResourceAttributeIds}
 *
 * @author Roar Bjurstrom, Visma Consulting.
 */
public class ResourceAttributeIdsTest {

	private static final String RESOURCE_TARGET = "urn:nav:ikt:tilgangskontroll:xacml:joark:resource:resource-target";
	private static final String ALL_FNRS = "urn:nav:ikt:tilgangskontroll:xacml:joark:resource:person:historiske:fodselsnumre";
	private static final String JOURNALPOST_AVSENDER = "urn:nav:ikt:tilgangskontroll:xacml:joark:resource:attr:journalpost:avsenderfnr";

	@Test
	public void shouldReturnAccessType() throws Exception {
		assertThat(ATTR_RESOURCE_TARGET, is(RESOURCE_TARGET));
	}

	@Test
	public void shouldReturnAccessTypeURN() throws Exception {
		assertThat(ResourceAttributeIds.RESOURCE_TARGET.getURN(), is(RESOURCE_TARGET));
	}

	@Test
	public void shouldReturnDocument() throws Exception {
		assertThat(ResourceAttributeIds.DOCUMENT, is("dokument"));
	}

	@Test
	public void shouldReturnHistoriskePersonnummerString() throws Exception {
		assertThat(ATTR_ALLE_HISTORISKE_PERSONNUMMER, is(ALL_FNRS));
	}

	@Test
	public void shouldReturnHistoriskePersonnummerUrn() throws Exception {
		assertThat(ResourceAttributeIds.ALLE_HISTORISKE_PERSONNUMMER.getURN(), is(ALL_FNRS));
	}

	@Test
	public void historiskePersonnummerUrnAndStringShouldBeEqual() throws Exception {
		assertThat(ResourceAttributeIds.ALLE_HISTORISKE_PERSONNUMMER.getURN(), is(ATTR_ALLE_HISTORISKE_PERSONNUMMER));
	}

	@Test
	public void shouldReturnJournalpostAvsenderString() throws Exception {
		assertThat(ResourceAttributeIds.ATTR_JOURNALPOST_AVSENDER, is(JOURNALPOST_AVSENDER));
	}

	@Test
	public void shouldReturnJournalpostAvsenderUrn() throws Exception {
		assertThat(ResourceAttributeIds.JOURNALPOST_AVSENDER.getURN(), is(JOURNALPOST_AVSENDER));
	}

	@Test
	public void journalpostAvsenderUrnAndStringShouldBeEqual() throws Exception {
		assertThat(ResourceAttributeIds.JOURNALPOST_AVSENDER.getURN(), is(ResourceAttributeIds.ATTR_JOURNALPOST_AVSENDER));
	}
}
