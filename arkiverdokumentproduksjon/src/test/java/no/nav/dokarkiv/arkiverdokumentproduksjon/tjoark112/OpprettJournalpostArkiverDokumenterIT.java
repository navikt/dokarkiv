package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark112;

import no.nav.dokarkiv.arkiverdokumentproduksjon.AbstractArkiverdokumentproduksjonItest;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.exceptions.ApplicationException;
import no.nav.dokarkiv.core.exceptions.DokarkivTechnicalException;
import no.nav.dokarkiv.core.exceptions.InvalidArgumentException;
import no.nav.dokarkiv.core.storage.BucketStorage;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.arkiverdokumentproduksjon.JournalpostType;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.OpprettJournalpostArkiverDokumenterRequest;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.OpprettJournalpostArkiverDokumenterResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.xml.datatype.DatatypeFactory;
import java.util.Optional;

import static no.nav.dokarkiv.arkiverdokumentproduksjon.ArkiverDokumentproduksjonConstants.BESTILLINGS_ID_KEY;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.ArkiverDokumentproduksjonConstants.FILREFERANSE_ID_KEY;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark112.OpprettJournalpostArkiverDokumenterDataUtil.DOKUMENT_INNHOLD_BASE64;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark112.OpprettJournalpostArkiverDokumenterDataUtil.FILREFERANSE_GCS;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark112.OpprettJournalpostArkiverDokumenterDataUtil.createJournalpost;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark112.OpprettJournalpostArkiverDokumenterDataUtil.createTilleggsopplysning;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class OpprettJournalpostArkiverDokumenterIT extends AbstractArkiverdokumentproduksjonItest {

	@Autowired
	private BucketStorage dokprodMellomlagerStorage;

	private no.nav.dokarkiv.core.domain.entities.Journalpost persistedJournalpost;

	@BeforeEach
	public void setUp() throws Exception {
		when(dokprodMellomlagerStorage.downloadObject(eq(FILREFERANSE_GCS), anyString())).thenReturn((Optional.of("""
				{
				  "axml" : "%s",
				  "pdf": "%s"
				}
				""".formatted(DOKUMENT_INNHOLD_BASE64, DOKUMENT_INNHOLD_BASE64))));
	}

	@Test
	public void shouldVerifyResponseHasJournalpostAndDokumentId() {
		OpprettJournalpostArkiverDokumenterResponse response = arkiverDokumentproduksjonProvider.opprettJournalpostArkiverDokumenter(createRequest());
		assertThat(response.getJournalpostId(), is(notNullValue()));
		assertThat(response.getDokumentInfoIdMap(), hasSize(2));
		assertThat(response.getDokumentInfoIdMap().get(0).getFilreferanse(), is(FILREFERANSE_GCS));
		assertThat(response.getDokumentInfoIdMap().get(0).getDokumentInfoId(), notNullValue());
		assertThat(response.getDokumentInfoIdMap().get(1).getFilreferanse(), is(FILREFERANSE_GCS));
		assertThat(response.getDokumentInfoIdMap().get(1).getDokumentInfoId(), notNullValue());
	}

	@Test
	public void shouldVerifyCorrectFieldsInJournalpost() {
		OpprettJournalpostArkiverDokumenterResponse response = arkiverDokumentproduksjonProvider.opprettJournalpostArkiverDokumenter(createRequest());
		persistedJournalpost = journalpostTestRepository.findById(response.getJournalpostId()).get();
		OpprettJournalpostArkiverDokumenterAssertUtil.assertEqualJournalposts(persistedJournalpost);
	}

	@Test
	public void shouldVerifyEqualResponseWhenTryingToJournalforSameRequestTwice() {
		OpprettJournalpostArkiverDokumenterResponse firstResponse = arkiverDokumentproduksjonProvider.opprettJournalpostArkiverDokumenter(createRequest());
		OpprettJournalpostArkiverDokumenterResponse secondResponse = arkiverDokumentproduksjonProvider.opprettJournalpostArkiverDokumenter(createRequest());

		assertThat(firstResponse, is(equalTo(secondResponse)));
	}

	@Test
	public void shouldVerifyNotEqualResponseWhenTryingToJournalforSameRequestTwiceAndIsDifferentBestillingId() {
		OpprettJournalpostArkiverDokumenterRequest firstRequest = createRequest();
		firstRequest.getJournalpost().getDokumentInfoHoveddokument().getTilleggsopplysninger().clear();
		firstRequest.getJournalpost().getDokumentInfoHoveddokument().getTilleggsopplysninger().add(createTilleggsopplysning(BESTILLINGS_ID_KEY, "test"));
		firstRequest.getJournalpost().getDokumentInfoHoveddokument().getTilleggsopplysninger().add(createTilleggsopplysning(FILREFERANSE_ID_KEY, FILREFERANSE_GCS));
		OpprettJournalpostArkiverDokumenterResponse firstResponse = arkiverDokumentproduksjonProvider.opprettJournalpostArkiverDokumenter(firstRequest);
		OpprettJournalpostArkiverDokumenterRequest secondRequest = createRequest();
		OpprettJournalpostArkiverDokumenterResponse secondResponse = arkiverDokumentproduksjonProvider.opprettJournalpostArkiverDokumenter(secondRequest);

		assertThat(firstResponse, is(not(equalTo(secondResponse))));
	}

	@Test
	public void shouldThrowExceptionIfDatoDokumentIsNull() {
		OpprettJournalpostArkiverDokumenterRequest request = createRequest();
		request.getJournalpost().setDatoDokument(null);

		assertThrows(ApplicationException.class,
				() -> arkiverDokumentproduksjonProvider.opprettJournalpostArkiverDokumenter(request),
				"DatoDokument must be set");
	}

	@Test
	public void shouldVerifyDatoDokument() throws Exception {
		OpprettJournalpostArkiverDokumenterRequest request = createRequest();
		request.getJournalpost().setDatoDokument(DatatypeFactory.newInstance().newXMLGregorianCalendar("2015-01-28"));
		arkiverDokumentproduksjonProvider.opprettJournalpostArkiverDokumenter(request);
	}

	@Test
	public void shouldThrowExceptionIfRequestDoesNotValidate() {
		OpprettJournalpostArkiverDokumenterRequest request = createRequest();
		request.getJournalpost().setFagomrade(null);

		assertThrows(InvalidArgumentException.class,
				() -> arkiverDokumentproduksjonProvider.opprettJournalpostArkiverDokumenter(request),
				"Journalpost.fagomrade must be set");
	}

	@Test
	public void shouldThrowExceptionIfRequestIsMissingDokumentFilReferanse() {
		OpprettJournalpostArkiverDokumenterRequest request = createRequest();
		request.getJournalpost().getDokumentInfoHoveddokument().getTilleggsopplysninger().clear();

		assertThrows(DokarkivTechnicalException.class,
				() -> arkiverDokumentproduksjonProvider.opprettJournalpostArkiverDokumenter(request));
	}

	@Test
	public void shouldRunWithJournalpostTypeN() {
		OpprettJournalpostArkiverDokumenterRequest request = createRequest();
		request.getJournalpost().setJournalpostType(JournalpostType.N);
		OpprettJournalpostArkiverDokumenterResponse response = arkiverDokumentproduksjonProvider.opprettJournalpostArkiverDokumenter(request);
		persistedJournalpost = journalpostTestRepository.findById(response.getJournalpostId()).get();
		assertThat(persistedJournalpost.getJournalposttype(), is(JournalpostTypeCode.N));
	}

	@Test
	public void shouldRunWithNullJournalpostType() {
		OpprettJournalpostArkiverDokumenterRequest request = createRequest();
		request.getJournalpost().setJournalpostType(null);
		OpprettJournalpostArkiverDokumenterResponse response = arkiverDokumentproduksjonProvider.opprettJournalpostArkiverDokumenter(request);
		persistedJournalpost = journalpostTestRepository.findById(response.getJournalpostId()).get();

		assertThat(persistedJournalpost.getJournalposttype(), is(JournalpostTypeCode.U));
	}

	private OpprettJournalpostArkiverDokumenterRequest createRequest() {
		OpprettJournalpostArkiverDokumenterRequest request = new OpprettJournalpostArkiverDokumenterRequest();
		request.setJournalpost(createJournalpost());
		return request;
	}
}
