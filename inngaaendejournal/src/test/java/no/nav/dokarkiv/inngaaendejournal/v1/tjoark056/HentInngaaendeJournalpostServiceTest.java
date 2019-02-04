package no.nav.dokarkiv.inngaaendejournal.v1.tjoark056;

import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static no.nav.dokarkiv.inngaaendejournal.v1.InngaaendeJournalDataProvider.DOKUMENT_INFO_ID_VEDLEGG;
import static no.nav.dokarkiv.inngaaendejournal.v1.InngaaendeJournalDataProvider.DOKUMENT_INFO_ID_VEDLEGG_2;
import static no.nav.dokarkiv.inngaaendejournal.v1.InngaaendeJournalDataProvider.DOKUMENT_INFO_ID_VEDLEGG_3;
import static no.nav.dokarkiv.inngaaendejournal.v1.InngaaendeJournalDataProvider.buildBaseJournalpost;
import static no.nav.dokarkiv.inngaaendejournal.v1.InngaaendeJournalDataProvider.buildJournalpost;
import static no.nav.dokarkiv.inngaaendejournal.v1.InngaaendeJournalDataProvider.createBaseVedleggDokumentInfo;
import static no.nav.dokarkiv.inngaaendejournal.v1.InngaaendeJournalDataProvider.createHovedDokumentInfo;
import static no.nav.dokarkiv.inngaaendejournal.v1.InngaaendeJournalDataProvider.createSaksrelasjon;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import no.nav.dokarkiv.core.domain.ChangeStamp;
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;
import no.nav.dokarkiv.core.domain.service.SkjermingService;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeInngaaendeException;
import no.nav.dokarkiv.core.exceptions.UgyldigInputException;
import no.nav.dokarkiv.core.repository.JoarkRepositorySkjermet;
import no.nav.dokarkiv.inngaaendejournal.v1.InngaaendeJournalDataProvider;
import no.nav.dokarkiv.inngaaendejournal.v1.tjoark056.to.AktoerTo;
import no.nav.dokarkiv.inngaaendejournal.v1.tjoark056.to.DokumentInnholdTo;
import no.nav.dokarkiv.inngaaendejournal.v1.tjoark056.to.DokumenttilstandTo;
import no.nav.dokarkiv.inngaaendejournal.v1.tjoark056.to.InngaaendeJournalpostTo;
import no.nav.dokarkiv.inngaaendejournal.v1.tjoark056.to.JournaltilstandTo;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@RunWith(MockitoJUnitRunner.class)
public class HentInngaaendeJournalpostServiceTest {

	@Mock
    private JoarkRepositorySkjermet repository;
	@Mock
	private SkjermingService skjermingService;

	private HentInngaaendeJournalpostService service;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void setUp() throws Exception {
		service = new HentInngaaendeJournalpostService(repository, new InngaaendeJournalpostToMapper(skjermingService));
	}

