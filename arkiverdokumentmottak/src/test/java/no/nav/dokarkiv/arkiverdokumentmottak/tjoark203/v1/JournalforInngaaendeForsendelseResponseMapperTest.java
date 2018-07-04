package no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v1;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import no.nav.dokarkiv.core.nsb.DokumentInfoIdVedleggTo;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.meldinger.JournalforInngaaendeForsendelseResponse;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collections;

/**
 * Test for {@link JournalforInngaaendeForsendelseResponseMapper}
 *
 * @author Leo-Andreas Ervik, Visma Consulting. 16.02.2017
 */
public class JournalforInngaaendeForsendelseResponseMapperTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private static final Long JOURNALPOST_ID = 1000L;
	private static final Long DOKUMENT_INFO_ID_HOVEDDOKUMENT = 1001L;
	private static final Long DOKUMENTINFO_ID = 1003L;
	private static final String DOKUMENTTYPE_ID = "TID";

	private JournalforInngaaendeForsendelseResponseMapper mapper = new JournalforInngaaendeForsendelseResponseMapper();
	private JournalforInngaaendeForsendelseResponseTo to;

	@Test
	public void testValidMap() throws Exception {
		DokumentInfoIdVedleggTo vedlegg = new DokumentInfoIdVedleggTo();
		vedlegg.setDokumentInfoId(DOKUMENTINFO_ID);
		vedlegg.setDokumentTypeId(DOKUMENTTYPE_ID);
		to = new JournalforInngaaendeForsendelseResponseTo(
				JOURNALPOST_ID,
				DOKUMENT_INFO_ID_HOVEDDOKUMENT,
				Collections.singletonList(vedlegg)
		);

		JournalforInngaaendeForsendelseResponse response = mapper.map(to);

		assertThat(response.getJournalpostId(), is(JOURNALPOST_ID));
		assertThat(response.getDokumentInfoIdHoveddokument(), is(DOKUMENT_INFO_ID_HOVEDDOKUMENT));
		assertThat(response.getDokumentInfoIdVedleggListe().get(0).getDokumentTypeId(), is(DOKUMENTTYPE_ID));
		assertThat(response.getDokumentInfoIdVedleggListe().get(0).getDokumentInfoId(), is(DOKUMENTINFO_ID));
		assertThat(response.getDokumentInfoIdVedleggListe(), hasSize(1));
	}

	@Test
	public void testInvalidMap() throws Exception {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("Feil ved mapping av JournalforInngaaendeForsendelseResponseTo til JournalforInngaaendeForsendelseResponse: TO objektet er null");
		mapper.map(null);
	}
}