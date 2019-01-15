package no.nav.dokarkiv.arkiverkorrigertdokument.rjoark104;

import static no.nav.dokarkiv.arkiverkorrigertdokument.util.TestUtils.FIL;
import static no.nav.dokarkiv.arkiverkorrigertdokument.util.TestUtils.begrensArkivVariantAvDokumentSomSkjermet;
import static no.nav.dokarkiv.arkiverkorrigertdokument.util.TestUtils.opprettHoveddokumentForIT;
import static no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService.AKSJONS_INFO_HEADER;
import static no.nav.dokarkiv.core.util.TestDataUtils.AKSJON_ANGRE_ARKIVER_DOKUMENT;
import static no.nav.dokarkiv.core.util.TestDataUtils.AKSJON_ARKIVER_DOKUMENT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Iterables;
import no.nav.dokarkiv.arkiverkorrigertdokument.AbstractArkiverKorrigertDokumentIT;
import no.nav.dokarkiv.arkiverkorrigertdokument.rjoark103.ArkiverKorrigertDokumentRequest;
import no.nav.dokarkiv.arkiverkorrigertdokument.rjoark103.ArkiverKorrigertDokumentRespons;
import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;
import no.nav.dokarkiv.core.domain.entities.DokumentFil;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections15.IteratorUtils;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

import java.io.IOException;
import java.util.List;

public class Rjoark104IT extends AbstractArkiverKorrigertDokumentIT {

