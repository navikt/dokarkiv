package no.nav.dokarkiv.dokumentproduksjoninfo.tjoark121;

import static no.nav.dokarkiv.core.domain.builder.DokumentFilBuilder.getDokumentFilBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static no.nav.dokarkiv.core.domain.codes.DokumentStatusCode.FERDIGSTILT;
import static no.nav.dokarkiv.core.domain.codes.DokumentStatusCode.UNDER_REDIGERING;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.FS;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import no.nav.dokarkiv.core.domain.Journalpost;
import no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder;
import no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.dokumentproduksjoninfo.AbstractDokumentproduksjoninfoItest;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.HentFerdigstilteDokumenterDokumenterIkkeFunnet;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.HentFerdigstilteDokumenterUgyldingInput;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.informasjon.Dokument;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.meldinger.HentFerdigstilteDokumenterRequest;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.meldinger.HentFerdigstilteDokumenterResponse;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;

/**
 * Integration test for HentFerdigstilteDokumenter.
 *
 * @author Stig Strøm, Antares
 */
public class HentFerdigstilteDokumenterIT extends AbstractDokumentproduksjoninfoItest {

	private static final String FILUUID = "355b166e-5f9f-430f-8e35-09a732156776";
	private static final byte[] FIL_AS_BYTE = "fil".getBytes();

	private Long journalpostId;
	private Long dokumentInfoId;
	
	private HentFerdigstilteDokumenterRequest request;
	
	@Before
	public void setUp() {
		Journalpost journalpost = buildAndPersistJournalpost(FILUUID, FS, FERDIGSTILT);
		journalpostId = journalpost.getId();
		dokumentInfoId = journalpost.findAllDokumentInfos().iterator().next().getId();
		
		dokumentFilRepository.save(getDokumentFilBuilder()
			.filUuid(FILUUID)
			.fil(FIL_AS_BYTE)
			.opprettetKildeNavn("Kent Clark")
			.build());
		
		createRequest(journalpostId, dokumentInfoId);
	}

	@Test
	public void shouldHentFerdigstilteDokumenter() throws Exception {
		HentFerdigstilteDokumenterResponse wsResponse = dokumentproduksjonInfoProvider.hentFerdigstilteDokumenter(request);
		assertThat(wsResponse.getDokumentListe().size(), is(1));
		assertThat(wsResponse.getDokumentListe().get(0).getDokumentInfoId(), is(dokumentInfoId));
//		assertThat(getFil(wsResponse.getDokumentListe().get(0)), is(FIL_AS_BYTE)); FIXME
	}
	
	@Test
	public void shouldThrowException_inputRequestIsNull() throws Exception {
		expectedException.expect(HentFerdigstilteDokumenterUgyldingInput.class);
		expectedException.expectMessage("request is null");
		dokumentproduksjonInfoProvider.hentFerdigstilteDokumenter(null);
	}
	
	@Test
	public void shouldThrowException_dokumentIsMissing() throws Exception {
		expectedException.expect(HentFerdigstilteDokumenterDokumenterIkkeFunnet.class);
		expectedException.expectMessage("Fildetaljer ikke funnet");
		
		Journalpost journalpost = buildAndPersistJournalpost("dokument_eksisterer_ikke", FS, FERDIGSTILT);
		journalpostId = journalpost.getId();
		dokumentInfoId = journalpost.findAllDokumentInfos().iterator().next().getId();
		createRequest(journalpostId, dokumentInfoId);
		
		dokumentproduksjonInfoProvider.hentFerdigstilteDokumenter(request);
	}
	
	@Test
	public void shouldThrowException_dokumentNotBelongingToJournalpost() throws Exception {
		expectedException.expectMessage("dokumentInfoId=56 hører ikke til journalpost");
		createRequest(journalpostId, dokumentInfoId, 56L);
		
		dokumentproduksjonInfoProvider.hentFerdigstilteDokumenter(request);
	}
	
	@Test
	public void shouldThrowException_invalidJournalStatus() throws Exception {
		expectedException.expectMessage("forventet JournalStatus FS, men har journalStatus=J");
		Journalpost journalpost = buildAndPersistJournalpost("dokument_eksisterer_ikke", JournalStatusCode.J, FERDIGSTILT);
		journalpostId = journalpost.getId();
		dokumentInfoId = journalpost.findAllDokumentInfos().iterator().next().getId();
		createRequest(journalpostId, dokumentInfoId);
		
		dokumentproduksjonInfoProvider.hentFerdigstilteDokumenter(request);
	}
	
	@Test
	public void shouldThrowException_dokumentIsNotFerdigstilt() throws Exception {
		expectedException.expectMessage("er ikke ferdigstilt");
		Journalpost journalpost = buildAndPersistJournalpost("dokument_eksisterer_ikke", FS, UNDER_REDIGERING);
		journalpostId = journalpost.getId();
		dokumentInfoId = journalpost.findAllDokumentInfos().iterator().next().getId();
		createRequest(journalpostId, dokumentInfoId);
		
		dokumentproduksjonInfoProvider.hentFerdigstilteDokumenter(request);
	}
	
	
	private byte[] getFil(Dokument dokument) throws Exception {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		dokument.getFil().writeTo(output);
		return output.toByteArray();
	}
	
	private Journalpost buildAndPersistJournalpost(String filuuid, JournalStatusCode journalStatus, DokumentStatusCode dokumentStatus ) {
		Journalpost journalpost = getJournalpostBuilder()
				.journalStatus(journalStatus)
				.journalpostType(JournalpostTypeCode.U)
				.opprettetAvNavn("testuser")
				.opprettetKildeNavn("test")
				.dokumentInfoRelasjoner(
						getJournalpostDokumentInfoRelasjonBuilder()
								.tilknyttetAvNavn("testuser")
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
								.opprettetKildeNavn("test")
								.dokumentInfo(DokumentInfoBuilder.getDokumentInfoBuilder()
										.opprettetKildeNavn("test")
										.dokumentstatus(dokumentStatus)
										.filDetaljerList(FilDetaljerBuilder.getFilDetaljerBuilder()
												.filtype(FilTypeCode.PDF)
												.filUuid(filuuid)
												.variantFormat(VariantFormatCode.ARKIV)
												.opprettetKildeNavn("test")
												.build())
										.build())
								.build())
				.build();
		joarkRepository.save(journalpost);
		return journalpost;
	}	
	
	private void createRequest(Long journalpostId, Long... dokumentInfoId) {
		request = new HentFerdigstilteDokumenterRequest();
		request.setJournalpostId(journalpostId);
		for (Long dokumentInfo : dokumentInfoId) {
			request.getDokumentInfoListe().add(dokumentInfo);
		}
	}
	
}
