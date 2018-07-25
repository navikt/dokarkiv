package no.nav.dokarkiv.core.security.abac;

import static no.nav.dokarkiv.core.domain.builder.BrukerBuilder.getBrukerBuilder;
import static no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder.getDokumentInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder.getFilDetaljerBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static no.nav.dokarkiv.core.domain.builder.SaksrelasjonBuilder.getSaksrelasjonBuilder;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.core.repository.RepositoryConfig;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Date;

/**
 * @author Martin Burheim Tingstad, Visma Consulting AS
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RepositoryConfig.class, JdbcAbacSecurityRepository.class})
@DataJpaTest
@Transactional
@ActiveProfiles("itest")
public class JdbcAbacSecurityRepositoryTest {
	@Rule
	public ExpectedException expected = ExpectedException.none();

	@Inject
	private JdbcAbacSecurityRepository jdbcAbacSecurityRepository;
	@Inject
	private JoarkRepository joarkRepository;

	@Before
	public void setUp() throws Exception {
		RequestContextUtil.createAndSetUsername("itest", "itest");
	}

	@Test
	public void shouldReturnSmallAbacResourcesForJournalpost() {

		Journalpost j = createJournalpost(JournalStatusCode.J);

		joarkRepository.save(j);

		AbacResources abacResources = jdbcAbacSecurityRepository.findAbacResources(j.getJournalpostId());
		assertThat(abacResources.getFagsystem(), is(FagsystemCode.PEN));
		assertThat(abacResources.getSakId(), is("236542"));
		assertThat(abacResources.getBrukerIds(), hasSize(1));
		assertThat(abacResources.getFagomrade(), is(FagomradeCode.PEN));
	}

	@Test
	public void shouldReturnCompleteAbacResourcesForJournalpost() {

		Journalpost j = createJournalpost(JournalStatusCode.J);

		Bruker b1 = createBruker("***gammelt_fnr***");
		Bruker b2 = createBruker("***gammelt_fnr***");
		j.addBruker(b1);
		j.addBruker(b2);
		joarkRepository.save(j);

		AbacResources abacResources = jdbcAbacSecurityRepository.findAbacResources(j.getJournalpostId());
		assertThat(abacResources.getFagsystem(), is(FagsystemCode.PEN));
		assertThat(abacResources.getSakId(), is("236542"));
		assertThat(abacResources.getBrukerIds(), hasSize(3));
		assertThat(abacResources.getFagomrade(), is(FagomradeCode.PEN));
	}

	@Test
	public void shouldReturnPartialAbacResourcesWithoutSakForJournalpost() {
		Journalpost j = createJournalpost(JournalStatusCode.J);

		Bruker b1 = createBruker("***gammelt_fnr***");
		Bruker b2 = createBruker("***gammelt_fnr***");
		j.addBruker(b1);
		j.addBruker(b2);
		j.setSaksrelasjon(null);
		joarkRepository.save(j);

		AbacResources abacResources = jdbcAbacSecurityRepository.findAbacResources(j.getJournalpostId());
		assertThat(abacResources.getFagsystem(), is(nullValue()));
		assertThat(abacResources.getSakId(), is(""));
		assertThat(abacResources.getBrukerIds().size(), is(3));
		assertThat(abacResources.getBrukerIds(), containsInAnyOrder("5", b1.getBrukerId(), b2.getBrukerId()));
		assertThat(abacResources.getFagomrade(), is(FagomradeCode.PEN));
	}

	@Test
	public void shouldReturnPartialAbacResourcesWithoutBrukereForJournalpost() {
		Journalpost j = createJournalpost(JournalStatusCode.J);
		j.clearBrukere();

		joarkRepository.save(j);

		AbacResources abacResources = jdbcAbacSecurityRepository.findAbacResources(j.getJournalpostId());
		assertThat(abacResources.getFagsystem(), is(FagsystemCode.PEN));
		assertThat(abacResources.getSakId(), is("236542"));
		assertThat(abacResources.getBrukerIds(), is(empty()));
		assertThat(abacResources.getFagomrade(), is(FagomradeCode.PEN));
	}

	@Test
	public void shouldReturnEmptyAbacResourcesForJournalpostWithFagomradeSet() {
		Journalpost j = createJournalpost(JournalStatusCode.J);
		j.clearBrukere();
		j.setSaksrelasjon(null);

		joarkRepository.save(j);

		AbacResources abacResources = jdbcAbacSecurityRepository.findAbacResources(j.getJournalpostId());
		assertThat(abacResources.getFagsystem(), is(nullValue()));
		assertThat(abacResources.getSakId(), is(""));
		assertThat(abacResources.getBrukerIds(), is(empty()));
		assertThat(abacResources.getFagomrade(), is(FagomradeCode.PEN));
	}

	@Test
	public void shouldReturnEmptyAbacResourcesForJournalpost() {
		Journalpost j = createJournalpost(JournalStatusCode.J);
		j.clearBrukere();
		j.setSaksrelasjon(null);
		j.setFagomrade(null);

		joarkRepository.save(j);

		AbacResources abacResources = jdbcAbacSecurityRepository.findAbacResources(j.getJournalpostId());
		assertThat(abacResources.getFagsystem(), is(nullValue()));
		assertThat(abacResources.getSakId(), is(""));
		assertThat(abacResources.getBrukerIds(), is(empty()));
		assertThat(abacResources.getFagomrade(), is(nullValue()));
	}

	@Test
	public void shouldReturnEmptyAbacResourcesWhenJournalpostDoesNotExist() {
		AbacResources abacResources = jdbcAbacSecurityRepository.findAbacResources(123123321L); //123123321L
		assertThat(abacResources.getFagsystem(), is(nullValue()));
		assertThat(abacResources.getSakId(), is(""));
		assertThat(abacResources.getBrukerIds(), is(empty()));
		assertThat(abacResources.getFagomrade(), is(nullValue()));
	}

	protected Bruker createBruker(String brukerId) {
		return getBrukerBuilder()
				.opprettetKildeNavn("Test")
				.brukerType(BrukerTypeCode.PERSON)
				.brukerId(brukerId)
				.build();
	}

	protected Journalpost createJournalpost(JournalStatusCode status) {
		return getJournalpostBuilder()
				.journalStatus(status)
				.fagomrade(FagomradeCode.PEN)
				.journalpostType(JournalpostTypeCode.I)
				.brukere(createBruker("5"))
				.saksrelasjon(createSaksrelasjon("236542"))
				.elektroniskDistribusjon(true)
				.journalDato(new Date())
				.opprettetKildeNavn("test")
				.dokumentInfoRelasjoner(createDokumentInfoRelasjon(createAndPersistDokumentInfo()))
				.build();
	}

	private Saksrelasjon createSaksrelasjon(String saksNummer) {
		return getSaksrelasjonBuilder()
				.sakId(saksNummer)
				.fagsystem(FagsystemCode.PEN)
				.feilregistrert(false)
				.opprettetKildeNavn("NAV")
				.build();
	}

	protected JournalpostDokumentInfoRelasjon createDokumentInfoRelasjon(DokumentInfo dokInfo) {
		return getJournalpostDokumentInfoRelasjonBuilder()
				.dokumentInfo(dokInfo)
				.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
				.tilknyttetAvNavn("JOARK")
				.opprettetKildeNavn("NAV")
				.build();
	}

	protected DokumentInfo createAndPersistDokumentInfo() {
		FilDetaljer fildetaljer = createFildetaljer(FilDetaljer.generateUuid());
		return getDokumentInfoBuilder()
				.dokumentstatus(DokumentStatusCode.FERDIGSTILT)
				.endretAvNavn("PEN")
				.opprettetKildeNavn("NAV")
				.filDetaljerList(fildetaljer)
				.build();
	}

	protected FilDetaljer createFildetaljer(String filUuid) {
		return getFilDetaljerBuilder()
				.filUuid(filUuid)
				.filtype(FilTypeCode.PDF)
				.variantFormat(VariantFormatCode.BREVBESTILLING)
				.opprettetKildeNavn("NAV")
				.build();
	}
}
