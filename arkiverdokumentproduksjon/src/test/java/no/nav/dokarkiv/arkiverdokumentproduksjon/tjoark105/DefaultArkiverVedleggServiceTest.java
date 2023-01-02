package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark105;

import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.journalbehandling.DokumentFilerDelegate;
import no.nav.dokarkiv.core.repository.JournalpostRepositorySkjermet;
import no.nav.dokarkiv.core.sporing.SporingPopulator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DefaultArkiverVedleggServiceTest {

	private static final Long JOURNALPOST_ID = 12L;
	private static final Long DOKUMENT_INFO_ID = 11L;
	private static final String DOKUMENT_TYPE_ID = "123";
	private static final String BREVKODE = "B01";
	private static final boolean SENSITIVT = false;
	private static final String TITTEL = "tittel";
	private static final String ENDRET_AV_NAVN = "b134122";
	private static final DokumentKategoriCode KATEGORI = DokumentKategoriCode.B;
	public static final String GENERATED_FILUUID = UUID.randomUUID().toString();

	@Mock
    private JournalpostRepositorySkjermet repositoryMock;

	@Mock
	private ArkiverVedleggValidator arkiverVedleggValidatorMock;

	@Mock
	private DokumentFilerDelegate dokumentFilerDelegateMock;

	@Mock
	private SporingPopulator sporingPopulatorMock;

	@InjectMocks
	private DefaultArkiverVedleggService service;

	private Journalpost journalpost;

	@BeforeEach
	public void setUp() {
		journalpost = createJournalpost();
		when(repositoryMock.findById(JOURNALPOST_ID)).thenReturn(Optional.of(journalpost));
		when(repositoryMock.save(any(Journalpost.class))).thenReturn(journalpost);
	}

	@Test
	public void shouldRunArkiverVedlegg() throws Exception {
		ArkiverVedleggResponseTo responseTo = service.arkiverVedlegg(createRequest(false));
		assertThat(responseTo.getJournalpostId(), is(JOURNALPOST_ID));
		assertThat(responseTo.getDokumentInfoId(), is(DOKUMENT_INFO_ID));
	}

	@Test
	public void shouldPopulateJournalpostAndDokumentInfo() throws Exception {
		service.arkiverVedlegg(createRequest(false));
		assertThat(journalpost.getJournalpostDokumentInfoRelasjoner().size(), is(1));
		DokumentInfo dokumentInfo = journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getDokumentInfo();
		assertThat(dokumentInfo.getDokumenttypeId(), is(DOKUMENT_TYPE_ID));
		assertThat(dokumentInfo.getBrevkode(), is(BREVKODE));
		assertThat(dokumentInfo.getKategori(), is(KATEGORI));
		assertThat(dokumentInfo.getTittel(), is(TITTEL));
		assertThat(dokumentInfo.getSensitivt(), is(SENSITIVT));
		assertThat(dokumentInfo.getDokumentstatus(), is(DokumentStatusCode.UNDER_REDIGERING));
		assertNull(dokumentInfo.getDokumentFerdigDato());

	}

	@Test
	public void shouldFerdigstilleDokument() throws Exception {
		ArkiverVedleggRequestTo request = createRequest(true);
		request.setFerdigstillDokument(true);
		service.arkiverVedlegg(request);
		DokumentInfo dokumentInfo = journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getDokumentInfo();
		assertThat(dokumentInfo.getDokumentstatus(), is(DokumentStatusCode.FERDIGSTILT));
		assertNotNull(dokumentInfo.getDokumentFerdigDato());
	}

	private ArkiverVedleggRequestTo createRequest(boolean ferdigstillDokument) {
		ArkiverVedleggRequestTo requestTo = new ArkiverVedleggRequestTo();
		requestTo.setEndretAvNavn(ENDRET_AV_NAVN);
		requestTo.setJournalpostId(JOURNALPOST_ID);
		requestTo.setFerdigstillDokument(ferdigstillDokument);
		DokumentInfo dokumentInfo = new DokumentInfo(DOKUMENT_INFO_ID, 1);
		dokumentInfo.setKategori(KATEGORI);
		dokumentInfo.setBrevkode(BREVKODE);
		dokumentInfo.setDokumenttypeId(DOKUMENT_TYPE_ID);
		dokumentInfo.setSensitivt(SENSITIVT);
		dokumentInfo.setTittel(TITTEL);
		dokumentInfo.addFilDetaljer(FilDetaljer.builder().filUuid(GENERATED_FILUUID).build());
		requestTo.setDokumentInfo(dokumentInfo);
		return requestTo;
	}

	private Journalpost createJournalpost() {

		return getJournalpostBuilder()
				.journalpostId(JOURNALPOST_ID)
				.build();
	}

}