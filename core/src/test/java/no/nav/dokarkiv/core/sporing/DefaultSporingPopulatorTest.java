package no.nav.dokarkiv.core.sporing;

import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.stelvio.RequestContextSetter;
import no.nav.dokarkiv.core.stelvio.SimpleRequestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static no.nav.dokarkiv.core.domain.builder.BrukerBuilder.getBrukerBuilder;
import static no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder.getDokumentInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder.getFilDetaljerBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static no.nav.dokarkiv.core.domain.builder.KryssreferanseBuilder.getKryssreferanseBuilder;
import static no.nav.dokarkiv.core.domain.builder.SaksrelasjonBuilder.getSaksrelasjonBuilder;
import static no.nav.dokarkiv.core.domain.builder.SkannetInnholdBuilder.getSkannetInnholdBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class DefaultSporingPopulatorTest {

	@Mock
	private KildeNavnPopulator kildeNavnPopulatorMock;

	@InjectMocks
	private DefaultSporingPopulator sporingPopulator;

	private final String kildeNavn = "Unittest";
	private final String endretAvNavn = "Siri Saksbehandler";
	private final String opprettetAvNavn = "Sigurd Saksbehandler";

	@BeforeEach
	public void setUp() {
		RequestContextSetter.setRequestContext(new SimpleRequestContext.Builder().componentId(kildeNavn).build());
	}

	@Test
	public void shouldPopulateEndretAvNavn() {
		long id = 100;
		Journalpost journalpost = createCompleteJournalpostStructure(id);

		sporingPopulator.populateSporingInfo(journalpost, endretAvNavn);

		assertEndretAvNavnSet(journalpost);
	}

	@Test
	public void shouldPopulateOpprettetAvNavn() {
		Journalpost journalpost = createCompleteJournalpostStructure();

		sporingPopulator.populateSporingInfo(journalpost, opprettetAvNavn);

		assertThat(journalpost.getOpprettetAvNavn(), is(opprettetAvNavn));
	}

	@Test
	public void shouldCallKildeNavnPopulator() {
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