	@Test
	public void should_map_journalpost_to_dto() throws Exception {
		when(repository.findById(any(Long.class))).thenReturn(Optional.of(buildJournalpost().build()));

		InngaaendeJournalpostTo to = service.hentJournalpost("1");

		assertThat(to.getAvsenderId(), is(InngaaendeJournalDataProvider.AVSENDER_MOTTAKERID));
		assertThat(to.getForsendelseMottatt(), is(InngaaendeJournalDataProvider.NOW));
		assertThat(to.getMottakskanal(), is(MottaksKanalCode.NAV_NO));
		assertThat(to.getTema(), is(FagomradeCode.PEN));
		assertThat(to.getKanalReferanseId(), is(InngaaendeJournalDataProvider.KANAL_REFERANSE_ID));
		assertThat(to.getJournaltilstand(), is(JournaltilstandTo.ENDELIG));
		assertThat(to.getArkivSak().getArkivSakId(), is(InngaaendeJournalDataProvider.ARKIV_SAKID));
		assertThat(to.getArkivSak().getFagsystem(), is(FagsystemCode.PEN));
		assertThat(findAktoerByType(to.getBrukere(), BrukerTypeCode.PERSON).getAktoerId(), is(InngaaendeJournalDataProvider.FNR));
		assertThat(findAktoerByType(to.getBrukere(), BrukerTypeCode.ORGANISASJON).getAktoerId(), is(InngaaendeJournalDataProvider.ORGNR));
		assertThat(to.getHoveddokument().getDokumentkategori(), is(DokumentKategoriCode.SOK));
		assertThat(to.getHoveddokument().getDokumenttypeId(), is(InngaaendeJournalDataProvider.DOKUMENTTYPE_ID));
		assertThat(to.getHoveddokument().getDokumentId(), is(InngaaendeJournalDataProvider.DOKUMENT_INFO_ID));
		assertThat(to.getHoveddokument().getDokumenttilstand(), is(DokumenttilstandTo.FERDIGSTILT));
		assertThat(findDokumentinnholdByVariant(to.getHoveddokument().getDokumentInnhold(), VariantFormatCode.ARKIV).getArkivFiltype(), is(FilTypeCode.PDFA));
		assertThat(findDokumentinnholdByVariant(to.getHoveddokument().getDokumentInnhold(), VariantFormatCode.PRODUKSJON).getArkivFiltype(), is(FilTypeCode.XML));
		assertThat(to.getVedlegg().get(0).getDokumentkategori(), is(DokumentKategoriCode.ES));
		assertThat(to.getVedlegg().get(0).getDokumenttypeId(), is(InngaaendeJournalDataProvider.DOKUMENTTYPE_ID_VEDLEGG));
		assertThat(to.getVedlegg().get(0).getDokumentId(), is(DOKUMENT_INFO_ID_VEDLEGG));
		assertThat(to.getVedlegg().get(0).getDokumenttilstand(), is(DokumenttilstandTo.FERDIGSTILT));
		assertThat(findDokumentinnholdByVariant(to.getVedlegg().get(0).getDokumentInnhold(), VariantFormatCode.ARKIV).getArkivFiltype(), is(FilTypeCode.PDFA));
		assertThat(findDokumentinnholdByVariant(to.getVedlegg().get(0).getDokumentInnhold(), VariantFormatCode.PRODUKSJON).getArkivFiltype(), is(FilTypeCode.XML));
		assertNull(findDokumentinnholdByVariant(to.getVedlegg().get(0).getDokumentInnhold(), VariantFormatCode.SLADDET));
	}

	@Test
	public void dto_should_have_journaltilstand_midlertidig() throws Exception {
		when(repository.findById(eq(1L))).thenReturn(Optional.of(buildJournalpost().journalStatus(JournalStatusCode.M).build()));
		when(repository.findById(eq(2L))).thenReturn(Optional.of(buildJournalpost().journalStatus(JournalStatusCode.MO).build()));
		when(repository.findById(eq(3L))).thenReturn(Optional.of(buildJournalpost().journalStatus(JournalStatusCode.UB).build()));
		when(repository.findById(eq(4L))).thenReturn(Optional.of(buildJournalpost().journalStatus(JournalStatusCode.OD).build()));

		assertMidlertidigJournaltilstand("1");
		assertMidlertidigJournaltilstand("2");
		assertMidlertidigJournaltilstand("3");
		assertMidlertidigJournaltilstand("4");
	}

	@Test
	public void dto_should_have_journaltilstand_utgaar() throws Exception {
		when(repository.findById(eq(1L))).thenReturn(Optional.of(buildJournalpost().journalStatus(JournalStatusCode.U).build()));
		Saksrelasjon feilregistrertSak = createSaksrelasjon();
		feilregistrertSak.setFeilregistrert(true);
		when(repository.findById(eq(2L))).thenReturn(Optional.of(buildJournalpost().saksrelasjon(feilregistrertSak).build()));
		when(repository.findById(eq(3L))).thenReturn(Optional.of(buildJournalpost().journalStatus(JournalStatusCode.M).saksrelasjon(feilregistrertSak).build()));
		when(repository.findById(eq(4L))).thenReturn(Optional.of(buildJournalpost().journalStatus(JournalStatusCode.MO).saksrelasjon(feilregistrertSak).build()));
		when(repository.findById(eq(5L))).thenReturn(Optional.of(buildJournalpost().journalStatus(JournalStatusCode.UB).saksrelasjon(feilregistrertSak).build()));
		when(repository.findById(eq(6L))).thenReturn(Optional.of(buildJournalpost().journalStatus(JournalStatusCode.OD).saksrelasjon(feilregistrertSak).build()));

		assertUtgaarJournaltilstand("1");
		assertUtgaarJournaltilstand("2");
		assertUtgaarJournaltilstand("3");
		assertUtgaarJournaltilstand("4");
		assertUtgaarJournaltilstand("5");
		assertUtgaarJournaltilstand("6");
	}

	@Test
	public void dto_hoveddokument_should_have_dokumenttilstand_avbrutt() throws Exception {
		Journalpost build = buildJournalpost().build();
		build.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().setDokumentstatus(DokumentStatusCode.AVBRUTT);
		when(repository.findById(eq(1L))).thenReturn(Optional.of(build));

		InngaaendeJournalpostTo to = service.hentJournalpost("1");
		assertThat(to.getHoveddokument().getDokumenttilstand(), is(DokumenttilstandTo.AVBRUTT));
	}

