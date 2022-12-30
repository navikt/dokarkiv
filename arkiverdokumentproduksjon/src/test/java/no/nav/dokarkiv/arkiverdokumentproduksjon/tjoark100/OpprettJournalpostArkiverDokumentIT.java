package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark100;

import no.nav.dokarkiv.arkiverdokumentproduksjon.AbstractArkiverdokumentproduksjonItest;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.exceptions.ApplicationException;
import no.nav.dokarkiv.core.exceptions.InvalidArgumentException;
import no.nav.dokarkiv.core.exceptions.InvalidJournalpostStructureException;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.arkiverdokumentproduksjon.JournalpostType;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.opprettjournalpostarkiverdokument.Fildetaljer;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.OpprettJournalpostArkiverDokumentRequest;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.OpprettJournalpostArkiverDokumentResponse;
import org.junit.jupiter.api.Test;

import javax.xml.datatype.DatatypeFactory;

import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark100.OpprettJournalpostArkiverDokumentDataUtil.createJournalpost;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Integration tests for the opprettOgFerdigstillJournalpost operation in the ArkiverDokumentproduksjon webservice
 *
 * @author Torgeir Cook
 */
public class OpprettJournalpostArkiverDokumentIT extends AbstractArkiverdokumentproduksjonItest {

	private no.nav.dokarkiv.core.domain.entities.Journalpost persistedJournalpost;
	private OpprettJournalpostArkiverDokumentResponse response;

	@Test
	public void shouldVerfiyResponseHasJournalpostAndDokumentId() throws Exception {
		response = arkiverDokumentproduksjonProvider.opprettJournalpostArkiverDokument(createRequest());
		assertThat(response.getJournalpostId(), is(notNullValue()));
		assertThat(response.getDokumentInfoId(), is(notNullValue()));
	}

	@Test
	public void shouldVerifyCorrectFieldsInJournalpost() throws Exception {
		response = arkiverDokumentproduksjonProvider.opprettJournalpostArkiverDokument(createRequest());
		persistedJournalpost = journalpostTestRepository.findById(response.getJournalpostId()).get();
		OpprettJournalpostArkiverDokumentAssertUtil.assertEqualJournalposts(persistedJournalpost);
	}

	@Test
	public void shouldVerifyCorrectFieldsInJournalpostLokalPrint() throws Exception {
		OpprettJournalpostArkiverDokumentRequest request = createRequest();
		request.getJournalpost().setUtsendingskanal(UtsendingsKanalCode.L.toString());
		response = arkiverDokumentproduksjonProvider.opprettJournalpostArkiverDokument(request);
		persistedJournalpost = journalpostTestRepository.findById(response.getJournalpostId()).get();
		assertThat(persistedJournalpost.getUtsendingskanal(), is(UtsendingsKanalCode.L));
		assertThat(persistedJournalpost.getJournalstatus(), is(JournalStatusCode.FL));
	}

	@Test
	public void shouldVerifyEqualResponseWhenTryingToJournalforSameRequestTwice() throws Exception {
		OpprettJournalpostArkiverDokumentResponse firstResponse = arkiverDokumentproduksjonProvider.opprettJournalpostArkiverDokument(createRequest());
		OpprettJournalpostArkiverDokumentResponse secondResponse = arkiverDokumentproduksjonProvider.opprettJournalpostArkiverDokument(createRequest());

		assertThat(firstResponse, is(equalTo(secondResponse)));
	}

	@Test
	public void shouldVerifyNotEqualResponseWhenTryingToJournalforSameRequestTwiceAndIsMissingBestillingId() throws Exception {
		OpprettJournalpostArkiverDokumentRequest firstRequest = createRequest();
		firstRequest.getJournalpost().getDokumentInfo().getTilleggsopplysninger().clear();
		OpprettJournalpostArkiverDokumentResponse firstResponse = arkiverDokumentproduksjonProvider.opprettJournalpostArkiverDokument(firstRequest);
		OpprettJournalpostArkiverDokumentResponse secondResponse = arkiverDokumentproduksjonProvider.opprettJournalpostArkiverDokument(createRequest());

		assertThat(firstResponse, is(not(equalTo(secondResponse))));
	}

	@Test
	public void shouldThrowExceptionIfDatoDokumentIsNull() throws Exception {
		OpprettJournalpostArkiverDokumentRequest request = createRequest();
		request.getJournalpost().setDatoDokument(null);

		assertThrows(ApplicationException.class,
				() -> response = arkiverDokumentproduksjonProvider.opprettJournalpostArkiverDokument(request),
				"DatoDokument must be set");
	}

