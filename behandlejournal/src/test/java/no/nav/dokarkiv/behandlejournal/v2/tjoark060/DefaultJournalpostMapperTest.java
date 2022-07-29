package no.nav.dokarkiv.behandlejournal.v2.tjoark060;

import no.nav.dokarkiv.behandlejournal.v2.datautil.ArkiverUstrukturertKravJournalpostAssertUtil;
import no.nav.dokarkiv.behandlejournal.v2.datautil.ArkiverUstrukturertKravJournalpostDataUtil;
import no.nav.dokarkiv.behandlejournal.v2.datautil.BehandleJournalCommonDataUtil;
import no.nav.dokarkiv.core.domain.util.DateProvider;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.arkiverustrukturertkrav.Journalpost;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.behandlejournal.EksternPart;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.behandlejournal.NorskIdent;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.behandlejournal.Organisasjon;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.behandlejournal.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

/**
 * Unit tests for DefaultJournalpostMapper.
 *
 * @author Thomas Eugen Bjørge, Visma Consulting
 */
public class DefaultJournalpostMapperTest {

	private static final String NAVN = "navn";
	private static final String ORG_NUMMER = "1235";
	private static final String FNR = "0000000000000000000";
	private DefaultJournalpostMapper journalpostMapper = new DefaultJournalpostMapper();

	private no.nav.dokarkiv.core.domain.entities.Journalpost domainJournalpost;

	@BeforeEach
	public void setUp() throws Exception {
		DateProvider.configure(true, "2014-08-27T12:00:00");
	}

	@Test
	public void shouldReturnNullForNullInput() throws Exception {
		no.nav.dokarkiv.core.domain.entities.Journalpost result = journalpostMapper.map(null);

		assertThat(result, is(nullValue()));
	}

	@Test
	public void shouldMapArkiverUstrukturertKravJournalpost() throws Exception {
		Journalpost wsJournalpost = ArkiverUstrukturertKravJournalpostDataUtil.createJournalpost();
		domainJournalpost = journalpostMapper.map(wsJournalpost);
		ArkiverUstrukturertKravJournalpostAssertUtil.assertEqualJournalposts(domainJournalpost, wsJournalpost);
	}

	@Test
	public void shouldMapArkiverUstrukturertKravWithBrukerOrganisasjon() throws Exception {
		Journalpost wsJournalpost = ArkiverUstrukturertKravJournalpostDataUtil.createJournalpost();
		wsJournalpost.getForBruker().clear();
		wsJournalpost.getForBruker().add(BehandleJournalCommonDataUtil.createOrganisasjon());
		domainJournalpost = journalpostMapper.map(wsJournalpost);
		ArkiverUstrukturertKravJournalpostAssertUtil.assertEqualJournalposts(domainJournalpost, wsJournalpost);
	}

	@Test
	public void shouldHandleThatEksternPartIsNull() throws Exception {
		Journalpost wsJournalpost = ArkiverUstrukturertKravJournalpostDataUtil.createJournalpost();
		domainJournalpost = journalpostMapper.map(wsJournalpost);

		assertThat(domainJournalpost.getAvsenderMottakerId(), is(nullValue()));
	}

	@Test
	public void shouldMapEksternPartNavnToAvsenderMottaker() throws Exception {
		Journalpost wsJournalpost = ArkiverUstrukturertKravJournalpostDataUtil.createJournalpost();
		wsJournalpost.setEksternPart(createEksternPartWithOrganisasjon());

		domainJournalpost = journalpostMapper.map(wsJournalpost);

		assertThat(domainJournalpost.getAvsenderMottaker(), is(NAVN));
	}

	@Test
	public void shouldMapOrganisasjonIdentToAvsenderMottakerId() throws Exception {
		Journalpost wsJournalpost = ArkiverUstrukturertKravJournalpostDataUtil.createJournalpost();
		wsJournalpost.setEksternPart(createEksternPartWithOrganisasjon());

		domainJournalpost = journalpostMapper.map(wsJournalpost);

		assertThat(domainJournalpost.getAvsenderMottakerId(), is(ORG_NUMMER));
	}

	@Test
	public void shouldMapPersonIdentToAvsenderMottakerId() throws Exception {
		Journalpost wsJournalpost = ArkiverUstrukturertKravJournalpostDataUtil.createJournalpost();
		wsJournalpost.setEksternPart(createEksternPartWithPerson());

		domainJournalpost = journalpostMapper.map(wsJournalpost);

		assertThat(domainJournalpost.getAvsenderMottakerId(), is(FNR));
	}

	@Test
	public void shouldHandleEmptyEksternPartNavn() throws Exception {
		Journalpost wsJournalpost = ArkiverUstrukturertKravJournalpostDataUtil.createJournalpost();
		wsJournalpost.setEksternPart(createEksternPartWithPerson());
		wsJournalpost.getEksternPart().setNavn("");

		domainJournalpost = journalpostMapper.map(wsJournalpost);

		assertThat(domainJournalpost.getAvsenderMottaker(), is(""));
	}

	private EksternPart createEksternPartWithOrganisasjon() {
		EksternPart eksternPart = new EksternPart();
		eksternPart.setNavn(NAVN);
		eksternPart.setEksternAktoer(createOrganisasjon());
		return eksternPart;
	}

	private EksternPart createEksternPartWithPerson() {
		EksternPart eksternPart = new EksternPart();
		eksternPart.setNavn(NAVN);
		eksternPart.setEksternAktoer(createPerson());
		return eksternPart;
	}

	private Organisasjon createOrganisasjon() {
		Organisasjon organisasjon = new Organisasjon();
		organisasjon.setOrgnummer(ORG_NUMMER);
		return organisasjon;
	}

	private Person createPerson() {
		Person person = new Person();
		NorskIdent norskIdent = new NorskIdent();
		norskIdent.setIdent(FNR);
		person.setIdent(norskIdent);
		return person;
	}

}
