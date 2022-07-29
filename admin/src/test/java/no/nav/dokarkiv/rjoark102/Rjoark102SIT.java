package no.nav.dokarkiv.rjoark102;


import no.nav.dokarkiv.AbstractAdminIT;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;
import no.nav.dokarkiv.core.domain.entities.ArkivElementEndring;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import org.apache.commons.collections15.IteratorUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.transaction.TestTransaction;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.DOKUMENT_INFO_KASSERT;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.fildetaljerSkjermingTypeVariant;
import static no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode.POL;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createJournalpostWithHoveddokument;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Rjoark102SIT extends AbstractAdminIT {

	@Test
	public void skalSkjermeDokumentForKassering() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(createJournalpostWithHoveddokument());
		DokumentInfo dokumentInfoSomSkalSkjermesSomKassert = journalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo();

		reinitTransaction();

		var responseEntity = restTemplate.exchange(
				URL_KASSERDOKUMENT_SKJERM + "/" + dokumentInfoSomSkalSkjermesSomKassert.getDokumentInfoId(),
				HttpMethod.POST,
				new HttpEntity<>(createHeadersWithAksjon()),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		Optional<DokumentInfo> dokInfoEtterKall = dokumentinfoRepository.findByDokumentInfoId(dokumentInfoSomSkalSkjermesSomKassert
				.getDokumentInfoId());
		assertTrue(dokInfoEtterKall.isPresent());
		assertThatAllFildetaljerIsSkjermet(dokInfoEtterKall.get(), POL);
		assertThat(dokInfoEtterKall.get().isKassert(), is(true));

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(1));
		assertAksjonsLogg(aksjonsLoggList.get(0), AksjonsTypeCode.ENDRE_SKJERMING, journalpost.getJournalpostId(), dokumentInfoSomSkalSkjermesSomKassert.getDokumentInfoId(),
				Arrays.asList(
						ArkivElementEndring.builder()
								.arkivElement(fildetaljerSkjermingTypeVariant(VariantFormatCode.ARKIV))
								.fraVerdi(null)
								.tilVerdi(SkjermingTypeCode.POL.name())
								.build(),
						ArkivElementEndring.builder()
								.arkivElement(fildetaljerSkjermingTypeVariant(VariantFormatCode.PRODUKSJON))
								.fraVerdi(null)
								.tilVerdi(SkjermingTypeCode.POL.name())
								.build(),
						ArkivElementEndring.builder()
								.arkivElement(DOKUMENT_INFO_KASSERT)
								.fraVerdi("false")
								.tilVerdi("true")
								.build()

				)
		);
		TestTransaction.end();

	}

	@Test
	public void skalOppheveSkjermingDokumentForKassering() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(createJournalpostWithHoveddokument());
		DokumentInfo dokumentInfoSomSkalSkjermesSomKassert = journalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo();

		dokumentInfoSomSkalSkjermesSomKassert.setKassert(true);
		dokumentInfoSomSkalSkjermesSomKassert.getFildetaljerListeAdmin()
				.forEach(filDetaljer -> skjermingService.setFildetaljerSkjerming(dokumentInfoSomSkalSkjermesSomKassert.getDokumentInfoId(), filDetaljer
						.getVariantFormat(), POL));

		reinitTransaction();

		var responseEntity = restTemplate.exchange(
				URL_KASSERDOKUMENT_SKJERM + "/" + dokumentInfoSomSkalSkjermesSomKassert.getDokumentInfoId(),
				HttpMethod.DELETE,
				new HttpEntity<>(createHeadersWithAksjon()),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		Optional<DokumentInfo> dokInfoEtterKall = dokumentinfoRepository.findByDokumentInfoId(dokumentInfoSomSkalSkjermesSomKassert
				.getDokumentInfoId());
		assertTrue(dokInfoEtterKall.isPresent());
		assertThatAllFildetaljerIsSkjermet(dokInfoEtterKall.get(), null);
		assertThat(dokInfoEtterKall.get().isKassert(), is(false));

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(1));
		assertAksjonsLogg(aksjonsLoggList.get(0), AksjonsTypeCode.ENDRE_SKJERMING, journalpost.getJournalpostId(), dokumentInfoSomSkalSkjermesSomKassert.getDokumentInfoId(),
				Arrays.asList(
						ArkivElementEndring.builder()
								.arkivElement(fildetaljerSkjermingTypeVariant(VariantFormatCode.ARKIV))
								.fraVerdi(POL.name())
								.tilVerdi(null)
								.build(),
						ArkivElementEndring.builder()
								.arkivElement(fildetaljerSkjermingTypeVariant(VariantFormatCode.PRODUKSJON))
								.fraVerdi(POL.name())
								.tilVerdi(null)
								.build(),
						ArkivElementEndring.builder()
								.arkivElement(DOKUMENT_INFO_KASSERT)
								.fraVerdi("true")
								.tilVerdi("false")
								.build()

				)
		);

		TestTransaction.end();
	}

	@Test
	public void skalFeileHvisDokumentIkkeFinnes() throws IOException {
		abacPermit();

		var responseEntity = restTemplate.exchange(
				URL_KASSERDOKUMENT_SKJERM + "/" + 1,
				HttpMethod.POST,
				new HttpEntity<>(createHeadersWithAksjon()),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));


		var responseEntityOpphev = restTemplate.exchange(
				URL_KASSERDOKUMENT_SKJERM + "/" + 1,
				HttpMethod.DELETE,
				new HttpEntity<>(createHeadersWithAksjon()),
				String.class);

		assertThat(responseEntityOpphev.getStatusCode(), is(HttpStatus.NOT_FOUND));

	}

	private void assertThatAllFildetaljerIsSkjermet(DokumentInfo dokInfoEtterKall, SkjermingTypeCode skjermingTypeCode) {

		dokInfoEtterKall.getFildetaljerListeAdmin().forEach(
				filDetaljer -> assertThat(filDetaljer.getSkjermingType(), is(skjermingTypeCode))
		);
	}
}
