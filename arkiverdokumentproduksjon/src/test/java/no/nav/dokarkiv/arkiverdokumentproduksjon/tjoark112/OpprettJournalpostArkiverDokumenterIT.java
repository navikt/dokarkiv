package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark112;

import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark112.OpprettJournalpostArkiverDokumenterDataUtil.DOKUMENT_INNHOLD_BASE64;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark112.OpprettJournalpostArkiverDokumenterDataUtil.FILREFERANSE_S3;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark112.OpprettJournalpostArkiverDokumenterDataUtil.createJournalpost;
import static no.nav.dokarkiv.core.storage.DokprodMellomlagerS3Storage.DOKPRODMELLOMLAGER_DIRECTORY_NAME;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import no.nav.dokarkiv.arkiverdokumentproduksjon.AbstractArkiverdokumentproduksjonItest;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.exceptions.ApplicationException;
import no.nav.dokarkiv.core.exceptions.DokarkivTechnicalException;
import no.nav.dokarkiv.core.exceptions.InvalidArgumentException;
import no.nav.dokarkiv.core.storage.Storage;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.arkiverdokumentproduksjon.JournalpostType;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.OpprettJournalpostArkiverDokumenterRequest;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.OpprettJournalpostArkiverDokumenterResponse;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import javax.xml.datatype.DatatypeFactory;
import java.util.Optional;

/**
 * Integration tests for the opprettOgFerdigstillJournalpost operation in the ArkiverDokumentproduksjon webservice
 *
 * @author Torgeir Cook
 */
public class OpprettJournalpostArkiverDokumenterIT extends AbstractArkiverdokumentproduksjonItest {

	@Inject
	private Storage dokprodMellomlagerStorage;

	private no.nav.dokarkiv.core.domain.entities.Journalpost persistedJournalpost;

	@Before
	public void setUp() throws Exception {
		when(dokprodMellomlagerStorage.get(eq(DOKPRODMELLOMLAGER_DIRECTORY_NAME), eq(FILREFERANSE_S3))).thenReturn(Optional.of("{\n" +
				"  \"axml\" : \"" + DOKUMENT_INNHOLD_BASE64 + "\",\n" +
				"  \"pdf\": \"" + DOKUMENT_INNHOLD_BASE64 + "\"\n" +
				"}"));
	}

	@Test
	public void shouldVerifyResponseHasJournalpostAndDokumentId() throws Exception {
		OpprettJournalpostArkiverDokumenterResponse response = arkiverDokumentproduksjonProvider.opprettJournalpostArkiverDokumenter(createRequest());
		assertThat(response.getJournalpostId(), is(notNullValue()));
		assertThat(response.getDokumentInfoIdListe(), is(notNullValue()));
		assertThat(response.getDokumentInfoIdListe().get(0), is(notNullValue()));
	}

	@Test
	public void shouldVerifyCorrectFieldsInJournalpost() throws Exception {
		OpprettJournalpostArkiverDokumenterResponse response = arkiverDokumentproduksjonProvider.opprettJournalpostArkiverDokumenter(createRequest());
		persistedJournalpost = joarkRepository.findById(response.getJournalpostId()).get();
		OpprettJournalpostArkiverDokumenterAssertUtil.assertEqualJournalposts(persistedJournalpost);
	}

	@Test
	public void shouldVerifyEqualResponseWhenTryingToJournalforSameRequestTwice() throws Exception {
		OpprettJournalpostArkiverDokumenterResponse firstResponse = arkiverDokumentproduksjonProvider.opprettJournalpostArkiverDokumenter(createRequest());
		OpprettJournalpostArkiverDokumenterResponse secondResponse = arkiverDokumentproduksjonProvider.opprettJournalpostArkiverDokumenter(createRequest());

		assertThat(firstResponse, is(equalTo(secondResponse)));
	}

