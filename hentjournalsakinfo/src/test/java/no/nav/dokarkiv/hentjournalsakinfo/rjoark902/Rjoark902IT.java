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
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

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
	private static final Long SAKID = 6293L;
	private static final FagsystemCode SAKRELASJONFAGSYSTEM = FagsystemCode.FS22;
	private static final Boolean SAKFEILREGISTRERT = true;
	private static final Date LESTDATO = Date.from(LocalDate.now().minusDays(3).atStartOfDay(ZoneId.systemDefault()).toInstant());

	private static final DokumentStatusCode DOKUMENTSTATUS = DokumentStatusCode.UNDER_REDIGERING;
	private static final String BREVKODE = "test dokumentinfo brevkode";
	private static final String TITTEL = "test tittel";
	private static final String ANTALL_RETUR = "3";
	public static final String KANAL_REFERANSE_ID = "KANAL REFERANSE ID";
	public static final String KANAL_REFERANSE_ID_WITH_OVER_200_TEGN = "KANALREFERANSEID12345".repeat(10);
	public static final String EPOSTADRESSE = "example@example.org";
	public static final String TELEFONNUMMER = "+4711111111";
	public static final String DIGITALKONTAKT_INFORMASJON = "{\n          \"epost\": \"epostaddress3@nav.no\",\n          \"sms\": \"11111111\"\n        }";
	public static final String VARSELTEKST = "Du har fått brev fra NAV";
	public static final LocalDateTime VARSELTIDSPUNKT = LocalDateTime.of(2023, 3, 1, 11, 0, 0, 0);

	public static final SafHentJournalpostResponseForTest.Varsel EPOSTVARSLER = new SafHentJournalpostResponseForTest.Varsel(TITTEL, VARSELTEKST, EPOSTADRESSE, null, VARSELTIDSPUNKT);
	public static final SafHentJournalpostResponseForTest.Varsel SMSVARSLER = new SafHentJournalpostResponseForTest.Varsel(null, VARSELTEKST, null, TELEFONNUMMER, VARSELTIDSPUNKT);

	// Happy path
	@Test
	public void shouldGetJournalpost() {
		"".toLowerCase();
		Journalpost storedJournalpost = buildAndPersistJournalpost();
		Long journalpostId = storedJournalpost.getJournalpostId();

		String uri = UriComponentsBuilder.fromUriString(HENTJOURNALSAKINFO_HENTJOURNALPOST)
				.path(journalpostId.toString())
				.build().toUriString();

		ResponseEntity<SafHentJournalpostResponseForTest> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, createHeaderEntity(), SafHentJournalpostResponseForTest.class);

		SafHentJournalpostResponseForTest.HentJournalpostDtoForTest responseJournalpost = responseEntity.getBody().hentJournalpostDto();

		assertEquals(journalpostId, responseJournalpost.journalpostId());
		assertEquals(INNHOLD, responseJournalpost.innhold());
		assertEquals(FAGOMRADE, responseJournalpost.fagomrade());
		assertEquals(JOURNALSTATUS, responseJournalpost.journalstatus());
		assertEquals(AVSENDER_MOTTAKER_ID, responseJournalpost.avsenderMottakerId());
		assertEquals(AVSENDER_MOTTAKER_ID_TYPE, responseJournalpost.avsenderMottakerIdType());
		assertEquals(AVSENDER, responseJournalpost.avsenderMottakerNavn());
		assertEquals(JOURNALFOERT_AV, responseJournalpost.journalfortAvNavn());
		assertEquals(MOTTAKSKANAL, responseJournalpost.mottakskanal());
		assertEquals(UTSENDINGSKANAL, responseJournalpost.utsendingskanal());
		assertEquals(JOURNALPOST_TYPE_CODE, responseJournalpost.journalposttype());

		assertEquals(SAKID.toString(), responseJournalpost.saksrelasjon().getSakId());
		assertEquals(SAKRELASJONFAGSYSTEM, responseJournalpost.saksrelasjon().getFagsystem());
		assertEquals(SAKFEILREGISTRERT, responseJournalpost.saksrelasjon().getFeilregistrert());
		assertEquals(ANTALL_RETUR, responseJournalpost.antallRetur());
		assertEquals(KANAL_REFERANSE_ID, responseJournalpost.kanalReferanseId());
		assertEquals(LESTDATO, responseJournalpost.lestDato());

		DokumentInfoDto responseDokumentInfo = responseJournalpost.dokumenter().get(0);

		assertEquals(DOKUMENTSTATUS, responseDokumentInfo.getDokumentstatus());
		assertEquals(BREVKODE, responseDokumentInfo.getBrevkode());
		assertEquals(DOKUMENT_TYPE_ID, responseDokumentInfo.getDokumenttypeId());
		assertEquals(TITTEL, responseDokumentInfo.getTittel());
		assertEquals(true, responseDokumentInfo.getKassert());
		assertEquals(true, responseDokumentInfo.getSensitivt());

		assertNotNull(responseDokumentInfo.getVarianter().get(0).getFiluuid());
		assertEquals(responseDokumentInfo.getVarianter().get(0).getFiltype(), FilTypeCode.PDF.name());

		UtsendingsInfoDto.NavNoVarsling navNoVarsling = responseJournalpost.utsendingsInfo().navNoVarsling();

		assertNull(navNoVarsling.getVarseltekst());
		assertEquals(DIGITALKONTAKT_INFORMASJON, navNoVarsling.getVarselSendtTil());

		List<SafHentJournalpostResponseForTest.Varsel> epostVarsel = responseJournalpost.utsendingsInfo().epostVarsel();
		assertThat(epostVarsel, hasSize(1));
		assertNull(epostVarsel.get(0).mobilnummer());
		assertEquals(epostVarsel.get(0).tittel(), EPOSTVARSLER.tittel());
		List<SafHentJournalpostResponseForTest.Varsel> smsVarsel = responseJournalpost.utsendingsInfo().smsVarsel();
		assertThat(smsVarsel, hasSize(1));
		assertEquals(smsVarsel.get(0).mobilnummer(), SMSVARSLER.mobilnummer());
		assertNull(smsVarsel.get(0).epostadresse());
		assertNull(smsVarsel.get(0).tittel());

		assertEquals(DIGITALKONTAKT_INFORMASJON, navNoVarsling.getVarselSendtTil());
	}

	// Happy path
	@Test
	public void shouldGetJournalpostByEksternReferanseId() {
		Journalpost storedJournalpost = buildAndPersistJournalpost();
		Long journalpostId = storedJournalpost.getJournalpostId();

		String uri = UriComponentsBuilder.fromUriString(HENTJOURNALSAKINFO_HENTJOURNALPOST)
				.path("eksternreferanse/" + KANAL_REFERANSE_ID)
				.build().toUriString();

		ResponseEntity<SafHentJournalpostResponseForTest> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, createHeaderEntity(), SafHentJournalpostResponseForTest.class);

		SafHentJournalpostResponseForTest.HentJournalpostDtoForTest responseJournalpost = responseEntity.getBody().hentJournalpostDto();

		assertEquals(journalpostId, responseJournalpost.journalpostId());
		assertEquals(INNHOLD, responseJournalpost.innhold());
		assertEquals(FAGOMRADE, responseJournalpost.fagomrade());
		assertEquals(JOURNALSTATUS, responseJournalpost.journalstatus());
		assertEquals(AVSENDER_MOTTAKER_ID, responseJournalpost.avsenderMottakerId());
		assertEquals(AVSENDER_MOTTAKER_ID_TYPE, responseJournalpost.avsenderMottakerIdType());
		assertEquals(AVSENDER, responseJournalpost.avsenderMottakerNavn());
		assertEquals(JOURNALFOERT_AV, responseJournalpost.journalfortAvNavn());
		assertEquals(MOTTAKSKANAL, responseJournalpost.mottakskanal());
		assertEquals(UTSENDINGSKANAL, responseJournalpost.utsendingskanal());
		assertEquals(JOURNALPOST_TYPE_CODE, responseJournalpost.journalposttype());

		assertEquals(SAKID.toString(), responseJournalpost.saksrelasjon().getSakId());
		assertEquals(SAKRELASJONFAGSYSTEM, responseJournalpost.saksrelasjon().getFagsystem());
		assertEquals(SAKFEILREGISTRERT, responseJournalpost.saksrelasjon().getFeilregistrert());
		assertEquals(ANTALL_RETUR, responseJournalpost.antallRetur());
		assertEquals(KANAL_REFERANSE_ID, responseJournalpost.kanalReferanseId());
		assertEquals(LESTDATO, responseJournalpost.lestDato());

		DokumentInfoDto responseDokumentInfo = responseJournalpost.dokumenter().get(0);

		assertEquals(DOKUMENTSTATUS, responseDokumentInfo.getDokumentstatus());
		assertEquals(BREVKODE, responseDokumentInfo.getBrevkode());
		assertEquals(DOKUMENT_TYPE_ID, responseDokumentInfo.getDokumenttypeId());
		assertEquals(TITTEL, responseDokumentInfo.getTittel());
		assertEquals(true, responseDokumentInfo.getKassert());
		assertEquals(true, responseDokumentInfo.getSensitivt());

		assertNotNull(responseDokumentInfo.getVarianter().get(0).getFiluuid());
		assertEquals(responseDokumentInfo.getVarianter().get(0).getFiltype(), FilTypeCode.PDF.name());

		UtsendingsInfoDto.NavNoVarsling navNoVarsling = responseJournalpost.utsendingsInfo().navNoVarsling();

		assertNull(navNoVarsling.getVarseltekst());
		assertEquals(DIGITALKONTAKT_INFORMASJON, navNoVarsling.getVarselSendtTil());

		List<SafHentJournalpostResponseForTest.Varsel> epostVarsel = responseJournalpost.utsendingsInfo().epostVarsel();
		assertThat(epostVarsel, hasSize(1));
		assertNull(epostVarsel.get(0).mobilnummer());
		assertEquals(epostVarsel.get(0).tittel(), EPOSTVARSLER.tittel());
		List<SafHentJournalpostResponseForTest.Varsel> smsVarsel = responseJournalpost.utsendingsInfo().smsVarsel();
		assertThat(smsVarsel, hasSize(1));
		assertEquals(smsVarsel.get(0).mobilnummer(), SMSVARSLER.mobilnummer());
		assertNull(smsVarsel.get(0).epostadresse());
		assertNull(smsVarsel.get(0).tittel());

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
		journalpostTestRepository.persist(journalpost);
		createVedleggRelasjon(journalpost, vedlegg2);
		journalpostTestRepository.persist(journalpost);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		String uri = UriComponentsBuilder.fromUriString(HENTJOURNALSAKINFO_HENTJOURNALPOST)
				.path(journalpost.getJournalpostId().toString())
				.build().toUriString();

		ResponseEntity<SafHentJournalpostResponse> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, createHeaderEntity(), SafHentJournalpostResponse.class);
		HentJournalpostDto responseJournalpost = responseEntity.getBody().getHentJournalpostDto();

		assertThat(responseJournalpost.getDokumenter(), hasSize(3));
		assertThat(responseJournalpost.getDokumenter().get(0).getDokumentInfoId(), is(hoveddokument.getDokumentInfoId()));
		assertThat(responseJournalpost.getDokumenter().get(1).getDokumentInfoId(), is(vedlegg1.getDokumentInfoId()));
		assertThat(responseJournalpost.getDokumenter().get(2).getDokumentInfoId(), is(vedlegg2.getDokumentInfoId()));
	}

	// Happy path
	@Test
	public void eksternReferanseIdWithSizeOver200ThrowsException() {
		Journalpost storedJournalpost = buildAndPersistJournalpost();
		storedJournalpost.setKanalReferanseId(KANAL_REFERANSE_ID_WITH_OVER_200_TEGN);

		String eksternReferanseId = storedJournalpost.getKanalReferanseId();

		String uri = UriComponentsBuilder.fromUriString(HENTJOURNALSAKINFO_HENTJOURNALPOST)
				.path("eksternreferanse/" + eksternReferanseId)
				.build().toUriString();

		ResponseEntity<SafHentJournalpostResponseForTest> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, createHeaderEntity(), SafHentJournalpostResponseForTest.class);

		assertThat(responseEntity.getStatusCode(), is(BAD_REQUEST));

	}

	//  Unhappy path
	@Test
	public void shouldFailToGetJournalpost() {
		buildAndPersistJournalpost();
		long journalpostId = 54321L;

		String uri = HENTJOURNALSAKINFO_HENTJOURNALPOST + journalpostId;
		ResponseEntity<SafHentJournalpostResponse> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, createHeaderEntity(), SafHentJournalpostResponse.class);

		assertThat(responseEntity.getStatusCode(), is(NOT_FOUND));
	}

	@Test
	public void shouldFailToGetJournalpostByEkstErnReferanseId() {
		buildAndPersistJournalpost();
		String ekstErnReferanseId = "ekstErnReferanseId";

		String uri = UriComponentsBuilder.fromUriString(HENTJOURNALSAKINFO_HENTJOURNALPOST)
				.path("eksternreferanse/" + ekstErnReferanseId)
				.build().toUriString();
		ResponseEntity<SafHentJournalpostResponse> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, createHeaderEntity(), SafHentJournalpostResponse.class);

		assertThat(responseEntity.getStatusCode(), is(NOT_FOUND));
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
		journalpost.setLestDato(OffsetDateTime.from(LocalDate.now().minusDays(3).atStartOfDay(ZoneId.systemDefault())));

		journalpost.getSaksrelasjon().setSakId(SAKID);
		journalpost.getSaksrelasjon().setFeilregistrert(SAKFEILREGISTRERT);
		journalpost.getSaksrelasjon().setFagsystem(SAKRELASJONFAGSYSTEM);

		DokumentInfo storedDokumentInfo = getDokumentInfoOfHoveddokument(journalpost);

		storedDokumentInfo.setDokumentstatus(DOKUMENTSTATUS);
		storedDokumentInfo.setBrevkode(BREVKODE);
		storedDokumentInfo.setTittel(TITTEL);
		storedDokumentInfo.setKassert(true);
		storedDokumentInfo.setSensitivt(true);

		journalpostTestRepository.persist(journalpost);
		utsendingsInfoTestRepository.persist(new UtsendingsInfo(journalpost, createNavNoVarsling(), createEpostVarsel(), createSmsVarsler()));
		TestTransaction.flagForCommit();
		TestTransaction.end();

		return journalpost;
	}

	private static UtsendingsInfo.SmsVarsler createSmsVarsler() {
		return new UtsendingsInfo.SmsVarsler(List.of(new UtsendingsInfo.SmsVarsel(VARSELTEKST, TELEFONNUMMER, "2023-03-01T11:00:00.000")));
	}

	private static UtsendingsInfo.EpostVarsler createEpostVarsel() {
		return new UtsendingsInfo.EpostVarsler(List.of(new UtsendingsInfo.EpostVarsel(TITTEL, VARSELTEKST, EPOSTADRESSE, "2023-03-01T11:00:00.000")));
	}


	public static UtsendingsInfo.NavNoVarsling createNavNoVarsling() {
		return new UtsendingsInfo.NavNoVarsling(DIGITALKONTAKT_INFORMASJON, null);
	}

	private DokumentInfo getDokumentInfoOfHoveddokument(Journalpost journalpost) {
		Set<JournalpostDokumentInfoRelasjon> hoveddokumentList = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT);
		JournalpostDokumentInfoRelasjon journalpostDokumentInfoRelasjon = hoveddokumentList.isEmpty() ? null : hoveddokumentList
				.iterator()
				.next();
		return journalpostDokumentInfoRelasjon.getDokumentInfo();
	}
}
