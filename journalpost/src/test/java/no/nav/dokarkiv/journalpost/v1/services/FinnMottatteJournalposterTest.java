package no.nav.dokarkiv.journalpost.v1.services;

import no.nav.dokarkiv.core.domain.ChangeStamp;
import no.nav.dokarkiv.core.domain.codes.Behandlingstema;
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.core.exceptions.InvalidArgumentException;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.core.repository.journalpostliste.JournalpostListeRepository;
import no.nav.dokarkiv.core.util.TestDataUtils;
import no.nav.dokarkiv.journalpost.v1.api.finnMottatteJournalposter.UbehandletJournalpost;
import org.joda.time.DateTime;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Date;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class FinnMottatteJournalposterTest {

	@InjectMocks
	FinnMottatteJournalposterService finnMottatteJournalposterService;

	@Mock
	private JournalpostListeRepository journalpostListeRepository;

	@Mock
	private JoarkRepository joarkRepository;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void FinnMottateJournalposterServiceMapsEmptyListToEmptyFinnMottatteJournalposterResponse() {
		when(journalpostListeRepository.findUbehandletjournalpostListe()).thenReturn(List.of());
		assertThat(finnMottatteJournalposterService.finnMottatteJournalposter().getJournalposter().isEmpty(), is(true));
	}

	@Test
	public void FinnMottatteJournalposterServiceMapsJournalpostToFinnMottateJournalposterResponse() {
		Date createdDate = DateTime.now().minusWeeks(2).toDate();

		when(journalpostListeRepository.findUbehandletjournalpostListe()).thenReturn(List.of(generateJournalpost(createdDate, "test")));
		List<UbehandletJournalpost> ubehandletJournalpostList = finnMottatteJournalposterService.finnMottatteJournalposter().getJournalposter();

		assertThat(ubehandletJournalpostList.size(), is(1));

		UbehandletJournalpost ubehandletJournalpost = ubehandletJournalpostList.get(0);

		assertThat(ubehandletJournalpost.getBehandlingstema(), is(Behandlingstema.ab0001));
		assertThat(ubehandletJournalpost.getBruker().getId(), is("test"));
		assertThat(ubehandletJournalpost.getBruker().getType(), is(BrukerTypeCode.PERSON));
		assertThat(ubehandletJournalpost.getDatoOpprettet().equals(createdDate), is(true));
		assertThat(ubehandletJournalpost.getJournalforendeEnhet(), is("test"));
		assertThat(ubehandletJournalpost.getJournalpostId(), is((long) 300000000));
		assertThat(ubehandletJournalpost.getJournalStatus(), is(JournalStatusCode.MO));
		assertThat(ubehandletJournalpost.getTema(), is(FagomradeCode.PEN));
	}

	@Test
	public void ifMultipleBrukereMapTheLastCreatedToResponse() {
		Journalpost journalpost = TestDataUtils.createUbehandletJournalpost(DateTime.now().minusWeeks(4).toDate(), JournalpostTypeCode.I, JournalStatusCode.MO);

		Bruker oldest = generateBruker("oldest", BrukerTypeCode.PERSON, DateTime.now().minusWeeks(4).toDate());
		Bruker middle = generateBruker("middle", BrukerTypeCode.PERSON, DateTime.now().minusWeeks(3).toDate());
		Bruker youngest = generateBruker("youngest", BrukerTypeCode.PERSON, DateTime.now().minusWeeks(2).toDate());

		journalpost.addBruker(oldest);
		journalpost.addBruker(youngest);
		journalpost.addBruker(middle);

		when(journalpostListeRepository.findUbehandletjournalpostListe()).thenReturn(List.of(journalpost));
		UbehandletJournalpost ubehandletJournalpost = finnMottatteJournalposterService.finnMottatteJournalposter().getJournalposter().get(0);

		assertThat(ubehandletJournalpost.getBruker().getId(), is("youngest"));
	}

	@Test
	public void handlesMultipleJournalposts() {
		when(journalpostListeRepository.findUbehandletjournalpostListe()).thenReturn((List.of(
				generateJournalpost(),
				generateJournalpost(),
				generateJournalpost(),
				generateJournalpost(),
				generateJournalpost()
		)));

		assertThat(finnMottatteJournalposterService.finnMottatteJournalposter().getJournalposter().size(), is(5));
	}

	@Test
	public void throwsIfJournalpostDoesNotValidate() {
		expectedException.expect(InvalidArgumentException.class);
		expectedException.expectMessage("Journalpost.journalposttype must be set");

		when(journalpostListeRepository.findUbehandletjournalpostListe()).thenReturn(List.of(new Journalpost()));
		finnMottatteJournalposterService.finnMottatteJournalposter();
	}

	@Test
	public void throwsIfYoungerThanAWeek() {
		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("changeStamp.createddate må være eldre enn en(1) uke");

		when(journalpostListeRepository.findUbehandletjournalpostListe()).thenReturn(List.of(generateJournalpost(DateTime.now().toDate())));
		finnMottatteJournalposterService.finnMottatteJournalposter();
	}

	@Test
	public void throwIfJournalpostIsNotTypeCodeI(){
		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("journalposttype må være I");

		when(journalpostListeRepository.findUbehandletjournalpostListe())
				.thenReturn(List.of(
						generateJournalpost(
								DateTime.now().minusWeeks(2).toDate(),
								"unitTest",
								BrukerTypeCode.PERSON,
								JournalpostTypeCode.U,
								JournalStatusCode.MO)
				));
		finnMottatteJournalposterService.finnMottatteJournalposter();
	}

	@Test
	public void throwIfJournalpostIsNotStatusCodeMOorM(){
		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("journalstatus må være MO eller M");

		when(journalpostListeRepository.findUbehandletjournalpostListe())
				.thenReturn(List.of(
						generateJournalpost(
								DateTime.now().minusWeeks(2).toDate(),
								"unitTest",
								BrukerTypeCode.PERSON,
								JournalpostTypeCode.I,
								JournalStatusCode.U)
				));
		finnMottatteJournalposterService.finnMottatteJournalposter();
	}

	private Journalpost generateJournalpost(){
		return generateJournalpost(DateTime.now().minusWeeks(2).toDate());
	}

	private Journalpost generateJournalpost(Date createdDate) {
		return generateJournalpost(createdDate, "unitTest");
	}

	private Journalpost generateJournalpost(Date createdDate, String brukerId) {
		return generateJournalpost(createdDate, brukerId, BrukerTypeCode.PERSON);
	}

	private Journalpost generateJournalpost(Date createdDate, String brukerid, BrukerTypeCode typeCode) {
		return generateJournalpost(createdDate, brukerid, typeCode, JournalpostTypeCode.I, JournalStatusCode.MO);
	}

	private Journalpost generateJournalpost(
			Date createdDate,
			String brukerid,
			BrukerTypeCode brukerTypeCode,
			JournalpostTypeCode journalpostTypeCode,
			JournalStatusCode journalStatusCode
	) {
		Journalpost journalpost = TestDataUtils.createUbehandletJournalpost(createdDate, journalpostTypeCode, journalStatusCode);
		journalpost.addBruker(generateBruker(brukerid, brukerTypeCode, createdDate));
		return journalpost;
	}

	private Bruker generateBruker(String id, BrukerTypeCode typeCode, Date createdDate) {
		Bruker bruker = Bruker.builder().brukerId(id).brukerType(typeCode).build();
		bruker.setChangeStamp(new ChangeStamp("unitTest", createdDate, "unitTest", createdDate));
		return bruker;
	}

}