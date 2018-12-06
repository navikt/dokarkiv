package no.nav.dokarkiv.arkiverkorrigertdokument.rjoark103;

import static no.nav.dokarkiv.arkiverkorrigertdokument.util.TestUtils.opprettHoveddokumentForIT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertTrue;

import no.nav.dokarkiv.arkiverkorrigertdokument.AbstractArkiverKorrigertDokumentIT;
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

	private byte[] FIL = "TEEEST".getBytes();

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
		TestTransaction.end();
	}

	@Test
	public void shouldFailWithBadRequestWhenDokumentInfoIdIsNull() {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());

		TestTransaction.flagForCommit();
		TestTransaction.end();


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


}
