package no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v2;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import no.nav.dokarkiv.arkiverdokumentmottak.DokumentInfoIdVedleggTo;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v2.informasjon.arkiverdokumentmottak.JournalTilstandEnum;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v2.meldinger.JournalforInngaaendeForsendelseResponse;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collections;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class JournalforInngaaendeForsendelseV2ResponseMapperTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private static final Long JOURNALPOST_ID = 1000L;
	private static final Long DOKUMENT_INFO_ID_HOVEDDOKUMENT = 1001L;
	private static final Long DOKUMENTINFO_ID = 1003L;
	private static final String DOKUMENTTYPE_ID = "TID";
	private static final String JOURNAL_TILSTAND_ENDELIG_STRING = "ENDELIG";
	private static final JournalTilstandEnum JOURNAL_TILSTAND_ENDELIG_ENUM = JournalTilstandEnum.ENDELIG;

	private JournalforInngaaendeForsendelseV2ResponseMapper mapper = new JournalforInngaaendeForsendelseV2ResponseMapper();

	@Test
	public void testValidMap() throws Exception {
		DokumentInfoIdVedleggTo vedlegg = DokumentInfoIdVedleggTo.builder()
				.dokumentInfoId(DOKUMENTINFO_ID)
				.dokumentTypeId(DOKUMENTTYPE_ID)
				.build();
		JournalforInngaaendeForsendelseV2ResponseTo to = new JournalforInngaaendeForsendelseV2ResponseTo(
				JOURNALPOST_ID,
				DOKUMENT_INFO_ID_HOVEDDOKUMENT,
				Collections.singletonList(vedlegg),
				JOURNAL_TILSTAND_ENDELIG_STRING);

		JournalforInngaaendeForsendelseResponse response = mapper.map(to);

		assertThat(response.getJournalpostId(), is(JOURNALPOST_ID));
		assertThat(response.getDokumentInfoIdHoveddokument(), is(DOKUMENT_INFO_ID_HOVEDDOKUMENT));
		assertThat(response.getJournalTilstand(), is(JOURNAL_TILSTAND_ENDELIG_ENUM));
		assertThat(response.getDokumentInfoIdVedleggListe().get(0).getDokumentTypeId(), is(DOKUMENTTYPE_ID));
		assertThat(response.getDokumentInfoIdVedleggListe().get(0).getDokumentInfoId(), is(DOKUMENTINFO_ID));
		assertThat(response.getDokumentInfoIdVedleggListe(), hasSize(1));
	}

	@Test
	public void testInvalidMap() throws Exception {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("Feil ved mapping av JournalforInngaaendeForsendelseV2ResponseTo til JournalforInngaaendeForsendelseResponse: TO objektet er null");
		mapper.map(null);
	}
}