package no.nav.dokarkiv.safintern.journalpost;

import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.Fagomrade;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.Sak;
import no.nav.dokarkiv.core.domain.entities.UtsendingsInfo;
import no.nav.dokarkiv.core.domain.service.SkjermingService;
import no.nav.dokarkiv.core.repository.FagomradeTestRepository;
import no.nav.dokarkiv.core.repository.JournalpostTestRepository;
import no.nav.dokarkiv.core.repository.RepositoryConfig;
import no.nav.dokarkiv.core.repository.SakTestRepository;
import no.nav.dokarkiv.core.repository.UtsendingsInfoTestRepository;
import no.nav.dokarkiv.core.skjerming.SkjermingServiceTest;
import no.nav.dokarkiv.safintern.SafinternConfig;
import no.nav.dokarkiv.safintern.views.AvsenderMottakerView;
import no.nav.dokarkiv.safintern.views.DigitalPostadresseView;
import no.nav.dokarkiv.safintern.views.DokumentinfoView;
import no.nav.dokarkiv.safintern.views.FildetaljerView;
import no.nav.dokarkiv.safintern.views.FysiskpostadresseView;
import no.nav.dokarkiv.safintern.views.JournalpostView;
import no.nav.dokarkiv.safintern.views.LogiskVedleggView;
import no.nav.dokarkiv.safintern.views.NavNoVarslingView;
import no.nav.dokarkiv.safintern.views.RelevanteDatoerView;
import no.nav.dokarkiv.safintern.views.UtsendingsInfoView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static com.blazebit.persistence.view.EntityViewSetting.create;
import static no.nav.dokarkiv.core.domain.codes.InnsynCode.BRUK_STANDARDREGLER;
import static no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode.POL;
import static no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode.HOVEDDOKUMENT;
import static no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode.VEDLEGG;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.ARKIV;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.BREVBESTILLING;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.FULLVERSJON;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.ORIGINAL;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.PRODUKSJON;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.PRODUKSJON_DLF;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.SKANNING_META;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.SLADDET;
import static no.nav.dokarkiv.core.stelvio.RequestContextUtil.createAndSetUsername;
import static no.nav.dokarkiv.safintern.journalpost.TestdataAsserter.assertBruker;
import static no.nav.dokarkiv.safintern.journalpost.TestdataAsserter.assertSak;
import static no.nav.dokarkiv.safintern.journalpost.TestdataFactory.ADRESSELINJE1;
import static no.nav.dokarkiv.safintern.journalpost.TestdataFactory.ADRESSELINJE2;
import static no.nav.dokarkiv.safintern.journalpost.TestdataFactory.ADRESSELINJE3;
import static no.nav.dokarkiv.safintern.journalpost.TestdataFactory.ANTALL_RETUR;
import static no.nav.dokarkiv.safintern.journalpost.TestdataFactory.AVSENDER_MOTTAKER_ID;
import static no.nav.dokarkiv.safintern.journalpost.TestdataFactory.AVSENDER_MOTTAKER_ID_TYPE;
import static no.nav.dokarkiv.safintern.journalpost.TestdataFactory.AVSENDER_MOTTAKER_LAND;
import static no.nav.dokarkiv.safintern.journalpost.TestdataFactory.AVSENDER_MOTTAKER_NAVN;
import static no.nav.dokarkiv.safintern.journalpost.TestdataFactory.BEHANDLINGSTEMA;
import static no.nav.dokarkiv.safintern.journalpost.TestdataFactory.BEHANDLINGSTEMA_DEKODE;
import static no.nav.dokarkiv.safintern.journalpost.TestdataFactory.BREVKODE;
import static no.nav.dokarkiv.safintern.journalpost.TestdataFactory.DOKUMENT_INFO_TITTEL;
import static no.nav.dokarkiv.safintern.journalpost.TestdataFactory.DOKUMENT_TYPE_ID;
import static no.nav.dokarkiv.safintern.journalpost.TestdataFactory.FIL_NAVN;
import static no.nav.dokarkiv.safintern.journalpost.TestdataFactory.INNHOLD;
import static no.nav.dokarkiv.safintern.journalpost.TestdataFactory.INNSYNSBESKRIVELSE_BRUK_STANDARDREGLER;
import static no.nav.dokarkiv.safintern.journalpost.TestdataFactory.JOURNALFOERENDE_ENHET;
import static no.nav.dokarkiv.safintern.journalpost.TestdataFactory.JOURNALFOERT_AV_NAVN;
import static no.nav.dokarkiv.safintern.journalpost.TestdataFactory.KANAL_REFERANSE_ID;
import static no.nav.dokarkiv.safintern.journalpost.TestdataFactory.LANDKODE_NO;
import static no.nav.dokarkiv.safintern.journalpost.TestdataFactory.OPPRETTET_AV_NAVN;
import static no.nav.dokarkiv.safintern.journalpost.TestdataFactory.POSTNUMMER;
import static no.nav.dokarkiv.safintern.journalpost.TestdataFactory.POSTSTED;
import static no.nav.dokarkiv.safintern.journalpost.TestdataFactory.SKANNET_INNHOLD_TITTEL;
import static no.nav.dokarkiv.safintern.journalpost.TestdataFactory.SKJERMING_TYPE_CODE;
import static no.nav.dokarkiv.safintern.journalpost.TestdataFactory.TILLEGGOPPLYSNINGER_KEY_1;
import static no.nav.dokarkiv.safintern.journalpost.TestdataFactory.TILLEGGOPPLYSNINGER_KEY_2;
import static no.nav.dokarkiv.safintern.journalpost.TestdataFactory.TILLEGGOPPLYSNINGER_KEY_3;
import static no.nav.dokarkiv.safintern.journalpost.TestdataFactory.TILLEGGOPPLYSNINGER_KEY_4;
import static no.nav.dokarkiv.safintern.journalpost.TestdataFactory.TILLEGGOPPLYSNINGER_VAL_1;
import static no.nav.dokarkiv.safintern.journalpost.TestdataFactory.TILLEGGOPPLYSNINGER_VAL_2;
import static no.nav.dokarkiv.safintern.journalpost.TestdataFactory.TILLEGGOPPLYSNINGER_VAL_3;
import static no.nav.dokarkiv.safintern.journalpost.TestdataFactory.TILLEGGOPPLYSNINGER_VAL_4;
import static no.nav.dokarkiv.safintern.journalpost.TestdataFactory.createDigitalPostUtsendingsInfo;
import static no.nav.dokarkiv.safintern.journalpost.TestdataFactory.createFildetaljerOgFil;
import static no.nav.dokarkiv.safintern.journalpost.TestdataFactory.createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg;
import static no.nav.dokarkiv.safintern.journalpost.TestdataFactory.createFysiskpostUtsendingsInfo;
import static no.nav.dokarkiv.safintern.journalpost.TestdataFactory.createGsak;
import static no.nav.dokarkiv.safintern.journalpost.TestdataFactory.createNavNoUtsendingsInfo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@DataJpaTest
@ContextConfiguration(classes = {RepositoryConfig.class, SkjermingService.class, SkjermingServiceTest.class, SafinternConfig.class})
@ActiveProfiles("itest")
class SafinternJournalpostRepositoryTest {

