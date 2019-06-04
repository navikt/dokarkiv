package no.nav.dokarkiv.behandlejournal;

import static no.nav.dokarkiv.core.domain.builder.BrukerBuilder.getBrukerBuilder;
import static no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder.getDokumentInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder.getFilDetaljerBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static no.nav.dokarkiv.core.domain.builder.KryssreferanseBuilder.getKryssreferanseBuilder;
import static no.nav.dokarkiv.core.domain.builder.SaksrelasjonBuilder.getSaksrelasjonBuilder;
import static no.nav.dokarkiv.core.domain.builder.SkannetInnholdBuilder.getSkannetInnholdBuilder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;

import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.sporing.KildeNavnPopulator;
import no.nav.dokarkiv.core.stelvio.RequestContextSetter;
import no.nav.dokarkiv.core.stelvio.SimpleRequestContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Unit tests for DefaultSporingMapper.
 *
 * @author Thomas Eugen Bjørge, Visma Sirius
 */
public class BehandleJournalSporingMapperTest {

	@Mock
	private KildeNavnPopulator kildeNavnPopulatorMock;
	
	private BehandleJournalSporingMapper mapper;
	
	private final String endretAvNavn = "Siri Saksbehandler";
	private final String kilde = "Kilden";
	private final long id = 100;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		mapper = new BehandleJournalSporingMapper(kildeNavnPopulatorMock);
		RequestContextSetter.setRequestContext(new SimpleRequestContext.Builder().componentId(kilde).build());
	}
	
	@Test
	public void shouldMapEndretAvNavn() throws Exception {
		Journalpost journalpost = createCompleteJournalpostStructure(id);
		
		mapper.mapSporingsinfo(journalpost, endretAvNavn);
		
		assertEndretAvNavnSet(journalpost);
	}

	@Test
	public void shouldMapOpprettetAvNavn() throws Exception {
		Journalpost journalpost = createCompleteJournalpostStructure(null);
		
		String opprettetAvNavn = "Test Testersen";
		mapper.mapSporingsinfo(journalpost, opprettetAvNavn );
		
		assertThat(journalpost.getOpprettetAvNavn(), is(opprettetAvNavn));
	}
	
	@Test
	public void shouldCallKildeNavnPopulator() throws Exception {
		Journalpost journalpost = createCompleteJournalpostStructure();
		
		mapper.mapSporingsinfo(journalpost, endretAvNavn);
		
		verify(kildeNavnPopulatorMock).populateKildeNavnForEntireJournalStructure(journalpost, kilde);
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
	
	private void assertEndretAvNavnSet(Journalpost journalpost) {
		assertThat(journalpost.getEndretAvNavn(), is(endretAvNavn));
		assertThat(journalpost.getSaksrelasjon().getEndretAvNavn(), is(endretAvNavn));
		for (DokumentInfo dokumentInfo : journalpost.findAllDokumentInfos()) {
			assertThat(dokumentInfo.getEndretAvNavn(), is(endretAvNavn));
		}
	}
	
}
