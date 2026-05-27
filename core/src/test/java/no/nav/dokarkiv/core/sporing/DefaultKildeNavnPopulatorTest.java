package no.nav.dokarkiv.core.sporing;

import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

/**
 * Unit tests for DefaultKildeNavnPopulator.
 *
 * @author Thomas Eugen Bjørge, Visma Sirius
 */
public class DefaultKildeNavnPopulatorTest {

	private DefaultKildeNavnPopulator kildeNavnPopulator;

	private final String kildeNavn = "Unittest";
	private final long id = 100;

	@BeforeEach
	public void setUp() {
		kildeNavnPopulator = new DefaultKildeNavnPopulator();
	}

	@Test
	public void shouldPopulateOpprettetKildeNavn() throws Exception {
		Journalpost journalpost = createCompleteJournalpostStructure();

		kildeNavnPopulator.populateKildeNavnForEntireJournalStructure(journalpost, kildeNavn);

		assertOpprettetKildeNavnSet(journalpost);
	}

	@Test
	public void shouldMapEndretKildeNavn() throws Exception {
		Journalpost journalpost = createCompleteJournalpostStructure(id);

		kildeNavnPopulator.populateKildeNavnForEntireJournalStructure(journalpost, kildeNavn);

		assertEndretKildeNavnSet(journalpost);
	}

	private void assertOpprettetKildeNavnSet(Journalpost journalpost) {
		assertThat(journalpost.getOpprettetKildeNavn(), is(kildeNavn));
		assertThat(journalpost.getSaksrelasjon().getOpprettetKildeNavn(), is(kildeNavn));
		assertThat(journalpost.getBrukere().iterator().next().getOpprettetKildeNavn(), is(kildeNavn));
		assertThat(journalpost.getKryssreferanser().iterator().next().getOpprettetKildeNavn(), is(kildeNavn));
		assertThat(journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getOpprettetKildeNavn(), is(kildeNavn));
		DokumentInfo dokumentInfo = journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getDokumentInfo();
		assertThat(dokumentInfo.getOpprettetKildeNavn(), is(kildeNavn));
		assertThat(dokumentInfo.getSkannetInnholdListe().iterator().next().getOpprettetKildeNavn(), is(kildeNavn));
		assertThat(dokumentInfo.getFildetaljerListe().iterator().next().getOpprettetKildeNavn(), is(kildeNavn));
	}

	private void assertEndretKildeNavnSet(Journalpost journalpost) {
		assertThat(journalpost.getEndretKildeNavn(), is(kildeNavn));
		assertThat(journalpost.getSaksrelasjon().getEndretKildeNavn(), is(kildeNavn));
		assertThat(journalpost.getBrukere().iterator().next().getEndretKildeNavn(), is(kildeNavn));
		assertThat(journalpost.getKryssreferanser().iterator().next().getEndretKildeNavn(), is(kildeNavn));
		assertThat(journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getEndretKildeNavn(), is(kildeNavn));
		DokumentInfo dokumentInfo = journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getDokumentInfo();
		assertThat(dokumentInfo.getEndretKildeNavn(), is(kildeNavn));
		assertThat(dokumentInfo.getSkannetInnholdListe().iterator().next().getEndretKildeNavn(), is(kildeNavn));
		assertThat(dokumentInfo.getFildetaljerListe().iterator().next().getEndretKildeNavn(), is(kildeNavn));
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