	@Autowired
	private EntityManager entityManager;
	@Autowired
	private JournalpostTestRepository journalpostTestRepository;
	@Autowired
	private SakTestRepository sakTestRepository;
	@Autowired
	private UtsendingsInfoTestRepository utsendingsInfoTestRepository;
	@Autowired
	private SafinternJournalpostRepository safinternJournalpostRepository;
	@Autowired
	private FagomradeTestRepository fagomradeTestRepository;

	@BeforeEach
	void setUp() {
		createAndSetUsername("itest", "itest");
		saveInnsyn();
		saveBehandlingstema();
		saveFagomrade();
	}

	@Test
	void shouldHentJournalpost() {
		Sak persistedSak = sakTestRepository.persist(createGsak());
		Long sakId = persistedSak.getSakId();
		Journalpost actualJournalpost = createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg(sakId);
		actualJournalpost.setUtsendingskanal(UtsendingsKanalCode.S);
		journalpostTestRepository.persist(actualJournalpost);
		UtsendingsInfo utsendingsInfo = createFysiskpostUtsendingsInfo(actualJournalpost);
		utsendingsInfoTestRepository.persist(utsendingsInfo);

		JournalpostView journalpostView = safinternJournalpostRepository.hentJournalpostById(actualJournalpost.getJournalpostId(), create(JournalpostView.class)).orElse(null);
		Long journalpostId = journalpostView.getJournalpostId();
		assertThat(journalpostId).isNotNull();
		assertJournalpost(journalpostView);
		assertTilleggsopplysninger(journalpostView.getTilleggsopplysninger());
		assertRelevanteDatoer(journalpostView.getRelevanteDatoer());
		assertAvsenderMottaker(journalpostView.getAvsenderMottaker());
		assertSak(sakId, journalpostView.getSaksrelasjon());
		assertBruker(journalpostView.getBruker());
		assertFysiskpostadresseUtsendingsInfo(journalpostView.getUtsendingsInfo());

		Set<DokumentinfoView> dokumenterView = journalpostView.getDokumenter();
		assertThat(dokumenterView).hasSize(2);

		Iterator<DokumentinfoView> iterator = dokumenterView.iterator();
		DokumentinfoView hoveddokumentView = iterator.next();
		assertHoveddokument(hoveddokumentView, journalpostId);

		DokumentinfoView vedleggView = iterator.next();
		assertVedlegg(vedleggView, journalpostId);
	}

