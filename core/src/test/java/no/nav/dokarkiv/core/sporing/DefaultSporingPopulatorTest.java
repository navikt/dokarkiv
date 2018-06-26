package no.nav.dokarkiv.core.sporing;

import static no.nav.dokarkiv.core.domain.builder.BrukerBuilder.getBrukerBuilder;
import static no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder.getDokumentInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder.getFilDetaljerBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static no.nav.dokarkiv.core.domain.builder.KryssreferanseBuilder.getKryssreferanseBuilder;
import static no.nav.dokarkiv.core.domain.builder.ReturInfoBuilder.getReturInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.SaksrelasjonBuilder.getSaksrelasjonBuilder;
import static no.nav.dokarkiv.core.domain.builder.SkannetInnholdBuilder.getSkannetInnholdBuilder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;

import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.stelvio.RequestContextSetter;
import no.nav.dokarkiv.core.stelvio.SimpleRequestContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Unit tests for DefaultSporingPopulator.
 *
 * @author Thomas Eugen Bjørge, Visma Sirius
 */
public class DefaultSporingPopulatorTest {

	@Mock
	private KildeNavnPopulator kildeNavnPopulatorMock;

	private DefaultSporingPopulator sporingPopulator;

	private final long id = 100;
	private String kildeNavn = "Unittest";
	private final String endretAvNavn = "Siri Saksbehandler";
	private final String opprettetAvNavn = "Sigurd Saksbehandler";

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		sporingPopulator = new DefaultSporingPopulator();
		sporingPopulator.setKildeNavnPopulator(kildeNavnPopulatorMock);

		RequestContextSetter.setRequestContext(new SimpleRequestContext.Builder().componentId(kildeNavn).build());
	}

	@Test
	public void shouldPopulateEndretAvNavn() throws Exception {
		Journalpost journalpost = createCompleteJournalpostStructure(id);

		sporingPopulator.populateSporingInfo(journalpost, endretAvNavn);

		assertEndretAvNavnSet(journalpost);
	}

	@Test
	public void shouldPopulateOpprettetAvNavn() throws Exception {
		Journalpost journalpost = createCompleteJournalpostStructure();

		sporingPopulator.populateSporingInfo(journalpost, opprettetAvNavn);

		assertThat(journalpost.getOpprettetAvNavn(), is(opprettetAvNavn));
	}

	@Test
	public void shouldCallKildeNavnPopulator() throws Exception {
		Journalpost journalpost = createCompleteJournalpostStructure();

		sporingPopulator.populateSporingInfo(journalpost, opprettetAvNavn);

		verify(kildeNavnPopulatorMock).populateKildeNavnForEntireJournalStructure(journalpost, kildeNavn);
	}

	private void assertEndretAvNavnSet(Journalpost journalpost) {
		assertThat(journalpost.getEndretAvNavn(), is(endretAvNavn));
		assertThat(journalpost.getSaksrelasjon().getEndretAvNavn(), is(endretAvNavn));
		for (DokumentInfo dokumentInfo : journalpost.findAllDokumentInfos()) {
			assertThat(dokumentInfo.getEndretAvNavn(), is(endretAvNavn));
		}
	}

	private Journalpost createCompleteJournalpostStructure() {
		return createCompleteJournalpostStructure(null);
	}

	private Journalpost createCompleteJournalpostStructure(Long id) {
		return getJournalpostBuilder()
				.journalpostId(id)
				.saksrelasjon(getSaksrelasjonBuilder()
						.saksrelasjonId(id)
						.build())
				.brukere(getBrukerBuilder()
						.brukerInfoId(id)
						.build())
				.returInfos(getReturInfoBuilder()
						.returInfoId(id)
						.build())
				.kryssReferanser(getKryssreferanseBuilder()
						.kryssreferanseId(id)
						.build())
				.dokumentInfoRelasjoner(getJournalpostDokumentInfoRelasjonBuilder()
						.journalpostDokumentInfoRelasjonId(id)
						.dokumentInfo(getDokumentInfoBuilder()
								.dokumentInfoId(id)
								.skannetInnhold(getSkannetInnholdBuilder()
										.skannetInnholdId(id)
										.build())
								.filDetaljerList(getFilDetaljerBuilder()
										.fildetaljerId(id)
										.build())
								.build())
						.build())
				.build();
	}
}