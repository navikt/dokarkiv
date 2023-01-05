package no.nav.dokarkiv.core.domain.entities;

import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder.getDokumentInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder.getFilDetaljerBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

/**
 * Unit tests for Journalpost, contains tests of collections searches, i.e. all find* methods.
 */
public class JournalpostCollectionsSearchTest {

	@Test
	public void shouldFindCorrectFilDetaljerForGivenFilUuid() {
		String filUuid1 = FilDetaljer.generateUuid();
		String filUuid2 = FilDetaljer.generateUuid();
		String filnavn2 = "fil2";

		Journalpost journalpost = createJournalpostWithTwoFilDetaljer(filUuid1, filUuid2, filnavn2);

		FilDetaljer filDetaljer = journalpost.findFilDetaljerByFilUuid(filUuid2);
		assertThat(filDetaljer.getFilnavn(), is(filnavn2));
	}

	@Test
	public void shouldReturnNullForNonMatchingFilDetaljerGivenFilUuid() {
		String filUuid1 = FilDetaljer.generateUuid();
		String filUuid2 = FilDetaljer.generateUuid();

		Journalpost journalpost = createJournalpostWithTwoFilDetaljer(filUuid1, filUuid2, "fil2");

		assertThat(journalpost.findFilDetaljerByFilUuid(FilDetaljer.generateUuid()), is(nullValue()));
	}

	@Test
	public void shouldFindCorrectFilDetaljerForGivenFilDetaljerId() {
		Long filDetaljerId = 100L;
		String filUuid = FilDetaljer.generateUuid();

		Journalpost journalpost = createJournalpostWithTwoDokumentInfoRelasjoner(filDetaljerId, filUuid);

		FilDetaljer filDetaljer = journalpost.findFilDetaljerByFilDetaljerId(filDetaljerId);
		assertThat(filDetaljer.getFilUuid(), is(filUuid));
	}

	@Test
	public void shouldReturnNullForNonMatchingFilDetaljerGivenFilDetaljerId() {
		Long filDetaljerId = 100L;
		String filUuid = FilDetaljer.generateUuid();

		Journalpost journalpost = createJournalpostWithTwoDokumentInfoRelasjoner(filDetaljerId, filUuid);

		assertThat(journalpost.findFilDetaljerByFilDetaljerId(200L), is(nullValue()));
	}

	@Test
	@SuppressWarnings({"rawtypes", "unchecked"})
	public void shouldFindAllFilDetaljer() {
		Journalpost journalpost =
				getJournalpostBuilder()
						.dokumentInfoRelasjoner(getJournalpostDokumentInfoRelasjonBuilder()
										.dokumentInfo(getDokumentInfoBuilder()
												.filDetaljerList(getFilDetaljerBuilder()
														.fildetaljerId(10L)
														.filUuid("test")
														.build())
												.build())
										.build(),
								getJournalpostDokumentInfoRelasjonBuilder()
										.dokumentInfo(getDokumentInfoBuilder()
												.filDetaljerList(getFilDetaljerBuilder()
																.fildetaljerId(34L)
																.filUuid("test2")
																.build(),
														getFilDetaljerBuilder()
																.fildetaljerId(55L)
																.filUuid("test3")
																.build())
												.build())
										.build())
						.build();
		List allFilDetaljer = journalpost.findAllFilDetaljer();
		assertThat((List<Object>) allFilDetaljer, hasItem(hasProperty("filUuid", is("test"))));
		assertThat((List<Object>) allFilDetaljer, hasItem(hasProperty("filUuid", is("test2"))));
		assertThat((List<Object>) allFilDetaljer, hasItem(hasProperty("filUuid", is("test3"))));
	}

	@Test
	@SuppressWarnings({"rawtypes", "unchecked"})
	public void shouldFindAllDokumentInfos() {
		long dokumentInfoId1 = 345;
		long dokumentInfoId2 = 657;
		Journalpost journalpost =
				getJournalpostBuilder()
						.dokumentInfoRelasjoner(getJournalpostDokumentInfoRelasjonBuilder()
										.dokumentInfo(getDokumentInfoBuilder()
												.dokumentInfoId(dokumentInfoId1)
												.build())
										.build(),
								getJournalpostDokumentInfoRelasjonBuilder()
										.dokumentInfo(getDokumentInfoBuilder()
												.dokumentInfoId(dokumentInfoId2)
												.build())
										.build())
						.build();
		List allDokumentInfos = journalpost.findAllDokumentInfos();
		assertThat((List<Object>) allDokumentInfos, hasItem(hasProperty("dokumentInfoId", is(dokumentInfoId1))));
		assertThat((List<Object>) allDokumentInfos, hasItem(hasProperty("dokumentInfoId", is(dokumentInfoId2))));
	}