	@Test
	void shouldHentJournalpostByEksternReferanseId() {
		Sak persistedSak = sakTestRepository.persist(createGsak());
		Long sakId = persistedSak.getSakId();
		Journalpost actualJournalpost = createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg(sakId);
		actualJournalpost.setUtsendingskanal(UtsendingsKanalCode.S);
		journalpostTestRepository.persist(actualJournalpost);
		UtsendingsInfo utsendingsInfo = createFysiskpostUtsendingsInfo(actualJournalpost);
		utsendingsInfoTestRepository.persist(utsendingsInfo);

		JournalpostView journalpostView = safinternJournalpostRepository.hentJournalpostByEksternReferanseId(KANAL_REFERANSE_ID, create(JournalpostView.class)).orElse(null);
		Long journalpostId = journalpostView.getJournalpostId();
		assertThat(journalpostId).isNotNull();
		assertJournalpost(journalpostView);
		assertTilleggsopplysninger(journalpostView.getTilleggsopplysninger());
		assertRelevanteDatoer(journalpostView.getRelevanteDatoer());
		assertAvsenderMottaker(journalpostView.getAvsenderMottaker());
		assertSak(sakId, journalpostView.getSaksrelasjon());
		assertBruker(journalpostView.getBruker());
		assertFysiskpostadresseUtsendingsInfo(journalpostView.getUtsendingsInfo());

		Set<DokumentinfoView> dokumenterView = journalpostView.getDokumenter();
		assertThat(dokumenterView).hasSize(2);

		Iterator<DokumentinfoView> iterator = dokumenterView.iterator();
		DokumentinfoView hoveddokumentView = iterator.next();
		assertHoveddokument(hoveddokumentView, journalpostId);

		DokumentinfoView vedleggView = iterator.next();
		assertVedlegg(vedleggView, journalpostId);
	}

	@Test
	void shouldHentJournalpostByJournalpostIdAndDokumentInfoId() {
		Sak persistedSak = sakTestRepository.persist(createGsak());
		Long sakId = persistedSak.getSakId();
		Journalpost actualJournalpost = createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg(sakId);
		actualJournalpost.setUtsendingskanal(UtsendingsKanalCode.S);
		journalpostTestRepository.persist(actualJournalpost);
		UtsendingsInfo utsendingsInfo = createFysiskpostUtsendingsInfo(actualJournalpost);
		utsendingsInfoTestRepository.persist(utsendingsInfo);

		Long dokumentInfoId = actualJournalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId();
		JournalpostView journalpostView = safinternJournalpostRepository.hentJournalpostByIdDokumentInfoId(actualJournalpost.getJournalpostId(), dokumentInfoId, create(JournalpostView.class)).orElse(null);
		Long journalpostId = journalpostView.getJournalpostId();
		assertThat(journalpostId).isNotNull();
		assertJournalpost(journalpostView);
		assertTilleggsopplysninger(journalpostView.getTilleggsopplysninger());
		assertRelevanteDatoer(journalpostView.getRelevanteDatoer());
		assertAvsenderMottaker(journalpostView.getAvsenderMottaker());
		assertSak(sakId, journalpostView.getSaksrelasjon());
		assertBruker(journalpostView.getBruker());
		assertFysiskpostadresseUtsendingsInfo(journalpostView.getUtsendingsInfo());

		Set<DokumentinfoView> dokumenterView = journalpostView.getDokumenter();
		assertThat(dokumenterView).hasSize(1);

		DokumentinfoView hoveddokumentView = dokumenterView.iterator().next();
		assertHoveddokument(hoveddokumentView, journalpostId);
	}

