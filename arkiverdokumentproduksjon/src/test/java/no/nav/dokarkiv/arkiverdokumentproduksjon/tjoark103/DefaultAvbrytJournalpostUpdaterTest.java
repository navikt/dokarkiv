package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark103;

import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.sporing.DefaultKildeNavnPopulator;
import no.nav.dokarkiv.core.sporing.DefaultSporingPopulator;
import no.nav.dokarkiv.core.stelvio.RequestContextSetter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder.getDokumentInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static no.nav.dokarkiv.core.domain.builder.SaksrelasjonBuilder.getSaksrelasjonBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class DefaultAvbrytJournalpostUpdaterTest {
	private static final Long JOURNALPOST_ID = 42L;
	private static final String ENDRET_AV_NAVN = "endretFlagg av navn";

	private DefaultAvbrytJournalpostUpdater avbrytJournalpostUpdater;

	@BeforeEach
	public void setUp() {
		RequestContextSetter.setRequestContextForUnitTest();
		DefaultSporingPopulator sporingPopulator = new DefaultSporingPopulator(new DefaultKildeNavnPopulator());
		avbrytJournalpostUpdater = new DefaultAvbrytJournalpostUpdater(sporingPopulator);
	}

	@Test
	public void shouldUpdateJournalpostUnderRedigering() {
		Journalpost journalPost = avbrytJournalpostUpdater.updateJournalpost(
				createJournalpost(DokumentStatusCode.UNDER_REDIGERING), ENDRET_AV_NAVN);
		assertThat(journalPost.getJournalstatus(), is(JournalStatusCode.A));
		assertThat(journalPost.getEndretAvNavn(), is(ENDRET_AV_NAVN));
		assertThat(journalPost.getSaksrelasjon().getEndretAvNavn(), is(ENDRET_AV_NAVN));
		assertThat(journalPost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentstatus(),
				is(DokumentStatusCode.AVBRUTT));
		assertThat(journalPost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getEndretAvNavn(), is(ENDRET_AV_NAVN));
	}

	@Test
	public void shouldUpdateJournalpostNotUnderRedigering() {
		Journalpost journalPost = avbrytJournalpostUpdater.updateJournalpost(createJournalpost(DokumentStatusCode.FERDIGSTILT),
				ENDRET_AV_NAVN);
		assertThat(journalPost.getJournalstatus(), is(JournalStatusCode.A));
		assertThat(journalPost.getEndretAvNavn(), is(ENDRET_AV_NAVN));
		assertThat(journalPost.getSaksrelasjon().getEndretAvNavn(), is(ENDRET_AV_NAVN));
		assertThat(journalPost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentstatus(),
				is(DokumentStatusCode.FERDIGSTILT));
		assertThat(journalPost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getEndretAvNavn(), is(ENDRET_AV_NAVN));
	}

	private Journalpost createJournalpost(DokumentStatusCode dokumentStatusCode) {
		return getJournalpostBuilder()
				.journalpostId(JOURNALPOST_ID)
				.saksrelasjon(
						getSaksrelasjonBuilder()
								.saknrfk("1").saksrelasjonId(2L).build())
				.dokumentInfoRelasjoner(
						getJournalpostDokumentInfoRelasjonBuilder()
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
								.dokumentInfo(
										getDokumentInfoBuilder()
												.dokumentInfoId(42L)
												.dokumentstatus(dokumentStatusCode)
												.build())
								.build())
				.build();
	}

}