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
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.TestTransaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static no.nav.dokarkiv.core.security.ValidateAdminConsumerAccessInterceptor.APP_NAME_WITH_NAMESPACE;
import static no.nav.dokarkiv.core.util.TestDataGenerator.BRUKER_ID;
import static no.nav.dokarkiv.core.util.TestDataGenerator.KANAL_REFERANSE_ID;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createJournalpostWithHoveddokument;
import static no.nav.dokarkiv.core.util.TestDataUtils.AKSJON_HJEMMEL;
import static no.nav.dokarkiv.core.util.TestDataUtils.AKSJON_MELDING;
import static no.nav.dokarkiv.core.util.TestDataUtils.AKSJON_UTFOERT_AV;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT,
		classes = {CoreConfig.class, AdminConfig.class, AbstractAdminIT.Config.class})
@ActiveProfiles({"itest", "wiremock"})
@EnableMockOAuth2Server
@AutoConfigureWireMock(port = 0)
public abstract class AbstractAdminIT extends AbstractRestIT {

	protected static final String URL_KASSERDOKUMENT = "/rest/admin/kasserdokument/";
	protected static final String URL_KASSERDOKUMENT_SKJERM = "/rest/admin/kasserdokument/skjerm";
	protected static final String URL_SKJERMARKIVENHET = "/rest/admin/skjermarkivenhet/";
	protected static final String URL_SLETTARKIVENHET = "/rest/admin/slettarkivenhet";

	public static class Config {
		@Bean
		AzureAdGraphService azureAdGraphService() {
			AzureAdGraphService azureAdGraphService = mock(AzureAdGraphService.class);

			when(azureAdGraphService.isUserMemberOfGroup(eq(MS_USER_ID_WITH_GROUP_ACCESS), eq(MS_AD_GROUP_ID), anyString())).thenReturn(true);
			when(azureAdGraphService.isUserMemberOfGroup(eq(MS_USER_ID_WITHOUT_GROUP_ACCESS), eq(MS_AD_GROUP_ID), anyString())).thenReturn(false);

			return azureAdGraphService;
		}
	}

	protected void reinitTransaction() {
		TestTransaction.flagForCommit();
		TestTransaction.end();
		TestTransaction.start();
	}

	protected AksjonsLogg getAksjonsLoggByJournalpostId(List<AksjonsLogg> aksjonsLoggList, Long journalpostId) {
		return aksjonsLoggList.stream()
				.filter(aksjonsLogg -> journalpostId.equals(aksjonsLogg.getJournalpostId()))
				.findAny()
				.get();
	}

	protected AksjonsLogg getAksjonsLoggByJournalpostIdAndDokumentInfoId(List<AksjonsLogg> aksjonsLoggList, Long journalpostId, Long dokumentInfoId) {
		return aksjonsLoggList.stream()
				.filter(aksjonsLogg -> (journalpostId == null ? aksjonsLogg.getJournalpostId() == null : journalpostId.equals(aksjonsLogg
						.getJournalpostId())) && (dokumentInfoId == null ? aksjonsLogg.getDokumentInfoId() == null : dokumentInfoId
						.equals(aksjonsLogg.getDokumentInfoId())))
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
		assertThat("arkivElementEndring.size()", aksjonsLogg.getArkivElementEndringer().size(), is(expectedArkivElementEndringList.size()));

		List<ArkivElementEndring> arkivElementEndringList = new ArrayList<>(aksjonsLogg.getArkivElementEndringer());

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
		assertThat("ufoertAv", aksjonsLogg.getUtfoertAv(), is(AKSJON_UTFOERT_AV));
		assertThat("hjemmel", aksjonsLogg.getHjemmel(), is(AKSJON_HJEMMEL));
		assertThat("melding", aksjonsLogg.getMelding(), is(expectedMelding == null ? AKSJON_MELDING : expectedMelding));
		assertThat("applikasjon", aksjonsLogg.getApplikasjon(), is(APP_NAME_WITH_NAMESPACE));
		assertThat("bruker", aksjonsLogg.getBruker(), is(BRUKER_ID));
	}

