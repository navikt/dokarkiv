package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark102;

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
import no.nav.dokarkiv.core.journalbehandling.DokumentFilerDelegate;
import no.nav.dokarkiv.core.repository.JournalpostRepositorySkjermet;
import no.nav.dokarkiv.core.sporing.SporingPopulator;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static java.time.temporal.ChronoUnit.SECONDS;
import static no.nav.dokarkiv.core.domain.builder.BrukerBuilder.getBrukerBuilder;
import static no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder.getDokumentInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder.getFilDetaljerBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static no.nav.dokarkiv.core.domain.builder.SaksrelasjonBuilder.getSaksrelasjonBuilder;
import static org.assertj.core.api.Assertions.within;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DefaultOppdaterJournalpostArkiverDokumentServiceTest {

	private static final Long JOURNALPOST_ID = 42L;
	private static final Long DOKUMENTINFO_ID = 56L;
	private static final boolean SENSITIVT_REQUEST = true;
	private static final String OPPRETTET_AV_NAVN = "Saksbehandler2";
	private static final String ENDRET_AV_NAVN = "Saksbehandler1";
	private static final byte[] DOKUMENTINNHOLD = "DOKUMENT".getBytes();
	private static final Long METAFORCEINSTANCE_ID = 37984L;

	private Journalpost journalpost;
	private OppdaterJournalpostArkiverDokumentRequestTo requestTo;

	@Mock
	private JournalpostRepositorySkjermet repositoryMock;
	@Mock
	private DokumentFilerDelegate dokumentFilerDelegateMock;
	@Mock
	private OppdaterJournalpostArkiverDokumentValidator validatorMock;
	@Mock
	private SporingPopulator sporingPopulatorMock;
	@InjectMocks
	DefaultOppdaterJournalpostArkiverDokumentService service;

	@BeforeEach
	public void setUp() {
		requestTo = createJournalpostRequestTo(JOURNALPOST_ID, DOKUMENTINFO_ID);
		journalpost = createJournalpost(JOURNALPOST_ID, DOKUMENTINFO_ID);
	}

	@Test
	public void shouldNotArkiverDokumentOgFerdigstillJournalpostOrThrowException() {
		when(repositoryMock.findById(JOURNALPOST_ID)).thenReturn(Optional.of(journalpost));

		service.oppdaterJournalpostArkiverDokument(requestTo);
		verify(validatorMock).validate(journalpost, requestTo);
		verify(sporingPopulatorMock).populateSporingInfo(journalpost, requestTo.getEndretAvNavn());
		verify(dokumentFilerDelegateMock).saveUpdateDokumentFiler(journalpost);
	}

	@Test
	public void shouldUpdateJournalpostIfFerdigstillJournalpostSentralPrint() {
		requestTo.setFerdigstillJournalpost(true);
		service.updateJournalpost(journalpost, requestTo);
		assertThat(journalpost.getJournalstatus(), is(JournalStatusCode.FS));
		Assertions.assertThat(journalpost.getJournalDato()).isCloseTo(LocalDateTime.now(), within(3, SECONDS));
		assertThat(journalpost.getJournalfortAvNavn(), is(requestTo.getEndretAvNavn()));
		assertThat(journalpost.getUtsendingskanal(), is(requestTo.getUtsendingskanal()));
	}

	@Test
	public void shouldUpdateJournalpostIfFerdigstillJournalpostLokalPrint() {
		requestTo.setFerdigstillJournalpost(true);
		requestTo.setUtsendingskanal(UtsendingsKanalCode.L);
		service.updateJournalpost(journalpost, requestTo);
		assertThat(journalpost.getJournalstatus(), is(JournalStatusCode.FL));
		Assertions.assertThat(journalpost.getJournalDato()).isCloseTo(LocalDateTime.now(), within(3, SECONDS));
		assertThat(journalpost.getJournalfortAvNavn(), is(requestTo.getEndretAvNavn()));
		assertThat(journalpost.getUtsendingskanal(), is(requestTo.getUtsendingskanal()));
	}

	@Test
	public void shouldUpdateJournalpostIfNotFerdigstillJournalpost() {
		JournalStatusCode journalStatusCode = journalpost.getJournalstatus();
		LocalDateTime journalDato = journalpost.getJournalDato();
		String journalfortAvNavn = journalpost.getJournalfortAvNavn();

		requestTo.setFerdigstillJournalpost(false);
		service.updateJournalpost(journalpost, requestTo);
		assertThat(journalpost.getJournalstatus(), is(journalStatusCode));
		assertThat(journalpost.getJournalDato(), is(journalDato));
		assertThat(journalpost.getJournalfortAvNavn(), is(journalfortAvNavn));
	}

	@Test
	public void shouldUpdateDokumentInfo() {
		service.updateJournalpost(journalpost, requestTo);
		DokumentInfo dokumentInfo = journalpost.findDokumentInfoById(DOKUMENTINFO_ID);
		assertThat(dokumentInfo.getDokumentstatus(), is(DokumentStatusCode.FERDIGSTILT));
		Assertions.assertThat(dokumentInfo.getDokumentFerdigDato()).isCloseTo(LocalDateTime.now(), within(3, SECONDS));
	}

	@Test
	public void shouldAddFileDetaljerToDokumentInfo() {
		service.updateJournalpost(journalpost, requestTo);
		DokumentInfo dokumentInfo = journalpost.findDokumentInfoById(DOKUMENTINFO_ID);
		Set<FilDetaljer> filDetaljer = dokumentInfo.getFildetaljerListeAdmin();
		assertTrue(containsVariantFormat(filDetaljer, VariantFormatCode.SLADDET));
		assertTrue(containsVariantFormat(filDetaljer, VariantFormatCode.ORIGINAL));
		assertThat(filDetaljer.size(), is(4));
	}

	@Test
	public void verfiyThatRepositoryExceptionPropagatesToService() {
		when(repositoryMock.findById(JOURNALPOST_ID))
				.thenThrow(new RuntimeException("joark repository is not available"));

		assertThrows(RuntimeException.class,
				() -> service.oppdaterJournalpostArkiverDokument(requestTo),
				"joark repository is not available");
	}


	@Test
	public void verifyThatMetaforceInstanceIdIsDeletedForProduksjonVariantIfFerdigstillJournalpost() {
		service.updateJournalpost(journalpost, requestTo);
		DokumentInfo dokumentInfo = journalpost.findDokumentInfoById(DOKUMENTINFO_ID);
		for (FilDetaljer filDetaljer : dokumentInfo.getFildetaljerListe()) {
			if (filDetaljer.getVariantFormat().equals(VariantFormatCode.PRODUKSJON)) {
				assertTrue(filDetaljer.getMetaforceInstanceId() == null);
			}
		}
	}

	@Test
	public void verifyThatMetaforceInstanceIdIsNotDeletedForProduksjonVariantIfNotFerdigstillJournalpost() {
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
				.avsenderMottakerId("01054512313")
				.avsenderMottaker("avsender")
				.brukere(
						getBrukerBuilder()
								.brukerId("01054512313")
								.brukerType(BrukerTypeCode.PERSON).build())
				.saksrelasjon(
						getSaksrelasjonBuilder()
								.sakId(1L)
								.fagsystem(FagsystemCode.FS22).build())
				.innhold("innhold")
				.journalpostType(JournalpostTypeCode.U)
				.utsendingskanal(UtsendingsKanalCode.EESSI)
				.fagomrade(FagomradeCode.AAP)
				.opprettetAvNavn(OPPRETTET_AV_NAVN)
				.journalForendeEnhetId("309480dfk")
				.dokumentDato(LocalDateTime.now())
				.land("Norge")
				.dokumentInfoRelasjoner(
						getJournalpostDokumentInfoRelasjonBuilder()
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
								.tilknyttetAvNavn("Tester")
								.dokumentInfo(
										getDokumentInfoBuilder()
												.dokumentInfoId(dokumentinfoId)
												.kategori(
														DokumentKategoriCode.SED)
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