package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark103;

import static no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder.getDokumentInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static no.nav.dokarkiv.core.domain.builder.SaksrelasjonBuilder.getSaksrelasjonBuilder;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThat;

import no.nav.dokarkiv.arkiverdokumentproduksjon.AbstractArkiverdokumentproduksjonItest;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.AvbrytJournalpostJournalpostIkkeFunnet;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.AvbrytJournalpostRequest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Integration tests for the avbrytJournalpost operation
 * in the ArkiverDokumentproduksjon webservice.
 *
 * @author Torgeir Cook
 */
public class AvbrytJournalpostIT extends AbstractArkiverdokumentproduksjonItest {

	private static final String OPPRETTET_KILDE_NAVN = "opprettet kilde";
	private static final String OPPRETTET_AV_NAVN = "Tester";
	private static final String TILKNYTTET_AV_NAVN = "Tilknyttetnavn";
	private static final String ENDRET_AV_NAVN = "Tester2";

	private AvbrytJournalpostRequest request;
	private Journalpost journalpost;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Before
	public void setUp() throws Exception {
		journalpost = createJournalpost(DokumentStatusCode.UNDER_REDIGERING, JournalStatusCode.D);
		joarkRepository.save(journalpost);
		request = createWsRequest(journalpost.getJournalpostId());
	}

	@Test
	public void shouldAvbrytJournalPost() throws Exception {
		arkiverDokumentproduksjonProvider.avbrytJournalpost(request);
		DokumentInfo dokumentInfo = journalpost.getJournalpostDokumentInfoRelasjoner()
				.iterator().next().getDokumentInfo();
		assertThat(dokumentInfo.getDokumentstatus(), is(DokumentStatusCode.AVBRUTT));
	}

	@Test
	public void shouldAvbrytFerdigLokalprint() throws Exception {
		journalpost = createJournalpost(DokumentStatusCode.FERDIGSTILT, JournalStatusCode.D);
		joarkRepository.save(journalpost);
		request = createWsRequest(journalpost.getJournalpostId());
		arkiverDokumentproduksjonProvider.avbrytJournalpost(request);

		Journalpost journalpostById = joarkRepository.findById(request.getJournalpostId()).get();
		assertThat(journalpostById.getJournalstatus(), is(JournalStatusCode.A));
	}

	@Test
	public void shouldVerifyJournalpostFields() throws Exception {
		arkiverDokumentproduksjonProvider.avbrytJournalpost(request);
		Journalpost journalpostById = joarkRepository.findById(request.getJournalpostId()).get();
		Saksrelasjon saksrelasjon = journalpostById.getSaksrelasjon();

		assertThat(journalpostById.getJournalstatus(), is(JournalStatusCode.A));
		assertThat(journalpostById.getEndretAvNavn(), is(request.getEndretAvNavn()));
		assertThat(saksrelasjon.getEndretAvNavn(), is(request.getEndretAvNavn()));
	}

	@Test
	public void shouldVerifyJournalpostFieldsWhenDokumentStatusIsNotUnderRedigering() throws Exception {
		journalpost = createJournalpost(DokumentStatusCode.FERDIGSTILT, JournalStatusCode.D);
		joarkRepository.save(journalpost);
		arkiverDokumentproduksjonProvider.avbrytJournalpost(request);
		Journalpost journalpostById = joarkRepository.findById(request.getJournalpostId()).get();
		Saksrelasjon saksrelasjon = journalpostById.getSaksrelasjon();

		assertNotSame(journalpostById.getJournalstatus(), is(JournalStatusCode.A));
		assertThat(journalpostById.getEndretAvNavn(), is(request.getEndretAvNavn()));
		assertThat(saksrelasjon.getEndretAvNavn(), is(request.getEndretAvNavn()));
	}

	@Test
	public void shouldThrowExceptionIfJournalpostDoesNotExist() throws Exception {
		request.setJournalpostId(66666);
		expectedException.expect(AvbrytJournalpostJournalpostIkkeFunnet.class);
		expectedException.expectMessage("Journalpost with id: 66666 not found");
		arkiverDokumentproduksjonProvider.avbrytJournalpost(request);
	}

	@Test
	public void shouldThrowExceptionIfNoEndretAvNavnInRequest() throws Exception {
		request.setEndretAvNavn(null);
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("EndretAvNavn cannot be empty or missing");
		arkiverDokumentproduksjonProvider.avbrytJournalpost(request);
	}

	private AvbrytJournalpostRequest createWsRequest(Long journalpostId) {
		AvbrytJournalpostRequest request = new AvbrytJournalpostRequest();
		request.setJournalpostId(journalpostId);
		request.setEndretAvNavn(ENDRET_AV_NAVN);
		return request;
	}

	private Journalpost createJournalpost(DokumentStatusCode dokumentStatusCode, JournalStatusCode journalStatusCode) {
		return getJournalpostBuilder()
				.avsenderMottakerId("02016126007")
				.journalStatus(journalStatusCode)
				.journalpostType(JournalpostTypeCode.U)
				.opprettetAvNavn(OPPRETTET_AV_NAVN)
				.opprettetKildeNavn(OPPRETTET_KILDE_NAVN)
				.fagomrade(FagomradeCode.PEN)
				.saksrelasjon(
						getSaksrelasjonBuilder()
								.sakId("1")
								.fagsystem(FagsystemCode.PEN)
								.opprettetKildeNavn(OPPRETTET_KILDE_NAVN)
								.build())
				.dokumentInfoRelasjoner(
						getJournalpostDokumentInfoRelasjonBuilder()
								.opprettetKildeNavn(OPPRETTET_KILDE_NAVN)
								.tilknyttetAvNavn(TILKNYTTET_AV_NAVN)
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
								.dokumentInfo(createDokumentInfo(dokumentStatusCode))
								.build())
				.build();
	}

	private DokumentInfo createDokumentInfo(DokumentStatusCode dokumentStatusCode) {
		return getDokumentInfoBuilder()
				.dokumentstatus(dokumentStatusCode)
				.endretAvNavn(ENDRET_AV_NAVN)
				.opprettetKildeNavn(OPPRETTET_KILDE_NAVN)
				.build();
	}
}
