package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark102;

import static no.nav.dokarkiv.core.domain.builder.BrukerBuilder.getBrukerBuilder;
import static no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder.getDokumentInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder.getFilDetaljerBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static no.nav.dokarkiv.core.domain.builder.SaksrelasjonBuilder.getSaksrelasjonBuilder;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.util.DateProvider;
import no.nav.dokarkiv.core.journalbehandling.DokumentFilerDelegate;
import no.nav.dokarkiv.core.repository.JoarkRepositorySkjermet;
import no.nav.dokarkiv.core.sporing.SporingPopulator;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@RunWith(MockitoJUnitRunner.class)
public class DefaultOppdaterJournalpostArkiverDokumentServiceTest {

	private static final Long JOURNALPOST_ID = 42L;
	private static final Long DOKUMENTINFO_ID = 56L;
	private static final String TODAY_DATE = "2018-06-20T14:31:54.767";
	private static final boolean SENSITIVT_REQUEST = true;
	private static final String OPPRETTET_AV_NAVN = "Saksbehandler2";
	private static final String ENDRET_AV_NAVN = "Saksbehandler1";
	private static final byte[] DOKUMENTINNHOLD = "DOKUMENT".getBytes();
	private static final Long METAFORCEINSTANCE_ID = 37984L;

	private Journalpost journalpost;
	private OppdaterJournalpostArkiverDokumentRequestTo requestTo;

	@Rule
	public ExpectedException expected = ExpectedException.none();
	@Mock
    private JoarkRepositorySkjermet repositoryMock;
	@Mock
	private DokumentFilerDelegate dokumentFilerDelegateMock;
	@Mock
	private OppdaterJournalpostArkiverDokumentValidator validatorMock;
	@Mock
	private SporingPopulator sporingPopulatorMock;
	@InjectMocks
	DefaultOppdaterJournalpostArkiverDokumentService service;

	@Before
	public void setUp() {
		requestTo = createJournalpostRequestTo(JOURNALPOST_ID, DOKUMENTINFO_ID);
		journalpost = createJournalpost(JOURNALPOST_ID, DOKUMENTINFO_ID);
		DateProvider.configure(true, TODAY_DATE);
		when(repositoryMock.findById(JOURNALPOST_ID)).thenReturn(Optional.of(journalpost));
	}

	@Test
	public void shouldNotArkiverDokumentOgFerdigstillJournalpostOrThrowException() throws Exception {
		service.oppdaterJournalpostArkiverDokument(requestTo);
		verify(validatorMock).validate(journalpost, requestTo);
		verify(sporingPopulatorMock).populateSporingInfo(journalpost, requestTo.getEndretAvNavn());
		verify(dokumentFilerDelegateMock).saveUpdateDokumentFiler(journalpost);
	}

	@Test
	public void shouldUpdateJournalpostIfFerdigstillJournalpostSentralPrint() throws Exception {
		requestTo.setFerdigstillJournalpost(true);
		service.updateJournalpost(journalpost, requestTo);
		assertThat(journalpost.getJournalstatus(), is(JournalStatusCode.FS));
		assertThat(journalpost.getJournalDato(), is(DateProvider.getToday()));
		assertThat(journalpost.getJournalfortAvNavn(), is(requestTo.getEndretAvNavn()));
		assertThat(journalpost.getUtsendingskanal(), is(requestTo.getUtsendingskanal()));
	}

	@Test
	public void shouldUpdateJournalpostIfFerdigstillJournalpostLokalPrint() throws Exception {
		requestTo.setFerdigstillJournalpost(true);
		requestTo.setUtsendingskanal(UtsendingsKanalCode.L);
		service.updateJournalpost(journalpost, requestTo);
		assertThat(journalpost.getJournalstatus(), is(JournalStatusCode.FL));
		assertThat(journalpost.getJournalDato(), is(DateProvider.getToday()));
		assertThat(journalpost.getJournalfortAvNavn(), is(requestTo.getEndretAvNavn()));
		assertThat(journalpost.getUtsendingskanal(), is(requestTo.getUtsendingskanal()));
	}

	@Test
	public void shouldUpdateJournalpostIfNotFerdigstillJournalpost() throws Exception {
		JournalStatusCode journalStatusCode = journalpost.getJournalstatus();
		Date journalDato = journalpost.getJournalDato();
		String journalfortAvNavn = journalpost.getJournalfortAvNavn();

		requestTo.setFerdigstillJournalpost(false);
		service.updateJournalpost(journalpost, requestTo);
		assertThat(journalpost.getJournalstatus(), is(journalStatusCode));
		assertThat(journalpost.getJournalDato(), is(journalDato));
		assertThat(journalpost.getJournalfortAvNavn(), is(journalfortAvNavn));
	}

	@Test
	public void shouldUpdateDokumentInfo() throws Exception {
		service.updateJournalpost(journalpost, requestTo);
		DokumentInfo dokumentInfo = journalpost.findDokumentInfoById(DOKUMENTINFO_ID);
		assertThat(dokumentInfo.getDokumentstatus(), is(DokumentStatusCode.FERDIGSTILT));
		assertThat(dokumentInfo.getDokumentFerdigDato(), is(DateProvider.getToday()));
	}

	@Test
	public void shouldAddFileDetaljerToDokumentInfo() throws Exception {
		service.updateJournalpost(journalpost, requestTo);
		DokumentInfo dokumentInfo = journalpost.findDokumentInfoById(DOKUMENTINFO_ID);
		Set<FilDetaljer> filDetaljer = dokumentInfo.getFildetaljerListe();
		assertTrue(containsVariantFormat(filDetaljer, VariantFormatCode.SLADDET));
		assertTrue(containsVariantFormat(filDetaljer, VariantFormatCode.ORIGINAL));
		assertThat(filDetaljer.size(), is(4));
	}

