package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark104;


import no.nav.dokarkiv.arkiverdokumentproduksjon.AbstractArkiverdokumentproduksjonItest;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.util.DateProvider;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.SettDatoSendtRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Arrays;
import java.util.GregorianCalendar;

import static no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder.getDokumentInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static no.nav.dokarkiv.core.domain.builder.SaksrelasjonBuilder.getSaksrelasjonBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;

/**
 * Itest for the settDatoSendt operation
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public class SettDatoSendtIT extends AbstractArkiverdokumentproduksjonItest {

	private static final String ENDRET_AV_NAVN = "Tester2";

	private SettDatoSendtRequest request;

	private Journalpost journalpost1;
	private Journalpost journalpost2;
	private Journalpost journalpost3;

	@BeforeEach
	public void setUp() throws Exception {
		DateProvider.configure(true, "2018-06-20T14:31:54.767");
		journalpost1 = createJournalpost();
		journalpost2 = createJournalpost();
		journalpost3 = createJournalpost();
		journalpostTestRepository.persist(journalpost1);
		journalpostTestRepository.persist(journalpost2);
		journalpostTestRepository.persist(journalpost3);
		request = createWsRequest(journalpost1.getJournalpostId(), journalpost2.getJournalpostId());
	}

	@Test
	public void shouldSettDatoSendtOnJournalpostIdsInRequest() throws Exception {
		arkiverDokumentproduksjonProvider.settDatoSendt(request);

		Journalpost persistedJournalpost1 = journalpostTestRepository.findById(journalpost1.getJournalpostId()).get();
		Journalpost persistedJournalpost2 = journalpostTestRepository.findById(journalpost2.getJournalpostId()).get();

		assertThat(persistedJournalpost1.getSendtPrintDato(), is(DateProvider.getToday()));
		assertThat(persistedJournalpost2.getSendtPrintDato(), is(DateProvider.getToday()));
	}

	@Test
	public void shouldNotSettDatoSendtOnOtherJournalposts() {
		arkiverDokumentproduksjonProvider.settDatoSendt(request);

		Journalpost untouchedJournalpost = journalpostTestRepository.findById(journalpost3.getJournalpostId()).get();
		assertThat(untouchedJournalpost.getSendtPrintDato(), nullValue());
	}

	private SettDatoSendtRequest createWsRequest(Long... journalpostIds) throws DatatypeConfigurationException {
		SettDatoSendtRequest settDatoSendtRequest = new SettDatoSendtRequest();
		settDatoSendtRequest.getJournalpostIdListe().addAll(Arrays.asList(journalpostIds));
		settDatoSendtRequest.setDatoSendt(xmlGregorianCalendarToday());
		settDatoSendtRequest.setEndretAvNavn(ENDRET_AV_NAVN);
		return settDatoSendtRequest;
	}

	private Journalpost createJournalpost() {
		return getJournalpostBuilder()
				.avsenderMottakerId("02016126007")
				.journalStatus(JournalStatusCode.D)
				.journalpostType(JournalpostTypeCode.U)
				.opprettetAvNavn("opprettetAvNavn")
				.opprettetKildeNavn("opprettetKildeNavn")
				.fagomrade(FagomradeCode.PEN)
				.saksrelasjon(
						getSaksrelasjonBuilder()
								.sakId("1")
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
	}

	public XMLGregorianCalendar xmlGregorianCalendarToday() throws DatatypeConfigurationException {
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(DateProvider.getToday());
		return DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
	}
}
