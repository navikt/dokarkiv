package no.nav.dokarkiv.behandlejournal;

import no.nav.dokarkiv.core.exceptions.ApplicationException;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for SporingUtil
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public class SporingUtilTest {
	private static final String SPORING_FORNAVN = "Bjarne";
	private static final String SPORING_ETTERNAVN = "Betjent";
	private static final String APPLIKASJONS_ID = "Sesam Stasjon";

	@Test
	public void shouldReturnFornavnEtternavn() {
		String sporingNavn = SporingUtil.decideSporingNavn(SPORING_FORNAVN, SPORING_ETTERNAVN, APPLIKASJONS_ID);
		assertThat(sporingNavn, is(SPORING_FORNAVN + " " + SPORING_ETTERNAVN));
	}

	@Test
	public void shouldReturnApplikasjonsID() {
		String sporingNavn = SporingUtil.decideSporingNavn(null, null, APPLIKASJONS_ID);
		assertThat(sporingNavn, is(APPLIKASJONS_ID));
	}

	@Test
	public void shouldThrowExceptionIfNeitherArgumentIsPresent() {
		assertThrows(ApplicationException.class,
				() -> SporingUtil.decideSporingNavn(null, null, null),
				"personFornavn, personEtternavn or applikasjonsID must be set.");
	}

	@Test
	public void shouldReturnFornavnEtternavnByPassingSporingsMetaData() {
		String sporingNavn = SporingUtil.decideSporingNavn(new SporingsMetaData(SPORING_FORNAVN, SPORING_ETTERNAVN, APPLIKASJONS_ID));
		assertThat(sporingNavn, is(SPORING_FORNAVN + " " + SPORING_ETTERNAVN));
	}

	@Test
	public void shouldReturnApplikasjonsIDByPassingSporingsMetaData() {
		String sporingNavn = SporingUtil.decideSporingNavn(new SporingsMetaData(null, null, APPLIKASJONS_ID));
		assertThat(sporingNavn, is(APPLIKASJONS_ID));
	}

	@Test
	public void shouldThrowExceptionIfNeitherArgumentIsPresentInSporingsMetaData() {
		assertThrows(ApplicationException.class,
				() -> SporingUtil.decideSporingNavn(new SporingsMetaData(null, null, null)),
				"personFornavn, personEtternavn or applikasjonsID must be set.");
	}

	@Test
	public void shouldThrowExceptionIfSporingsMetaDataIsNull() {
		assertThrows(ApplicationException.class,
				() -> SporingUtil.decideSporingNavn(null),
				"sporingsMetaData with personFornavn, personEtternavn or applikasjonsID must be set.");
	}
}
