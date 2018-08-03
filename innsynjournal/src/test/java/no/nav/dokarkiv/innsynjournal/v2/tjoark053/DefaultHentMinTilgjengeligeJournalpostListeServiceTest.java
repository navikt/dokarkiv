package no.nav.dokarkiv.innsynjournal.v2.tjoark053;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import no.nav.dokarkiv.core.domain.ChangeStamp;
import no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder;
import no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder;
import no.nav.dokarkiv.core.domain.builder.JournalpostBuilder;
import no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.repository.journalpostliste.HentMinJPListeParameters;
import no.nav.dokarkiv.core.repository.journalpostliste.JournalpostListeRepository;
import no.nav.dokarkiv.core.repository.journalpostliste.SakFagsystem;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;


/**
 * Unit tests for {@link HentMinTilgjengeligeJournalpostListeService}
 *
 * @author Torgeir Cook, Visma Consulting
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultHentMinTilgjengeligeJournalpostListeServiceTest {

	public static final Date EARLIEST_DATE_ALLOWED = Date.from(LocalDate.of(2018, Month.JULY, 23).atStartOfDay(ZoneId.systemDefault()).toInstant());
	public static final String SAK_ID = "1256";
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Mock
	private JournalpostListeRepository journalpostListeRepository;

	@InjectMocks
	private DefaultHentMinTilgjengeligeJournalpostListeService service;

	private List<Journalpost> journalposts;

	@Before
	public void setUp() throws Exception {
		journalposts = new ArrayList<>();
		when(journalpostListeRepository.findJournalpostListe(any(HentMinJPListeParameters.class))).thenReturn(journalposts);
		service.setEarliestDateAllowed(LocalDate.of(2018, Month.JULY, 23));
	}

	@Test
	public void shouldCreateParams() throws Exception {
		ArgumentCaptor<HentMinJPListeParameters> captor = ArgumentCaptor.forClass(HentMinJPListeParameters.class);
		when(journalpostListeRepository.findJournalpostListe(captor.capture()))
				.thenReturn(journalposts);

		service.hentMineTilgjengeligeJournalposter(createRequest(new SakFagsystem(FagsystemCode.PEN, SAK_ID)));

		HentMinJPListeParameters parameters = captor.getValue();
		assertThat(parameters.getTidligstInnsynDato(), is(EARLIEST_DATE_ALLOWED));
		assertThat(parameters.getSaksListe(), hasSize(1));
		assertThat(parameters.getSaksListe().get(0).getFagsystem(), is(FagsystemCode.PEN));
		assertThat(parameters.getSaksListe().get(0).getSakId(), is(SAK_ID));
		assertThat(parameters.getTillattInnsynStatus(), containsInAnyOrder(JournalStatusCode.J, JournalStatusCode.FS,
				JournalStatusCode.FL, JournalStatusCode.E));
		assertThat(parameters.isVisFeilRegistrert(), is(false));
		assertThat(parameters.getSkjulFagomraade(), contains(FagomradeCode.KTR));
	}

	@Test
	public void shouldThrowIfEmptySakslist() {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("SaksListe can not be empty");
		service.hentMineTilgjengeligeJournalposter(createRequest());
	}

	@Test
	public void shouldThrowIfSaksMapContainsSakWithEmptySaksId() {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("SaksId must be set");
		service.hentMineTilgjengeligeJournalposter(
				createRequest(new SakFagsystem(FagsystemCode.BID, "0"), new SakFagsystem(FagsystemCode.FS19, null)));
	}

	@Test
	public void shouldThrowIfSaksListConstainsSakWithEmptyFagsystemCode() {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("FagsystemCode of sak with saksId 0, must be set");
		service.hentMineTilgjengeligeJournalposter(createRequest(new SakFagsystem(null, "0"), new SakFagsystem(FagsystemCode.AO01, "1")));
	}

	@Test
	public void shouldEvictJournalpostsFromSession() {
//		when(sessionMock.contains(journalposts)).thenReturn(true);
//		when(joarkRepository.findJournalpostListe(any(HentMinJPListeParameters.class)))
//				.thenReturn(journalposts);
//		service.hentMineTilgjengeligeJournalposter(createRequest(new SakFagsystem(FagsystemCode.BID, "0")));
//		verify(sessionMock).evict(journalposts);
	}

	@Test
	public void shouldNotRemoveHovedDokOrVedlegg() {
		journalposts.add(createLegalJournalpost());
		journalposts = service.hentMineTilgjengeligeJournalposter(createRequest(new SakFagsystem(FagsystemCode.BID, "1")));
		assertThat(journalposts.size(), is(1));
		assertNotNull(journalposts.get(0).findHoveddokumentDokumentInfoRelasjon());
		assertThat(journalposts.get(0).findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG).size(), is(2));
	}

	@Test
	public void shouldRemoveJournalpostIfNoHoveddokument() {
		journalposts.add(createLegalJournalpost());
		Journalpost journalpost = createLegalJournalpost();
		journalpost.removeJournalpostDokumentInfoRelasjon(journalpost.findHoveddokumentDokumentInfoRelasjon());
		journalposts.add(journalpost);
		journalposts = service.hentMineTilgjengeligeJournalposter(createRequest(new SakFagsystem(FagsystemCode.BID, "1")));
		assertThat(journalposts.size(), is(1));
	}

	@Test
	public void shouldRemoveJournalpostIfHovedDokKategoriNotForvaltningsNotat() {
		Journalpost journalpost = createLegalJournalpost();
		journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().setKategori(DokumentKategoriCode.F);
		journalposts.add(journalpost);
		journalposts.add(createLegalJournalpost());
		journalposts = service.hentMineTilgjengeligeJournalposter(createRequest(new SakFagsystem(FagsystemCode.BID, "1")));
		assertThat(journalposts.size(), is(1));
	}

	@Test
	public void shouldRemoveJournalpostIfHovedDokIsOrganInternt() {
		Journalpost journalpost = createLegalJournalpost();
		journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().setOrganInternt(true);
		journalposts.add(journalpost);
		journalposts.add(createLegalJournalpost());
		journalposts = service.hentMineTilgjengeligeJournalposter(createRequest(new SakFagsystem(FagsystemCode.BID, "1")));

		assertThat(journalposts.size(), is(1));
	}

	@Test
	public void shouldRemoveJournalpostIfJournaltypeUorNAndHovedDokStatusNotFerdigstilt() {
		Journalpost journalpost = createLegalJournalpost();
		journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().setDokumentstatus(DokumentStatusCode.AVBRUTT);
		Journalpost journalpostUt = createLegalJournalpost();
		journalpostUt.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().setDokumentstatus(DokumentStatusCode.AVBRUTT);
		journalpostUt.setJournalposttype(JournalpostTypeCode.U);
		journalposts.add(journalpost);
		journalposts.add(journalpostUt);
		journalposts.add(createLegalJournalpost());
		journalposts = service.hentMineTilgjengeligeJournalposter(createRequest(new SakFagsystem(FagsystemCode.BID, "1")));

		assertThat(journalposts.size(), is(1));
	}

	@Test
	public void shouldRemoveJournalpostIfHovedDokIsSlettet() {
		Journalpost journalpost = createLegalJournalpost();
		journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().setSlettet(true);
		journalposts.add(journalpost);
		journalposts.add(createLegalJournalpost());
		journalposts = service.hentMineTilgjengeligeJournalposter(createRequest(new SakFagsystem(FagsystemCode.BID, "1")));

		assertThat(journalposts.size(), is(1));
	}

	@Test
	public void shouldRemoveVedleggIfNotForvaltningsNotat() {
		Journalpost journalpost = createJournalpostWithInfoRelasjon
				(createDokumentInfoRelasjonVedlegg(true, true, DokumentStatusCode.AVBRUTT));
		journalposts.add(journalpost);
		journalposts = service.hentMineTilgjengeligeJournalposter(createRequest(new SakFagsystem(FagsystemCode.BID, "1")));

		assertThat(journalposts.get(0).findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG).size(), is(2));
	}

	@Test
	public void shouldRemoveVedleggIfOrganInternt() {
		Journalpost journalpost = createJournalpostWithInfoRelasjon
				(createDokumentInfoRelasjonVedlegg(true, false, DokumentStatusCode.AVBRUTT));

		journalposts.add(journalpost);
		journalposts = service.hentMineTilgjengeligeJournalposter(createRequest(new SakFagsystem(FagsystemCode.BID, "1")));

		assertThat(journalposts.size(), is(1));
		assertThat(journalposts.get(0).findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG).size(), is(2));
	}

	@Test
	public void shouldRemoveVedleggIfIsSlettet() {
		Journalpost journalpost = createJournalpostWithInfoRelasjon
				(createDokumentInfoRelasjonVedlegg(false, true, DokumentStatusCode.AVBRUTT));

		journalposts.add(journalpost);
		journalposts = service.hentMineTilgjengeligeJournalposter(createRequest(new SakFagsystem(FagsystemCode.BID, "1")));

		assertThat(journalposts.size(), is(1));
		assertThat(journalposts.get(0).findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG).size(), is(2));
	}

	@Test
	public void shouldRemoveVedleggIfJournaltypeUorNAndVedleggStatusNotFerdigstilt() {
		Journalpost journalpost = createJournalpostWithInfoRelasjon(createDokumentInfoRelasjonVedlegg(true, true, DokumentStatusCode.AVBRUTT));
		Journalpost journalpostUt = createJournalpostWithInfoRelasjon(createDokumentInfoRelasjonVedlegg(true, true, DokumentStatusCode.AVBRUTT));
		journalpostUt.setJournalposttype(JournalpostTypeCode.U);

		journalposts.add(journalpost);
		journalposts.add(journalpostUt);
		journalposts = service.hentMineTilgjengeligeJournalposter(createRequest(new SakFagsystem(FagsystemCode.BID, "1")));

		assertThat(journalposts.size(), is(2));
		assertThat(journalposts.get(0).findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG).size(), is(2));
		assertThat(journalposts.get(1).findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG).size(), is(2));
	}

	@Test
	public void shouldRemoveFilDetalajerOfVedleggWithVariantFormatNotArkiv() {
		journalposts.add(createLegalJournalpost());
		journalposts = service.hentMineTilgjengeligeJournalposter(createRequest(new SakFagsystem(FagsystemCode.BID, "1")));

		assertThat(journalposts.size(), is(1));
		Set<JournalpostDokumentInfoRelasjon> vedlegg = journalposts.get(0).findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG);
		assertThat(vedlegg.size(), is(2));
		assertThat(vedlegg.iterator().next().getDokumentInfo().getFildetaljerListe().size(), is(1));
		assertThat(vedlegg.iterator().next().getDokumentInfo().getFildetaljerListe().size(), is(1));
	}

	private Journalpost createLegalJournalpost() {
		return JournalpostBuilder.getJournalpostBuilder()
				.journalpostType(JournalpostTypeCode.N)
				.changeStamp(new ChangeStamp(null, new Date(2L), null, null))
				.dokumentInfoRelasjoner(
						createDokumentInfoRelasjon(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
						, createDokumentInfoRelasjon(TilknyttetJournalpostSomCode.VEDLEGG)
						, createDokumentInfoRelasjon(TilknyttetJournalpostSomCode.VEDLEGG))
				.build();
	}

	private Journalpost createJournalpostWithInfoRelasjon(JournalpostDokumentInfoRelasjon infoRelasjon) {
		return JournalpostBuilder.getJournalpostBuilder()
				.journalpostType(JournalpostTypeCode.N)
				.changeStamp(new ChangeStamp(null, new Date(2L), null, null))
				.dokumentInfoRelasjoner(
						createDokumentInfoRelasjon(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
						, createDokumentInfoRelasjon(TilknyttetJournalpostSomCode.VEDLEGG)
						, createDokumentInfoRelasjon(TilknyttetJournalpostSomCode.VEDLEGG)
						, infoRelasjon)
				.build();
	}

	private JournalpostDokumentInfoRelasjon createDokumentInfoRelasjon(TilknyttetJournalpostSomCode tilknyttetJournalpostSomCode) {
		return JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder()
				.tilknyttetJournalpostSom(tilknyttetJournalpostSomCode)
				.dokumentInfo(DokumentInfoBuilder.getDokumentInfoBuilder()
						.kategori(DokumentKategoriCode.FORVALTNINGSNOTAT)
						.organInternt(false)
						.dokumentstatus(DokumentStatusCode.FERDIGSTILT)
						.slettet(false)
						.filDetaljerList(
								FilDetaljerBuilder.getFilDetaljerBuilder()
										.variantFormat(VariantFormatCode.PRODUKSJON)
										.filtype(FilTypeCode.AXML)
										.build(),
								FilDetaljerBuilder.getFilDetaljerBuilder()
										.variantFormat(VariantFormatCode.ARKIV)
										.filtype(FilTypeCode.PDF)
										.build()
						)
						.build())
				.build();
	}

	private JournalpostDokumentInfoRelasjon createDokumentInfoRelasjonVedlegg
			(boolean organInternt, boolean isSlettet, DokumentStatusCode dokumentStatusCode) {
		return JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder()
				.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.dokumentInfo(DokumentInfoBuilder.getDokumentInfoBuilder()
						.kategori(DokumentKategoriCode.B)
						.organInternt(organInternt)
						.dokumentstatus(dokumentStatusCode)
						.slettet(isSlettet)
						.filDetaljerList(FilDetaljerBuilder.getFilDetaljerBuilder()
								.variantFormat(VariantFormatCode.ARKIV)
								.filtype(FilTypeCode.PDF)
								.build())
						.build())
				.build();
	}


	private HentJournalpostListeToRequest createRequest(SakFagsystem... saks) {
		HentJournalpostListeToRequest hentJournalpostListeTo = HentJournalpostListeToRequest.builder().build();
		List<SakFagsystem> saksListe = hentJournalpostListeTo.getSaksListe();
		for (SakFagsystem sak : saks) {
			saksListe.add(sak);
		}
		return hentJournalpostListeTo;
	}
}