	@Test
	public void shouldVerifyDatoDokument() throws Exception {
		OpprettJournalpostArkiverDokumentRequest request = createRequest();
		request.getJournalpost().setDatoDokument(DatatypeFactory.newInstance().newXMLGregorianCalendar("2015-01-28"));
		response = arkiverDokumentproduksjonProvider.opprettJournalpostArkiverDokument(request);
	}

	@Test
	public void shouldThrowExceptionIfRequestDoesNotValidate() throws Exception {
		OpprettJournalpostArkiverDokumentRequest request = createRequest();
		request.getJournalpost().setFagomrade(null);

		assertThrows(InvalidArgumentException.class,
				() -> response = arkiverDokumentproduksjonProvider.opprettJournalpostArkiverDokument(request),
				"Journalpost.fagomrade must be set");
	}

	@Test
	public void shouldThrowExceptionIfRequestIsMissingDokument() throws Exception {
		OpprettJournalpostArkiverDokumentRequest request = createRequest();
		request.getJournalpost().getDokumentInfo().getFildetaljerListe().get(0).setIkkeRedigerbartdokument(null);

		assertThrows(ApplicationException.class,
				() -> response = arkiverDokumentproduksjonProvider.opprettJournalpostArkiverDokument(request),
				"FileContent must be set");
	}

	@Test
	public void shouldThrowExceptionIfRequestGotTwoArkivDokuments() throws Exception {
		Fildetaljer anotherArkiv = new Fildetaljer();
		anotherArkiv.setIkkeRedigerbartdokument("foo".getBytes());
		anotherArkiv.setFiltype("AXML");
		anotherArkiv.setVariantformat("ARKIV");
		OpprettJournalpostArkiverDokumentRequest request = createRequest();
		request.getJournalpost().getDokumentInfo().getFildetaljerListe().add(anotherArkiv);

		assertThrows(InvalidJournalpostStructureException.class,
				() -> response = arkiverDokumentproduksjonProvider.opprettJournalpostArkiverDokument(request),
				"found 2 ARKIV");
	}

	@Test
	public void shouldRunWithJournalpostTypeN() throws Exception {
		OpprettJournalpostArkiverDokumentRequest request = createRequest();
		request.getJournalpost().setJournalpostType(JournalpostType.N);
		response = arkiverDokumentproduksjonProvider.opprettJournalpostArkiverDokument(request);
		persistedJournalpost = journalpostTestRepository.findById(response.getJournalpostId()).get();
		assertThat(persistedJournalpost.getJournalposttype(), is(JournalpostTypeCode.N));
	}

	@Test
	public void shouldRunWithNullJournalpostType() throws Exception {
		OpprettJournalpostArkiverDokumentRequest request = createRequest();
		request.getJournalpost().setJournalpostType(null);
		response = arkiverDokumentproduksjonProvider.opprettJournalpostArkiverDokument(request);
		persistedJournalpost = journalpostTestRepository.findById(response.getJournalpostId()).get();

		assertThat(persistedJournalpost.getJournalposttype(), is(JournalpostTypeCode.U));
	}


	@Test
	public void shouldNotThrowExceptionNoUtsendingskanal() throws Exception {
		OpprettJournalpostArkiverDokumentRequest request = createRequest();
		request.getJournalpost().setJournalpostType(JournalpostType.N);
		request.getJournalpost().setUtsendingskanal(null);
		response = arkiverDokumentproduksjonProvider.opprettJournalpostArkiverDokument(request);
		persistedJournalpost = journalpostTestRepository.findById(response.getJournalpostId()).get();

		assertThat(persistedJournalpost.getJournalposttype(), is(JournalpostTypeCode.N));
	}

	@Test
	public void shouldNotThrowExceptionNoUtsendingskanal2() throws Exception {
		OpprettJournalpostArkiverDokumentRequest request = createRequest();
		request.getJournalpost().setUtsendingskanal(null);
		request.setFerdigstillJournalpost(false);
		response = arkiverDokumentproduksjonProvider.opprettJournalpostArkiverDokument(request);
		persistedJournalpost = journalpostTestRepository.findById(response.getJournalpostId()).get();

		assertThat(persistedJournalpost.getUtsendingskanal(), is(nullValue()));
	}

	@Test
	public void shouldThrowExceptionNoUtsendingskanal() throws Exception {
		OpprettJournalpostArkiverDokumentRequest request = createRequest();
		request.getJournalpost().setUtsendingskanal(null);

		assertThrows(ApplicationException.class,
				() -> arkiverDokumentproduksjonProvider.opprettJournalpostArkiverDokument(request),
				"Missing parameter in request: Utsendingskanal");
	}

	private OpprettJournalpostArkiverDokumentRequest createRequest() throws Exception {
		OpprettJournalpostArkiverDokumentRequest request = new OpprettJournalpostArkiverDokumentRequest();
		request.setFerdigstillJournalpost(true);
		request.setJournalpost(createJournalpost());
		return request;
	}
}