	@Test
	public void dto_vedlegg_should_have_dokumenttilstand_avbrutt() throws Exception {
		Journalpost build = buildJournalpost().build();
		build.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG).iterator().next().getDokumentInfo().setDokumentstatus(DokumentStatusCode.AVBRUTT);
		when(repository.findById(eq(1L))).thenReturn(Optional.of(build));

		InngaaendeJournalpostTo to = service.hentJournalpost("1");
		assertThat(to.getVedlegg().get(0).getDokumenttilstand(), is(DokumenttilstandTo.AVBRUTT));
	}

	@Test
	public void dto_hoveddokument_should_have_dokumenttilstand_under_redigering() throws Exception {
		Journalpost build = buildJournalpost().build();
		build.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().setDokumentstatus(DokumentStatusCode.UNDER_REDIGERING);
		when(repository.findById(eq(1L))).thenReturn(Optional.of(build));

		InngaaendeJournalpostTo to = service.hentJournalpost("1");
		assertThat(to.getHoveddokument().getDokumenttilstand(), is(DokumenttilstandTo.UNDER_REDIGERING));
	}

	@Test
	public void dto_vedlegg_should_have_dokumenttilstand_under_redigering() throws Exception {
		Journalpost build = buildJournalpost().build();
		build.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG).iterator().next().getDokumentInfo().setDokumentstatus(DokumentStatusCode.UNDER_REDIGERING);
		when(repository.findById(eq(1L))).thenReturn(Optional.of(build));

		InngaaendeJournalpostTo to = service.hentJournalpost("1");
		assertThat(to.getVedlegg().get(0).getDokumenttilstand(), is(DokumenttilstandTo.UNDER_REDIGERING));
	}

	@Test
	public void should_have_vedlegg_sorted_by_createdDate_descending() throws Exception {
		Journalpost build = buildBaseJournalpost().dokumentInfoRelasjoner(
				getJournalpostDokumentInfoRelasjonBuilder()
						.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
						.dokumentInfo(createHovedDokumentInfo().build())
						.build(),
				getJournalpostDokumentInfoRelasjonBuilder()
						.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
						.dokumentInfo(createBaseVedleggDokumentInfo().dokumentInfoId(DOKUMENT_INFO_ID_VEDLEGG_3)
								.changeStamp(new ChangeStamp("test", Date.from(LocalDateTime.parse("2017-05-29T00:59:00").atZone(ZoneId.systemDefault()).toInstant()), null, null))
								.build())
						.build(),
				getJournalpostDokumentInfoRelasjonBuilder()
						.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
						.dokumentInfo(createBaseVedleggDokumentInfo().dokumentInfoId(DOKUMENT_INFO_ID_VEDLEGG)
								.changeStamp(new ChangeStamp("test", Date.from(LocalDateTime.parse("2017-05-30T01:00:00").atZone(ZoneId.systemDefault()).toInstant()), null, null))
								.build())
						.build(),
				getJournalpostDokumentInfoRelasjonBuilder()
						.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
						.dokumentInfo(createBaseVedleggDokumentInfo().dokumentInfoId(DOKUMENT_INFO_ID_VEDLEGG_2)
								.changeStamp(new ChangeStamp("test", Date.from(LocalDateTime.parse("2017-05-29T01:00:00").atZone(ZoneId.systemDefault()).toInstant()), null, null))
								.build())
						.build())
				.build();
		when(repository.findById(eq(1L))).thenReturn(Optional.of(build));

		InngaaendeJournalpostTo to = service.hentJournalpost("1");

		assertThat(to.getVedlegg().get(0).getDokumentId(), is(DOKUMENT_INFO_ID_VEDLEGG_3));
		assertThat(to.getVedlegg().get(1).getDokumentId(), is(DOKUMENT_INFO_ID_VEDLEGG_2));
		assertThat(to.getVedlegg().get(2).getDokumentId(), is(DOKUMENT_INFO_ID_VEDLEGG));
	}

	@Test
	public void should_throw_UgyldigInputException_on_invalid_input() throws Exception {
		thrown.expect(UgyldigInputException.class);

		service.hentJournalpost(null);
	}

	@Test
	public void should_throw_UgyldigInputException_on_invalid_journalpostId_type() throws Exception {
		thrown.expect(UgyldigInputException.class);

		service.hentJournalpost("jippi");
	}

	@Test
	public void should_throw_JournalpostIkkeFunnetException_if_journalpost_not_found() throws Exception {
		thrown.expect(JournalpostIkkeFunnetException.class);

		service.hentJournalpost("1");
	}

	@Test
	public void should_throw_JournalpostIkkeInngaaendeException_if_jptype_not_inngaaende() throws Exception {
		when(repository.findById(any(Long.class))).thenReturn(Optional.of(buildJournalpost().journalpostType(JournalpostTypeCode.U).build()));

		thrown.expect(JournalpostIkkeInngaaendeException.class);

		service.hentJournalpost("1");
	}

	@Test
	public void should_map_journalpost_to_dto_kassert() throws Exception {
		when(repository.findById(any(Long.class))).thenReturn(Optional.of(buildJournalpost().build()));
		when(skjermingService.isDokumentInfoKassert(any(DokumentInfo.class))).thenReturn(true);

		InngaaendeJournalpostTo to = service.hentJournalpost("1");

		assertThat(to.getAvsenderId(), is(InngaaendeJournalDataProvider.AVSENDER_MOTTAKERID));
		assertThat(to.getForsendelseMottatt(), is(InngaaendeJournalDataProvider.NOW));
		assertThat(to.getMottakskanal(), is(MottaksKanalCode.NAV_NO));
		assertThat(to.getTema(), is(FagomradeCode.PEN));
		assertThat(to.getKanalReferanseId(), is(InngaaendeJournalDataProvider.KANAL_REFERANSE_ID));
		assertThat(to.getJournaltilstand(), is(JournaltilstandTo.ENDELIG));
		assertThat(to.getArkivSak().getArkivSakId(), is(InngaaendeJournalDataProvider.ARKIV_SAKID));
		assertThat(to.getArkivSak().getFagsystem(), is(FagsystemCode.PEN));
		assertThat(findAktoerByType(to.getBrukere(), BrukerTypeCode.PERSON).getAktoerId(), is(InngaaendeJournalDataProvider.FNR));
		assertThat(findAktoerByType(to.getBrukere(), BrukerTypeCode.ORGANISASJON).getAktoerId(), is(InngaaendeJournalDataProvider.ORGNR));
		assertThat(to.getHoveddokument().getDokumentkategori(), is(DokumentKategoriCode.SOK));
		assertThat(to.getHoveddokument().getDokumenttypeId(), is(InngaaendeJournalDataProvider.DOKUMENTTYPE_ID));
		assertThat(to.getHoveddokument().getDokumentId(), is(InngaaendeJournalDataProvider.DOKUMENT_INFO_ID));
		assertThat(to.getHoveddokument().getDokumenttilstand(), is(DokumenttilstandTo.FERDIGSTILT));
		assertThat(to.getHoveddokument().getDokumentInnhold().size(), is(0));
		assertThat(to.getVedlegg().get(0).getDokumenttilstand(), is(DokumenttilstandTo.FERDIGSTILT));
		assertThat(to.getVedlegg().get(0).getDokumenttypeId(), is(InngaaendeJournalDataProvider.DOKUMENTTYPE_ID_VEDLEGG));
		assertThat(to.getVedlegg().get(0).getDokumentInnhold().size(), is(0));
	}

	private AktoerTo findAktoerByType(final List<AktoerTo> brukere, final BrukerTypeCode brukerType) {
		return Iterables.find(brukere, new Predicate<AktoerTo>() {
			@Override
			public boolean apply(AktoerTo aktoerTo) {
				return brukerType == aktoerTo.getAktoerType();
			}
		});
	}

	private DokumentInnholdTo findDokumentinnholdByVariant(final List<DokumentInnholdTo> dokumentInnholds, final VariantFormatCode variantFormat) {
		return Iterables.find(dokumentInnholds, new Predicate<DokumentInnholdTo>() {
			@Override
			public boolean apply(DokumentInnholdTo dokumentInnholdTo) {
				return variantFormat == dokumentInnholdTo.getVariantFormat();
			}
		}, null);
	}

	private void assertUtgaarJournaltilstand(String journalpostId) throws Exception {
		assertJournaltilstand(journalpostId, JournaltilstandTo.UTGAAR);
	}

	private void assertMidlertidigJournaltilstand(String journalpostId) throws Exception {
		assertJournaltilstand(journalpostId, JournaltilstandTo.MIDLERTIDIG);
	}

	private void assertJournaltilstand(String journalpostId, JournaltilstandTo expectedJournaltilstand) throws Exception {
		InngaaendeJournalpostTo to = service.hentJournalpost(journalpostId);
		assertThat(to.getJournaltilstand(), is(expectedJournaltilstand));
	}
}