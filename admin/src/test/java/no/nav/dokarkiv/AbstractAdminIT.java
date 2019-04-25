package no.nav.dokarkiv;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import no.nav.dokarkiv.core.AbstractRestIT;
import no.nav.dokarkiv.core.CoreConfig;
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
import no.nav.freg.security.test.oidc.tools.TestToolsAutoConfig;
import org.apache.commons.collections15.IteratorUtils;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.TestTransaction;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		classes = {CoreConfig.class, AdminConfig.class, TestToolsAutoConfig.class, AbstractAdminIT.Config.class})
@ActiveProfiles("itest,wiremock,ldap,oidc")
@AutoConfigureWireMock(port = 0)
public abstract class AbstractAdminIT extends AbstractRestIT {
	protected static final String URL_KASSERDOKUMENT = "/rest/admin/kasserdokument/";
	protected static final String URL_SKJERMARKIVENHET = "/rest/admin/skjermarkivenhet/";
	protected static final String URL_SLETTARKIVENHET = "/rest/admin/slettarkivenhet";

	protected static final String NO_ACCESS_PERSON_USER_ID = "Z111111";

	public static class Config {
		@Bean
		NavLdapService navLdapService() {
			NavLdapService mockNavLdapService = mock(NavLdapService.class);
			when(mockNavLdapService.findByUserId(PERSON_USER_ID)).thenReturn(NavUser.builder()
					.memberOf(new HashSet<>(Arrays.asList("0000-GA-joark-vedlikehold")))
					.userId(PERSON_USER_ID)
					.userExistsInLdap(true)
					.build());
			when(mockNavLdapService.findByUserId(NO_ACCESS_PERSON_USER_ID)).thenReturn(NavUser.builder()
					.memberOf(new HashSet<>(Arrays.asList("0000-GA-NOTHING")))
					.userId(NO_ACCESS_PERSON_USER_ID)
					.userExistsInLdap(true)
					.build());
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

	protected AksjonsLogg getAksjonsLoggByDokumentInfoId(List<AksjonsLogg> aksjonsLoggList, Long dokumentInfoId) {
		return aksjonsLoggList.stream()
				.filter(aksjonsLogg -> aksjonsLogg.getJournalpostId() == null && dokumentInfoId.equals(aksjonsLogg.getDokumentInfoId()))
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

		assertCommongAksjonsLoggValues(aksjonsLogg, expectedAksjonsTypeCode);
		assertThat("journalpostId", aksjonsLogg.getJournalpostId(), is(journalpostId));
		assertThat("dokumentInfoId", aksjonsLogg.getDokumentInfoId(), is(dokumentInfoId));
		assertThat("arkivElementEndring.size()", aksjonsLogg.getArkivElementEndringer()
				.size(), is(expectedArkivElementEndringList.size()));

		List<ArkivElementEndring> arkivElementEndringList = IteratorUtils.toList(aksjonsLogg.getArkivElementEndringer()
				.iterator());

		assertThat(arkivElementEndringList.stream()
				.map(ArkivElementEndring::toStringElementFraTil)
				.collect(Collectors.toList()), hasItems(expectedArkivElementEndringList.stream()
				.map(ArkivElementEndring::toStringElementFraTil)
				.distinct()
				.toArray()));
	}

	protected void assertCommongAksjonsLoggValues(AksjonsLogg aksjonsLogg, AksjonsTypeCode expectedAksjonsTypeCode) {
		assertThat(aksjonsLogg.getAksjon(), is(expectedAksjonsTypeCode));
		assertThat(aksjonsLogg.getUtfoertAv(), is(TestDataUtils.AKSJON_UTFOERT_AV));
		assertThat(aksjonsLogg.getHjemmel(), is(TestDataUtils.AKSJON_HJEMMEL));
		assertThat(aksjonsLogg.getMelding(), is(TestDataUtils.AKSJON_MELDING));
		assertThat(aksjonsLogg.getApplikasjon(), is(SERVICE_USER_ID));
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
		assertThat(entityManager.createNativeQuery("select '1' from t_retur_info where journalpost_id= :jp")
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
		assertThat(entityManager.createNativeQuery("select '1' from t_retur_info where journalpost_id= :jp")
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
