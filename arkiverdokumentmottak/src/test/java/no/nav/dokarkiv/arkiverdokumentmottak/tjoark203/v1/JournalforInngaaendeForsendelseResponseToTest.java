package no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v1;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import no.nav.dokarkiv.arkiverdokumentmottak.DokumentInfoIdVedleggTo;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collections;
import java.util.List;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class JournalforInngaaendeForsendelseResponseToTest {

	private static final Long JOURNALPOST_ID = 1000L;
	private static final Long DOKUMENTINFO_ID = 1001L;
	private static final Long DOKUMENTINFO_ID_HOVEDDOKUMENT = 1002L;
	private static final String DOKUMENTTYPE_ID = "DTYPEID";

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private JournalforInngaaendeForsendelseResponseTo to;

	@Before
	public void setUp() throws Exception {
		DokumentInfoIdVedleggTo vedlegg = DokumentInfoIdVedleggTo.builder()
				.dokumentInfoId(DOKUMENTINFO_ID)
				.dokumentTypeId(DOKUMENTTYPE_ID)
				.build();
		to = new JournalforInngaaendeForsendelseResponseTo(
				JOURNALPOST_ID,
				DOKUMENTINFO_ID_HOVEDDOKUMENT,
				Collections.singletonList(vedlegg)
		);
	}

	@Test
	public void testGetJournalPostId() throws Exception {
		Long journalpost = to.getJournalpostId();
		assertThat(journalpost, is(JOURNALPOST_ID));
	}

	@Test
	public void testGetDokumentInfoIdHoveddokument() throws Exception {
		Long dokumentInfoIdHoveddokument = to.getDokumentInfoIdHoveddokument();
		assertThat(dokumentInfoIdHoveddokument, is(DOKUMENTINFO_ID_HOVEDDOKUMENT));
	}

	@Test
	public void testGetDokumentInfoIdVedlegg() throws Exception {
		List<DokumentInfoIdVedleggTo> vedlegg = to.getDokumentInfoIdVedleggTo();
		assertThat(vedlegg.get(0).getDokumentInfoId(), is(DOKUMENTINFO_ID));
		assertThat(vedlegg.get(0).getDokumentTypeId(), is(DOKUMENTTYPE_ID));
	}
}