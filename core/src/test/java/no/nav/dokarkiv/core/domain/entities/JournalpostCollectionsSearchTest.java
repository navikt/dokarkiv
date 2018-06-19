package no.nav.dokarkiv.core.domain.entities;

import static no.nav.dokarkiv.core.domain.builder.BrukerBuilder.getBrukerBuilder;
import static no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder.getDokumentInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder.getFilDetaljerBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static no.nav.dokarkiv.core.domain.builder.KryssreferanseBuilder.getKryssreferanseBuilder;
import static no.nav.dokarkiv.core.domain.builder.ReturInfoBuilder.getReturInfoBuilder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import no.nav.dokarkiv.core.domain.codes.ArsakReturCode;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import org.junit.Test;

import java.util.List;
import java.util.Set;

/**
 * Unit tests for Journalpost, contains tests of collections searches, i.e. all find* methods.
 * 
 * @author Thomas Eugen Bjørge, Visma Sirius
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
	public void shouldFindCorrectFilDetaljerForGivenFilDetaljerId() throws Exception {
		Long filDetaljerId = 100L;
		String filUuid = FilDetaljer.generateUuid();
		
		Journalpost journalpost = createJournalpostWithTwoDokumentInfoRelasjoner(filDetaljerId, filUuid);
		
		FilDetaljer filDetaljer = journalpost.findFilDetaljerByFilDetaljerId(filDetaljerId);
		assertThat(filDetaljer.getFilUuid(), is(filUuid));
	}

	@Test
	public void shouldReturnNullForNonMatchingFilDetaljerGivenFilDetaljerId() throws Exception {
		Long filDetaljerId = 100L;
		String filUuid = FilDetaljer.generateUuid();
		
		Journalpost journalpost = createJournalpostWithTwoDokumentInfoRelasjoner(filDetaljerId, filUuid);
		
		assertThat(journalpost.findFilDetaljerByFilDetaljerId(200L), is(nullValue()));
	}
	
	@Test
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void shouldFindAllFilDetaljer() throws Exception {
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
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void shouldFindAllDokumentInfos() throws Exception {
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
	public void shouldFindReturInfoById() throws Exception {
		long returInfoId = 100L;
		Journalpost journalpost = getJournalpostBuilder()
									.returInfos(getReturInfoBuilder()
													.returInfoId(returInfoId)
													.arsakRetur(ArsakReturCode.IKKE_HENTET)
													.build(),
												getReturInfoBuilder()
													.returInfoId(200L)
													.arsakRetur(ArsakReturCode.ANNET)
													.build())
									.build();
		
		ReturInfo returInfo = journalpost.findReturInfoById(returInfoId);
		assertThat(returInfo.getArsakRetur(), is(ArsakReturCode.IKKE_HENTET));
	}
	
	@Test
	public void shouldFindKryssReferanseById() throws Exception {
		long kryssReferanseId = 100L;
		Long referanseNr = 123123L;
		Journalpost journalpost = getJournalpostBuilder()
									.kryssReferanser(getKryssreferanseBuilder()
														.kryssreferanseId(kryssReferanseId)
														.referanseNr(referanseNr)
														.build(),
													 getKryssreferanseBuilder()
													 	.kryssreferanseId(200L)
													 	.referanseNr(987987L)
													 	.build())
									.build();
		
		Kryssreferanse kryssreferanse = journalpost.findKryssreferanseById(kryssReferanseId);
		assertThat(kryssreferanse.getReferanseNr(), is(referanseNr));
	}

	@Test
	public void shouldFindDokumentInfoById() throws Exception {
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
	public void shouldReturnNullForNonMatchingDokumentInfoGivenDokumentInfoId() throws Exception {
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
	public void shouldFindBrukerById() throws Exception {
		long brukerInfoId = 100L;
		String brukerId = "***gammelt_fnr***";
		Journalpost journalpost = getJournalpostBuilder()
									.brukere(getBrukerBuilder()
												.brukerInfoId(150L)
												.brukerId("test")
												.build(),
											 getBrukerBuilder()
												.brukerInfoId(brukerInfoId)
												.brukerId(brukerId)
												.build())
									.build();
		
		Bruker bruker = journalpost.findBrukerById(brukerInfoId);
		assertThat(bruker.getBrukerId(), is(brukerId));
	}
	
	@Test
	public void shouldFindBrukerByBrukerId() throws Exception {
		String brukerId = "***gammelt_fnr***";
		long brukerInfoId = 100L;
		Journalpost journalpost = getJournalpostBuilder()
									.brukere(getBrukerBuilder()
												.brukerInfoId(150L)
												.brukerId("123123")
												.build(),
											 getBrukerBuilder()
												.brukerInfoId(brukerInfoId)
												.brukerId(brukerId)
												.build())
									.build();
		
		Bruker bruker = journalpost.findBrukerByBrukerId(brukerId);
		assertThat(bruker.getBrukerInfoId(), is(brukerInfoId));
	}

	@Test
	public void shouldFindDokumentInfoRelasjonById() throws Exception {
		long relasjonId = 100L;
		Journalpost journalpost = createDokumentInfoRelasjon(relasjonId);
		
		JournalpostDokumentInfoRelasjon dokumentInfoRelasjon = journalpost.findDokumentInfoRelasjonById(relasjonId);
		assertThat(dokumentInfoRelasjon.getTilknyttetJournalpostSom(), is(TilknyttetJournalpostSomCode.VEDLEGG));
	}
	
	@Test
	public void shouldFindDokumentInfoRelasjonByTilknyttetJournalpostSom() throws Exception {
		long relasjonId = 100L;
		Journalpost journalpost = createDokumentInfoRelasjon(relasjonId);
		
		Set<JournalpostDokumentInfoRelasjon> vedleggs = journalpost
				.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG);
		assertThat(vedleggs.size(), is(1));
		assertThat(vedleggs.iterator().next().getId(), is(relasjonId));
	}
	
	@Test
	public void shouldFindHoveddokumentDokumentRelasjon() throws Exception {
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
