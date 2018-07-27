package no.nav.dokarkiv.behandleinngaaendejournal.v1.tjoark067;

import static no.nav.dokarkiv.behandleinngaaendejournal.v1.BehandleInngaaendeJournalDataProvider.buildNoBrukerJournalpost;
import static no.nav.dokarkiv.behandleinngaaendejournal.v1.BehandleInngaaendeJournalDataProvider.buildNoRelasjonJournalpost;
import static no.nav.dokarkiv.behandleinngaaendejournal.v1.BehandleInngaaendeJournalDataProvider.createHovedDokumentInfo;
import static no.nav.dokarkiv.core.domain.builder.BrukerBuilder.getBrukerBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static no.nav.dokarkiv.core.domain.builder.SaksrelasjonBuilder.getSaksrelasjonBuilder;

import no.nav.dokarkiv.behandleinngaaendejournal.v1.BehandleInngaaendeJournalDataProvider;
import no.nav.dokarkiv.behandleinngaaendejournal.v1.exceptions.FerdigstillingIkkeMuligException;
import no.nav.dokarkiv.core.domain.builder.JournalpostBuilder;
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public class FerdigstillJournalfoeringFieldValidatorTest {

	private FerdigstillJournalfoeringFieldValidator validator = new FerdigstillJournalfoeringFieldValidator();

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void should_throw_ferdigstillingikkemuligexception_when_missing_sak_saksnummer() throws Exception {
		expectFerdigstillingIkkeMuligExceptionWithMessage("sakId");

		validator.validate(buildJournalpost().journalStatus(JournalStatusCode.J).saksrelasjon(getSaksrelasjonBuilder().fagsystem(FagsystemCode.PEN).build()).build());
	}

	@Test
	public void should_not_throw_ferdigstillingikkemuligexception_when_missing_journalpost_dokumentDato() throws Exception {
		validator.validate(buildJournalpost().dokumentDato(null).build());
	}

	@Test
	public void should_throw_ferdigstillingikkemuligexception_when_missing_sak_fagsystem() throws Exception {
		expectFerdigstillingIkkeMuligExceptionWithMessage("fagsystem");

		validator.validate(buildJournalpost().saksrelasjon(getSaksrelasjonBuilder().sakId("1").fagsystem(null).build()).build());
	}

	@Test
	public void should_throw_ferdigstillingikkemuligexception_when_missing_journalpost_fagomrade() throws Exception {
		expectFerdigstillingIkkeMuligExceptionWithMessage("Journalpost.fagomrade");

		validator.validate(buildJournalpost().fagomrade(null).build());
	}

	@Test
	public void should_throw_ferdigstillingikkemuligexception_when_missing_journalpost_innhold() throws Exception {
		expectFerdigstillingIkkeMuligExceptionWithMessage("Journalpost.innhold");

		validator.validate(buildJournalpost().innhold(null).build());
	}

	@Test
	public void should_throw_ferdigstillingikkemuligexception_when_missing_journalpost_avsenderMottaker() throws Exception {
		expectFerdigstillingIkkeMuligExceptionWithMessage("Journalpost.avsenderMottaker");

		validator.validate(buildJournalpost().avsenderMottaker(null).build());
	}

	@Test
	public void should_throw_ferdigstillingikkemuligexception_when_missing_journalpost_avsenderMottakerId() throws Exception {
		expectFerdigstillingIkkeMuligExceptionWithMessage("Journalpost.avsenderMottakerId");

		validator.validate(buildJournalpost().avsenderMottakerId(null).build());
	}

	@Test
	public void should_throw_ferdigstillingikkemuligexception_when_missing_journalpost_brukere() throws Exception {
		expectFerdigstillingIkkeMuligExceptionWithMessage("Journalpost.brukere");

		validator.validate(buildNoBrukerJournalpost().build());
	}

	@Test
	public void should_throw_ferdigstillingikkemuligexception_when_missing_journalpost_brukerId() throws Exception {
		expectFerdigstillingIkkeMuligExceptionWithMessage("Bruker.brukerId");

		validator.validate(buildNoBrukerJournalpost().brukere(getBrukerBuilder().brukerType(BrukerTypeCode.PERSON).build()).build());
	}

	@Test
	public void should_throw_ferdigstillingikkemuligexception_when_missing_journalpost_brukerType() throws Exception {
		expectFerdigstillingIkkeMuligExceptionWithMessage("Bruker.brukerType");

		validator.validate(buildNoBrukerJournalpost().brukere(getBrukerBuilder().brukerId("123").build()).build());
	}

	@Test
	public void should_not_throw_ferdigstillingikkemuligexception_when_missing_dokumentinfo_sensitivt() throws Exception {
		validator.validate(buildJournalpost()
				.dokumentInfoRelasjoner(getJournalpostDokumentInfoRelasjonBuilder()
						.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
						.tilknyttetAvNavn("tilknyttet")
						.dokumentInfo(createHovedDokumentInfo().sensitivt(null).build())
						.build()).build());
	}

	@Test
	public void should_throw_ferdigstillingikkemuligexception_when_missing_dokumentinfo_kategori() throws Exception {
		expectFerdigstillingIkkeMuligExceptionWithMessage("kategori");

		validator.validate(buildNoRelasjonJournalpost().journalStatus(JournalStatusCode.J)
				.journalForendeEnhetId("test")
				.dokumentInfoRelasjoner(getJournalpostDokumentInfoRelasjonBuilder()
						.journalpostDokumentInfoRelasjonId(1L)
						.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
						.tilknyttetAvNavn("tilknyttet")
						.dokumentInfo(createHovedDokumentInfo().kategori(null).build())
						.build()).build());
	}

	@Test
	public void should_throw_ferdigstillingikkemuligexception_when_missing_dokumentinfo_tittel() throws Exception {
		expectFerdigstillingIkkeMuligExceptionWithMessage("tittel");

		validator.validate(buildNoRelasjonJournalpost().journalStatus(JournalStatusCode.J)
				.journalForendeEnhetId("test")
				.dokumentInfoRelasjoner(getJournalpostDokumentInfoRelasjonBuilder()
						.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
						.tilknyttetAvNavn("tilknyttet")
						.dokumentInfo(createHovedDokumentInfo().tittel(null).build())
						.build()).build());
	}

	private void expectFerdigstillingIkkeMuligExceptionWithMessage(String messageSubstring) {
		thrown.expect(FerdigstillingIkkeMuligException.class);
		thrown.expectMessage(messageSubstring);
	}

	private JournalpostBuilder buildJournalpost() {
		return BehandleInngaaendeJournalDataProvider.buildJournalpost()
				.journalStatus(JournalStatusCode.J)
				.journalForendeEnhetId("0101");
	}


}