	@Test
	void shouldHentJournalpostWhenNavNo() {
		Sak persistedSak = sakTestRepository.persist(createGsak());
		Long sakId = persistedSak.getSakId();
		Journalpost actualJournalpost = createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg(sakId);
		journalpostTestRepository.persist(actualJournalpost);
		UtsendingsInfo utsendingsInfo = createNavNoUtsendingsInfo(actualJournalpost);
		utsendingsInfoTestRepository.persist(utsendingsInfo);

		JournalpostView journalpostView = safinternJournalpostRepository.hentJournalpostById(actualJournalpost.getJournalpostId(), create(JournalpostView.class)).orElse(null);
		assertNavNoUtsendingsInfo(journalpostView.getUtsendingsInfo());
	}

	@Test
	void shouldHentJournalpostWhenDigitalpost() {
		Sak persistedSak = sakTestRepository.persist(createGsak());
		Long sakId = persistedSak.getSakId();
		Journalpost actualJournalpost = createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg(sakId);
		actualJournalpost.setUtsendingskanal(UtsendingsKanalCode.SDP);
		journalpostTestRepository.persist(actualJournalpost);
		UtsendingsInfo utsendingsInfo = createDigitalPostUtsendingsInfo(actualJournalpost);
		utsendingsInfoTestRepository.persist(utsendingsInfo);

		JournalpostView journalpostView = safinternJournalpostRepository.hentJournalpostById(actualJournalpost.getJournalpostId(), create(JournalpostView.class)).orElse(null);
		assertDigitalPostUtsendingsInfo(journalpostView.getUtsendingsInfo());
	}

	@Test
	void shouldReturnNullWhenJournalpostIdNotFound() {
		Optional<JournalpostView> journalpostView = safinternJournalpostRepository.hentJournalpostById(123L, create(JournalpostView.class));

		assertThat(journalpostView).isEmpty();
	}

	@Test
	void shouldReturnNullWhenEksternReferanseIdNotFound() {
		Optional<JournalpostView> journalpostView = safinternJournalpostRepository.hentJournalpostByEksternReferanseId("ekstern", create(JournalpostView.class));

		assertThat(journalpostView).isEmpty();
	}

	@Test
	void shouldReturnNullWhenJournalpostIdAndDokumentInfoIdNotFound() {
		Optional<JournalpostView> journalpostView = safinternJournalpostRepository.hentJournalpostByIdDokumentInfoId(123L, 321L, create(JournalpostView.class));

		assertThat(journalpostView).isEmpty();
	}

	@Test
	void shouldReturnFiveValidVariantformater() {
		Journalpost actualJournalpost = createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg(1L);
		DokumentInfo dokumentInfo = actualJournalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		dokumentInfo.addFilDetaljer(createFildetaljerOgFil(dokumentInfo, SLADDET, FilTypeCode.XML, UUID.randomUUID().toString()));
		dokumentInfo.addFilDetaljer(createFildetaljerOgFil(dokumentInfo, ORIGINAL, FilTypeCode.XML, UUID.randomUUID().toString()));
		dokumentInfo.addFilDetaljer(createFildetaljerOgFil(dokumentInfo, FULLVERSJON, FilTypeCode.XML, UUID.randomUUID().toString()));
		// disse skal ikke hentes
		dokumentInfo.addFilDetaljer(createFildetaljerOgFil(dokumentInfo, SKANNING_META, FilTypeCode.XML, UUID.randomUUID().toString()));
		dokumentInfo.addFilDetaljer(createFildetaljerOgFil(dokumentInfo, BREVBESTILLING, FilTypeCode.XML, UUID.randomUUID().toString()));
		dokumentInfo.addFilDetaljer(createFildetaljerOgFil(dokumentInfo, PRODUKSJON_DLF, FilTypeCode.XML, UUID.randomUUID().toString()));
		journalpostTestRepository.persist(actualJournalpost);

		JournalpostView journalpostView = safinternJournalpostRepository.hentJournalpostById(actualJournalpost.getJournalpostId(), create(JournalpostView.class)).orElse(null);
		assertThat(journalpostView.getDokumenter().iterator().next().getFildetaljer())
				.hasSize(5)
				.extracting(FildetaljerView::getFormat)
				.containsExactlyInAnyOrder(ARKIV, SLADDET, PRODUKSJON, FULLVERSJON, ORIGINAL);
	}

