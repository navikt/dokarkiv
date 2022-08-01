package no.nav.dokarkiv.journalfoerinngaaende.v1.rjoark002i;

import no.nav.dok.tjenester.journalfoerinngaaende.ArkivSakWithArkivsakSystemEnum;
import no.nav.dok.tjenester.journalfoerinngaaende.Avsender;
import no.nav.dok.tjenester.journalfoerinngaaende.Bruker;
import no.nav.dok.tjenester.journalfoerinngaaende.PutJournalpostRequest;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.repository.BrukerRepository;
import no.nav.dokarkiv.journalfoerinngaaende.v1.util.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

@ExtendWith(MockitoExtension.class)
public class PutInngaaendeJournalpostMapperTest {

	@Mock
	private BrukerRepository brukerRepositoryMock;

	private PutJournalpostRequest putJournalpostRequest;

	private Journalpost journalpost;

	@InjectMocks
	private PutInngaaendeJournalpostMapper mapper = new PutInngaaendeJournalpostMapper();


	@Test
	public void shouldUpdateJournalpost() {
		putJournalpostRequest = createPutJournalpostRequest();

		journalpost = TestUtils.createJournalpost();

		assertThat(journalpost.getBrukere(), hasSize(2));

		mapper.oppdaterJournalpost(journalpost, putJournalpostRequest);

		assertThat(journalpost.getFagomrade().name(), is(putJournalpostRequest.getTema()));
		assertThat(journalpost.getInnhold(), is(putJournalpostRequest.getTittel()));
		assertThat(journalpost.getBrukere(), hasSize(1));
	}

	@Test
	public void shouldNotClearBrukerListeVedOppdateringAvEksisterende() {
		putJournalpostRequest = createPutJournalpostRequest();

		journalpost = TestUtils.createJournalpostForOppdatering();

		mapper.oppdaterJournalpost(journalpost, putJournalpostRequest);

		assertThat(journalpost.getBrukere(), hasSize(1));
	}

	private PutJournalpostRequest createPutJournalpostRequest() {
		PutJournalpostRequest request = new PutJournalpostRequest();
		request.setForsoekEndeligJF(true);
		request.setAvsender(createAvsenderPerson());
		request.setBruker(createBrukerPerson());
		request.setArkivSak(createArkivSak());
		request.setTema(FagomradeCode.AAP.name());
		request.setTittel("TITTEL");
		request.setJournalfEnhet("1337");
		return request;
	}

	private static Avsender createAvsenderPerson() {
		Avsender avsender = new Avsender();
		avsender.setNavn(TestUtils.AVSENDER_NAVN);
		avsender.setAvsenderType(Avsender.AvsenderType.PERSON);
		avsender.setIdentifikator(TestUtils.AVSENDER_ID_PERSON);
		return avsender;
	}

	private static Avsender createAvsenderOrganisasjon() {
		Avsender avsender = new Avsender();
		avsender.setNavn(TestUtils.AVSENDER_NAVN_ORGANISASJON);
		avsender.setAvsenderType(Avsender.AvsenderType.ORGANISASJON);
		avsender.setIdentifikator(TestUtils.AVSENDER_ID_ORGANISASJON);
		return avsender;
	}

	private static Bruker createBrukerPerson() {
		Bruker bruker = new Bruker();
		bruker.setIdentifikator(TestUtils.BRUKER_ID_PERSON);
		bruker.setBrukerType(Bruker.BrukerType.PERSON);
		return bruker;
	}

	private static Bruker createBrukerOrganisasjon() {
		Bruker bruker = new Bruker();
		bruker.setIdentifikator(TestUtils.BRUKER_ID_ORGANISASJON);
		bruker.setBrukerType(Bruker.BrukerType.ORGANISASJON);
		return bruker;
	}

	private static ArkivSakWithArkivsakSystemEnum createArkivSak() {
		ArkivSakWithArkivsakSystemEnum arkivSak = new ArkivSakWithArkivsakSystemEnum();
		arkivSak.setArkivSakId(TestUtils.SAK_ID);
		arkivSak.setArkivSakSystem(ArkivSakWithArkivsakSystemEnum.ArkivSakSystem.GSAK);
		return arkivSak;
	}
}