	@Test
	public void verfiyThatRepositoryExceptionPropagatesToService() throws Exception {
		when(repositoryMock.findById(JOURNALPOST_ID))
				.thenThrow(new RuntimeException("joark repository is not available"));
		expected.expectMessage("joark repository is not available");
		service.oppdaterJournalpostArkiverDokument(requestTo);
	}


	@Test
	public void verifyThatMetaforceInstanceIdIsDeletedForProduksjonVariantIfFerdigstillJournalpost() throws Exception {
		service.updateJournalpost(journalpost, requestTo);
		DokumentInfo dokumentInfo = journalpost.findDokumentInfoById(DOKUMENTINFO_ID);
		for (FilDetaljer filDetaljer : dokumentInfo.getFildetaljerListe()) {
			if (filDetaljer.getVariantFormat().equals(VariantFormatCode.PRODUKSJON)) {
				assertTrue(filDetaljer.getMetaforceInstanceId() == null);
			}
		}
	}

	@Test
	public void verifyThatMetaforceInstanceIdIsNotDeletedForProduksjonVariantIfNotFerdigstillJournalpost() throws Exception {
		requestTo.setFerdigstillJournalpost(false);
		service.updateJournalpost(journalpost, requestTo);
		DokumentInfo dokumentInfo = journalpost.findDokumentInfoById(DOKUMENTINFO_ID);
		for (FilDetaljer filDetaljer : dokumentInfo.getFildetaljerListe()) {
			if (filDetaljer.getVariantFormat().equals(VariantFormatCode.PRODUKSJON)) {
				assertFalse(filDetaljer.getMetaforceInstanceId() == null);
			}
		}
	}

	private boolean containsVariantFormat(Set<FilDetaljer> filDetaljer, VariantFormatCode variantFormatCode) {
		for (FilDetaljer filDetalj : filDetaljer) {
			if (filDetalj.getVariantFormat().equals(variantFormatCode)) {
				return true;
			}
		}
		return false;
	}

	private OppdaterJournalpostArkiverDokumentRequestTo createJournalpostRequestTo(Long journalpostId, Long dokumentinfoId) {
		return OppdaterJournalpostArkiverDokumentRequestTo.builder() // getOppdaterJournalpostArkiverDokumentRequestToBuilder()
				.dokumentInfoId(dokumentinfoId)
				.endretAvNavn(ENDRET_AV_NAVN)
				.fildetaljerSet(addFildetaljerSet())
				.journalpostId(journalpostId)
				.utsendingskanal(UtsendingsKanalCode.NAV_NO)
				.ferdigstillJournalpost(true)
				.build();
	}

	private Set<FilDetaljer> addFildetaljerSet() {
		HashSet<FilDetaljer> set = new HashSet<>();

		FilDetaljer filDetaljer1 = FilDetaljer.builder()
				.variantFormat(VariantFormatCode.SLADDET)
				.filtype(FilTypeCode.RTF)
				.fileContent(DOKUMENTINNHOLD)
				.build();

		FilDetaljer filDetaljer2 = FilDetaljer.builder()
				.variantFormat(VariantFormatCode.ORIGINAL)
				.filtype(FilTypeCode.PDFA)
				.fileContent(DOKUMENTINNHOLD)
				.build();

		FilDetaljer filDetaljer3 = FilDetaljer.builder()
				.variantFormat(VariantFormatCode.PRODUKSJON)
				.filtype(FilTypeCode.PDFA)
				.fileContent(DOKUMENTINNHOLD)
				.build();

		set.add(filDetaljer1);
		set.add(filDetaljer2);
		set.add(filDetaljer3);

		return set;
	}

	private Journalpost createJournalpost(Long journalpostId, Long dokumentinfoId) {
		return getJournalpostBuilder()
				.endretKildeNavn("sd")
				.journalpostId(journalpostId)
				.avsenderMottakerId("***gammelt_fnr***")
				.avsenderMottaker("avsender")
				.brukere(
						getBrukerBuilder()
								.brukerId("***gammelt_fnr***")
								.brukerType(BrukerTypeCode.PERSON).build())
				.saksrelasjon(
						getSaksrelasjonBuilder()
								.sakId("1")
								.fagsystem(FagsystemCode.BID).build())
				.innhold("innhold")
				.journalpostType(JournalpostTypeCode.U)
				.utsendingskanal(UtsendingsKanalCode.ALTINN)
				.fagomrade(FagomradeCode.AAP)
				.opprettetAvNavn(OPPRETTET_AV_NAVN)
				.journalForendeEnhetId("309480dfk")
				.dokumentDato(new Date())
				.land("Norge")
				.dokumentInfoRelasjoner(
						getJournalpostDokumentInfoRelasjonBuilder()
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
								.tilknyttetAvNavn("Tester")
								.dokumentInfo(
										getDokumentInfoBuilder()
												.dokumentInfoId(dokumentinfoId)
												.kategori(
														DokumentKategoriCode.E_BLANKETT)
												.tittel("Brev")
												.dokumenttypeId(
														"dokumenttypeId")
												.sensitivt(SENSITIVT_REQUEST)
												.filDetaljerList(
														getFilDetaljerBuilder()
																.filtype(FilTypeCode.PDF)
																.metaforceInstanceId(METAFORCEINSTANCE_ID)
																.variantFormat(VariantFormatCode.ARKIV)
																.build(),
														getFilDetaljerBuilder()
																.filtype(FilTypeCode.PDF)
																.metaforceInstanceId(METAFORCEINSTANCE_ID)
																.variantFormat(VariantFormatCode.PRODUKSJON)
																.build()
												)
												.build())
								.build())
				.build();

	}
}