	private void saveBehandlingstema() {
		entityManager.createNativeQuery("""
					INSERT INTO T_K_BEHANDLINGSTEMA (k_behandlingstema,dekode,dato_fom,dato_tom,er_gyldig,dato_opprettet,opprettet_av,dato_endret,endret_av) 
					VALUES ('ab0438','Lønnskompensasjon',date '1900-01-01',NULL,'1',timestamp '2018-10-05 13:00:00','itest',timestamp '2018-10-05 13:00:00','itest');
				""").executeUpdate();
	}

	private int saveInnsyn() {
		return entityManager.createNativeQuery("""
					INSERT INTO T_K_INNSYN (k_innsyn,beskrivelse) 
					VALUES ('BRUK_STANDARDREGLER','Standardreglene avgjør om dokumentet vises');
				""").executeUpdate();
	}

	private void saveFagomrade() {
		fagomradeTestRepository.persist(
				Fagomrade.builder()
						.kode("RPO")
						.dekode("Retting av personopplysninger")
						.erGyldig(false)
						.datoTilOgMed(LocalDate.of(2023, 5, 1))
						.build());
	}

	private static void assertJournalpost(JournalpostView journalpostView) {
		assertThat(journalpostView.getFagomraade()).isEqualTo(FagomradeCode.RPO);
		assertThat(journalpostView.getFagomraadenavn()).isEqualTo("Retting av personopplysninger");
		assertThat(journalpostView.getStatus()).isEqualTo(JournalStatusCode.FS);
		assertThat(journalpostView.getType()).isEqualTo(JournalpostTypeCode.U);
		assertThat(journalpostView.getKanalreferanseId()).isEqualTo(KANAL_REFERANSE_ID);
		assertThat(journalpostView.getMottakskanal()).isEqualTo(MottaksKanalCode.NAV_NO);
		assertThat(journalpostView.getUtsendingskanal()).isEqualTo(UtsendingsKanalCode.S);
		assertThat(journalpostView.getBehandlingstema()).isEqualTo(BEHANDLINGSTEMA);
		assertThat(journalpostView.getBehandlingstemanavn()).isEqualTo(BEHANDLINGSTEMA_DEKODE);
		assertThat(journalpostView.getInnhold()).isEqualTo(INNHOLD);
		assertThat(journalpostView.getJournalfoerendeEnhet()).isEqualTo(JOURNALFOERENDE_ENHET);
		assertThat(journalpostView.getJournalfoertAvNavn()).isEqualTo(JOURNALFOERT_AV_NAVN);
		assertThat(journalpostView.getOpprettetAvNavn()).isEqualTo(OPPRETTET_AV_NAVN);
		assertThat(journalpostView.getAntallRetur()).isEqualTo(ANTALL_RETUR);
		assertThat(journalpostView.getInnsyn()).isEqualTo(BRUK_STANDARDREGLER);
		assertThat(journalpostView.getInnsynsbeskrivelse()).isEqualTo(INNSYNSBESKRIVELSE_BRUK_STANDARDREGLER);
		assertThat(journalpostView.getSkjerming()).isEqualTo(SKJERMING_TYPE_CODE);
	}

	private static void assertTilleggsopplysninger(Map<String, String> tilleggsopplysninger) {
		assertThat(tilleggsopplysninger).hasSize(4);
		assertThat(tilleggsopplysninger).extractingByKey(TILLEGGOPPLYSNINGER_KEY_1).isEqualTo(TILLEGGOPPLYSNINGER_VAL_1);
		assertThat(tilleggsopplysninger).extractingByKey(TILLEGGOPPLYSNINGER_KEY_2).isEqualTo(TILLEGGOPPLYSNINGER_VAL_2);
		assertThat(tilleggsopplysninger).extractingByKey(TILLEGGOPPLYSNINGER_KEY_3).isEqualTo(TILLEGGOPPLYSNINGER_VAL_3);
		assertThat(tilleggsopplysninger).extractingByKey(TILLEGGOPPLYSNINGER_KEY_4).isEqualTo(TILLEGGOPPLYSNINGER_VAL_4);
	}

