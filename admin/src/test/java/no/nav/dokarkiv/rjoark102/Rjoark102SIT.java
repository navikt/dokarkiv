package no.nav.dokarkiv.rjoark102;

import no.nav.dokarkiv.AbstractAdminIT;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;
import no.nav.dokarkiv.core.domain.entities.ArkivElementEndring;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;

import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.DOKUMENT_INFO_KASSERT;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.fildetaljerSkjermingTypeVariant;
import static no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode.ENDRE_SKJERMING;
import static no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode.POL;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.ARKIV;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.PRODUKSJON;
import static no.nav.dokarkiv.core.security.ValidateAdminConsumerAccessInterceptor.APP_NAME_WITH_NAMESPACE;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createJournalpostWithHoveddokument;
import static no.nav.dokarkiv.util.TestUtil.createKasserDokumentRequest;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

public class Rjoark102SIT extends AbstractAdminIT {

	@Test
	public void skalSkjermeDokumentForKassering() {
		stubMsGraphMemberOfJoarkVelikehold(MS_ID_SAKSBEHANDLER);

		Journalpost journalpost = journalpostTestRepository.persist(createJournalpostWithHoveddokument());
		DokumentInfo dokumentInfoSomSkalSkjermesSomKassert = journalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo();

		reinitTransaction();

		var responseEntity = restTemplate.exchange(URL_KASSERDOKUMENT_SKJERM + "/" + dokumentInfoSomSkalSkjermesSomKassert.getDokumentInfoId(), POST, new HttpEntity<>(createHeadersWithClientCredentialAndAksjonslogg(API_ADMIN_ROLE)), String.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);

		Optional<DokumentInfo> dokInfoEtterKall = dokumentInfoTestRepository.findById(dokumentInfoSomSkalSkjermesSomKassert.getDokumentInfoId());
		assertTrue(dokInfoEtterKall.isPresent());
		assertThatAllFildetaljerIsSkjermet(dokInfoEtterKall.get(), POL);
		assertThat(dokInfoEtterKall.get().isKassert()).isTrue();

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList.size()).isEqualTo(1);
		assertAksjonsLogg(aksjonsLoggList.get(0), ENDRE_SKJERMING, journalpost.getJournalpostId(), dokumentInfoSomSkalSkjermesSomKassert.getDokumentInfoId(),
				asList(
						ArkivElementEndring.builder()
								.arkivElement(fildetaljerSkjermingTypeVariant(ARKIV))
								.fraVerdi(null)
								.tilVerdi(POL.name())
								.build(),
						ArkivElementEndring.builder()
								.arkivElement(fildetaljerSkjermingTypeVariant(PRODUKSJON))
								.fraVerdi(null)
								.tilVerdi(POL.name())
								.build(),
						ArkivElementEndring.builder()
								.arkivElement(DOKUMENT_INFO_KASSERT)
								.fraVerdi("false")
								.tilVerdi("true")
								.build()
				)
		);
	}

	@Test
	public void skalSkjermeDokumentForKasseringForStsTokenFraJoarkadmin() {
		stubMsGraphMemberOfJoarkVelikehold(MS_ID_SAKSBEHANDLER);

		Journalpost journalpost = journalpostTestRepository.persist(createJournalpostWithHoveddokument());
		DokumentInfo dokumentInfoSomSkalSkjermesSomKassert = journalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo();

		reinitTransaction();

		var httpHeaders = createHeadersWithClientCredentialAndAksjonslogg(API_ADMIN_ROLE);

		var responseEntity = restTemplate.exchange(URL_KASSERDOKUMENT_SKJERM + "/" + dokumentInfoSomSkalSkjermesSomKassert.getDokumentInfoId(), POST,
				new HttpEntity<>(httpHeaders), String.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);

		Optional<DokumentInfo> dokInfoEtterKall = dokumentInfoTestRepository.findById(dokumentInfoSomSkalSkjermesSomKassert.getDokumentInfoId());
		assertTrue(dokInfoEtterKall.isPresent());
		assertThatAllFildetaljerIsSkjermet(dokInfoEtterKall.get(), POL);
		assertThat(dokInfoEtterKall.get().isKassert()).isTrue();

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList.size()).isEqualTo(1);
		assertAksjonsLoggSts(aksjonsLoggList.get(0), ENDRE_SKJERMING, journalpost.getJournalpostId(), dokumentInfoSomSkalSkjermesSomKassert.getDokumentInfoId(),
				asList(
						ArkivElementEndring.builder()
								.arkivElement(fildetaljerSkjermingTypeVariant(ARKIV))
								.fraVerdi(null)
								.tilVerdi(POL.name())
								.build(),
						ArkivElementEndring.builder()
								.arkivElement(fildetaljerSkjermingTypeVariant(PRODUKSJON))
								.fraVerdi(null)
								.tilVerdi(POL.name())
								.build(),
						ArkivElementEndring.builder()
								.arkivElement(DOKUMENT_INFO_KASSERT)
								.fraVerdi("false")
								.tilVerdi("true")
								.build()
				)
		);
	}

	@Test
	public void skalOppheveSkjermingDokumentForKassering() {
		stubMsGraphMemberOfJoarkVelikehold(MS_ID_SAKSBEHANDLER);

		Journalpost journalpost = journalpostTestRepository.persist(createJournalpostWithHoveddokument());
		DokumentInfo dokumentInfoSomSkalSkjermesSomKassert = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		dokumentInfoSomSkalSkjermesSomKassert.setKassert(true);
		dokumentInfoSomSkalSkjermesSomKassert.getFildetaljerListeAdmin()
				.forEach(filDetaljer -> skjermingService.setFildetaljerSkjerming(dokumentInfoSomSkalSkjermesSomKassert.getDokumentInfoId(), filDetaljer.getVariantFormat(), POL));

		reinitTransaction();

		var responseEntity = restTemplate.exchange(URL_KASSERDOKUMENT_SKJERM + "/" + dokumentInfoSomSkalSkjermesSomKassert.getDokumentInfoId(), DELETE, new HttpEntity<>(createHeadersWithAksjonslogg(AZP_NAME_JOARKADMIN, MS_USER_ID_WITH_GROUP_ACCESS)), String.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);

		Optional<DokumentInfo> dokInfoEtterKall = dokumentInfoTestRepository.findById(dokumentInfoSomSkalSkjermesSomKassert.getDokumentInfoId());
		assertTrue(dokInfoEtterKall.isPresent());
		assertThatAllFildetaljerIsSkjermet(dokInfoEtterKall.get(), null);
		assertThat(dokInfoEtterKall.get().isKassert()).isFalse();

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList.size()).isEqualTo(1);
		assertAksjonsLogg(aksjonsLoggList.get(0), ENDRE_SKJERMING, journalpost.getJournalpostId(), dokumentInfoSomSkalSkjermesSomKassert.getDokumentInfoId(),
				asList(
						ArkivElementEndring.builder()
								.arkivElement(fildetaljerSkjermingTypeVariant(ARKIV))
								.fraVerdi(POL.name())
								.tilVerdi(null)
								.build(),
						ArkivElementEndring.builder()
								.arkivElement(fildetaljerSkjermingTypeVariant(PRODUKSJON))
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
	}

	@Test
	public void skalFeileHvisDokumentIkkeFinnes() {
		stubMsGraphMemberOfJoarkVelikehold(MS_ID_SAKSBEHANDLER);

		var responseEntity = restTemplate.exchange(URL_KASSERDOKUMENT_SKJERM + "/" + 1, POST, new HttpEntity<>(createHeadersWithAksjonslogg(AZP_NAME_JOARKADMIN, MS_USER_ID_WITH_GROUP_ACCESS)), String.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(NOT_FOUND);

		var responseEntityOpphev = restTemplate.exchange(URL_KASSERDOKUMENT_SKJERM + "/" + 1, DELETE, new HttpEntity<>(createHeadersWithAksjonslogg(AZP_NAME_JOARKADMIN, MS_USER_ID_WITH_GROUP_ACCESS)), String.class);

		assertThat(responseEntityOpphev.getStatusCode()).isEqualTo(NOT_FOUND);
	}

	@Test
	public void skalReturnereUnauthorizedHvisStsTokenIkkeErFraJoarkadmin() {
		var headers = createHeadersWithServiceUserAndAksjonslogg(SERVICEUSER_IKKE_JOARKADMIN);

		var responseEntity = restTemplate.exchange(URL_KASSERDOKUMENT_SKJERM + "/" + 1, POST, new HttpEntity<>(createKasserDokumentRequest(123L), headers), String.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(UNAUTHORIZED);
		assertThat(responseEntity.getBody()).contains("OIDC-token på Authorization-header må være et client credential token som tilhører " + APP_NAME_WITH_NAMESPACE);
	}

	@Test
	public void skalReturnereUnauthorizedHvisClientCredentialToken() {
		var headers = createHeadersWithClientCredentialToken();

		var responseEntity = restTemplate.exchange(URL_KASSERDOKUMENT_SKJERM + "/" + 1, POST, new HttpEntity<>(createKasserDokumentRequest(123L), headers), String.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(UNAUTHORIZED);
		assertThat(responseEntity.getBody()).contains("OIDC-token på Authorization-header må være et client credential token som tilhører " + APP_NAME_WITH_NAMESPACE);
	}

	@Test
	public void skalReturnereUnauthorizedHvisKallendeBrukerManglerRiktigGruppe() {
		stubMsGraphMemberOfNotJoarkVelikeholdAdmin(MS_ID_SAKSBEHANDLER);
		var headers = createHeadersWithAksjonslogg(AZP_NAME_JOARKADMIN, MS_USER_ID_WITH_GROUP_ACCESS);

		var responseEntity = restTemplate.exchange(URL_KASSERDOKUMENT_SKJERM + "/" + 1, POST, new HttpEntity<>(createKasserDokumentRequest(123L), headers), String.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(UNAUTHORIZED);
		assertThat(responseEntity.getBody()).contains("NAV-ansatt må være medlem av gruppen");
	}

	private void assertThatAllFildetaljerIsSkjermet(DokumentInfo dokInfoEtterKall, SkjermingTypeCode skjermingTypeCode) {

		dokInfoEtterKall.getFildetaljerListeAdmin().forEach(
				filDetaljer -> assertThat(filDetaljer.getSkjermingType()).isEqualTo(skjermingTypeCode));
	}
}
