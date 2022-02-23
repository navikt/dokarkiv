package no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v1;

import static no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder.getDokumentInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;

import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class JournalforInngaaendeForsendelseRequestToTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private JournalforInngaaendeForsendelseRequestTo requestTo;

	@Before
	public void setUp() {
		requestTo = new JournalforInngaaendeForsendelseRequestTo(createJournalpost());
	}

	@Test
	public void shouldValidateOK() {
		requestTo.validate();
	}


	@Test
	public void shouldThrowException_missingJournalpost() {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("Journalpost");
		JournalforInngaaendeForsendelseRequestTo requestTo = new JournalforInngaaendeForsendelseRequestTo(null);

		requestTo.validate();
	}

	@Test
	public void shouldThrowException_missingSaksrelasjon() {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("Saksrelasjon");
		requestTo.getJournalpost().setSaksrelasjon(null);

		requestTo.validate();
	}

	@Test
	public void shouldThrowException_missingBruker() {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("Brukere");
		requestTo.getJournalpost().clearBrukere();

		requestTo.validate();
	}

	@Test
	public void shouldThrowException_missingJpDokRelasjoner() {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("JournalpostDokumentInfoRelasjoner");
		requestTo.getJournalpost().clearJournalpostDokumentInfoRelasjoner();

		requestTo.validate();
	}

	@Test
	public void shouldThrowException_missingDokumentInfo() {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("JournalpostDokumentInfoRelasjoner.DokumentInfo");
		requestTo.getJournalpost().clearJournalpostDokumentInfoRelasjoner();
		requestTo.getJournalpost().addJournalpostDokumentInfoRelasjon(new JournalpostDokumentInfoRelasjon());

		requestTo.validate();
	}

	@Test
	public void shouldThrowException_missingFilDetaljer() {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("JournalpostDokumentInfoRelasjoner.DokumentInfo.Fildetaljer");

		requestTo.getJournalpost()
				.getJournalpostDokumentInfoRelasjoner()
				.iterator()
				.next()
				.getDokumentInfo()
				.clearFildetaljerListe();
		requestTo.validate();
	}


	private Journalpost createJournalpost() {
		return getJournalpostBuilder()
				.saksrelasjon(new Saksrelasjon())
				.brukere(new Bruker())
				.dokumentInfoRelasjoner(
						getJournalpostDokumentInfoRelasjonBuilder()
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
								.dokumentInfo(
										getDokumentInfoBuilder()
												.filDetaljerList(new FilDetaljer())
												.build())
								.build())
				.build();
	}

}