	@Test
	public void shouldFindDokumentInfoById() {
		long dokumentInfoId = 100L;
		DokumentKategoriCode dokumentKategori = DokumentKategoriCode.B;
		Journalpost journalpost =
				getJournalpostBuilder()
						.dokumentInfoRelasjoner(getJournalpostDokumentInfoRelasjonBuilder()
										.dokumentInfo(getDokumentInfoBuilder()
												.dokumentInfoId(dokumentInfoId)
												.kategori(dokumentKategori)
												.build())
										.build(),
								getJournalpostDokumentInfoRelasjonBuilder()
										.dokumentInfo(getDokumentInfoBuilder()
												.dokumentInfoId(300L)
												.kategori(DokumentKategoriCode.SED)
												.build())
										.build())
						.build();

		DokumentInfo dokumentInfo = journalpost.findDokumentInfoById(dokumentInfoId);
		assertThat(dokumentInfo.getKategori(), is(dokumentKategori));
	}

	@Test
	public void shouldReturnNullForNonMatchingDokumentInfoGivenDokumentInfoId() {
		long dokumentInfoId = 100L;
		DokumentKategoriCode dokumentKategori = DokumentKategoriCode.B;
		Journalpost journalpost =
				getJournalpostBuilder()
						.dokumentInfoRelasjoner(getJournalpostDokumentInfoRelasjonBuilder()
										.dokumentInfo(getDokumentInfoBuilder()
												.dokumentInfoId(dokumentInfoId)
												.kategori(dokumentKategori)
												.build())
										.build(),
								getJournalpostDokumentInfoRelasjonBuilder()
										.dokumentInfo(getDokumentInfoBuilder()
												.dokumentInfoId(300L)
												.kategori(DokumentKategoriCode.SED)
												.build())
										.build())
						.build();

		assertThat(journalpost.findDokumentInfoById(200L), is(nullValue()));
	}

	@Test
	public void shouldFindDokumentInfoRelasjonById() {
		long relasjonId = 100L;
		Journalpost journalpost = createDokumentInfoRelasjon(relasjonId);

		JournalpostDokumentInfoRelasjon dokumentInfoRelasjon = journalpost.findDokumentInfoRelasjonById(relasjonId);
		assertThat(dokumentInfoRelasjon.getTilknyttetJournalpostSom(), is(TilknyttetJournalpostSomCode.VEDLEGG));
	}

	@Test
	public void shouldFindDokumentInfoRelasjonByTilknyttetJournalpostSom() {
		long relasjonId = 100L;
		Journalpost journalpost = createDokumentInfoRelasjon(relasjonId);

		Set<JournalpostDokumentInfoRelasjon> vedleggs = journalpost
				.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG);
		assertThat(vedleggs.size(), is(1));
		assertThat(vedleggs.iterator().next().getId(), is(relasjonId));
	}

	@Test
	public void shouldFindHoveddokumentDokumentRelasjon() {
		Journalpost journalpost = createDokumentInfoRelasjon(100L);

		JournalpostDokumentInfoRelasjon hoveddokumentRelasjon = journalpost.findHoveddokumentDokumentInfoRelasjon();

		assertThat(hoveddokumentRelasjon.getId(), is(90L));
	}

	private Journalpost createDokumentInfoRelasjon(long relasjonId) {
		Journalpost journalpost =
				getJournalpostBuilder()
						.dokumentInfoRelasjoner(
								getJournalpostDokumentInfoRelasjonBuilder()
										.journalpostDokumentInfoRelasjonId(90L)
										.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
										.build(),
								getJournalpostDokumentInfoRelasjonBuilder()
										.journalpostDokumentInfoRelasjonId(relasjonId)
										.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
										.build())
						.build();
		return journalpost;
	}

	private Journalpost createJournalpostWithTwoFilDetaljer(String filUuid1, String filUuid2, String filnavn) {
		return getJournalpostBuilder()
				.dokumentInfoRelasjoner(getJournalpostDokumentInfoRelasjonBuilder()
						.dokumentInfo(getDokumentInfoBuilder()
								.filDetaljerList(getFilDetaljerBuilder()
												.filUuid(filUuid1)
												.filnavn("fil")
												.build(),
										getFilDetaljerBuilder()
												.filUuid(filUuid2)
												.filnavn(filnavn)
												.build())
								.build())
						.build())
				.build();
	}

	private Journalpost createJournalpostWithTwoDokumentInfoRelasjoner(Long filDetaljerId, String filUuid) {
		return getJournalpostBuilder()
				.dokumentInfoRelasjoner(getJournalpostDokumentInfoRelasjonBuilder()
								.dokumentInfo(getDokumentInfoBuilder()
										.filDetaljerList(getFilDetaljerBuilder()
												.fildetaljerId(10L)
												.filUuid("test")
												.build())
										.build())
								.build(),
						getJournalpostDokumentInfoRelasjonBuilder()
								.dokumentInfo(getDokumentInfoBuilder()
										.filDetaljerList(getFilDetaljerBuilder()
												.fildetaljerId(filDetaljerId)
												.filUuid(filUuid)
												.build())
										.build())
								.build())
				.build();
	}

}
