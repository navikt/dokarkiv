package no.nav.dokarkiv.behandlejournal.v2.tjoark062;

import no.nav.dokarkiv.behandlejournal.v2.AbstractBehandleJournalV2Itest;
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.util.DateProvider;
import no.nav.dokarkiv.core.stelvio.RequestContextSetter;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.FerdigstillDokumentopplastingFerdigstillDokumentopplastingjournalpostIkkeFunnet;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.meldinger.FerdigstillDokumentopplastingRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static no.nav.dokarkiv.core.domain.builder.BrukerBuilder.getBrukerBuilder;
import static no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder.getDokumentInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder.getFilDetaljerBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static no.nav.dokarkiv.core.domain.builder.SaksrelasjonBuilder.getSaksrelasjonBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Integration tests for the ferdigstillDokumentopplasting operation.
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public class FerdigstillDokumentopplastingIT extends AbstractBehandleJournalV2Itest {
	private static final String SPORING_FORNAVN = "fornavn";
	private static final String SPORING_ETTERNAVN = "etternavn";
	private static final String NONEXISTING_JOURNALPOST_ID = "12345";

	private FerdigstillDokumentopplastingRequest request;

	private Journalpost journalpost;

	@BeforeEach
	public void setUp() {
		DateProvider.configure(true, "2018-07-10T14:20");
		RequestContextSetter.setRequestContextForUnitTest();
		createRequest();
	}

	private void createRequest() {
		request = new FerdigstillDokumentopplastingRequest();
		request.setPersonFornavn(SPORING_FORNAVN);
		request.setPersonEtternavn(SPORING_ETTERNAVN);
		request.setApplikasjonsID("applikasjonsid");
	}

	@Test
	public void shouldThrowExceptionWhenFerdigstillDokumentopplastingForJoarkdokumenterIfJournalpostDoesNotExist() {
		request.setJournalpostId(NONEXISTING_JOURNALPOST_ID);

		assertThrows(FerdigstillDokumentopplastingFerdigstillDokumentopplastingjournalpostIkkeFunnet.class,
				() -> behandleJournalProvider.ferdigstillDokumentopplasting(request));
	}

	@Test
	public void shouldFerdigstillDokumentopplastingForJoarkPensjonsdokumenter() throws Exception {
		journalpost = buildAndPersistJournalpost(FagomradeCode.PEN);
		request.setJournalpostId(journalpost.getJournalpostId().toString());

		behandleJournalProvider.ferdigstillDokumentopplasting(request);

		Journalpost ferdigstiltJournalpost = joarkRepository.findById(journalpost.getJournalpostId()).get();
		assertThat(ferdigstiltJournalpost.getJournalstatus(), is(JournalStatusCode.M));

		assertThat(ferdigstiltJournalpost.getEndretAvNavn(), is(SPORING_FORNAVN + " " + SPORING_ETTERNAVN));
		assertThat(ferdigstiltJournalpost.getEndretKildeNavn(), is("testComponentId"));

		assertThat(ferdigstiltJournalpost.getSaksrelasjon().getEndretAvNavn(), is(SPORING_FORNAVN + " " + SPORING_ETTERNAVN));
		assertThat(ferdigstiltJournalpost.getSaksrelasjon().getEndretKildeNavn(), is("testComponentId"));

		for (DokumentInfo dokumentInfo : ferdigstiltJournalpost.findAllDokumentInfos()) {
			assertThat(dokumentInfo.getEndretAvNavn(), is(SPORING_FORNAVN + " " + SPORING_ETTERNAVN));
			assertThat(dokumentInfo.getEndretKildeNavn(), is("testComponentId"));
		}
	}

	@Test
	public void shouldFerdigstillDokumentopplastingForJoarkdokumenter() throws Exception {
		journalpost = buildAndPersistJournalpost(FagomradeCode.FOR);
		request.setJournalpostId(journalpost.getJournalpostId().toString());

		behandleJournalProvider.ferdigstillDokumentopplasting(request);

		Journalpost ferdigstiltJournalpost = joarkRepository.findById(journalpost.getJournalpostId()).get();
		assertThat(ferdigstiltJournalpost.getJournalstatus(), is(JournalStatusCode.MO));

		assertThat(ferdigstiltJournalpost.getEndretAvNavn(), is(SPORING_FORNAVN + " " + SPORING_ETTERNAVN));
		assertThat(ferdigstiltJournalpost.getEndretKildeNavn(), is("testComponentId"));

		assertThat(ferdigstiltJournalpost.getSaksrelasjon().getEndretAvNavn(), is(SPORING_FORNAVN + " " + SPORING_ETTERNAVN));
		assertThat(ferdigstiltJournalpost.getSaksrelasjon().getEndretKildeNavn(), is("testComponentId"));

		for (DokumentInfo dokumentInfo : ferdigstiltJournalpost.findAllDokumentInfos()) {
			assertThat(dokumentInfo.getEndretAvNavn(), is(SPORING_FORNAVN + " " + SPORING_ETTERNAVN));
			assertThat(dokumentInfo.getEndretKildeNavn(), is("testComponentId"));
		}
	}

	private Journalpost buildAndPersistJournalpost(FagomradeCode fagomradeCode) {
		Journalpost build = getJournalpostBuilder()
				.avsenderMottakerId("02016126007")
				.journalStatus(JournalStatusCode.OD)
				.journalpostType(JournalpostTypeCode.I)
				.opprettetAvNavn("opprettetAvNavn")
				.opprettetKildeNavn("opprettetKildeNavn")
				.fagomrade(fagomradeCode)
				.innhold("innhold")
				.avsenderMottaker("avsenderMottaker")
				.journalForendeEnhetId("PEN")
				.signatur(true)
				.mottattDato(DateProvider.getToday())
				.dokumentDato(DateProvider.getToday())
				.mottakskanal(MottaksKanalCode.ALTINN)
				.utsendingskanal(UtsendingsKanalCode.EESSI)
				.saksrelasjon(
						getSaksrelasjonBuilder().sakId("1").fagsystem(FagsystemCode.PEN)
								.opprettetKildeNavn("opprettetKildeNavn").build())
				.brukere(
						getBrukerBuilder().brukerId("02016126007").brukerType(BrukerTypeCode.PERSON)
								.opprettetKildeNavn("opprettetKildeNavn").build())
				.dokumentInfoRelasjoner(
						getJournalpostDokumentInfoRelasjonBuilder()
								.opprettetKildeNavn("opprettetkildeNavn")
								.tilknyttetAvNavn("tilknyttetAvNavn")
								.dokumentInfo(
										getDokumentInfoBuilder()
												.opprettetKildeNavn("opprettetKildeNavn")
												.dokumentstatus(DokumentStatusCode.FERDIGSTILT)
												.brevkode("NAV-01-02-03")
												.kategori(DokumentKategoriCode.ES)
												.tittel("tittel")
												.sensitivt(true)
												.integritet("integritet")
												.konfidensialitet("konfidensialitet")
												.tilgjengelighet("tilgjengelighet")
												.filDetaljerList(
														getFilDetaljerBuilder()
																.opprettetKildeNavn("opprettetKildeNavn")
																.filtype(FilTypeCode.PDF)
																.variantFormat(VariantFormatCode.ARKIV)
																.fileContent("Test pdf".getBytes()).build()).build())
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT).build()).build();
		return joarkRepository.save(build);
	}

}
