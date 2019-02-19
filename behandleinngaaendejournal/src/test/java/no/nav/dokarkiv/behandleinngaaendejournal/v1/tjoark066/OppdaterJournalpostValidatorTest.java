package no.nav.dokarkiv.behandleinngaaendejournal.v1.tjoark066;

import static no.nav.dokarkiv.behandleinngaaendejournal.v1.BehandleInngaaendeJournalDataProvider.buildJournalpost;

import no.nav.dokarkiv.behandleinngaaendejournal.v1.BehandleInngaaendeJournalDataProvider;
import no.nav.dokarkiv.behandleinngaaendejournal.v1.exceptions.DokumentInfoIkkeTilknyttetJournalpostException;
import no.nav.dokarkiv.behandleinngaaendejournal.v1.exceptions.OppdaterJournalpostIkkeMuligException;
import no.nav.dokarkiv.behandleinngaaendejournal.v1.tjoark066.to.AktoerTo;
import no.nav.dokarkiv.behandleinngaaendejournal.v1.tjoark066.to.ArkivSakTo;
import no.nav.dokarkiv.behandleinngaaendejournal.v1.tjoark066.to.AvsenderTo;
import no.nav.dokarkiv.behandleinngaaendejournal.v1.tjoark066.to.DokumentInformasjonTo;
import no.nav.dokarkiv.behandleinngaaendejournal.v1.tjoark066.to.OppdaterJournalpostRequestTo;
import no.nav.dokarkiv.behandleinngaaendejournal.v1.tjoark066.to.OppdaterJournalpostTo;
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeInngaaendeException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeMidlertidigException;
import no.nav.dokarkiv.core.exceptions.UgyldigInputException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

/**
 * Test for {@link OppdaterJournalpostValidator}
 *
 * @author Leo-Andreas Ervik, Visma Consulting. 02.06.2017.
 */
@RunWith(MockitoJUnitRunner.class)
public class OppdaterJournalpostValidatorTest {

	@Rule
	public ExpectedException expected = ExpectedException.none();
	
	private static final String ENDRINGSSPORING = "E149028";
	private static final String JOURNALPOST_ID = "1";
	
	private OppdaterJournalpostValidator validator;
	private OppdaterJournalpostRequestTo requestTo;
	private Journalpost journalpost;
	
	@Before
	public void setUp() throws Exception {
		validator = new OppdaterJournalpostValidator();
		requestTo = createRequest();
		journalpost = buildJournalpost().build();
	}
	
	@Test
	public void shouldValidate() throws Exception {
		validator.validateInput(requestTo);
		validator.validateJournalpost(journalpost, requestTo.getOppdaterJournalpostTo());
	}
	
	@Test
	public void shouldFailOnMissingInput() throws Exception {
		expected.expect(UgyldigInputException.class);
		expected.expectMessage("Tjenesten kan ikke utføres fordi OppdaterJournalpost i kallet er null.");
		
		requestTo.setOppdaterJournalpostTo(null);
		validator.validateInput(requestTo);
	}
	
	@Test
	public void shouldFailOnInputMissingJournalpostId() throws Exception {
		expected.expect(UgyldigInputException.class);
		expected.expectMessage("Tjenesten kan ikke utføres fordi input er ugyldig. journalpostId=null");
		
		requestTo.getOppdaterJournalpostTo().setJournalpostId(null);
		validator.validateInput(requestTo);
	}
	
	@Test
	public void shouldFailOnWrongJournalpostIdFormat() throws Exception {
		expected.expect(UgyldigInputException.class);
		expected.expectMessage("JournalpostId må være et nummer. journalpostId=");
		
		requestTo.getOppdaterJournalpostTo().setJournalpostId("nan");
		validator.validateInput(requestTo);
	}
	
	@Test
	public void shouldFailInMissingInputAktoerId() throws Exception {
		expected.expect(UgyldigInputException.class);
		expected.expectMessage("Mangler informasjon på Aktoer for oppdatering av journalpost. journalpostId=");
		
		requestTo.getOppdaterJournalpostTo().getAktoerTo().setAktoerId(null);
		validator.validateInput(requestTo);
	}
	
	@Test
	public void shouldFailOnMissingInputAktoerBrukertypeCode() throws Exception {
		expected.expect(UgyldigInputException.class);
		expected.expectMessage("Mangler informasjon på Aktoer for oppdatering av journalpost. journalpostId=");
		
		requestTo.getOppdaterJournalpostTo().getAktoerTo().setBrukerTypeCode(null);
		validator.validateInput(requestTo);
	}
	
	@Test
	public void shouldFailOnMissingInputHoveddokumentId() throws Exception {
		expected.expect(UgyldigInputException.class);
		expected.expectMessage("Mangler informasjon på Hoveddokument for oppdatering av journalpost. journalpostId=");
		
		requestTo.getOppdaterJournalpostTo().getHoveddokument().setDokumentId(null);
		validator.validateInput(requestTo);
	}
	
	@Test
	public void shouldFailOnMissingInputVedleggId() throws Exception {
		expected.expect(UgyldigInputException.class);
		expected.expectMessage("Mangler informasjon på Vedlegg for oppdatering av journalpost. journalpostId=");
		
		requestTo.getOppdaterJournalpostTo().getVedlegg().get(0).setDokumentId(null);
		validator.validateInput(requestTo);
	}
	
	@Test
	public void shouldFailOnMissingJournalpost() throws Exception {
		expected.expect(JournalpostIkkeFunnetException.class);
		expected.expectMessage("Journalpost ikke funnet. journalpostId=");
		
		journalpost = null;
		validator.validateJournalpost(journalpost, requestTo.getOppdaterJournalpostTo());
	}
	
