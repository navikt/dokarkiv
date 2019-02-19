package no.nav.dokarkiv;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;

import no.nav.dokarkiv.core.AbstractRestIT;
import no.nav.dokarkiv.core.CoreConfig;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.freg.security.test.oidc.tools.TestToolsAutoConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		classes = {CoreConfig.class, AdminConfig.class, TestToolsAutoConfig.class})
@ActiveProfiles("itest,wiremock,ldap,oidc")
@AutoConfigureWireMock(port = 0)
public abstract class AbstractAdminIT extends AbstractRestIT {
	protected static final String URL_KASSERDOKUMENT = "/rest/kasserdokument/";
	protected static final String URL_SKJERMARKIVENHET = "/rest/skjermarkivenhet/";
	protected static final String URL_SLETTARKIVENHET = "/rest/slettarkivenhet";

	protected void abacPermit() {
		stubFor(post(urlEqualTo("/abac"))
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json")));
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
