package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark112;

import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark100.OpprettJournalpostArkiverDokumentDataUtil.createJournalpost;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import no.nav.dokarkiv.arkiverdokumentproduksjon.AbstractArkiverdokumentproduksjonItest;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark100.OpprettJournalpostArkiverDokumentAssertUtil;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.exceptions.ApplicationException;
import no.nav.dokarkiv.core.exceptions.InvalidArgumentException;
import no.nav.dokarkiv.core.exceptions.InvalidJournalpostStructureException;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.arkiverdokumentproduksjon.JournalpostType;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.OpprettJournalpostArkiverDokumenterRequest;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.OpprettJournalpostArkiverDokumenterResponse;
import org.junit.Ignore;
import org.junit.Test;

import javax.xml.datatype.DatatypeFactory;

/**
 * Integration tests for the opprettOgFerdigstillJournalpost operation in the ArkiverDokumentproduksjon webservice
 *
 * @author Torgeir Cook
 */
@Ignore
//FIXME
public class OpprettJournalpostArkiverDokumenterIT extends AbstractArkiverdokumentproduksjonItest {

	private no.nav.dokarkiv.core.domain.entities.Journalpost persistedJournalpost;
	private OpprettJournalpostArkiverDokumenterResponse response;

	@Test
	public void shouldVerfiyResponseHasJournalpostAndDokumentId() throws Exception {
		response = arkiverDokumentproduksjonProvider.opprettJournalpostArkiverDokumenter(createRequest());
		assertThat(response.getJournalpostId(), is(notNullValue()));
		assertThat(response.getDokumentInfoIdListe(), is(notNullValue()));
		assertThat(response.getDokumentInfoIdListe().get(0), is(notNullValue()));
	}

	@Test
	public void shouldVerifyCorrectFieldsInJournalpost() throws Exception {
		response = arkiverDokumentproduksjonProvider.opprettJournalpostArkiverDokumenter(createRequest());
		persistedJournalpost = joarkRepository.findById(response.getJournalpostId()).get();
		OpprettJournalpostArkiverDokumentAssertUtil.assertEqualJournalposts(persistedJournalpost);
	}

	@Test
	@Ignore
	public void shouldVerifyCorrectFieldsInJournalpostLokalPrint() throws Exception {
		OpprettJournalpostArkiverDokumenterRequest request = createRequest();
//		request.getJournalpost().setUtsendingskanal(UtsendingsKanalCode.L.toString());
		response = arkiverDokumentproduksjonProvider.opprettJournalpostArkiverDokumenter(request);
		persistedJournalpost = joarkRepository.findById(response.getJournalpostId()).get();
		assertThat(persistedJournalpost.getUtsendingskanal(), is(UtsendingsKanalCode.L));
		assertThat(persistedJournalpost.getJournalstatus(), is(JournalStatusCode.FL));
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
		OpprettJournalpostArkiverDokumenterResponse secondResponse = arkiverDokumentproduksjonProvider.opprettJournalpostArkiverDokumenter(createRequest());

		assertThat(firstResponse, is(not(equalTo(secondResponse))));
	}

	@Test
	public void shouldThrowExceptionIfDatoDokumentIsNull() throws Exception {
		expectedException.expect(ApplicationException.class);
		expectedException.expectMessage("DatoDokument must be set");
		OpprettJournalpostArkiverDokumenterRequest request = createRequest();
		request.getJournalpost().setDatoDokument(null);
		response = arkiverDokumentproduksjonProvider.opprettJournalpostArkiverDokumenter(request);
	}

	@Test
	public void shouldVerifyDatoDokument() throws Exception {
		OpprettJournalpostArkiverDokumenterRequest request = createRequest();
		request.getJournalpost().setDatoDokument(DatatypeFactory.newInstance().newXMLGregorianCalendar("2015-01-28"));
		response = arkiverDokumentproduksjonProvider.opprettJournalpostArkiverDokumenter(request);
	}

	@Test
	public void shouldThrowExceptionIfRequestDoesNotValidate() throws Exception {
		expectedException.expect(InvalidArgumentException.class);
		expectedException.expectMessage("Journalpost.fagomrade must be set");
		OpprettJournalpostArkiverDokumenterRequest request = createRequest();
		request.getJournalpost().setFagomrade(null);
		response = arkiverDokumentproduksjonProvider.opprettJournalpostArkiverDokumenter(request);
	}

	@Test
	@Ignore
	public void shouldThrowExceptionIfRequestIsMissingDokument() throws Exception {
		expectedException.expect(ApplicationException.class);
		expectedException.expectMessage("FileContent must be set");
		OpprettJournalpostArkiverDokumenterRequest request = createRequest();
//		request.getJournalpost().getDokumentInfoHoveddokument().getFildetaljerListe().get(0).setIkkeRedigerbartdokument(null);
		response = arkiverDokumentproduksjonProvider.opprettJournalpostArkiverDokumenter(request);
	}

	@Test
	@Ignore
	public void shouldThrowExceptionIfRequestGotTwoArkivDokuments() throws Exception {
		expectedException.expect(InvalidJournalpostStructureException.class);
		expectedException.expectMessage("found 2 ARKIV");
//		Fildetaljer anotherArkiv = new Fildetaljer();
//		anotherArkiv.setIkkeRedigerbartdokument("foo".getBytes());
//		anotherArkiv.setFiltype("AXML");
//		anotherArkiv.setVariantformat("ARKIV");
		OpprettJournalpostArkiverDokumenterRequest request = createRequest();
//		request.getJournalpost().getDokumentInfo().getFildetaljerListe().add(anotherArkiv);
		response = arkiverDokumentproduksjonProvider.opprettJournalpostArkiverDokumenter(request);
	}

	@Test
	public void shouldRunWithJournalpostTypeN() throws Exception {
		OpprettJournalpostArkiverDokumenterRequest request = createRequest();
		request.getJournalpost().setJournalpostType(JournalpostType.N);
		response = arkiverDokumentproduksjonProvider.opprettJournalpostArkiverDokumenter(request);
		persistedJournalpost = joarkRepository.findById(response.getJournalpostId()).get();
		assertThat(persistedJournalpost.getJournalposttype(), is(JournalpostTypeCode.N));
	}

	@Test
	public void shouldRunWithNullJournalpostType() throws Exception {
		OpprettJournalpostArkiverDokumenterRequest request = createRequest();
		request.getJournalpost().setJournalpostType(null);
		response = arkiverDokumentproduksjonProvider.opprettJournalpostArkiverDokumenter(request);
		persistedJournalpost = joarkRepository.findById(response.getJournalpostId()).get();

		assertThat(persistedJournalpost.getJournalposttype(), is(JournalpostTypeCode.U));
	}

	private OpprettJournalpostArkiverDokumenterRequest createRequest() throws Exception {
		OpprettJournalpostArkiverDokumenterRequest request = new OpprettJournalpostArkiverDokumenterRequest();
//		request.setJournalpost(createJournalpost());
		return request;
	}
}