	@Test
	public void shouldFailOnJournalpostHoveddokumentIdMismatch() throws Exception {
		expected.expect(DokumentInfoIkkeTilknyttetJournalpostException.class);
		expected.expectMessage("Innsendt hoveddokument er ikke knyttet til journalposten. journalpostId=");
		
		requestTo.getOppdaterJournalpostTo().getHoveddokument().setDokumentId(4845133L);
		validator.validateJournalpost(journalpost, requestTo.getOppdaterJournalpostTo());
	}
	
	@Test
	public void shouldFailOnJournalpostVedleggIdMismatch() throws Exception {
		expected.expect(DokumentInfoIkkeTilknyttetJournalpostException.class);
		expected.expectMessage("Ett eller flere innsendte vedlegg er ikke knyttet til journalposten. journalpostId=");
		
		requestTo.getOppdaterJournalpostTo().setHoveddokument(null);
		requestTo.getOppdaterJournalpostTo().getVedlegg().get(0).setDokumentId(18602143L);
		validator.validateJournalpost(journalpost, requestTo.getOppdaterJournalpostTo());
	}
	
	@Test
	public void shouldFailOnJournalpostNotInngaaende() throws Exception {
		expected.expect(JournalpostIkkeInngaaendeException.class);
		expected.expectMessage("Journalpost er ikke av type Inngående. journalpostId=");
		
		journalpost.setJournalposttype(JournalpostTypeCode.U);
		validator.validateJournalpost(journalpost, requestTo.getOppdaterJournalpostTo());
	}
	
	@Test
	public void shouldFailOnJournalpostNotMidlertidig() throws Exception {
		expected.expect(JournalpostIkkeMidlertidigException.class);
		expected.expectMessage("Journalpost er ikke av status Midlertidig. Status=");
		
		journalpost.setJournalstatus(JournalStatusCode.U);
		validator.validateJournalpost(journalpost, requestTo.getOppdaterJournalpostTo());
	}
	
	@Test
	public void shouldFailOnJournalpostFeilregistrert() throws Exception {
		expected.expect(OppdaterJournalpostIkkeMuligException.class);
		expected.expectMessage("Journalpost saksrelasjon er markert som feilregistrert. journalpostId=");
		
		journalpost.getSaksrelasjon().setFeilregistrert(true);
		validator.validateJournalpost(journalpost, requestTo.getOppdaterJournalpostTo());
	}
	
	@Test
	public void shouldFailOnJournalpostDokumentInfoUnderRedigering() throws Exception {
		expected.expect(OppdaterJournalpostIkkeMuligException.class);
		expected.expectMessage("Dokument har ugyldig status for oppdatering. dokumentStatus=");
		
		journalpost.findAllDokumentInfos().get(0).setDokumentstatus(DokumentStatusCode.UNDER_REDIGERING);
		validator.validateJournalpost(journalpost, requestTo.getOppdaterJournalpostTo());
	}
	
	@Test
	public void shouldFailOnJournalpostDokumentInfoAvbrutt() throws Exception {
		expected.expect(OppdaterJournalpostIkkeMuligException.class);
		expected.expectMessage("Dokument har ugyldig status for oppdatering. dokumentStatus=");
		
		journalpost.findAllDokumentInfos().get(0).setDokumentstatus(DokumentStatusCode.AVBRUTT);
		validator.validateJournalpost(journalpost, requestTo.getOppdaterJournalpostTo());
	}
	
	private OppdaterJournalpostRequestTo createRequest() {
		return OppdaterJournalpostRequestTo.builder()
				.endringssporing(ENDRINGSSPORING)
				.oppdaterJournalpostTo(
						OppdaterJournalpostTo.builder()
								.journalpostId(JOURNALPOST_ID)
								.avsenderTo(
										AvsenderTo.builder()
												.avsenderId(BehandleInngaaendeJournalDataProvider.AVSENDER_MOTTAKERID)
												.avsenderNavn(BehandleInngaaendeJournalDataProvider.AVSENDER_MOTTAKER_NAVN)
												.build()
								)
								.innhold(BehandleInngaaendeJournalDataProvider.INNHOLD)
								.arkivSak(
										ArkivSakTo.builder()
												.arkivSakId(BehandleInngaaendeJournalDataProvider.ARKIV_SAKID)
												.arkivSakSystem(BehandleInngaaendeJournalDataProvider.ARKIV_SAK_FAGSYSTEM)
												.build()
								)
								.tema(BehandleInngaaendeJournalDataProvider.JOURNALPOST_FAGOMRADE)
								.aktoerTo(
										AktoerTo.builder()
												.aktoerId(BehandleInngaaendeJournalDataProvider.ORGNR)
												.brukerTypeCode(BrukerTypeCode.ORGANISASJON)
												.build()
								)
								.hoveddokument(
										DokumentInformasjonTo.builder()
												.dokumentId(BehandleInngaaendeJournalDataProvider.DOKUMENT_INFO_ID)
												.dokumentkategori(BehandleInngaaendeJournalDataProvider.HOVEDDOKUMENT_KATEGORI_KODE)
												.tittel(BehandleInngaaendeJournalDataProvider.TITTEL)
												.build()
								)
								.vedlegg(
										Collections.singletonList(
												DokumentInformasjonTo.builder()
														.dokumentId(BehandleInngaaendeJournalDataProvider.DOKUMENT_INFO_ID_VEDLEGG)
														.dokumentkategori(BehandleInngaaendeJournalDataProvider.VEDLEGG_KATEGORI_KODE)
														.tittel(BehandleInngaaendeJournalDataProvider.TITTEL)
														.build()
										)
								)
								.build()
				)
				.build();
	}
}