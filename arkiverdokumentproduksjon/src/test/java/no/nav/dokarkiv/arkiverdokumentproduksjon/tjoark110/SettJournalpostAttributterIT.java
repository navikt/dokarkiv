package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark110;


import no.nav.dokarkiv.arkiverdokumentproduksjon.AbstractArkiverdokumentproduksjonItest;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.ApplicationException;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.SettJournalpostAttributterRequest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.LocalDateTime;
import java.util.GregorianCalendar;

import static java.time.temporal.ChronoUnit.SECONDS;
import static no.nav.dokarkiv.core.CoreConfig.ZONEID_NORGE;
import static no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder.getDokumentInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static no.nav.dokarkiv.core.domain.builder.SaksrelasjonBuilder.getSaksrelasjonBuilder;
import static org.assertj.core.api.Assertions.within;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SettJournalpostAttributterIT extends AbstractArkiverdokumentproduksjonItest {

	private static final String ENDRET_AV_NAVN = "Tester2";
	private static final int ANTALL_RETURPOST = 1;
	public static final String ORIGINAL_ENDRET_AV_NAVN = "original";
	public static final String UTSENDINGSKANAL = UtsendingsKanalCode.EESSI.name();

	@Test
	public void shouldSetAttributterOnJournalpostId() throws Exception {
		Journalpost journalpost1 = buildAndPersistJournalpost();

		SettJournalpostAttributterRequest request = createWsRequest(journalpost1.getJournalpostId());
		arkiverDokumentproduksjonProvider.settJournalpostAttributter(request);

		Journalpost persistedJournalpost1 = journalpostTestRepository.findById(journalpost1.getJournalpostId()).get();

		Assertions.assertThat(persistedJournalpost1.getSendtPrintDato()).isCloseTo(LocalDateTime.now(), within(3, SECONDS));
		assertThat(persistedJournalpost1.getAntallRetur(), is(ANTALL_RETURPOST));
		assertThat(persistedJournalpost1.getEndretAvNavn(), is(ENDRET_AV_NAVN));
		assertThat(persistedJournalpost1.getUtsendingskanal().name(), is(UTSENDINGSKANAL));
	}

	@Test
	public void shouldOnlySetUtsendingskanalAndEndretAvNavn() throws Exception {
		Journalpost journalpost1 = buildAndPersistJournalpost();

		SettJournalpostAttributterRequest request = createWsRequest(journalpost1.getJournalpostId())
				.withAntallReturpost(null)
				.withDatoSendt(null);
		arkiverDokumentproduksjonProvider.settJournalpostAttributter(request);

		Journalpost persistedJournalpost1 = journalpostTestRepository.findById(journalpost1.getJournalpostId()).get();

		assertThat(persistedJournalpost1.getSendtPrintDato(), is(nullValue()));
		assertThat(persistedJournalpost1.getAntallRetur(), is(nullValue()));
		assertThat(persistedJournalpost1.getAvsendtReturDato(), is(nullValue()));
		assertThat(persistedJournalpost1.getEndretAvNavn(), is(ENDRET_AV_NAVN));
		assertThat(persistedJournalpost1.getUtsendingskanal().name(), is(UTSENDINGSKANAL));
	}

	@Test
	public void shouldnotSetUtsendingskanalIfEndretAvNavnIsNull() throws Exception {
		Journalpost journalpost1 = buildAndPersistJournalpost();

		SettJournalpostAttributterRequest request = createWsRequest(journalpost1.getJournalpostId())
				.withEndretAvNavn(null);
		arkiverDokumentproduksjonProvider.settJournalpostAttributter(request);

		Journalpost persistedJournalpost1 = journalpostTestRepository.findById(journalpost1.getJournalpostId()).get();

		Assertions.assertThat(persistedJournalpost1.getSendtPrintDato()).isCloseTo(LocalDateTime.now(), within(3, SECONDS));
		assertThat(persistedJournalpost1.getAntallRetur(), is(ANTALL_RETURPOST));
		assertThat(persistedJournalpost1.getEndretAvNavn(), is(ORIGINAL_ENDRET_AV_NAVN));
		assertThat(persistedJournalpost1.getUtsendingskanal(), is(nullValue()));
	}

	@Test
	public void shouldNotSetUtsendingskanal() throws Exception {
		Journalpost journalpost1 = buildAndPersistJournalpost();

		SettJournalpostAttributterRequest request = createWsRequest(journalpost1.getJournalpostId())
				.withUtsendingskanal(null);
		arkiverDokumentproduksjonProvider.settJournalpostAttributter(request);

		Journalpost persistedJournalpost1 = journalpostTestRepository.findById(journalpost1.getJournalpostId()).get();

		Assertions.assertThat(persistedJournalpost1.getSendtPrintDato()).isCloseTo(LocalDateTime.now(), within(3, SECONDS));
		assertThat(persistedJournalpost1.getAntallRetur(), is(ANTALL_RETURPOST));
		assertThat(persistedJournalpost1.getEndretAvNavn(), is(ENDRET_AV_NAVN));
		assertThat(persistedJournalpost1.getUtsendingskanal(), is(nullValue()));
	}

	@Test
	public void shouldThrowExceptionIllegalUtsendingskanal() throws Exception {
		Journalpost journalpost1 = buildAndPersistJournalpost();

		SettJournalpostAttributterRequest request = createWsRequest(journalpost1.getJournalpostId())
				.withUtsendingskanal("Ugyldig_kanal");

		assertThrows(ApplicationException.class,
				() -> arkiverDokumentproduksjonProvider.settJournalpostAttributter(request));
	}

	@Test
	public void shouldOnlySetAntallReturAttributt() throws Exception {
		Journalpost journalpost1 = buildAndPersistJournalpost();

		SettJournalpostAttributterRequest request = createWsRequest(journalpost1.getJournalpostId())
				.withEndretAvNavn("")
				.withDatoSendt(null);
		arkiverDokumentproduksjonProvider.settJournalpostAttributter(request);

		Journalpost persistedJournalpost = journalpostTestRepository.findById(journalpost1.getJournalpostId()).get();

		assertThat(persistedJournalpost.getSendtPrintDato(), is(nullValue()));
		assertThat(persistedJournalpost.getAntallRetur(), is(ANTALL_RETURPOST));
		assertThat(persistedJournalpost.getEndretAvNavn(), is(ORIGINAL_ENDRET_AV_NAVN));
		Assertions.assertThat(persistedJournalpost.getAvsendtReturDato()).isCloseTo(LocalDateTime.now(), within(3, SECONDS));
	}

	@Test
	public void shouldSetAttributterOnJournalpostIdsInRequest() throws Exception {
		Journalpost journalpost1 = buildAndPersistJournalpost();
		Journalpost journalpost2 = buildAndPersistJournalpost();

		SettJournalpostAttributterRequest request = createWsRequest(journalpost1.getJournalpostId(), journalpost2.getJournalpostId());
		arkiverDokumentproduksjonProvider.settJournalpostAttributter(request);

		Journalpost persistedJournalpost1 = journalpostTestRepository.findById(journalpost1.getJournalpostId()).get();
		Journalpost persistedJournalpost2 = journalpostTestRepository.findById(journalpost2.getJournalpostId()).get();

		Assertions.assertThat(persistedJournalpost1.getSendtPrintDato()).isCloseTo(LocalDateTime.now(), within(3, SECONDS));
		Assertions.assertThat(persistedJournalpost2.getSendtPrintDato()).isCloseTo(LocalDateTime.now(), within(3, SECONDS));
		assertThat(persistedJournalpost1.getAntallRetur(), is(ANTALL_RETURPOST));
		assertThat(persistedJournalpost2.getAntallRetur(), is(ANTALL_RETURPOST));
		assertThat(persistedJournalpost1.getEndretAvNavn(), is(ENDRET_AV_NAVN));
		assertThat(persistedJournalpost2.getEndretAvNavn(), is(ENDRET_AV_NAVN));
		Assertions.assertThat(persistedJournalpost1.getAvsendtReturDato()).isCloseTo(LocalDateTime.now(), within(3, SECONDS));
		Assertions.assertThat(persistedJournalpost2.getAvsendtReturDato()).isCloseTo(LocalDateTime.now(), within(3, SECONDS));
	}

	@Test
	public void shouldNotSettDatoSendtOnOtherJournalposts() throws Exception {
		Journalpost journalpost1 = buildAndPersistJournalpost();
		Journalpost journalpost2 = buildAndPersistJournalpost();
		Journalpost journalpost3 = buildAndPersistJournalpost();

		SettJournalpostAttributterRequest request = createWsRequest(journalpost1.getJournalpostId(), journalpost2.getJournalpostId());
		arkiverDokumentproduksjonProvider.settJournalpostAttributter(request);

		Journalpost untouchedJournalpost = journalpostTestRepository.findById(journalpost3.getJournalpostId()).get();
		assertThat(untouchedJournalpost.getSendtPrintDato(), nullValue());
	}

	private SettJournalpostAttributterRequest createWsRequest(Long... journalpostIds) throws DatatypeConfigurationException {
		return new SettJournalpostAttributterRequest()
				.withJournalpostIdListe(journalpostIds)
				.withDatoSendt(xmlGregorianCalendarToday())
				.withEndretAvNavn(ENDRET_AV_NAVN)
				.withAntallReturpost(ANTALL_RETURPOST)
				.withUtsendingskanal(UTSENDINGSKANAL);
	}

	private Journalpost buildAndPersistJournalpost() {
		Journalpost journalpost = getJournalpostBuilder()
				.avsenderMottakerId("02016126007")
				.journalStatus(JournalStatusCode.D)
				.journalpostType(JournalpostTypeCode.U)
				.opprettetAvNavn("opprettetAvNavn")
				.opprettetKildeNavn("opprettetKildeNavn")
				.fagomrade(FagomradeCode.PEN)
				.endretAvNavn(ORIGINAL_ENDRET_AV_NAVN)
				.saksrelasjon(
						getSaksrelasjonBuilder()
								.sakId(1L)
								.fagsystem(FagsystemCode.PEN)
								.opprettetKildeNavn("opprettetKildeNavn")
								.build())
				.dokumentInfoRelasjoner(
						getJournalpostDokumentInfoRelasjonBuilder()
								.opprettetKildeNavn("opprettetKildeNavn")
								.tilknyttetAvNavn("tilknyttetAvNavn")
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
								.dokumentInfo(getDokumentInfoBuilder()
										.dokumentstatus(DokumentStatusCode.UNDER_REDIGERING)
										.endretAvNavn(ENDRET_AV_NAVN)
										.opprettetKildeNavn("opprettetKildeNavn")
										.build())
								.build())

				.build();

		journalpostTestRepository.persist(journalpost);
		return journalpost;
	}

	public XMLGregorianCalendar xmlGregorianCalendarToday() throws DatatypeConfigurationException {
		GregorianCalendar calendar = GregorianCalendar.from(LocalDateTime.now().atZone(ZONEID_NORGE));
		return DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
	}
}
