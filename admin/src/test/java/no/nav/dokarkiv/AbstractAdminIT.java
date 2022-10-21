package no.nav.dokarkiv;

import no.nav.dokarkiv.core.AbstractRestIT;
import no.nav.dokarkiv.core.CoreConfig;
import no.nav.dokarkiv.core.consumer.azure.AzureAdGraphService;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;
import no.nav.dokarkiv.core.domain.entities.ArkivElementEndring;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.security.ldap.NavLdapService;
import no.nav.dokarkiv.core.security.ldap.NavUser;
import no.nav.dokarkiv.core.util.TestDataUtils;
import no.nav.security.token.support.test.spring.TokenGeneratorConfiguration;
import org.apache.commons.collections15.IteratorUtils;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.TestTransaction;

import java.util.List;
import java.util.stream.Collectors;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static no.nav.dokarkiv.core.util.TestDataGenerator.BRUKER_ID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		classes = {CoreConfig.class, AdminConfig.class, AbstractAdminIT.Config.class, TokenGeneratorConfiguration.class},
		properties = {"spring.main.allow-bean-definition-overriding=true"})
@ActiveProfiles({"itest", "wiremock", "ldap"})
@AutoConfigureWireMock(port = 0)
public abstract class AbstractAdminIT extends AbstractRestIT {
	protected static final String URL_KASSERDOKUMENT = "/rest/admin/kasserdokument/";
	protected static final String URL_KASSERDOKUMENT_SKJERM = "/rest/admin/kasserdokument/skjerm";
	protected static final String URL_SKJERMARKIVENHET = "/rest/admin/skjermarkivenhet/";
	protected static final String URL_SLETTARKIVENHET = "/rest/admin/slettarkivenhet";

	protected static final String NO_ACCESS_PERSON_USER_ID = "Z111111";

	public static class Config {
		@Bean
		AzureAdGraphService azureAdGraphService() {
			AzureAdGraphService azureAdGraphService = mock(AzureAdGraphService.class);
			when(azureAdGraphService.hentFulltNavn(PERSON_USER_ID)).thenReturn(PERSON_USER_NAME);
			when(azureAdGraphService.userInGroup(PERSON_USER_ID, "0000-GA-joark-vedlikehold")).thenReturn(true);

			when(azureAdGraphService.hentFulltNavn(NO_ACCESS_PERSON_USER_ID)).thenReturn(NO_ACCESS_PERSON_USER_ID);
			when(azureAdGraphService.userInGroup(NO_ACCESS_PERSON_USER_ID, "0000-GA-joark-vedlikehold")).thenReturn(false);
			return azureAdGraphService;
		}

		@Bean
		NavLdapService navLdapService() {
			NavLdapService mockNavLdapService = mock(NavLdapService.class);

			when(mockNavLdapService.findByServiceuserId(SERVICE_USER_ID)).thenReturn(NavUser.builder()
					.userId(SERVICE_USER_ID)
					.userExistsInLdap(true)
					.build());
			when(mockNavLdapService.findByServiceuserId(NO_ACCESS_SERVICE_USER_ID)).thenReturn(NavUser.builder()
					.userId(NO_ACCESS_SERVICE_USER_ID)
					.userExistsInLdap(true)
					.build());
			return mockNavLdapService;
		}

	}

	protected void reinitTransaction() {
		TestTransaction.flagForCommit();
		TestTransaction.end();
		TestTransaction.start();
	}