	private static void assertRelevanteDatoer(RelevanteDatoerView relevanteDatoer) {
		assertThat(relevanteDatoer).hasNoNullFieldsOrProperties();
	}

	private static void assertAvsenderMottaker(AvsenderMottakerView avsenderMottaker) {
		assertThat(avsenderMottaker.getId()).isEqualTo(AVSENDER_MOTTAKER_ID);
		assertThat(avsenderMottaker.getType()).isEqualTo(AVSENDER_MOTTAKER_ID_TYPE);
		assertThat(avsenderMottaker.getNavn()).isEqualTo(AVSENDER_MOTTAKER_NAVN);
		assertThat(avsenderMottaker.getLand()).isEqualTo(AVSENDER_MOTTAKER_LAND);
	}

	private static void assertFysiskpostadresseUtsendingsInfo(UtsendingsInfoView utsendingsInfo) {
		assertThat(utsendingsInfo.getNavNoVarsling()).isNull();
		assertThat(utsendingsInfo.getDigitalPostadresse()).isNull();
		assertThat(utsendingsInfo.getEpostVarsel()).isNull();
		assertThat(utsendingsInfo.getSmsVarsel()).isNull();
		FysiskpostadresseView fysiskPostadresse = utsendingsInfo.getFysiskPostadresse();
		assertThat(fysiskPostadresse.getAdresselinje1()).isEqualTo(ADRESSELINJE1);
		assertThat(fysiskPostadresse.getAdresselinje2()).isEqualTo(ADRESSELINJE2);
		assertThat(fysiskPostadresse.getAdresselinje3()).isEqualTo(ADRESSELINJE3);
		assertThat(fysiskPostadresse.getPostnummer()).isEqualTo(POSTNUMMER);
		assertThat(fysiskPostadresse.getPoststed()).isEqualTo(POSTSTED);
		assertThat(fysiskPostadresse.getLandkode()).isEqualTo(LANDKODE_NO);
	}

	private static void assertNavNoUtsendingsInfo(UtsendingsInfoView utsendingsInfo) {
		assertThat(utsendingsInfo.getDigitalPostadresse()).isNull();
		assertThat(utsendingsInfo.getFysiskPostadresse()).isNull();
		NavNoVarslingView navNoVarsling = utsendingsInfo.getNavNoVarsling();
		assertThat(navNoVarsling.getVarselSendtTil()).isEqualTo("navno-identifikator-for-mottaker");
		assertThat(navNoVarsling.getVarseltekst()).isEqualTo("varslingstekst");
		assertThat(utsendingsInfo.getEpostVarsel()).isEqualToIgnoringWhitespace("[{\"tittel\":\"tittel\",\"tekst\":\"tekst\",\"epostadresse\":\"homer@epos.gr\",\"varslingstidspunkt\":\"2023-02-27T12:30:00.000\"}]");
		assertThat(utsendingsInfo.getSmsVarsel()).isEqualToIgnoringWhitespace("[{\"tekst\":\"tekst\",\"mobilnummer\":\"+4700000000\",\"varslingstidspunkt\":\"2023-02-27T12:30:00.000\"}]");
	}

	private static void assertDigitalPostUtsendingsInfo(UtsendingsInfoView utsendingsInfo) {
		assertThat(utsendingsInfo.getNavNoVarsling()).isNull();
		assertThat(utsendingsInfo.getFysiskPostadresse()).isNull();
		DigitalPostadresseView digitalPostadresse = utsendingsInfo.getDigitalPostadresse();
		assertThat(digitalPostadresse.getAdresse()).isEqualTo("bjarne.betjent#12AB");
		assertThat(digitalPostadresse.getLeverandoer()).isEqualTo("12345678");
		assertThat(utsendingsInfo.getEpostVarsel()).isEqualToIgnoringWhitespace("[{\"tittel\":\"tittel\",\"tekst\":\"tekst\",\"epostadresse\":\"homer@epos.gr\",\"varslingstidspunkt\":\"2023-02-27T12:30:00.000\"}]");
		assertThat(utsendingsInfo.getSmsVarsel()).isEqualToIgnoringWhitespace("[{\"tekst\":\"tekst\",\"mobilnummer\":\"+4700000000\",\"varslingstidspunkt\":\"2023-02-27T12:30:00.000\"}]");
	}

