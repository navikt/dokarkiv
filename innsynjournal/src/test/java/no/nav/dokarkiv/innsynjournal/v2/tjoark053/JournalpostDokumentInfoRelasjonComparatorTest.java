package no.nav.dokarkiv.innsynjournal.v2.tjoark053;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.Lists;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.DokumentinfoRelasjon;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.TilknyttetJournalpostSom;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

/**
 * Unit tests for {@link JournalpostDokumentInfoRelasjonV2Comparator}
 *
 * @author Ketill Fenne, Visma Consulting.
 */
public class JournalpostDokumentInfoRelasjonComparatorTest {

	@Test
	public void shouldPlaceHovedDokFirst() throws Exception {
		List<DokumentinfoRelasjon> relasjonList = Lists.newArrayList(
				createRelasjoninfo(TilknyttetJournalpostSomCode.VEDLEGG, "3"),
				createRelasjoninfo(TilknyttetJournalpostSomCode.HOVEDDOKUMENT, "4")
		);

		Collections.sort(relasjonList, new JournalpostDokumentInfoRelasjonV2Comparator());

		assertThat(relasjonList.get(0).getDokumentinfoRelasjonId(), equalTo("4"));
		assertThat(relasjonList.get(1).getDokumentinfoRelasjonId(), equalTo("3"));
	}

	@Test
	public void shouldSortVedleggs() throws Exception {
		List<DokumentinfoRelasjon> relasjonList = Lists.newArrayList(
				createRelasjoninfo(TilknyttetJournalpostSomCode.VEDLEGG, "3"),
				createRelasjoninfo(TilknyttetJournalpostSomCode.VEDLEGG, "1"),
				createRelasjoninfo(TilknyttetJournalpostSomCode.VEDLEGG, "4"),
				createRelasjoninfo(TilknyttetJournalpostSomCode.VEDLEGG, "2")
		);

		Collections.sort(relasjonList, new JournalpostDokumentInfoRelasjonV2Comparator());

		assertThat(relasjonList.get(0).getDokumentinfoRelasjonId(), equalTo("1"));
		assertThat(relasjonList.get(1).getDokumentinfoRelasjonId(), equalTo("2"));
		assertThat(relasjonList.get(2).getDokumentinfoRelasjonId(), equalTo("3"));
		assertThat(relasjonList.get(3).getDokumentinfoRelasjonId(), equalTo("4"));
	}

	@Test
	public void shouldSortVedleggsAndHovedDokument() throws Exception {
		List<DokumentinfoRelasjon> relasjonList = Lists.newArrayList(
				createRelasjoninfo(TilknyttetJournalpostSomCode.VEDLEGG, "3"),
				createRelasjoninfo(TilknyttetJournalpostSomCode.VEDLEGG, "1"),
				createRelasjoninfo(TilknyttetJournalpostSomCode.HOVEDDOKUMENT, "4"),
				createRelasjoninfo(TilknyttetJournalpostSomCode.VEDLEGG, "5"),
				createRelasjoninfo(TilknyttetJournalpostSomCode.VEDLEGG, "2")
		);

		Collections.sort(relasjonList, new JournalpostDokumentInfoRelasjonV2Comparator());

		assertThat(relasjonList.get(0).getDokumentinfoRelasjonId(), equalTo("4"));
		assertThat(relasjonList.get(1).getDokumentinfoRelasjonId(), equalTo("1"));
		assertThat(relasjonList.get(2).getDokumentinfoRelasjonId(), equalTo("2"));
		assertThat(relasjonList.get(3).getDokumentinfoRelasjonId(), equalTo("3"));
		assertThat(relasjonList.get(4).getDokumentinfoRelasjonId(), equalTo("5"));
	}

	private DokumentinfoRelasjon createRelasjoninfo(TilknyttetJournalpostSomCode tilknyttetJournalpostSom, String id) {
		DokumentinfoRelasjon relasjon = new DokumentinfoRelasjon();
		relasjon.setDokumentinfoRelasjonId(id);

		TilknyttetJournalpostSom code = new TilknyttetJournalpostSom();
		code.setValue(tilknyttetJournalpostSom.name());
		relasjon.setDokumentTilknyttetJournalpost(code);
		return relasjon;
	}

}