	@Test
	public void skalLagreAksjon() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());

		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		DokumentFil dokumentFil = dokumentInfo.findFilDetaljerByVariantFormat(VariantFormatCode.ARKIV).createDokumentFil();
		dokumentFil.setOpprettetKildeNavn(OPPRETTET_KILDE_NAVN);

		dokumentFilRepository.save(dokumentFil);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		assertArkiverKorrigertDokument(dokumentInfo);
		aksjonsLoggRepository.deleteAll();

		List<AksjonsLogg> aksjonsLoggListBefore = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggListBefore.size(), is(0));

		ResponseEntity<ArkiverKorrigertDokumentRespons> responseEntity = restTemplate.exchange(
				URL_ANGREARKIVERKORRIGERTDOKUMENT + dokumentInfo.getDokumentInfoId(),
				HttpMethod.POST,
				new HttpEntity<>(createHeadersWithAksjon(AKSJON_ANGRE_ARKIVER_DOKUMENT)),
				ArkiverKorrigertDokumentRespons.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(1));
		assertThat(aksjonsLoggList.get(0).getAksjon(), is(AKSJON_ANGRE_ARKIVER_DOKUMENT));
	}

	@Test
	public void skalFeileNårAksjonsLoggHeaderIkkeErSatt() {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());

		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		DokumentFil dokumentFil = dokumentInfo.findFilDetaljerByVariantFormat(VariantFormatCode.ARKIV).createDokumentFil();
		dokumentFil.setOpprettetKildeNavn(OPPRETTET_KILDE_NAVN);

		dokumentFilRepository.save(dokumentFil);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		List<AksjonsLogg> aksjonsLoggListBefore = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggListBefore.size(), is(0));

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_ANGREARKIVERKORRIGERTDOKUMENT + dokumentInfo.getDokumentInfoId(),
				HttpMethod.POST,
				new HttpEntity<>(createHeaders()),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
		assertThat(responseEntity.getBody(), containsString(String.format("Missing request header '%s'", AKSJONS_INFO_HEADER)));

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(0));
	}

	@Test
	public void skalIkkeLagreAksjonsLoggVedFeil() throws IOException {
		abacPermit();


		List<AksjonsLogg> aksjonsLoggListBefore = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggListBefore.size(), is(0));

		ResponseEntity<ArkiverKorrigertDokumentRespons> responseEntity = restTemplate.exchange(
				URL_ANGREARKIVERKORRIGERTDOKUMENT + 1,
				HttpMethod.POST,
				new HttpEntity<>(createHeadersWithAksjon(AKSJON_ANGRE_ARKIVER_DOKUMENT)),
				ArkiverKorrigertDokumentRespons.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(0));
	}


	@Test
	public void skallIkkeAngreArkiverKorrigertDokument_ettersomDokumentInfoErNull() throws IOException {
		abacPermit();

		Long dokumentInfoId = null;

		ResponseEntity<ArkiverKorrigertDokumentRespons> responseEntity = restTemplate.exchange(
				URL_ANGREARKIVERKORRIGERTDOKUMENT + dokumentInfoId,
				HttpMethod.POST,
				new HttpEntity<>(createHeadersWithAksjon(AKSJON_ARKIVER_DOKUMENT)),
				ArkiverKorrigertDokumentRespons.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
	}

	@Test
	public void skallIkkeAngreArkiverKorrigertDokument_ettersomDokumentInfoIkkeFinnes() throws IOException {
		abacPermit();

		Long dokumentInfoId = 23042304L;

		ResponseEntity<ArkiverKorrigertDokumentRespons> responseEntity = restTemplate.exchange(
				URL_ANGREARKIVERKORRIGERTDOKUMENT + dokumentInfoId,
				HttpMethod.POST,
				new HttpEntity<>(createHeadersWithAksjon(AKSJON_ARKIVER_DOKUMENT)),
				ArkiverKorrigertDokumentRespons.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));
	}

	@Test
	public void skallIkkeAngreArkiverKorrigertDokument_ettersomDokumentInfoIkkeErBegrenset() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());

		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		ResponseEntity<ArkiverKorrigertDokumentRespons> responseEntity = restTemplate.exchange(
				URL_ANGREARKIVERKORRIGERTDOKUMENT + dokumentInfo.getDokumentInfoId(),
				HttpMethod.POST,
				new HttpEntity<>(createHeadersWithAksjon(AKSJON_ARKIVER_DOKUMENT)),
				ArkiverKorrigertDokumentRespons.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));
	}

	@Test
	public void skallIkkeAngreArkiverKorrigertDokument_ettersomDokumentIkkeHarSladdetVariantFormat() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());

		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		begrensningRepository.save(begrensArkivVariantAvDokumentSomSkjermet(dokumentInfo));

		ResponseEntity<ArkiverKorrigertDokumentRespons> responseEntity = restTemplate.exchange(
				URL_ANGREARKIVERKORRIGERTDOKUMENT,
				HttpMethod.POST,
				new HttpEntity<>(createHeadersWithAksjon(AKSJON_ARKIVER_DOKUMENT)),
				ArkiverKorrigertDokumentRespons.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));
	}


	@Test
	public void skallAngreArkiverKorrigertDokument() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());

		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		DokumentFil dokumentFil = dokumentInfo.findFilDetaljerByVariantFormat(VariantFormatCode.ARKIV).createDokumentFil();
		dokumentFil.setOpprettetKildeNavn(OPPRETTET_KILDE_NAVN);

		dokumentFilRepository.save(dokumentFil);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		assertArkiverKorrigertDokument(dokumentInfo);
		assertAngreArkiverKorrigertDokument(dokumentInfo);

	}

	public void assertArkiverKorrigertDokument(DokumentInfo dokumentInfo) throws IOException {
		HttpEntity httpEntity = new HttpEntity(ArkiverKorrigertDokumentRequest.builder()
				.dokumentInfoId(dokumentInfo.getDokumentInfoId())
				.fil(Base64.encodeBase64String(FIL))
				.build(),
				createHeadersWithAksjon(AKSJON_ARKIVER_DOKUMENT)
		);


		ResponseEntity<ArkiverKorrigertDokumentRespons> responseEntity = restTemplate.exchange(
				URL_ARKIVERKORRIGERTDOKUMENT,
				HttpMethod.POST,
				httpEntity,
				ArkiverKorrigertDokumentRespons.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		TestTransaction.start();
		assertTrue(dokumentinfoRepository.findByDokumentInfoId(dokumentInfo.getDokumentInfoId()).isPresent());
		DokumentInfo persistedDokumentInfo = dokumentinfoRepository.findByDokumentInfoId(dokumentInfo.getDokumentInfoId())
				.get();
		//DokumentFil tester
		assertThat(persistedDokumentInfo.getFildetaljerListe().size(), is(2));
		assertThat(persistedDokumentInfo.findFilDetaljerByVariantFormat(VariantFormatCode.ARKIV), notNullValue());
		assertThat(persistedDokumentInfo.findFilDetaljerByVariantFormat(VariantFormatCode.SLADDET), notNullValue());
		assertThat(dokumentFilRepository.findByFilUuid(persistedDokumentInfo.findFilDetaljerByVariantFormat(VariantFormatCode.ARKIV)
				.getFilUuid()), notNullValue());
		assertThat(dokumentFilRepository.findByFilUuid(persistedDokumentInfo.findFilDetaljerByVariantFormat(VariantFormatCode.SLADDET)
				.getFilUuid()), notNullValue());
		TestTransaction.end();

	}

	public void assertAngreArkiverKorrigertDokument(DokumentInfo dokumentInfo) throws IOException {
		ResponseEntity<ArkiverKorrigertDokumentRespons> responseEntity = restTemplate.exchange(
				URL_ANGREARKIVERKORRIGERTDOKUMENT + dokumentInfo.getDokumentInfoId(),
				HttpMethod.POST,
				new HttpEntity<>(createHeadersWithAksjon(AKSJON_ARKIVER_DOKUMENT)),
				ArkiverKorrigertDokumentRespons.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		TestTransaction.start();
		assertTrue(dokumentinfoRepository.findByDokumentInfoId(dokumentInfo.getDokumentInfoId()).isPresent());
		DokumentInfo persistedDokumentInfo = dokumentinfoRepository.findByDokumentInfoId(dokumentInfo.getDokumentInfoId())
				.get();

		// Dokumentet finnes men SLADDET variant er slettet
		assertThat(persistedDokumentInfo.getFildetaljerListe().size(), is(1));
		assertThat(persistedDokumentInfo.findFilDetaljerByVariantFormat(VariantFormatCode.ARKIV), notNullValue());
		assertNull(persistedDokumentInfo.findFilDetaljerByVariantFormat(VariantFormatCode.SLADDET));
		assertThat(dokumentFilRepository.findByFilUuid(dokumentInfo.findFilDetaljerByVariantFormat(VariantFormatCode.ARKIV)
				.getFilUuid()), notNullValue());

		// Begrensning er slettet
		assertFalse(begrensningRepository.findByDokumentInfoIdAndVariantFormatAndBegrensningType(
				dokumentInfo.getDokumentInfoId(), VariantFormatCode.SLADDET, BegrensningTypeCode.SKJERMET).isPresent());
		assertThat(Iterables.size(begrensningRepository.findAll()), is(0));
		TestTransaction.end();
	}

	@Test
	public void noAccess() {
		abacPermit();

		ResponseEntity<ArkiverKorrigertDokumentRespons> responseEntity = restTemplate.exchange(
				URL_ARKIVERKORRIGERTDOKUMENT,
				HttpMethod.POST,
				createNoAccesHeaders(),
				ArkiverKorrigertDokumentRespons.class);


		assertThat(responseEntity.getStatusCode(), is(HttpStatus.UNAUTHORIZED));
	}
}
