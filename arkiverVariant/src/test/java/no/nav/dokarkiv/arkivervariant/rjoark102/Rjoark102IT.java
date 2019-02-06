package no.nav.dokarkiv.arkivervariant.rjoark102;

import static no.nav.dokarkiv.arkivervariant.util.TestUtils.FIL;
import static no.nav.dokarkiv.arkivervariant.util.TestUtils.opprettHoveddokumentForIT;
import static no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService.AKSJONS_LOGG_HEADER;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertTrue;

import no.nav.dokarkiv.arkivervariant.AbstractArkiverVariantIT;
import no.nav.dokarkiv.core.domain.codes.AksjonTypeCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
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

public class Rjoark102IT extends AbstractArkiverVariantIT {

	@Test
	public void skalFeileNårAksjonsLoggHeaderIkkeErSatt() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());

		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		TestTransaction.flagForCommit();
		TestTransaction.end();

		List<AksjonsLogg> aksjonsLoggListBefore = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggListBefore.size(), is(0));

		HttpEntity httpEntity = new HttpEntity(Base64.encodeBase64String(FIL), createHeaders());

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_ARKIVERVARIANT + dokumentInfo.getDokumentInfoId() + "/" + VariantFormatCode.SLADDET.name(),
				HttpMethod.POST,
				httpEntity,
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
		assertThat(responseEntity.getBody(), containsString(String.format("Missing request header '%s'", AKSJONS_LOGG_HEADER)));

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(0));
	}

	@Test
	public void skalIkkeLagreAksjonsLoggVedFeil() throws IOException {
		abacPermit();

		List<AksjonsLogg> aksjonsLoggListBefore = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggListBefore.size(), is(0));

		HttpEntity httpEntity = new HttpEntity(Base64.encodeBase64String(FIL), createHeadersWithAksjon(AksjonTypeCode.ARKIVERING.name()));

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_ARKIVERVARIANT + 13123L,
				HttpMethod.POST,
				httpEntity,
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(0));
	}

	@Test
	public void shouldSaveFileAsSladdetVariant() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());

		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		TestTransaction.flagForCommit();
		TestTransaction.end();


		HttpEntity httpEntity = new HttpEntity(Base64.encodeBase64String(FIL), createHeadersWithAksjon(AksjonTypeCode.ARKIVERING.name()));

		ResponseEntity<ArkiverVariantResponse> responseEntity = restTemplate.exchange(
				URL_ARKIVERVARIANT + dokumentInfo.getDokumentInfoId()+ "/" + VariantFormatCode.SLADDET.name(),
				HttpMethod.POST,
				httpEntity,
				ArkiverVariantResponse.class);

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

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(1));
		assertThat(aksjonsLoggList.get(0).getAksjon(), is(AksjonTypeCode.ARKIVERING));
	}

	@Test
	public void FailWithBadRequestWhenVariantAlreadyExists() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());

		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		TestTransaction.flagForCommit();
		TestTransaction.end();


		HttpEntity httpEntity = new HttpEntity(Base64.encodeBase64String(FIL), createHeadersWithAksjon(AksjonTypeCode.ARKIVERING.name()));

		ResponseEntity<ArkiverVariantResponse> responseEntity = restTemplate.exchange(
				URL_ARKIVERVARIANT + dokumentInfo.getDokumentInfoId() + "/" + VariantFormatCode.SLADDET.name(),
				HttpMethod.POST,
				httpEntity,
				ArkiverVariantResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		byte[] FIL2 = "NEW FILE".getBytes();
		HttpEntity httpEntity2 = new HttpEntity(Base64.encodeBase64String(FIL2), createHeadersWithAksjon(AksjonTypeCode.ARKIVERING.name()));

		ResponseEntity<ArkiverVariantResponse> responseEntity2 = restTemplate.exchange(
				URL_ARKIVERVARIANT + dokumentInfo.getDokumentInfoId() + "/" + VariantFormatCode.SLADDET.name(),
				HttpMethod.POST,
				httpEntity2,
				ArkiverVariantResponse.class);
		assertThat(responseEntity2.getStatusCode(), is(HttpStatus.BAD_REQUEST));
	}

	@Test
	public void shouldFailWithBadRequestWhenVariantIsInvalid() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());

		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		TestTransaction.flagForCommit();
		TestTransaction.end();

		HttpEntity httpEntity = new HttpEntity(Base64.encodeBase64String(FIL), createHeadersWithAksjon(AksjonTypeCode.ARKIVERING.name()));

		ResponseEntity<ArkiverVariantResponse> responseEntity = restTemplate.exchange(
				URL_ARKIVERVARIANT + dokumentInfo.getDokumentInfoId() + "/def",
				HttpMethod.POST,
				httpEntity,
				ArkiverVariantResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));

	}

	@Test
	public void shouldFailWithNotFoundWhenDokumentInfoIsNotFound() throws IOException {
		abacPermit();

		HttpEntity httpEntity = new HttpEntity(Base64.encodeBase64String(FIL), createHeadersWithAksjon(AksjonTypeCode.ARKIVERING.name()));


		ResponseEntity<ArkiverVariantResponse> responseEntity = restTemplate.exchange(
				URL_ARKIVERVARIANT + 1231223L + "/" + VariantFormatCode.SLADDET.name(),
				HttpMethod.POST,
				httpEntity,
				ArkiverVariantResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));

	}

	@Test
	public void shouldNotAllowOperationIfNotSrvJoarkadminConsumer() {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());

		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		TestTransaction.flagForCommit();
		TestTransaction.end();

		HttpEntity httpEntity = new HttpEntity(Base64.encodeBase64String(FIL), createHeadersNotSrvJoarkadmin());

		ResponseEntity<ArkiverVariantResponse> responseEntity = restTemplate.exchange(
				URL_ARKIVERVARIANT + dokumentInfo.getDokumentInfoId() + "/" + VariantFormatCode.SLADDET.name(),
				HttpMethod.POST,
				httpEntity,
				ArkiverVariantResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.UNAUTHORIZED));


	}


}