	protected void abacPermit() {
		stubFor(post(urlEqualTo("/abac"))
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json")));
	}

	protected AksjonsLogg getAksjonsLoggByJournalpostId(List<AksjonsLogg> aksjonsLoggList, Long journalpostId) {
		return aksjonsLoggList.stream()
				.filter(aksjonsLogg -> journalpostId.equals(aksjonsLogg.getJournalpostId())).findAny().get();
	}

	protected AksjonsLogg getAksjonsLoggByJournalpostIdAndDokumentInfoId(List<AksjonsLogg> aksjonsLoggList, Long journalpostId, Long dokumentInfoId) {
		return aksjonsLoggList.stream()
				.filter(aksjonsLogg -> (journalpostId == null ? aksjonsLogg.getJournalpostId() == null : journalpostId.equals(aksjonsLogg
						.getJournalpostId())) && (dokumentInfoId == null ? aksjonsLogg.getDokumentInfoId() == null : dokumentInfoId
						.equals(aksjonsLogg
								.getDokumentInfoId())))
				.findAny()
				.get();
	}

	protected JournalpostDokumentInfoRelasjon getRelasjonByDokumentInfoId(Journalpost journalpost, Long dokumentInfoId) {
		return journalpost.getJournalpostDokumentInfoRelasjoner()
				.stream()
				.filter(rel -> rel.getDokumentInfo().getDokumentInfoId().equals(dokumentInfoId))
				.findAny()
				.get();
	}

	protected void assertAksjonsLogg(AksjonsLogg aksjonsLogg, AksjonsTypeCode expectedAksjonsTypeCode, Long journalpostId, Long dokumentInfoId, List<ArkivElementEndring> expectedArkivElementEndringList) {
		assertAksjonsLogg(aksjonsLogg, expectedAksjonsTypeCode, journalpostId, dokumentInfoId, null, expectedArkivElementEndringList);
	}

	protected void assertAksjonsLogg(AksjonsLogg aksjonsLogg, AksjonsTypeCode expectedAksjonsTypeCode, Long journalpostId, Long dokumentInfoId, String expectedMelding, List<ArkivElementEndring> expectedArkivElementEndringList) {

		assertCommongAksjonsLoggValues(aksjonsLogg, expectedAksjonsTypeCode, expectedMelding);
		assertThat("journalpostId", aksjonsLogg.getJournalpostId(), is(journalpostId));
		assertThat("dokumentInfoId", aksjonsLogg.getDokumentInfoId(), is(dokumentInfoId));
		assertThat("arkivElementEndring.size()", aksjonsLogg.getArkivElementEndringer()
				.size(), is(expectedArkivElementEndringList.size()));

		List<ArkivElementEndring> arkivElementEndringList = IteratorUtils.toList(aksjonsLogg.getArkivElementEndringer()
				.iterator());

		assertThat(arkivElementEndringList.stream()
						.map(ArkivElementEndring::toStringElementFraTil)
						.collect(Collectors.toList()),
				hasItems(expectedArkivElementEndringList.stream()
						.map(ArkivElementEndring::toStringElementFraTil)
						.distinct()
						.toArray()));
	}

	protected void assertCommongAksjonsLoggValues(AksjonsLogg aksjonsLogg, AksjonsTypeCode expectedAksjonsTypeCode, String expectedMelding) {
		assertThat("aksjon", aksjonsLogg.getAksjon(), is(expectedAksjonsTypeCode));
		assertThat("ufoertAv", aksjonsLogg.getUtfoertAv(), is(TestDataUtils.AKSJON_UTFOERT_AV));
		assertThat("hjemmel", aksjonsLogg.getHjemmel(), is(TestDataUtils.AKSJON_HJEMMEL));
		assertThat("melding", aksjonsLogg.getMelding(), is(expectedMelding == null ? TestDataUtils.AKSJON_MELDING : expectedMelding));
		assertThat("applikasjon", aksjonsLogg.getApplikasjon(), is(SERVICE_USER_ID));
		assertThat("bruker", aksjonsLogg.getBruker(), is(BRUKER_ID));
	}


	protected void assertThatJournalpostIsDeleted(Long journalpostId) {
		assertThat(joarkRepository.findById(journalpostId).isPresent(), is(false));
		assertThat(entityManager.createQuery("select '1' from Saksrelasjon where journalpost.journalpostId= :jp")
				.setParameter("jp", journalpostId)
				.getResultList()
				.size(), is(0));
		assertThat(entityManager.createNativeQuery("select '1' from t_bruker where journalpost_id= :jp")
				.setParameter("jp", journalpostId)
				.getResultList()
				.size(), is(0));
		assertThat(entityManager.createNativeQuery("select '1' from t_kryssreferanse where journalpost_id= :jp")
				.setParameter("jp", journalpostId)
				.getResultList()
				.size(), is(0));
		assertThat(entityManager.createNativeQuery("select '1' from t_jp_tillegg where journalpost_id= :jp")
				.setParameter("jp", journalpostId)
				.getResultList()
				.size(), is(0));
		assertThat(journalpostDokumentInfoRelasjonRepository.findAllByJournalpostJournalpostId(journalpostId).size(), is(0));
	}

	protected void assertThatDokumentInfoIsDeleted(DokumentInfo dokumentInfo) {
		Long dokumentInfoId = dokumentInfo.getDokumentInfoId();
		assertThat(dokumentinfoRepository.findById(dokumentInfoId).isPresent(), is(false));
		assertThat(journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(dokumentInfoId).size(), is(0));
		assertThat(entityManager.createNativeQuery("select '1' from t_dok_info_tillegg where dokument_info_id= :dok")
				.setParameter("dok", dokumentInfoId)
				.getResultList()
				.size(), is(0));
		assertThat(entityManager.createNativeQuery("select '1' from t_skannet_innhold where dokument_info_id= :dok")
				.setParameter("dok", dokumentInfoId)
				.getResultList()
				.size(), is(0));
		assertThat(entityManager.createNativeQuery("select '1' from t_fil_detaljer where dokument_info_id= :dok")
				.setParameter("dok", dokumentInfoId)
				.getResultList()
				.size(), is(0));

		for (FilDetaljer filDetaljer : dokumentInfo.getFildetaljerListe()) {
			assertThatDokumentFilIsDeleted(filDetaljer.getFilUuid());
		}
	}

	protected void assertThatFildetaljerIsDeleted(FilDetaljer filDetaljer) {
		Long dokumentInfoId = filDetaljer.getDokumentInfo().getDokumentInfoId();
		VariantFormatCode variantFormatCode = filDetaljer.getVariantFormat();
		assertThat(entityManager.createNativeQuery("select '1' from t_fil_detaljer where dokument_info_id= :dok and k_variant_format=:variant")
				.setParameter("dok", dokumentInfoId)
				.setParameter("variant", variantFormatCode.name())
				.getResultList().size(), is(0));
		assertThatDokumentFilIsDeleted(filDetaljer.getFilUuid());
	}

	protected void assertThatDokumentFilIsDeleted(String filuuid) {
		assertThat(dokumentFilRepository.findByFilUuid(filuuid), nullValue());
	}

	protected void assertThatJournalpostRelasjonerIsNotDeleted(Journalpost journalpost) {
		assertThat(entityManager.createQuery("select '1' from JournalpostDokumentInfoRelasjon where journalpost.journalpostId= :jp")
				.setParameter("jp", journalpost.getJournalpostId())
				.getResultList()
				.size(), is(journalpost.getJournalpostDokumentInfoRelasjonerAdmin().size()));

		for (JournalpostDokumentInfoRelasjon rel : journalpost.getJournalpostDokumentInfoRelasjoner()) {
			assertThatDokumentInfoAndFildetaljerIsNotDeleted(rel.getDokumentInfo());
		}
	}

	protected void assertThatJournalpostIsNotDeleted(Journalpost journalpost) {
		Long journalpostId = journalpost.getJournalpostId();
		assertThat(joarkRepository.findById(journalpostId).isPresent(), is(true));
		assertThat(entityManager.createQuery("select '1' from Saksrelasjon where journalpost.journalpostId= :jp")
				.setParameter("jp", journalpostId)
				.getResultList()
				.size(), is(1));
		assertThat(entityManager.createNativeQuery("select '1' from t_bruker where journalpost_id= :jp")
				.setParameter("jp", journalpostId)
				.getResultList()
				.size(), is(1));
		assertThat(entityManager.createNativeQuery("select '1' from t_kryssreferanse where journalpost_id= :jp")
				.setParameter("jp", journalpostId)
				.getResultList()
				.size(), is(1));
		assertThat(entityManager.createNativeQuery("select '1' from t_jp_tillegg where journalpost_id= :jp")
				.setParameter("jp", journalpostId)
				.getResultList()
				.size(), is(1));
	}

	protected void assertThatDokumentInfoIsNotDeleted(DokumentInfo dokumentInfo) {
		Long dokumentInfoId = dokumentInfo.getDokumentInfoId();
		assertThat(dokumentinfoRepository.findById(dokumentInfoId).isPresent(), is(true));
		assertThat(entityManager.createNativeQuery("select '1' from t_dok_info_tillegg where dokument_info_id= :dok")
				.setParameter("dok", dokumentInfoId)
				.getResultList()
				.size(), is(1));
		assertThat(entityManager.createNativeQuery("select '1' from t_skannet_innhold where dokument_info_id= :dok")
				.setParameter("dok", dokumentInfoId)
				.getResultList()
				.size(), is(1));

	}

	protected void assertThatDokumentInfoAndFildetaljerIsNotDeleted(DokumentInfo dokumentInfo) {
		assertThatDokumentInfoIsNotDeleted(dokumentInfo);
		assertThat(entityManager.createNativeQuery("select '1' from t_fil_detaljer where dokument_info_id= :dok")
				.setParameter("dok", dokumentInfo.getDokumentInfoId())
				.getResultList()
				.size(), is(2));
		for (FilDetaljer filDetaljer : dokumentInfo.getFildetaljerListe()) {
			assertThatDokumentFilIsNotDeleted(filDetaljer.getFilUuid());
		}
	}

	protected void assertThatFildetaljerIsNotDeleted(FilDetaljer filDetaljer) {
		Long dokumentInfoId = filDetaljer.getDokumentInfo().getDokumentInfoId();
		VariantFormatCode variantFormatCode = filDetaljer.getVariantFormat();
		assertThat(entityManager.createNativeQuery("select '1' from t_fil_detaljer where dokument_info_id= :dok and k_variant_format=:variant")
				.setParameter("dok", dokumentInfoId)
				.setParameter("variant", variantFormatCode.name())
				.getResultList().size(), is(1));
		assertThatDokumentFilIsNotDeleted(filDetaljer.getFilUuid());
	}

	protected void assertThatDokumentFilIsNotDeleted(String filuuid) {
		assertThat(dokumentFilRepository.findByFilUuid(filuuid), notNullValue());
	}
}