	private static void assertHoveddokument(DokumentinfoView hoveddokument, Long journalpostId) {
		assertThat(hoveddokument.getDokumentInfoId()).isNotNull();
		assertThat(hoveddokument.getTilknyttetSom()).isEqualTo(HOVEDDOKUMENT);
		assertThat(hoveddokument.getSkjerming()).isNull();
		assertThat(hoveddokument.getStatus()).isEqualTo(DokumentStatusCode.FERDIGSTILT);
		assertThat(hoveddokument.getFerdigDato()).isNotNull();
		assertThat(hoveddokument.getBrevkode()).isEqualTo(BREVKODE);
		assertThat(hoveddokument.getDokumenttypeId()).isEqualTo(DOKUMENT_TYPE_ID);
		assertThat(hoveddokument.getTittel()).isEqualTo(DOKUMENT_INFO_TITTEL);
		assertThat(hoveddokument.getKassert()).isFalse();
		assertThat(hoveddokument.getKategori()).isEqualTo(DokumentKategoriCode.ES);
		assertThat(hoveddokument.getSensitivt()).isTrue();
		assertThat(hoveddokument.getOriginalJournalpostId()).isEqualTo(journalpostId);

		assertVarianter(hoveddokument.getFildetaljer());
		assertLogiskVedlegg(hoveddokument.getLogiskVedlegg());
	}

	private static void assertVedlegg(DokumentinfoView vedlegg, Long journalpostId) {
		assertThat(vedlegg.getDokumentInfoId()).isNotNull();
		assertThat(vedlegg.getTilknyttetSom()).isEqualTo(VEDLEGG);
		assertThat(vedlegg.getSkjerming()).isEqualTo(SKJERMING_TYPE_CODE);
		assertThat(vedlegg.getStatus()).isEqualTo(DokumentStatusCode.FERDIGSTILT);
		assertThat(vedlegg.getFerdigDato()).isNotNull();
		assertThat(vedlegg.getBrevkode()).isEqualTo(BREVKODE);
		assertThat(vedlegg.getDokumenttypeId()).isEqualTo(DOKUMENT_TYPE_ID);
		assertThat(vedlegg.getTittel()).isEqualTo(DOKUMENT_INFO_TITTEL);
		assertThat(vedlegg.getKassert()).isFalse();
		assertThat(vedlegg.getKategori()).isEqualTo(DokumentKategoriCode.ES);
		assertThat(vedlegg.getSensitivt()).isTrue();
		assertThat(vedlegg.getOriginalJournalpostId()).isEqualTo(journalpostId);

		assertVarianter(vedlegg.getFildetaljer());
		assertLogiskVedlegg(vedlegg.getLogiskVedlegg());
	}

	private static void assertVarianter(Set<FildetaljerView> varianter) {
		assertThat(varianter).hasSize(2);

		assertThat(varianter).extracting(FildetaljerView::getUuid)
				.doesNotContainNull();
		assertThat(varianter).extracting(FildetaljerView::getNavn,
				FildetaljerView::getType,
				FildetaljerView::getStoerrelse,
				FildetaljerView::getSkjerming,
				FildetaljerView::getFormat).containsExactlyInAnyOrder(
				tuple(FIL_NAVN, FilTypeCode.PDF, "13", POL, ARKIV),
				tuple(FIL_NAVN, FilTypeCode.JSON, "13", POL, VariantFormatCode.PRODUKSJON)
		);
	}

	private static void assertLogiskVedlegg(Set<LogiskVedleggView> logiskVedlegg) {
		assertThat(logiskVedlegg).hasSize(1);
		assertThat(logiskVedlegg).extracting(LogiskVedleggView::getVedleggId)
				.doesNotContainNull();
		assertThat(logiskVedlegg).extracting(LogiskVedleggView::getTittel)
				.containsExactly(SKANNET_INNHOLD_TITTEL);
	}
}