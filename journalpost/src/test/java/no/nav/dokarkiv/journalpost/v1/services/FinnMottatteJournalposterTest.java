package no.nav.dokarkiv.journalpost.v1.services;

import no.nav.dokarkiv.core.domain.ChangeStamp;
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.repository.JoarkRepository;
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
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class FinnMottatteJournalposterTest {

	@InjectMocks
	FinnMottatteJournalposterService finnMottatteJournalposterService;

	@Mock
	private JoarkRepository joarkRepository;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void FinnMottateJournalposterServiceMapsEmptyListToEmptyFinnMottatteJournalposterResponse() {
		when(joarkRepository.findUbehandledeJournalposts(any(Date.class))).thenReturn(Optional.empty());
		assertTrue(finnMottatteJournalposterService.finnMottatteJournalposter().getJournalposter().isEmpty());
	}

	@Test
	public void FinnMottatteJournalposterServiceMapsJournalpostToFinnMottateJournalposterResponse() {
		Date createdDate = DateTime.now().minusWeeks(2).toDate();

		when(joarkRepository.findUbehandledeJournalposts(any(Date.class))).thenReturn(Optional.of(List.of(generateJournalpost(createdDate, "test"))));
		List<UbehandletJournalpost> ubehandletJournalpostList = finnMottatteJournalposterService.finnMottatteJournalposter().getJournalposter();

		assertEquals(1, ubehandletJournalpostList.size());

		UbehandletJournalpost ubehandletJournalpost = ubehandletJournalpostList.get(0);

		assertEquals("ab0001", ubehandletJournalpost.getBehandlingstema());
		assertEquals("test", ubehandletJournalpost.getBruker().getId());
		assertEquals(BrukerTypeCode.PERSON.name(), ubehandletJournalpost.getBruker().getType());
		assertEquals(createdDate, ubehandletJournalpost.getDatoOpprettet());
		assertEquals("test", ubehandletJournalpost.getJournalforendeEnhet());
		assertEquals(300000000L, ubehandletJournalpost.getJournalpostId());
		assertEquals(JournalStatusCode.MO.name(), ubehandletJournalpost.getJournalStatus());
		assertEquals(FagomradeCode.PEN.name(), ubehandletJournalpost.getTema());
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

		when(joarkRepository.findUbehandledeJournalposts(any(Date.class))).thenReturn(Optional.of(List.of(journalpost)));
		UbehandletJournalpost ubehandletJournalpost = finnMottatteJournalposterService.finnMottatteJournalposter().getJournalposter().get(0);

		assertEquals("youngest", ubehandletJournalpost.getBruker().getId());
	}

	@Test
	public void handlesMultipleJournalposts() {
		when(joarkRepository.findUbehandledeJournalposts(any(Date.class))).thenReturn(Optional.of(List.of(
				generateJournalpost(),
				generateJournalpost(),
				generateJournalpost(),
				generateJournalpost(),
				generateJournalpost()
		)));

		assertEquals(5, finnMottatteJournalposterService.finnMottatteJournalposter().getJournalposter().size());
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