package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.arkiverdokumentproduksjon.JournalTilstand;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.OpprettUtgaaendeJournalpostArkiverDokumentResponse;
import org.junit.Test;

import java.util.Arrays;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class OpprettUtgaaendeJournalpostArkiverDokumentResponseMapperTest {

	private static final Long DOKUMENTINFOID_HOVEDDOK = 1l;
	private static final Long DOKUMENTINFOID_VEDLEGG_1 = 2l;
	private static final Long DOKUMENTINFOID_VEDLEGG_2 = 3l;
	private static final Long JOURNALPOSTID = 4l;
	private static final JournalStatusCode JOURNAL_STATUS_CODE = JournalStatusCode.FS;


	private OpprettUtgaaendeJournalpostArkiverDokumentResponseMapper mapper = new OpprettUtgaaendeJournalpostArkiverDokumentResponseMapper();

	@Test
	public void shouldMap() {
		OpprettUtgaaendeJournalpostArkiverDokumentResponse response = mapper.map(createResponseTo());
		assertThat(response.getDokumentInfoIdHoveddokument(), is(DOKUMENTINFOID_HOVEDDOK));
		assertThat(response.getDokumentInfoIdVedleggListe(), is(Arrays.asList(DOKUMENTINFOID_VEDLEGG_1, DOKUMENTINFOID_VEDLEGG_2)));
		assertThat(response.getJournalpostId(), is(JOURNALPOSTID));
		assertThat(response.getJournalTilstand(), is(JournalTilstand.FERDIGSTILT));
	}

	@Test
	public void shouldMapJournalTilstandFerdigstiltWhenJournalStatusCodeIsFL() {
		OpprettUtgaaendeJournalpostArkiverDokumentResponseTo responseTo = createResponseTo();
		responseTo.setJournalStatus(JournalStatusCode.FL);

		OpprettUtgaaendeJournalpostArkiverDokumentResponse response = mapper.map(responseTo);

		assertThat(response.getJournalTilstand(), is(JournalTilstand.FERDIGSTILT));
	}

	@Test
	public void shouldMapJournalTilstandUnderArbeidWhenJournalStatusCodeIsD() {
		OpprettUtgaaendeJournalpostArkiverDokumentResponseTo responseTo = createResponseTo();
		responseTo.setJournalStatus(JournalStatusCode.D);

		OpprettUtgaaendeJournalpostArkiverDokumentResponse response = mapper.map(responseTo);

		assertThat(response.getJournalTilstand(), is(JournalTilstand.UNDER_ARBEID));
	}


	private OpprettUtgaaendeJournalpostArkiverDokumentResponseTo createResponseTo() {
		OpprettUtgaaendeJournalpostArkiverDokumentResponseTo to = OpprettUtgaaendeJournalpostArkiverDokumentResponseTo.builder()
				.dokumentInfoIdHoveddokument(DOKUMENTINFOID_HOVEDDOK)
				.journalpostId(JOURNALPOSTID)
				.journalStatus(JOURNAL_STATUS_CODE)
				.build();
		to.getDokumentInfoIdVedlegg().addAll(Arrays.asList(DOKUMENTINFOID_VEDLEGG_1, DOKUMENTINFOID_VEDLEGG_2));

		return to;
	}


}