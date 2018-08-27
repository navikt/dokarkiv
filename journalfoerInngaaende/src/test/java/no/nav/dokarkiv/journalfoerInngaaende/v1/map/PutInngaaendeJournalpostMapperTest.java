package no.nav.dokarkiv.journalfoerInngaaende.v1.map;

import static no.nav.dokarkiv.journalfoerInngaaende.v1.util.TestUtils.AVSENDER_ID_ORGANISASJON;
import static no.nav.dokarkiv.journalfoerInngaaende.v1.util.TestUtils.AVSENDER_ID_PERSON;
import static no.nav.dokarkiv.journalfoerInngaaende.v1.util.TestUtils.AVSENDER_NAVN;
import static no.nav.dokarkiv.journalfoerInngaaende.v1.util.TestUtils.AVSENDER_NAVN_ORGANISASJON;
import static no.nav.dokarkiv.journalfoerInngaaende.v1.util.TestUtils.BRUKER_ID_ORGANISASJON;
import static no.nav.dokarkiv.journalfoerInngaaende.v1.util.TestUtils.BRUKER_ID_PERSON;
import static no.nav.dokarkiv.journalfoerInngaaende.v1.util.TestUtils.SAK_ID;
import static no.nav.dokarkiv.journalfoerInngaaende.v1.util.TestUtils.createJournalpost;
import static no.nav.dokarkiv.journalfoerInngaaende.v1.util.TestUtils.createJournalpostForOppdatering;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;

import no.nav.dok.tjenester.journalfoerinngaaende.ArkivSak;
import no.nav.dok.tjenester.journalfoerinngaaende.Avsender;
import no.nav.dok.tjenester.journalfoerinngaaende.Bruker;
import no.nav.dok.tjenester.journalfoerinngaaende.PutJournalpostRequest;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import org.junit.Test;

public class PutInngaaendeJournalpostMapperTest {

	private PutJournalpostRequest putJournalpostRequest;

	private Journalpost journalpost;

	private PutInngaaendeJournalpostMapper mapper = new PutInngaaendeJournalpostMapper();

	@Test
	public void shouldUpdateJournalpost() {
		putJournalpostRequest = createPutJournalpostRequest();

		journalpost = createJournalpost();

		assertThat(journalpost.getBrukere(), hasSize(2));

		mapper.map(journalpost, putJournalpostRequest);

		assertThat(journalpost.getFagomrade().name(), is(putJournalpostRequest.getTema()));
		assertThat(journalpost.getInnhold(), is(putJournalpostRequest.getTittel()));
		assertThat(journalpost.getBrukere(), hasSize(1));
	}

	@Test
	public void shouldNotClearBrukerListeVedOppdateringAvEksisterende() {
		putJournalpostRequest = createPutJournalpostRequest();

		journalpost = createJournalpostForOppdatering();

		mapper.map(journalpost, putJournalpostRequest);

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
		avsender.setNavn(AVSENDER_NAVN);
		avsender.setAvsenderType(Avsender.AvsenderType.PERSON);
		avsender.setIdentifikator(AVSENDER_ID_PERSON);
		return avsender;
	}

	private static Avsender createAvsenderOrganisasjon() {
		Avsender avsender = new Avsender();
		avsender.setNavn(AVSENDER_NAVN_ORGANISASJON);
		avsender.setAvsenderType(Avsender.AvsenderType.ORGANISASJON);
		avsender.setIdentifikator(AVSENDER_ID_ORGANISASJON);
		return avsender;
	}

	private static Bruker createBrukerPerson() {
		Bruker bruker = new Bruker();
		bruker.setIdentifikator(BRUKER_ID_PERSON);
		bruker.setBrukerType(Bruker.BrukerType.PERSON);
		return bruker;
	}

	private static Bruker createBrukerOrganisasjon() {
		Bruker bruker = new Bruker();
		bruker.setIdentifikator(BRUKER_ID_ORGANISASJON);
		bruker.setBrukerType(Bruker.BrukerType.ORGANISASJON);
		return bruker;
	}

	private static ArkivSak createArkivSak() {
		ArkivSak arkivSak = new ArkivSak();
		arkivSak.setArkivSakId(SAK_ID);
		arkivSak.setArkivSakSystem("GSAK");
		return arkivSak;
	}
}