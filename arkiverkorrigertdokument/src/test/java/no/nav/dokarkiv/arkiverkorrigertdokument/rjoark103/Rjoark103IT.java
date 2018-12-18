package no.nav.dokarkiv.arkiverkorrigertdokument.rjoark103;

import static no.nav.dokarkiv.arkiverkorrigertdokument.util.TestUtils.FIL;
import static no.nav.dokarkiv.arkiverkorrigertdokument.util.TestUtils.opprettHoveddokumentForIT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Iterables;
import no.nav.dokarkiv.arkiverkorrigertdokument.AbstractArkiverKorrigertDokumentIT;
import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentFil;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import org.apache.commons.codec.binary.Base64;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

public class Rjoark103IT extends AbstractArkiverKorrigertDokumentIT {

	@Test
	public void shouldSaveFileAsSladdetVariant() {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());

		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		TestTransaction.flagForCommit();
		TestTransaction.end();


		HttpEntity httpEntity = new HttpEntity(ArkiverKorrigertDokumentRequest.builder()
				.dokumentInfoId(dokumentInfo.getDokumentInfoId())
				.fil(Base64.encodeBase64String(FIL))
				.build(), createHeaders());

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
		assertThat(persistedDokumentInfo.getFildetaljerListe().size(), is(2));
		assertThat(persistedDokumentInfo.findFilDetaljerByVariantFormat(VariantFormatCode.ARKIV), notNullValue());
		assertThat(persistedDokumentInfo.findFilDetaljerByVariantFormat(VariantFormatCode.SLADDET), notNullValue());
		assertThat(persistedDokumentInfo.findFilDetaljerByVariantFormat(VariantFormatCode.SLADDET)
				.getFiltype(), is(FilTypeCode.PDF));
		DokumentFil dokumentFil = dokumentFilRepository.findByFilUuid(persistedDokumentInfo.findFilDetaljerByVariantFormat(VariantFormatCode.SLADDET)
				.getFilUuid());
		assertThat(dokumentFil.getFil(), is(FIL));

		assertThat(begrensningRepository.findByDokumentInfoIdAndVariantFormatAndBegrensningType(dokumentInfo.getDokumentInfoId(), VariantFormatCode.ARKIV, BegrensningTypeCode.SKJERMET)
				.isPresent(), is(true));
		assertThat(Iterables.size(begrensningRepository.findAll()), is(1));
		TestTransaction.end();
	}

	@Test
	public void shouldDeleteExistingSladdetDokumentWhenSavingNew() {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());

		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		TestTransaction.flagForCommit();
		TestTransaction.end();


		HttpEntity httpEntity = new HttpEntity(ArkiverKorrigertDokumentRequest.builder()
				.dokumentInfoId(dokumentInfo.getDokumentInfoId())
				.fil(Base64.encodeBase64String(FIL))
				.build(), createHeaders());

		ResponseEntity<ArkiverKorrigertDokumentRespons> responseEntity = restTemplate.exchange(
				URL_ARKIVERKORRIGERTDOKUMENT,
				HttpMethod.POST,
				httpEntity,
				ArkiverKorrigertDokumentRespons.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		byte[] FIL2 = "NEW FILE".getBytes();
		HttpEntity httpEntity2 = new HttpEntity(ArkiverKorrigertDokumentRequest.builder()
				.dokumentInfoId(dokumentInfo.getDokumentInfoId())
				.fil(Base64.encodeBase64String(FIL2))
				.build(), createHeaders());

		ResponseEntity<ArkiverKorrigertDokumentRespons> responseEntity2 = restTemplate.exchange(
				URL_ARKIVERKORRIGERTDOKUMENT,
				HttpMethod.POST,
				httpEntity2,
				ArkiverKorrigertDokumentRespons.class);

		assertThat(responseEntity2.getStatusCode(), is(HttpStatus.OK));


		TestTransaction.start();
		assertTrue(dokumentinfoRepository.findByDokumentInfoId(dokumentInfo.getDokumentInfoId()).isPresent());
		DokumentInfo persistedDokumentInfo = dokumentinfoRepository.findByDokumentInfoId(dokumentInfo.getDokumentInfoId())
				.get();
		assertThat(persistedDokumentInfo.getFildetaljerListe().size(), is(2));
		assertThat(persistedDokumentInfo.getFildetaljerListe()
				.stream()
				.filter(detalj -> detalj.getVariantFormat().equals(VariantFormatCode.SLADDET))
				.count(), is(1L));
		assertThat(persistedDokumentInfo.findFilDetaljerByVariantFormat(VariantFormatCode.ARKIV), notNullValue());
		assertThat(persistedDokumentInfo.findFilDetaljerByVariantFormat(VariantFormatCode.SLADDET), notNullValue());
		assertThat(persistedDokumentInfo.findFilDetaljerByVariantFormat(VariantFormatCode.SLADDET)
				.getFiltype(), is(FilTypeCode.PDF));
		DokumentFil dokumentFil = dokumentFilRepository.findByFilUuid(persistedDokumentInfo.findFilDetaljerByVariantFormat(VariantFormatCode.SLADDET)
				.getFilUuid());
		assertThat(dokumentFil.getFil(), is(FIL2));
		assertThat(begrensningRepository.findByDokumentInfoIdAndVariantFormatAndBegrensningType(dokumentInfo.getDokumentInfoId(), VariantFormatCode.ARKIV, BegrensningTypeCode.SKJERMET)
				.isPresent(), is(true));
		assertThat(Iterables.size(begrensningRepository.findAll()), is(1));
		TestTransaction.end();
	}

