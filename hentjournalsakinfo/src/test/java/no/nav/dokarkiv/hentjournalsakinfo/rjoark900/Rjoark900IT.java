package no.nav.dokarkiv.hentjournalsakinfo.rjoark900;

import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.Sak;
import no.nav.dokarkiv.core.util.TestDataGenerator;
import no.nav.dokarkiv.hentjournalsakinfo.AbstractHentjournalsakinfoItest;
import no.nav.dokarkiv.hentjournalsakinfo.dto.DokumentInfoDto;
import no.nav.dokarkiv.hentjournalsakinfo.dto.JournalpostDto;
import no.nav.dokarkiv.hentjournalsakinfo.dto.VariantDto;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static no.nav.dokarkiv.core.domain.codes.InnsynCode.BRUK_STANDARDREGLER;
import static no.nav.dokarkiv.core.util.TestDataGenerator.AKTOER_ID;
import static no.nav.dokarkiv.core.util.TestDataGenerator.ANTALL_RETUR;
import static no.nav.dokarkiv.core.util.TestDataGenerator.API_GSAK_ID;
import static no.nav.dokarkiv.core.util.TestDataGenerator.API_PSAK_ID;
import static no.nav.dokarkiv.core.util.TestDataGenerator.AVSENDER_MOTTAKER_ID;
import static no.nav.dokarkiv.core.util.TestDataGenerator.AVSENDER_MOTTAKER_ID_TYPE;
import static no.nav.dokarkiv.core.util.TestDataGenerator.AVSENDER_MOTTAKER_LAND;
import static no.nav.dokarkiv.core.util.TestDataGenerator.AVSENDER_MOTTAKER_NAVN;
import static no.nav.dokarkiv.core.util.TestDataGenerator.BREVKODE;
import static no.nav.dokarkiv.core.util.TestDataGenerator.BRUKER_ID;
import static no.nav.dokarkiv.core.util.TestDataGenerator.DOKUMENT_INFO_TITTEL;
import static no.nav.dokarkiv.core.util.TestDataGenerator.DOKUMENT_TYPE_ID;
import static no.nav.dokarkiv.core.util.TestDataGenerator.FIL;
import static no.nav.dokarkiv.core.util.TestDataGenerator.FIL_NAVN;
import static no.nav.dokarkiv.core.util.TestDataGenerator.GSAK_FAGSAKNR;
import static no.nav.dokarkiv.core.util.TestDataGenerator.INNHOLD;
import static no.nav.dokarkiv.core.util.TestDataGenerator.JOURNALFOERENDE_ENHET;
import static no.nav.dokarkiv.core.util.TestDataGenerator.JOURNALFOERT_AV_NAVN;
import static no.nav.dokarkiv.core.util.TestDataGenerator.SKANNET_INNHOLD_TITTEL;
import static no.nav.dokarkiv.core.util.TestDataGenerator.TILLEGGOPPLYSNINGER_KEY;
import static no.nav.dokarkiv.core.util.TestDataGenerator.TILLEGGOPPLYSNINGER_VAL;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createDokumentInfo;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createDokumentInfoWithMoreData;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createPsakSaksrelasjon;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createSakForAktoerId;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createSaksrelasjon;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createVedleggRelasjon;
import static no.nav.dokarkiv.core.util.TestDataUtils.KANAL_REFERANSE_ID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Rjoark900IT extends AbstractHentjournalsakinfoItest {
	private static final String FINNJOURNALPOSTER_STATUS = "/hentjournalsakinfo/finnjournalposter";

	@Test
	public void shouldReturnEmptyResponseWhenNotFound() {
		FinnJournalposterResponseTo responseTo = finnJournalposterRest(createRequest(JournalStatusCode.U));
		assertThat(responseTo.getTilgangJournalposter(), hasSize(0));
	}

	@Test
	public void shouldFindAllJournalpostWithJournalstatusFS() {
		Journalpost ferdigstiltJournalpost1 = createUniqueJournalpost();
		Journalpost ferdigstiltJournalpost2 = createUniqueJournalpost();
		journalpostTestRepository.persist(ferdigstiltJournalpost1);
		journalpostTestRepository.persist(ferdigstiltJournalpost2);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		FinnJournalposterRequestTo request = createRequest(JournalStatusCode.FS);
		request.setFoerste(2);
		FinnJournalposterResponseTo responseTo = finnJournalposterRest(request);

		assertThat(responseTo.getTilgangJournalposter(), hasSize(2));
	}

	@Test
	public void shouldFindAllJournalpostForGsakAndPsak() {
		Journalpost gsakJournalpost = createUniqueJournalpost();
		Journalpost psakJournalpost = createUniqueJournalpost();
		psakJournalpost.setSaksrelasjon(createPsakSaksrelasjon());
		journalpostTestRepository.persist(gsakJournalpost);
		journalpostTestRepository.persist(psakJournalpost);
		TestTransaction.flagForCommit();
		TestTransaction.end();
		FinnJournalposterRequestTo request = createRequest(JournalStatusCode.FS);
		request.setFoerste(2);
		request.setPsakSakIds(Collections.singletonList(API_PSAK_ID));

		FinnJournalposterResponseTo responseTo = finnJournalposterRest(request);

		assertThat(responseTo.getTilgangJournalposter(), hasSize(2));
	}

	@Test
	public void shouldMapMetadata() {
		populateInnsyn();
		Sak sak = createSakForAktoerId(FagomradeCode.RPO.name(), AKTOER_ID, "AO01", GSAK_FAGSAKNR);
		sakTestRepository.persist(sak);
		DokumentInfo vedlegg2 = createDokumentInfo();
		DokumentInfo vedlegg1 = createDokumentInfo();
		Journalpost journalpost = createUniqueJournalpost();
		createVedleggRelasjon(journalpost, vedlegg1);
		createVedleggRelasjon(journalpost, vedlegg2);
		journalpost.setSaksrelasjon(createSaksrelasjon(journalpost, sak.getSakId()));
		journalpostTestRepository.persist(journalpost);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		FinnJournalposterRequestTo request = createRequest(JournalStatusCode.FS, sak.getSakId().toString());
		request.setFoerste(1);
		FinnJournalposterResponseTo responseTo = finnJournalposterRest(request);

		assertThat(responseTo.getTilgangJournalposter(), hasSize(1));
		JournalpostDto journalpostDto = responseTo.getTilgangJournalposter().get(0);

		assertThat(journalpostDto.getJournalpostId(), is(journalpost.getJournalpostId()));
		assertNull(journalpostDto.getPrevJournalpostId());
		assertNull(journalpostDto.getNextJournalpostId());
		assertThat(journalpostDto.getTotaltAntall(), is(1L));
		assertThat(journalpostDto.getInnhold(), is(INNHOLD));
		assertThat(journalpostDto.getFagomrade(), is(FagomradeCode.RPO.name()));
		assertNull(journalpostDto.getBehandlingstema());
		assertNull(journalpostDto.getBehandlingstemanavn());
		assertThat(journalpostDto.getJournalstatus(), is(JournalStatusCode.FS.name()));
		assertThat(journalpostDto.getAvsenderMottakerId(), is(AVSENDER_MOTTAKER_ID));
		assertThat(journalpostDto.getAvsenderMottakerIdType(), is(AVSENDER_MOTTAKER_ID_TYPE.name()));
		assertThat(journalpostDto.getAvsenderMottakerNavn(), is(AVSENDER_MOTTAKER_NAVN));
		assertThat(journalpostDto.getAvsenderMottakerLand(), is(AVSENDER_MOTTAKER_LAND));
		assertThat(journalpostDto.getJournalforendeEnhet(), is(JOURNALFOERENDE_ENHET));
		assertThat(journalpostDto.getJournalfortAvNavn(), is(JOURNALFOERT_AV_NAVN));
		assertThat(journalpostDto.getOpprettetAvNavn(), is(TestDataGenerator.OPPRETTET_AV_NAVN));
		assertThat(journalpostDto.getMottakskanal(), is(MottaksKanalCode.NAV_NO.name()));
		assertThat(journalpostDto.getUtsendingskanal(), is(UtsendingsKanalCode.NAV_NO.name()));
		assertThat(journalpostDto.getJournalposttype(), is(JournalpostTypeCode.U.name()));
		assertNotNull(journalpostDto.getDatoOpprettet());
		assertNull(journalpostDto.getMottattDato());
		assertNotNull(journalpostDto.getJournalDato());
		assertNotNull(journalpostDto.getDokumentDato());
		assertNotNull(journalpostDto.getAvsReturDato());
		assertNotNull(journalpostDto.getSendtPrintDato());
		assertNotNull(journalpostDto.getEkspedertDato());
		assertNotNull(journalpostDto.getLestDato());
		assertNull(journalpostDto.getSkjerming());
		assertThat(journalpostDto.getAntallRetur(), is(ANTALL_RETUR.toString()));
		assertThat(journalpostDto.getKanalReferanseId(), startsWith(KANAL_REFERANSE_ID));
		assertThat(journalpostDto.getInnsyn(), is(BRUK_STANDARDREGLER.name()));
		assertThat(journalpostDto.getInnsynbeskrivelse(), is("beskrivelse av " + BRUK_STANDARDREGLER));

		assertThat(journalpostDto.getSaksrelasjon().getAktoerId(), is(AKTOER_ID));
		assertThat(journalpostDto.getSaksrelasjon().getApplikasjon(), is("AO01"));
		assertNull(journalpostDto.getSaksrelasjon().getOrgnr());
		assertThat(journalpostDto.getSaksrelasjon().getFagsystem(), is(FagsystemCode.FS22.name()));
		assertThat(journalpostDto.getSaksrelasjon().getTema(), is(FagomradeCode.RPO.name()));
		assertFalse(journalpostDto.getSaksrelasjon().isFeilregistrert());
		assertThat(journalpostDto.getSaksrelasjon().getSakId(), is(sak.getSakId().toString()));
		assertThat(journalpostDto.getSaksrelasjon().getOpprettetAv(), is("Donald Duck"));
		assertThat(journalpostDto.getSaksrelasjon().getFagsakNr(), is(GSAK_FAGSAKNR));

		assertThat(journalpostDto.getBruker().getBrukerId(), is(BRUKER_ID));
		assertThat(journalpostDto.getBruker().getBrukerIdType(), is(BrukerTypeCode.PERSON.name()));

		assertThat(journalpostDto.getTilleggsopplysninger().get(0).getNokkel(), is(TILLEGGOPPLYSNINGER_KEY));
		assertThat(journalpostDto.getTilleggsopplysninger().get(0).getVerdi(), is(TILLEGGOPPLYSNINGER_VAL));

		assertThat(journalpostDto.getDokumenter(), hasSize(3));
		assertHoveddokument(journalpostDto.getDokumenter().get(0));
		assertVedlegg(journalpostDto.getDokumenter().get(1));
		assertVedlegg(journalpostDto.getDokumenter().get(2));
	}

	private static void assertHoveddokument(DokumentInfoDto dokumentInfo) {
		assertDokumentInfo(dokumentInfo);
		assertNotNull(dokumentInfo.getOrigJournalpostId());
	}

	private static void assertVedlegg(DokumentInfoDto dokumentInfo) {
		assertDokumentInfo(dokumentInfo);
		assertNull(dokumentInfo.getOrigJournalpostId());
	}

	private static void assertDokumentInfo(DokumentInfoDto dokumentInfo) {
		assertNotNull(dokumentInfo.getDokumentInfoId());
		assertThat(dokumentInfo.getDokumentstatus(), is(DokumentStatusCode.FERDIGSTILT.name()));
		assertNotNull(dokumentInfo.getDatoFerdigstilt());
		assertThat(dokumentInfo.getBrevkode(), is(BREVKODE));
		assertThat(dokumentInfo.getDokumenttypeId(), is(DOKUMENT_TYPE_ID));
		assertThat(dokumentInfo.getTittel(), is(DOKUMENT_INFO_TITTEL));
		assertNull(dokumentInfo.getSkjerming());
		assertFalse(dokumentInfo.isKassert());
		assertThat(dokumentInfo.getKategori(), is(DokumentKategoriCode.ES.name()));
		assertTrue(dokumentInfo.isSensitivt());

		assertNotNull(dokumentInfo.getLogiske().get(0).getVedleggId());
		assertThat(dokumentInfo.getLogiske().get(0).getTittel(), is(SKANNET_INNHOLD_TITTEL));

		assertThat(dokumentInfo.getVarianter(), hasSize(2));
		assertVariant(dokumentInfo.getVarianter(), VariantFormatCode.ARKIV);
		assertVariant(dokumentInfo.getVarianter(), VariantFormatCode.PRODUKSJON);
	}

	private static void assertVariant(List<VariantDto> varianter, VariantFormatCode variantFormatCode) {
		VariantDto variant = varianter.stream().filter(h -> VariantFormatCode.valueOf(h.getVariantf()) == variantFormatCode).findFirst().get();
		assertThat(variant.getVariantf(), is(variantFormatCode.name()));
		assertThat(variant.getFilnavn(), is(FIL_NAVN));
		assertNotNull(variant.getFiluuid());
		assertThat(variant.getFiltype(), is(FilTypeCode.PDF.name()));
		assertThat(variant.getFilstorrelse(), is(String.valueOf(FIL.length)));
		assertNull(variant.getSkjerming());
	}

	@Test
	public void shouldReturnVedleggOrderedByRelasjonId() {
		populateInnsyn();
		DokumentInfo vedlegg2 = createDokumentInfo();
		dokumentInfoRepository.persist(vedlegg2);
		DokumentInfo vedlegg1 = createDokumentInfo();
		dokumentInfoRepository.persist(vedlegg1);
		Journalpost journalpost = createUniqueJournalpost();
		DokumentInfo hoveddokument = journalpost.getDokumentInfoFromJpDokInfoRelasjoner(0);
		createVedleggRelasjon(journalpost, vedlegg1);
		journalpostTestRepository.persist(journalpost);
		createVedleggRelasjon(journalpost, vedlegg2);
		journalpostTestRepository.persist(journalpost);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		FinnJournalposterRequestTo request = createRequest(JournalStatusCode.FS);
		request.setFoerste(1);
		FinnJournalposterResponseTo responseTo = finnJournalposterRest(request);

		assertThat(responseTo.getTilgangJournalposter(), hasSize(1));
		JournalpostDto journalpostDto = responseTo.getTilgangJournalposter().get(0);
		assertThat(journalpostDto.getDokumenter(), hasSize(3));
		assertThat(journalpostDto.getDokumenter().get(0).getDokumentInfoId(), is(hoveddokument.getDokumentInfoId()));
		assertThat(journalpostDto.getDokumenter().get(1).getDokumentInfoId(), is(vedlegg1.getDokumentInfoId()));
		assertThat(journalpostDto.getDokumenter().get(2).getDokumentInfoId(), is(vedlegg2.getDokumentInfoId()));
		assertThat(journalpostDto.getInnsyn(), is(BRUK_STANDARDREGLER.name()));
		assertThat(journalpostDto.getInnsynbeskrivelse(), is("beskrivelse av " + BRUK_STANDARDREGLER));
	}

	@Test
	public void shouldReturnNewDokumentInfoValues() {
		DokumentInfo vedlegg = createDokumentInfoWithMoreData();
		Journalpost journalpost = createUniqueJournalpost();
		createVedleggRelasjon(journalpost, vedlegg);
		journalpostTestRepository.persist(journalpost);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		FinnJournalposterRequestTo request = createRequest(JournalStatusCode.FS);
		request.setFoerste(1);
		FinnJournalposterResponseTo responseTo = finnJournalposterRest(request);

		DokumentInfoDto dokumentInfoDto = responseTo.getTilgangJournalposter().get(0).getDokumenter().get(1);

		assertThat(dokumentInfoDto.getKategori(), is(DokumentKategoriCode.B.name()));
		assertThat(dokumentInfoDto.isSensitivt(), is(true));
	}

	@Test
	void shouldTestPadding() {
		finnJournalposterRest(createPaddingRequest(1, 1));
		finnJournalposterRest(createPaddingRequest(6, 6));
		finnJournalposterRest(createPaddingRequest(103, 103));
	}

	private FinnJournalposterRequestTo createRequest(JournalStatusCode journalStatusCode) {
		return createRequest(journalStatusCode, API_GSAK_ID);
	}

	private FinnJournalposterRequestTo createRequest(JournalStatusCode journalStatusCode, String sakId) {
		FinnJournalposterRequestTo requestTo = new FinnJournalposterRequestTo();
		requestTo.setFraDato("2019-01-01");
		requestTo.setGsakSakIds(Collections.singletonList(sakId));
		requestTo.setInkluderJournalStatus(Collections.singletonList(journalStatusCode));
		requestTo.setInkluderJournalpostType(Arrays.asList(JournalpostTypeCode.I, JournalpostTypeCode.U, JournalpostTypeCode.N));
		requestTo.setFoerste(1);
		return requestTo;
	}

	private FinnJournalposterRequestTo createPaddingRequest(int antallGsaker, int antallPsaker) {
		FinnJournalposterRequestTo requestTo = new FinnJournalposterRequestTo();
		requestTo.setFraDato("2019-01-01");
		requestTo.setGsakSakIds(IntStream.range(0, antallGsaker).mapToObj(i -> "gsak" + i).toList());
		requestTo.setPsakSakIds(IntStream.range(0, antallPsaker).mapToObj(i -> "psak" + i).toList());
		requestTo.setInkluderJournalStatus(Collections.singletonList(JournalStatusCode.J));
		requestTo.setInkluderJournalpostType(Arrays.asList(JournalpostTypeCode.I, JournalpostTypeCode.U, JournalpostTypeCode.N));
		requestTo.setFoerste(1);
		return requestTo;
	}

	private FinnJournalposterResponseTo finnJournalposterRest(FinnJournalposterRequestTo finnJournalposterRequestTo) {
		HttpEntity<FinnJournalposterRequestTo> requestEntity = new HttpEntity<>(finnJournalposterRequestTo, createDefaultHeaders());
		ResponseEntity<Object> exchange = restTemplate.exchange(FINNJOURNALPOSTER_STATUS, HttpMethod.POST, requestEntity, Object.class);
		if (exchange.getStatusCode() == HttpStatus.OK) {
			return objectMapper.convertValue(exchange.getBody(), FinnJournalposterResponseTo.class);
		} else {
			throw new HttpClientErrorException(exchange.getStatusCode());
		}
	}
}
