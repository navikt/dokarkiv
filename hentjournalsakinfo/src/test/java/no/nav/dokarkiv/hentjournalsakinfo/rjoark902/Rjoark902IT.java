package no.nav.dokarkiv.hentjournalsakinfo.rjoark902;

import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.entities.UtsendingsInfo;
import no.nav.dokarkiv.core.util.TestDataGenerator;
import no.nav.dokarkiv.hentjournalsakinfo.AbstractHentjournalsakinfoItest;
import no.nav.dokarkiv.hentjournalsakinfo.dto.DokumentInfoDto;
import no.nav.dokarkiv.hentjournalsakinfo.dto.UtsendingsInfoDto;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Set;

import static no.nav.dokarkiv.core.util.TestDataGenerator.AVSENDER_MOTTAKER_ID;
import static no.nav.dokarkiv.core.util.TestDataGenerator.AVSENDER_MOTTAKER_ID_TYPE;
import static no.nav.dokarkiv.core.util.TestDataGenerator.DOKUMENT_TYPE_ID;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createDokumentInfo;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createJournalpostWithHoveddokument;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createVedleggRelasjon;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Rjoark902IT extends AbstractHentjournalsakinfoItest {

	private static final String HENTJOURNALSAKINFO_HENTJOURNALPOST = "/hentjournalsakinfo/hentjournalpost/";
	private static final String AVSENDER = "bob";
	private static final String JOURNALFOERT_AV = "test user journalfoert";
	private static final String JOURNALFOERENDE_ENHET = "2990";
	private static final JournalpostTypeCode JOURNALPOST_TYPE_CODE = JournalpostTypeCode.U;
	private static final String INNHOLD = "test innhold";
	private static final FagomradeCode FAGOMRADE = FagomradeCode.PEN;
	private static final JournalStatusCode JOURNALSTATUS = JournalStatusCode.FS;
	private static final MottaksKanalCode MOTTAKSKANAL = MottaksKanalCode.NAV_NO;
	private static final UtsendingsKanalCode UTSENDINGSKANAL = UtsendingsKanalCode.NAV_NO;
	private static final SkjermingTypeCode SKJERMINGTYPE = SkjermingTypeCode.POL;
	private static final String SAKID = "6293";
	private static final FagsystemCode SAKRELASJONFAGSYSTEM = FagsystemCode.FS22;
	private static final Boolean SAKFEILREGISTRERT = true;
	private static final Date LESTDATO = Date.from(LocalDate.now().minusDays(3).atStartOfDay(ZoneId.systemDefault()).toInstant());

	private static final DokumentStatusCode DOKUMENTSTATUS = DokumentStatusCode.UNDER_REDIGERING;
	private static final String BREVKODE = "test dokumentinfo brevkode";
	private static final String TITTEL = "test tittel";
	private static final String ANTALL_RETUR = "3";
	public static final String KANAL_REFERANSE_ID = "KANAL REFERANSE ID";
	public static final String ADRESSELINJE1 = "adresselinje1";
	public static final String ADRESSELINJE2 = "adresselinje2";
	public static final String ADRESSELINJE3 = "adresselinje3";
	public static final String POSTNUMMER = "postnummer";
	public static final String POSTSTED = "poststed";
	public static final String LANDKODE = "landkode";
	public static final String DIGITALKONTAKT_INFORMASJON = "{\n          \"epost\": \"epostaddress3@nav.no\",\n          \"sms\": \"11111111\"\n        }";
	public static final String VARSELTEKST = "{\n          \"epost\": \"Du har fått brev fra NAV\",\n          \"sms\": \"Du har fått brev fra NAV\"\n        }";
	public static final String DIGITALPOSTKASSEADRESSE = "0000487236";
	public static final String DIGITALPOSTKASSELEVERANDOR = "123456789";

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
		assertEquals(AVSENDER_MOTTAKER_ID_TYPE, responseJournalpost.getAvsenderMottakerIdType());
		assertEquals(AVSENDER, responseJournalpost.getAvsenderMottakerNavn());
		assertEquals(JOURNALFOERT_AV, responseJournalpost.getJournalfortAvNavn());
		assertEquals(MOTTAKSKANAL, responseJournalpost.getMottakskanal());
		assertEquals(UTSENDINGSKANAL, responseJournalpost.getUtsendingskanal());
		assertEquals(JOURNALPOST_TYPE_CODE, responseJournalpost.getJournalposttype());

		assertEquals(SAKID, responseJournalpost.getSaksrelasjon().getSakId());
		assertEquals(SAKRELASJONFAGSYSTEM, responseJournalpost.getSaksrelasjon().getFagsystem());
		assertEquals(SAKFEILREGISTRERT, responseJournalpost.getSaksrelasjon().getFeilregistrert());
		assertEquals(ANTALL_RETUR, responseJournalpost.getAntallRetur());
		assertEquals(KANAL_REFERANSE_ID, responseJournalpost.getKanalReferanseId());
		assertEquals(LESTDATO, responseJournalpost.getLestDato());

		DokumentInfoDto responseDokumentInfo = responseJournalpost.getDokumenter().get(0);

		assertEquals(DOKUMENTSTATUS, responseDokumentInfo.getDokumentstatus());
		assertEquals(BREVKODE, responseDokumentInfo.getBrevkode());
		assertEquals(DOKUMENT_TYPE_ID, responseDokumentInfo.getDokumenttypeId());
		assertEquals(TITTEL, responseDokumentInfo.getTittel());
		assertEquals(true, responseDokumentInfo.getKassert());

		assertNotNull(responseDokumentInfo.getVarianter().get(0).getFiluuid());
		assertEquals(responseDokumentInfo.getVarianter().get(0).getFiltype(), FilTypeCode.PDF.name());

		UtsendingsInfoDto.NavNoVarsling navNoVarsling = responseJournalpost.getUtsendingsInfo().getNavNoVarsling();

		assertEquals(VARSELTEKST, navNoVarsling.getVarseltekst());
		assertEquals(DIGITALKONTAKT_INFORMASJON, navNoVarsling.getVarselSendtTil());
	}

	@Test
	public void shouldReturnVedleggOrderedByRelasjonId() {
		DokumentInfo vedlegg2 = createDokumentInfo();
		dokumentInfoRepository.persist(vedlegg2);
		DokumentInfo vedlegg1 = createDokumentInfo();
		dokumentInfoRepository.persist(vedlegg1);
		Journalpost journalpost = createJournalpostWithHoveddokument();
		DokumentInfo hoveddokument = journalpost.getDokumentInfoFromJpDokInfoRelasjoner(0);
		createVedleggRelasjon(journalpost, vedlegg1);
		journalpostRepository.save(journalpost);
		createVedleggRelasjon(journalpost, vedlegg2);
		journalpostRepository.save(journalpost);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		String uri = HENTJOURNALSAKINFO_HENTJOURNALPOST + journalpost.getJournalpostId();
		ResponseEntity<SafHentJournalpostResponse> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, createHeaderEntity(), SafHentJournalpostResponse.class);
		HentJournalpostDto responseJournalpost = responseEntity.getBody().getHentJournalpostDto();

		assertThat(responseJournalpost.getDokumenter(), hasSize(3));
		assertThat(responseJournalpost.getDokumenter().get(0).getDokumentInfoId(), is(hoveddokument.getDokumentInfoId()));
		assertThat(responseJournalpost.getDokumenter().get(1).getDokumentInfoId(), is(vedlegg1.getDokumentInfoId()));
		assertThat(responseJournalpost.getDokumenter().get(2).getDokumentInfoId(), is(vedlegg2.getDokumentInfoId()));
	}

	//  Unhappy path
	@Test
	public void shouldFailToGetJournalpost() {
		buildAndPersistJournalpost();
		long journalpostId = 54321L;

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
		journalpost.setUtsendingsInfo(createNavNoVarsling());
		journalpost.setLestDato(OffsetDateTime.from(LocalDate.now().minusDays(3).atStartOfDay(ZoneId.systemDefault())));

		journalpost.getSaksrelasjon().setSakId(SAKID);
		journalpost.getSaksrelasjon().setFeilregistrert(SAKFEILREGISTRERT);
		journalpost.getSaksrelasjon().setFagsystem(SAKRELASJONFAGSYSTEM);

		DokumentInfo storedDokumentInfo = getDokumentInfoOfHoveddokument(journalpost);

		storedDokumentInfo.setDokumentstatus(DOKUMENTSTATUS);
		storedDokumentInfo.setBrevkode(BREVKODE);
		storedDokumentInfo.setTittel(TITTEL);
		storedDokumentInfo.setKassert(true);

		journalpostRepository.save(journalpost);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		return journalpost;
	}

	public static UtsendingsInfo.FysiskPostadresse createFysiskPostadresse() {
		return new UtsendingsInfo.FysiskPostadresse(ADRESSELINJE1, ADRESSELINJE2, ADRESSELINJE3, POSTNUMMER, POSTSTED, LANDKODE);
	}

	public static UtsendingsInfo.NavNoVarsling createNavNoVarsling() {
		return new UtsendingsInfo.NavNoVarsling(DIGITALKONTAKT_INFORMASJON, VARSELTEKST);
	}

	public static UtsendingsInfo.DigitalPostadresse createDigitalPostadresse() {
		return new UtsendingsInfo.DigitalPostadresse(DIGITALPOSTKASSEADRESSE, DIGITALPOSTKASSELEVERANDOR);
	}

	private DokumentInfo getDokumentInfoOfHoveddokument(Journalpost journalpost) {
		Set<JournalpostDokumentInfoRelasjon> hoveddokumentList = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT);
		JournalpostDokumentInfoRelasjon journalpostDokumentInfoRelasjon = hoveddokumentList.isEmpty() ? null : hoveddokumentList
				.iterator()
				.next();
		return journalpostDokumentInfoRelasjon.getDokumentInfo();
	}
}