//	@Test
//	public void shouldSaveFileAsSladdetVariantWhenDocumentOriginalJournalpostIsNull() {
//		abacPermit();
//
//		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());
//
//		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
//		dokumentInfo.setOriginalJournalpost(null);
//
//		TestTransaction.flagForCommit();
//		TestTransaction.end();
//
//
//		HttpEntity httpEntity = new HttpEntity(ArkiverKorrigertDokumentRequest.builder()
//				.dokumentInfoId(dokumentInfo.getDokumentInfoId())
//				.fil(Base64.encodeBase64String(FIL))
//				.build(), createHeaders());
//
//		ResponseEntity<ArkiverKorrigertDokumentRespons> responseEntity = restTemplate.exchange(
//				URL_ARKIVERKORRIGERTDOKUMENT,
//				HttpMethod.POST,
//				httpEntity,
//				ArkiverKorrigertDokumentRespons.class);
//
//		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
//
//		TestTransaction.start();
//		assertTrue(dokumentinfoRepository.findByDokumentInfoId(dokumentInfo.getDokumentInfoId()).isPresent());
//		DokumentInfo persistedDokumentInfo = dokumentinfoRepository.findByDokumentInfoId(dokumentInfo.getDokumentInfoId())
//				.get();
//		assertThat(persistedDokumentInfo.getFildetaljerListe().size(), is(2));
//		assertThat(persistedDokumentInfo.findFilDetaljerByVariantFormat(VariantFormatCode.ARKIV), notNullValue());
//		assertThat(persistedDokumentInfo.findFilDetaljerByVariantFormat(VariantFormatCode.SLADDET), notNullValue());
//		assertThat(persistedDokumentInfo.findFilDetaljerByVariantFormat(VariantFormatCode.SLADDET)
//				.getFiltype(), is(FilTypeCode.PDF));
//		DokumentFil dokumentFil = dokumentFilRepository.findByFilUuid(persistedDokumentInfo.findFilDetaljerByVariantFormat(VariantFormatCode.SLADDET)
//				.getFilUuid());
//		assertThat(dokumentFil.getFil(), is(FIL));
//
//		assertThat(begrensningRepository.findByDokumentInfoIdAndVariantFormatAndBegrensningType(dokumentInfo.getDokumentInfoId(), VariantFormatCode.ARKIV, BegrensningTypeCode.SKJERMET)
//				.isPresent(), is(true));
//		assertThat(Iterables.size(begrensningRepository.findAll()), is(1));
//		TestTransaction.end();
//	}


	@Test
	public void shouldFailWithBadRequestWhenDokumentInfoIdIsNull() {
		abacPermit();

		HttpEntity httpEntity = new HttpEntity(ArkiverKorrigertDokumentRequest.builder()
				.dokumentInfoId(null)
				.fil(Base64.encodeBase64String(FIL))
				.build(), createHeaders());

		ResponseEntity<ArkiverKorrigertDokumentRespons> responseEntity = restTemplate.exchange(
				URL_ARKIVERKORRIGERTDOKUMENT,
				HttpMethod.POST,
				httpEntity,
				ArkiverKorrigertDokumentRespons.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));

	}

	@Test
	public void shouldFailWithNotFoundWhenDokumentInfoIsNotFound() {
		abacPermit();

		HttpEntity httpEntity = new HttpEntity(ArkiverKorrigertDokumentRequest.builder()
				.dokumentInfoId(213213L)
				.fil(Base64.encodeBase64String(FIL))
				.build(), createHeaders());

		ResponseEntity<ArkiverKorrigertDokumentRespons> responseEntity = restTemplate.exchange(
				URL_ARKIVERKORRIGERTDOKUMENT,
				HttpMethod.POST,
				httpEntity,
				ArkiverKorrigertDokumentRespons.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));

	}

	@Test
	public void shouldNotAllowOperationIfNotSrvJoarkadminConsumer() {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());

		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		TestTransaction.flagForCommit();
		TestTransaction.end();


		HttpEntity httpEntity = new HttpEntity(ArkiverKorrigertDokumentRequest.builder()
				.dokumentInfoId(dokumentInfo.getDokumentInfoId())
				.fil(Base64.encodeBase64String(FIL))
				.build(), createHeadersNotSrvJoarkadmin());

		ResponseEntity<ArkiverKorrigertDokumentRespons> responseEntity = restTemplate.exchange(
				URL_ARKIVERKORRIGERTDOKUMENT,
				HttpMethod.POST,
				httpEntity,
				ArkiverKorrigertDokumentRespons.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.UNAUTHORIZED));


	}


}
