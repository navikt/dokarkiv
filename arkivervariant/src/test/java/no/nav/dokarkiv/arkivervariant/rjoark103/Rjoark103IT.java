package no.nav.dokarkiv.arkivervariant.rjoark103;

import no.nav.dokarkiv.arkivervariant.AbstractArkiverVariantIT;
import no.nav.dokarkiv.core.consumer.RestConsumerExceptionResponse;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;
import no.nav.dokarkiv.core.domain.entities.ArkivElementEndring;
import no.nav.dokarkiv.core.domain.entities.DokumentFil;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.util.TestDataUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections15.IteratorUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static no.nav.dokarkiv.arkivervariant.util.TestUtils.FIL;
import static no.nav.dokarkiv.arkivervariant.util.TestUtils.opprettHoveddokumentForIT;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.FILDETALJER_FILUUID;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.FILDETALJER_VARIANTFORMAT;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.SLADDET;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Rjoark103IT extends AbstractArkiverVariantIT {

	@Test
	public void shouldSaveFileAsSladdetVariant() throws IOException {
		abacPermit();

		Journalpost journalpost = journalpostRepository.save(opprettHoveddokumentForIT());

		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ArkiverVariantRequest request = ArkiverVariantRequest.builder()
				.dokumentInfoId(dokumentInfo.getDokumentInfoId())
				.fil(Base64.encodeBase64String(FIL))
				.filnavn("filnavn")
				.variant(SLADDET)
				.filType(FilTypeCode.PDF).build();

		var httpEntity = new HttpEntity<>(request, createHeadersWithAksjon());

		ResponseEntity<ArkiverVariantResponse> responseEntity = restTemplate.exchange(
				URL_ARKIVERVARIANT,
				HttpMethod.POST,
				httpEntity,
				ArkiverVariantResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		TestTransaction.start();
		assertTrue(dokumentInfoTestRepository.findById(dokumentInfo.getDokumentInfoId()).isPresent());
		DokumentInfo persistedDokumentInfo = dokumentInfoTestRepository.findById(dokumentInfo.getDokumentInfoId())
				.get();
		assertThat(persistedDokumentInfo.getFildetaljerListeAdmin().size(), is(2));
		assertThat(persistedDokumentInfo.findFilDetaljerByVariantFormat(VariantFormatCode.ARKIV), notNullValue());
		assertThat(persistedDokumentInfo.findFilDetaljerByVariantFormat(SLADDET), notNullValue());
		assertThat(persistedDokumentInfo.findFilDetaljerByVariantFormat(SLADDET)
				.getFiltype(), is(FilTypeCode.PDF));
		DokumentFil dokumentFil = dokumentFilTestRepository.findByFilUuid(persistedDokumentInfo.findFilDetaljerByVariantFormat(SLADDET)
				.getFilUuid());
		assertThat(dokumentFil.getFil(), is(FIL));
		assertThat(responseEntity.getBody().getFilUuid(), is(dokumentFil.getFilUuid()));
		assertThat(responseEntity.getBody().getVariantFormatCode(), is(SLADDET));
		assertThat(responseEntity.getBody().getDokumentInfoId(), is(persistedDokumentInfo.getDokumentInfoId()));

		TestTransaction.end();

		TestTransaction.start();
		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(1));

		AksjonsLogg aksjonsLogg = aksjonsLoggList.get(0);
		assertThat(aksjonsLogg.getAksjon(), is(AksjonsTypeCode.ARKIVERING));
		assertThat(aksjonsLogg.getUtfoertAv(), is(TestDataUtils.AKSJON_UTFOERT_AV));
		assertThat(aksjonsLogg.getHjemmel(), is(TestDataUtils.AKSJON_HJEMMEL));
		assertThat(aksjonsLogg.getMelding(), is(TestDataUtils.AKSJON_MELDING));
		assertThat(aksjonsLogg.getJournalpostId(), is(journalpost.getJournalpostId()));
		assertThat(aksjonsLogg.getDokumentInfoId(), is(dokumentInfo.getDokumentInfoId()));
		assertThat(aksjonsLogg.getApplikasjon(), is(SERVICE_USER_ID));
		assertThat(aksjonsLogg.getArkivElementEndringer().size(), is(2));

		List<ArkivElementEndring> arkivElementEndringList = IteratorUtils.toList(aksjonsLogg.getArkivElementEndringer()
				.iterator());
		assertThat(arkivElementEndringList.stream()
				.map(ArkivElementEndring::toStringElementFraTil)
				.collect(Collectors.toList()), hasItems(ArkivElementEndring.builder()
						.arkivElement(FILDETALJER_FILUUID)
						.fraVerdi(null)
						.tilVerdi(responseEntity.getBody().getFilUuid())
						.build().toStringElementFraTil(),
				ArkivElementEndring.builder()
						.arkivElement(FILDETALJER_VARIANTFORMAT)
						.fraVerdi(null)
						.tilVerdi(SLADDET.name())
						.build().toStringElementFraTil()

		));
		TestTransaction.end();
	}

	@Test
	public void shouldFailWithBadRequestWhenVariantAlreadyExists() throws IOException {
		abacPermit();

		Journalpost journalpost = journalpostRepository.save(opprettHoveddokumentForIT());

		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ArkiverVariantRequest request = ArkiverVariantRequest.builder()
				.dokumentInfoId(dokumentInfo.getDokumentInfoId())
				.fil(Base64.encodeBase64String(FIL))
				.filnavn("filnavn")
				.variant(SLADDET)
				.filType(FilTypeCode.PDF).build();

		var httpEntity = new HttpEntity<>(request, createHeadersWithAksjon());

		ResponseEntity<ArkiverVariantResponse> responseEntity = restTemplate.exchange(
				URL_ARKIVERVARIANT,
				HttpMethod.POST,
				httpEntity,
				ArkiverVariantResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		byte[] FIL2 = "NEW FILE".getBytes();

		request = ArkiverVariantRequest.builder()
				.dokumentInfoId(dokumentInfo.getDokumentInfoId())
				.fil(Base64.encodeBase64String(FIL2))
				.filnavn("filnavn")
				.variant(SLADDET)
				.filType(FilTypeCode.PDF).build();

		var httpEntity2 = new HttpEntity<>(request, createHeadersWithAksjon());

		ResponseEntity<RestConsumerExceptionResponse> responseEntity2 = restTemplate.exchange(
				URL_ARKIVERVARIANT,
				HttpMethod.POST,
				httpEntity2,
				RestConsumerExceptionResponse.class);
		assertThat(responseEntity2.getStatusCode(), is(HttpStatus.BAD_REQUEST));
	}

	@Test
	public void shouldFailWithNotFoundWhenDokumentInfoIsNotFound() throws IOException {
		abacPermit();

		ArkiverVariantRequest request = ArkiverVariantRequest.builder()
				.dokumentInfoId(123456L)
				.fil(Base64.encodeBase64String(FIL))
				.filnavn("filnavn")
				.variant(SLADDET)
				.filType(FilTypeCode.PDF).build();

		var httpEntity = new HttpEntity<>(request, createHeadersWithAksjon());

		ResponseEntity<RestConsumerExceptionResponse> responseEntity = restTemplate.exchange(
				URL_ARKIVERVARIANT,
				HttpMethod.POST,
				httpEntity,
				RestConsumerExceptionResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));

	}

	@Test
	public void shouldNotAllowOperationIfNotSrvJoarkadminConsumer() {
		abacPermit();

		Journalpost journalpost = journalpostRepository.save(opprettHoveddokumentForIT());

		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		TestTransaction.flagForCommit();
		TestTransaction.end();

		var httpEntity = new HttpEntity<>(Base64.encodeBase64String(FIL), createHeadersWithServiceUserToken(NO_ACCESS_SERVICE_USER_ID));

		ResponseEntity<RestConsumerExceptionResponse> responseEntity = restTemplate.exchange(
				URL_ARKIVERVARIANT,
				HttpMethod.POST,
				httpEntity,
				RestConsumerExceptionResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.UNAUTHORIZED));


	}


}
