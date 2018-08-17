package no.nav.dokarkiv.behandlejournal;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import no.nav.dokarkiv.core.exceptions.ApplicationException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Tests for SporingUtil
 * 
 * @author Joakim Bjørnstad, Visma Consulting
 *
 */
public class SporingUtilTest {
	private static final String SPORING_FORNAVN = "Bjarne";
	private static final String SPORING_ETTERNAVN = "Betjent";
	private static final String APPLIKASJONS_ID = "Sesam Stasjon";
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
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
		expectedException.expect(ApplicationException.class);
		expectedException.expectMessage("personFornavn, personEtternavn or applikasjonsID must be set.");
		
		SporingUtil.decideSporingNavn(null, null, null);
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
		expectedException.expect(ApplicationException.class);
		expectedException.expectMessage("personFornavn, personEtternavn or applikasjonsID must be set.");
		
		SporingUtil.decideSporingNavn(new SporingsMetaData(null, null, null));
	}
	
	@Test
	public void shouldThrowExceptionIfSporingsMetaDataIsNull() {
		expectedException.expect(ApplicationException.class);
		expectedException.expectMessage("sporingsMetaData with personFornavn, personEtternavn or applikasjonsID must be set.");
		
		SporingUtil.decideSporingNavn(null);
	}
}