	protected void assertAksjonsLoggSts(AksjonsLogg aksjonsLogg, AksjonsTypeCode expectedAksjonsTypeCode, Long journalpostId, Long dokumentInfoId, List<ArkivElementEndring> expectedArkivElementEndringList) {
		assertAksjonsLoggForSts(aksjonsLogg, expectedAksjonsTypeCode, journalpostId, dokumentInfoId, null, expectedArkivElementEndringList);
	}

	protected void assertAksjonsLoggForSts(AksjonsLogg aksjonsLogg, AksjonsTypeCode expectedAksjonsTypeCode, Long journalpostId, Long dokumentInfoId, String expectedMelding, List<ArkivElementEndring> expectedArkivElementEndringList) {

		assertCommongAksjonsLoggValuesForSts(aksjonsLogg, expectedAksjonsTypeCode, expectedMelding);
		assertThat("journalpostId", aksjonsLogg.getJournalpostId(), is(journalpostId));
		assertThat("dokumentInfoId", aksjonsLogg.getDokumentInfoId(), is(dokumentInfoId));
		assertThat("arkivElementEndring.size()", aksjonsLogg.getArkivElementEndringer().size(), is(expectedArkivElementEndringList.size()));

		List<ArkivElementEndring> arkivElementEndringList = new ArrayList<>(aksjonsLogg.getArkivElementEndringer());

		assertThat(arkivElementEndringList.stream()
						.map(ArkivElementEndring::toStringElementFraTil)
						.collect(Collectors.toList()),
				hasItems(expectedArkivElementEndringList.stream()
						.map(ArkivElementEndring::toStringElementFraTil)
						.distinct()
						.toArray()));
	}

	protected void assertCommongAksjonsLoggValuesForSts(AksjonsLogg aksjonsLogg, AksjonsTypeCode expectedAksjonsTypeCode, String expectedMelding) {
		assertThat("aksjon", aksjonsLogg.getAksjon(), is(expectedAksjonsTypeCode));
		assertThat("ufoertAv", aksjonsLogg.getUtfoertAv(), is(AKSJON_UTFOERT_AV));
		assertThat("hjemmel", aksjonsLogg.getHjemmel(), is(AKSJON_HJEMMEL));
		assertThat("melding", aksjonsLogg.getMelding(), is(expectedMelding == null ? AKSJON_MELDING : expectedMelding));
		assertThat("applikasjon", aksjonsLogg.getApplikasjon(), is(APP_NAME_WITH_NAMESPACE));
		assertThat("bruker", aksjonsLogg.getBruker(), is(BRUKER_ID));
	}

	protected void assertThatJournalpostIsDeleted(Long journalpostId) {
		assertThat(journalpostTestRepository.findById(journalpostId).isPresent(), is(false));
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
		assertThat(entityManager.createQuery("select '1' from UtsendingsInfo where journalpostId=:journalpostId")
				.setParameter("journalpostId", journalpostId)
				.getResultList()
				.size(), is(0));
		assertThat(journalpostDokumentInfoRelasjonTestRepository.findAllByJournalpostJournalpostId(journalpostId).size(), is(0));
	}

	protected void assertThatDokumentInfoIsDeleted(DokumentInfo dokumentInfo) {
		Long dokumentInfoId = dokumentInfo.getDokumentInfoId();
		assertThat(dokumentInfoTestRepository.findById(dokumentInfoId).isPresent(), is(false));
		assertThat(journalpostDokumentInfoRelasjonTestRepository.findAllByDokumentInfoDokumentInfoId(dokumentInfoId).size(), is(0));
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
		assertThat(dokumentFilTestRepository.findByFilUuid(filuuid), nullValue());
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
		assertThat(journalpostTestRepository.findById(journalpostId).isPresent(), is(true));
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
		Optional<DokumentInfo> byId = dokumentInfoTestRepository.findById(dokumentInfoId);
		assertThat(byId.isPresent(), is(true));
		byId.get().getSkannetInnholdListe();
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
		assertThat(dokumentFilTestRepository.findByFilUuid(filuuid), notNullValue());
	}

	protected Journalpost createUniqueJournalpostWithHoveddokument() {
		Journalpost journalpostWithHoveddokument = createJournalpostWithHoveddokument();
		journalpostWithHoveddokument.setKanalReferanseId(KANAL_REFERANSE_ID + UUID.randomUUID());
		return journalpostWithHoveddokument;
	}
}
