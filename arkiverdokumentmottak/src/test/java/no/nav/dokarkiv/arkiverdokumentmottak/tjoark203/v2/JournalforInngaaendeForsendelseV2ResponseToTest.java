package no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v2;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import no.nav.dokarkiv.arkiverdokumentmottak.DokumentInfoIdVedleggTo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class JournalforInngaaendeForsendelseV2ResponseToTest {

	private static final Long JOURNALPOST_ID = 2000L;
	private static final Long DOKUMENTINFO_ID_HOVEDDOKUMENT = 2002L;
	private static final Long DOKUMENTINFO_ID = 2001L;
	private static final String DOKUMENTTYPE_ID = "DokumentTypeId";
	private static final String JOURNALTILSTAND_ENDELIG = "ENDELIG";

	private JournalforInngaaendeForsendelseV2ResponseTo responseTo;

	@Before
	public void setUp() throws Exception {
		responseTo = new JournalforInngaaendeForsendelseV2ResponseTo(
				JOURNALPOST_ID,
				DOKUMENTINFO_ID_HOVEDDOKUMENT,
				createVedlegg(),
				JOURNALTILSTAND_ENDELIG
		);
	}

	@Test
	public void assertJournalpostId() throws Exception {
		Long journalpostId = responseTo.getJournalpostId();
		assertThat(journalpostId, is(JOURNALPOST_ID));
	}

	@Test
	public void assertHoveddokument() throws Exception {
		Long dokumentInfoIdHoveddokument = responseTo.getDokumentInfoIdHoveddokument();
		assertThat(dokumentInfoIdHoveddokument, is(DOKUMENTINFO_ID_HOVEDDOKUMENT));
	}

	@Test
	public void assertDokumentInfoIdVedlegg() throws Exception {
		List<DokumentInfoIdVedleggTo> vedlegg = responseTo.getDokumentInfoIdVedleggTo();
		Assert.assertThat(vedlegg.get(0).getDokumentInfoId(), is(DOKUMENTINFO_ID));
		Assert.assertThat(vedlegg.get(0).getDokumentTypeId(), is(DOKUMENTTYPE_ID));
	}

	@Test
	public void assertJournalTilstand() throws Exception {
		String journalTilstand = responseTo.getJournalTilstand();
		assertThat(journalTilstand, is(JOURNALTILSTAND_ENDELIG));
	}

	private List<DokumentInfoIdVedleggTo> createVedlegg() {
		DokumentInfoIdVedleggTo vedlegg = DokumentInfoIdVedleggTo.builder()
				.dokumentInfoId(DOKUMENTINFO_ID)
				.dokumentTypeId(DOKUMENTTYPE_ID)
				.build();
		return Collections.singletonList(vedlegg);
	}
}