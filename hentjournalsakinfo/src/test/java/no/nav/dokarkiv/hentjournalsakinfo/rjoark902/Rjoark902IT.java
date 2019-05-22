package no.nav.dokarkiv.hentjournalsakinfo.rjoark902;

import static no.nav.dokarkiv.core.util.TestDataGenerator.AVSENDER_MOTTAKER_ID;
import static no.nav.dokarkiv.core.util.TestDataGenerator.DOKUMENT_TYPE_ID;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createJournalpostWithHoveddokument;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.util.TestDataGenerator;
import no.nav.dokarkiv.hentjournalsakinfo.AbstractHentjournalsakinfoItest;
import no.nav.dokarkiv.hentjournalsakinfo.dto.DokumentInfoDto;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

import java.util.Set;

public class Rjoark902IT extends AbstractHentjournalsakinfoItest {

	private static final String HENTJOURNALSAKINFO_HENTJOURNALPOST = "/hentjournalsakinfo/hentjournalpost/";
	private static final String AVSENDER = "bob";
	private static final String JOURNALFOERT_AV = "test user journalfoert";
	private static final String JOURNALFOERENDE_ENHET = "test journalfoerende enhet";
	private static final JournalpostTypeCode JOURNALPOST_TYPE_CODE = JournalpostTypeCode.U;
	private static final String INNHOLD = "test innhold";
	private static final FagomradeCode FAGOMRADE = FagomradeCode.PEN;
	private static final JournalStatusCode JOURNALSTATUS = JournalStatusCode.FS;
	private static final MottaksKanalCode MOTTAKSKANAL = MottaksKanalCode.NAV_NO;
	private static final UtsendingsKanalCode UTSENDINGSKANAL = UtsendingsKanalCode.NAV_NO;
	private static final SkjermingTypeCode SKJERMINGTYPE = SkjermingTypeCode.POL;
	private static final String SAKID = "test sakid";
	private static final FagsystemCode SAKRELASJONFAGSYSTEM = FagsystemCode.AO01;
	private static final Boolean SAKFEILREGISTRERT = true;

	private static final DokumentStatusCode DOKUMENTSTATUS = DokumentStatusCode.UNDER_REDIGERING;
	private static final String BREVKODE = "test dokumentinfo brevkode";
	private static final String TITTEL = "test tittel";


	// Happy path
	@Test
	public void shouldGetJournalpost() {

		Journalpost storedJournalpost = buildAndPersistJournalpost();
		Long journalpostId = storedJournalpost.getJournalpostId();

		String uri = HENTJOURNALSAKINFO_HENTJOURNALPOST + journalpostId;
		ResponseEntity<SafHentJournalpostResponse> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, createHeaderEntity(), SafHentJournalpostResponse.class);

		HentJournalpostDto responseJournalpost = responseEntity.getBody().getHentJournalpostDto();

		assertEquals(journalpostId, responseJournalpost.getJournalpostId());
		assertEquals(INNHOLD, responseJournalpost.getInnhold());
		assertEquals(FAGOMRADE, responseJournalpost.getFagomrade());
		assertEquals(JOURNALSTATUS, responseJournalpost.getJournalstatus());
		assertEquals(AVSENDER_MOTTAKER_ID, responseJournalpost.getAvsenderMottakerId());
		assertEquals(AVSENDER, responseJournalpost.getAvsenderMottakerNavn());
		assertEquals(JOURNALFOERT_AV, responseJournalpost.getJournalfortAvNavn());
		assertEquals(MOTTAKSKANAL, responseJournalpost.getMottakskanal());
		assertEquals(UTSENDINGSKANAL, responseJournalpost.getUtsendingskanal());
		assertEquals(JOURNALPOST_TYPE_CODE, responseJournalpost.getJournalposttype());

		assertEquals(SAKID, responseJournalpost.getSaksrelasjon().getSakId());
		assertEquals(SAKRELASJONFAGSYSTEM, responseJournalpost.getSaksrelasjon().getFagsystem());
		assertEquals(SAKFEILREGISTRERT, responseJournalpost.getSaksrelasjon().getFeilregistrert());

		DokumentInfoDto responseDokumentInfo = responseJournalpost.getDokumenter().get(0);

		assertEquals(DOKUMENTSTATUS, responseDokumentInfo.getDokumentstatus());
		assertEquals(BREVKODE, responseDokumentInfo.getBrevkode());
		assertEquals(DOKUMENT_TYPE_ID, responseDokumentInfo.getDokumenttypeId());
		assertEquals(TITTEL, responseDokumentInfo.getTittel());
		assertEquals(true, responseDokumentInfo.getKassert());
	}

	//  Unhappy path
	@Test
	public void shouldFailToGetJournalpost() {

		Journalpost storedJournalpost = buildAndPersistJournalpost();
		Long journalpostId = 54321L;

		String uri = HENTJOURNALSAKINFO_HENTJOURNALPOST + journalpostId;
		ResponseEntity<SafHentJournalpostResponse> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, createHeaderEntity(), SafHentJournalpostResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));
	}

	private Journalpost buildAndPersistJournalpost() {

		Journalpost journalpost = createJournalpostWithHoveddokument();
		TestDataGenerator.createDokumentInfoVedleggRelasjon(journalpost);
		saveJournalpost(journalpost);

		journalpost.setJournalForendeEnhetId(JOURNALFOERENDE_ENHET);
		journalpost.setInnhold(INNHOLD);
		journalpost.setFagomrade(FAGOMRADE);
		journalpost.setJournalstatus(JOURNALSTATUS);
		journalpost.setAvsenderMottaker(AVSENDER);
		journalpost.setJournalfortAvNavn(JOURNALFOERT_AV);
		journalpost.setMottakskanal(MOTTAKSKANAL);
		journalpost.setUtsendingskanal(UTSENDINGSKANAL);
		journalpost.setJournalposttype(JOURNALPOST_TYPE_CODE);

		journalpost.getSaksrelasjon().setSakId(SAKID);
		journalpost.getSaksrelasjon().setFeilregistrert(SAKFEILREGISTRERT);
		journalpost.getSaksrelasjon().setFagsystem(SAKRELASJONFAGSYSTEM);

		DokumentInfo storedDokumentInfo = getDokumentInfoOfHoveddokument(journalpost);

		storedDokumentInfo.setDokumentstatus(DOKUMENTSTATUS);
		storedDokumentInfo.setBrevkode(BREVKODE);
		storedDokumentInfo.setTittel(TITTEL);
		storedDokumentInfo.setKassert(true);

		joarkRepository.save(journalpost);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		return journalpost;
	}

	private DokumentInfo getDokumentInfoOfHoveddokument(Journalpost journalpost) {
		Set<JournalpostDokumentInfoRelasjon> hoveddokumentList = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT);
		JournalpostDokumentInfoRelasjon journalpostDokumentInfoRelasjon = hoveddokumentList.isEmpty() ? null : hoveddokumentList.iterator().next();
		return journalpostDokumentInfoRelasjon.getDokumentInfo();
	}
}
