package no.nav.dokarkiv.hentjournalsakinfo.rjoark902;

import static no.nav.dokarkiv.hentjournalsakinfo.TestDataGenerator2.createJournalpostWithHoveddokument;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.hentjournalsakinfo.AbstractHentjournalsakinfoItest;
import no.nav.dokarkiv.hentjournalsakinfo.rjoark900.DokumentInfoDto;
import no.nav.dokarkiv.hentjournalsakinfo.rjoark920.HentJournalpostDto;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

public class Rjoark902IT extends AbstractHentjournalsakinfoItest {

	private static final String HENTJOURNALSAKINFO_HENTJOURNALPOST = "/hentjournalsakinfo/hentjournalpost/";
	private static final String AVSENDER = "bob";
	private static final String JOURNALFOERT_AV = "test user journalfoert";
	private static final String JOURNALFOERENDE_ENHET = "test journalfoerende enhet";
	private static final JournalpostTypeCode JOURNALPOST_TYPE_CODE = JournalpostTypeCode.U;
	private static final Long DOKUMENTINFOID = 200000000L;

	// Happy path
	@Test
	public void shouldGetJournalpost() {

		Journalpost storedJournalpost = buildAndPersistJournalpost();
		TestTransaction.flagForCommit();
		TestTransaction.end();

		Long journalpostId = storedJournalpost.getJournalpostId();

		String uri = HENTJOURNALSAKINFO_HENTJOURNALPOST + journalpostId;
		ResponseEntity<SafHentJournalpostResponseTo> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, createHeaderEntity(), SafHentJournalpostResponseTo.class);

		HentJournalpostDto responseJournalpost = responseEntity.getBody().getHentJournalpostDto();

		assertEquals(journalpostId, responseJournalpost.getJournalpostId());
		assertEquals(storedJournalpost.getJournalposttype(), responseJournalpost.getJournalposttype());
		assertEquals(storedJournalpost.getInnhold(), responseJournalpost.getInnhold());
		assertEquals(storedJournalpost.getFagomrade(), responseJournalpost.getFagomrade());
		assertEquals(storedJournalpost.getJournalstatus(), responseJournalpost.getJournalstatus());
		assertEquals(storedJournalpost.getAvsenderMottaker(), responseJournalpost.getAvsenderMottakerNavn());
		assertEquals(storedJournalpost.getJournalfortAvNavn(), responseJournalpost.getJournalfortAvNavn());
		assertEquals(storedJournalpost.getMottakskanal(), responseJournalpost.getMottakskanal());
		assertEquals(storedJournalpost.getUtsendingskanal(), responseJournalpost.getUtsendingskanal());
		assertEquals(storedJournalpost.getJournalposttype(), responseJournalpost.getJournalposttype());
		assertEquals(storedJournalpost.getSkjermingType(), responseJournalpost.getSkjerming());


		assertEquals(storedJournalpost.getSaksrelasjon().getSakId(), responseJournalpost.getSaksrelasjon().getSakId());
		assertEquals(storedJournalpost.getSaksrelasjon().getFagsystem(), responseJournalpost.getSaksrelasjon().getFagsystem());
		assertEquals(storedJournalpost.getSaksrelasjon().getFeilregistrert(), responseJournalpost.getSaksrelasjon().getFeilregistrert());

		DokumentInfo storedDokumentInfo = storedJournalpost.getDokumentInfoFromJpDokInfoRelasjonerByDokumentInfoId(DOKUMENTINFOID);
		DokumentInfoDto responseDokumentInfo = responseJournalpost.getDokumenter().get(0);

		assertEquals(storedDokumentInfo.getDokumentstatus(), responseDokumentInfo.getDokumentstatus());
		assertEquals(storedDokumentInfo.getBrevkode(), responseDokumentInfo.getBrevkode());
		assertEquals(storedDokumentInfo.getTittel(), responseDokumentInfo.getTittel());

	}

	//  Unhappy path
	@Test
	public void shouldFailToGetJournalpost() {

		Journalpost storedJournalpost = buildAndPersistJournalpost();
		TestTransaction.flagForCommit();
		TestTransaction.end();

		Long journalpostId = 54321L;

		String uri = HENTJOURNALSAKINFO_HENTJOURNALPOST + journalpostId;
		ResponseEntity<SafHentJournalpostResponseTo> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, createHeaderEntity(), SafHentJournalpostResponseTo.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));
	}

	private Journalpost buildAndPersistJournalpost() {

		Journalpost journalpost = createJournalpostWithHoveddokument();
		journalpost.setJournalForendeEnhetId(JOURNALFOERENDE_ENHET);
		journalpost.setAvsenderMottaker(AVSENDER);
		journalpost.setJournalfortAvNavn(JOURNALFOERT_AV);
		journalpost.setJournalposttype(JOURNALPOST_TYPE_CODE);
		journalpost.getSaksrelasjon().setFeilregistrert(true);

		joarkRepository.save(journalpost);

		return journalpost;
	}
}