	@Test
	public void shouldVerifyNotEqualResponseWhenTryingToJournalforSameRequestTwiceAndIsMissingBestillingId() throws Exception {
		OpprettJournalpostArkiverDokumenterRequest firstRequest = createRequest();
		firstRequest.getJournalpost().getDokumentInfoHoveddokument().getTilleggsopplysninger().clear();
		OpprettJournalpostArkiverDokumenterResponse firstResponse = arkiverDokumentproduksjonProvider.opprettJournalpostArkiverDokumenter(firstRequest);
		OpprettJournalpostArkiverDokumenterRequest secondRequest = createRequest();
		OpprettJournalpostArkiverDokumenterResponse secondResponse = arkiverDokumentproduksjonProvider.opprettJournalpostArkiverDokumenter(secondRequest);

		assertThat(firstResponse, is(not(equalTo(secondResponse))));
	}

	@Test
	public void shouldThrowExceptionIfDatoDokumentIsNull() throws Exception {
		expectedException.expect(ApplicationException.class);
		expectedException.expectMessage("DatoDokument must be set");
		OpprettJournalpostArkiverDokumenterRequest request = createRequest();
		request.getJournalpost().setDatoDokument(null);
		arkiverDokumentproduksjonProvider.opprettJournalpostArkiverDokumenter(request);
	}

	@Test
	public void shouldVerifyDatoDokument() throws Exception {
		OpprettJournalpostArkiverDokumenterRequest request = createRequest();
		request.getJournalpost().setDatoDokument(DatatypeFactory.newInstance().newXMLGregorianCalendar("2015-01-28"));
		arkiverDokumentproduksjonProvider.opprettJournalpostArkiverDokumenter(request);
	}

	@Test
	public void shouldThrowExceptionIfRequestDoesNotValidate() throws Exception {
		expectedException.expect(InvalidArgumentException.class);
		expectedException.expectMessage("Journalpost.fagomrade must be set");
		OpprettJournalpostArkiverDokumenterRequest request = createRequest();
		request.getJournalpost().setFagomrade(null);
		arkiverDokumentproduksjonProvider.opprettJournalpostArkiverDokumenter(request);
	}

	@Test
	public void shouldThrowExceptionIfRequestIsMissingDokumentFilReferanse() throws Exception {
		expectedException.expect(DokarkivTechnicalException.class);
		OpprettJournalpostArkiverDokumenterRequest request = createRequest();
		request.getJournalpost().getDokumentInfoHoveddokument().setFilreferanse(null);
		arkiverDokumentproduksjonProvider.opprettJournalpostArkiverDokumenter(request);
	}

	@Test
	public void shouldRunWithJournalpostTypeN() throws Exception {
		OpprettJournalpostArkiverDokumenterRequest request = createRequest();
		request.getJournalpost().setJournalpostType(JournalpostType.N);
		OpprettJournalpostArkiverDokumenterResponse response = arkiverDokumentproduksjonProvider.opprettJournalpostArkiverDokumenter(request);
		persistedJournalpost = joarkRepository.findById(response.getJournalpostId()).get();
		assertThat(persistedJournalpost.getJournalposttype(), is(JournalpostTypeCode.N));
	}

	@Test
	public void shouldRunWithNullJournalpostType() throws Exception {
		OpprettJournalpostArkiverDokumenterRequest request = createRequest();
		request.getJournalpost().setJournalpostType(null);
		OpprettJournalpostArkiverDokumenterResponse response = arkiverDokumentproduksjonProvider.opprettJournalpostArkiverDokumenter(request);
		persistedJournalpost = joarkRepository.findById(response.getJournalpostId()).get();

		assertThat(persistedJournalpost.getJournalposttype(), is(JournalpostTypeCode.U));
	}

	private OpprettJournalpostArkiverDokumenterRequest createRequest() throws Exception {
		OpprettJournalpostArkiverDokumenterRequest request = new OpprettJournalpostArkiverDokumenterRequest();
		request.setJournalpost(createJournalpost());
		return request;
	}
}
