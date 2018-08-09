package no.nav.dokarkiv.inngaaendejournal.v1.tjoark057;

import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static no.nav.dokarkiv.core.domain.builder.SaksrelasjonBuilder.getSaksrelasjonBuilder;
import static no.nav.dokarkiv.inngaaendejournal.v1.InngaaendeJournalDataProvider.DOKUMENT_INFO_ID;
import static no.nav.dokarkiv.inngaaendejournal.v1.InngaaendeJournalDataProvider.DOKUMENT_INFO_ID_VEDLEGG;
import static no.nav.dokarkiv.inngaaendejournal.v1.InngaaendeJournalDataProvider.buildJournalpost;
import static no.nav.dokarkiv.inngaaendejournal.v1.InngaaendeJournalDataProvider.createHovedDokumentInfo;
import static no.nav.dokarkiv.inngaaendejournal.v1.InngaaendeJournalDataProvider.createVedleggDokumentInfo;
import static no.nav.dokarkiv.inngaaendejournal.v1.common.JournalfoeringsbehovTo.MANGLER;
import static no.nav.dokarkiv.inngaaendejournal.v1.common.JournalfoeringsbehovTo.MANGLER_IKKE;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeInngaaendeException;
import no.nav.dokarkiv.core.exceptions.UgyldigInputException;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.inngaaendejournal.v1.common.JournalpostManglerTo;
import no.nav.dokarkiv.inngaaendejournal.v1.exceptions.JournalpostKanIkkeBehandlesException;
import no.nav.dokarkiv.inngaaendejournal.v1.tjoark056.JournalpostManglerToMapper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@RunWith(MockitoJUnitRunner.class)
public class UtledJournalfoeringsbehovServiceTest {

	@Mock
	private JoarkRepository repository;

	private UtledJournalfoeringsbehovService utledJournalfoeringsbehovService;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void setUp() throws Exception {
		utledJournalfoeringsbehovService = new UtledJournalfoeringsbehovService(repository, new JournalpostManglerToMapper());
	}

	@Test
	public void should_utlede_journalfoeringsbehov_mangler_ikke() throws Exception {
		when(repository.findById(any(Long.class))).thenReturn(Optional.of(buildJournalpost().journalStatus(JournalStatusCode.M).build()));

		JournalpostManglerTo to = utledJournalfoeringsbehovService.utledJournalfoeringsbehov("1");

		assertThat(to.getAvsenderId(), is(MANGLER_IKKE));
		assertThat(to.getAvsenderNavn(), is(MANGLER_IKKE));
		assertThat(to.getArkivSak(), is(MANGLER_IKKE));
		assertThat(to.getInnhold(), is(MANGLER_IKKE));
		assertThat(to.getTema(), is(MANGLER_IKKE));
		assertThat(to.getBruker(), is(MANGLER_IKKE));
		assertThat(to.getHoveddokument().getDokumentId(), is(DOKUMENT_INFO_ID));
		assertThat(to.getHoveddokument().getTittel(), is(MANGLER_IKKE));
		assertThat(to.getHoveddokument().getDokumentKategori(), is(MANGLER_IKKE));
		assertThat(to.getVedlegg().get(0).getDokumentId(), is(DOKUMENT_INFO_ID_VEDLEGG));
		assertThat(to.getVedlegg().get(0).getTittel(), is(MANGLER_IKKE));
		assertThat(to.getVedlegg().get(0).getDokumentKategori(), is(MANGLER_IKKE));
	}

	@Test
	public void should_utlede_journalfoeringsbehov_mangler() throws Exception {
		when(repository.findById(any(Long.class))).thenReturn(Optional.of(getJournalpostBuilder()
				.journalpostType(JournalpostTypeCode.I)
				.journalStatus(JournalStatusCode.MO)
				.dokumentInfoRelasjoner(
						getJournalpostDokumentInfoRelasjonBuilder()
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
								.dokumentInfo(createHovedDokumentInfo().tittel(null).kategori(null).build())
								.build(),
						getJournalpostDokumentInfoRelasjonBuilder()
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
								.dokumentInfo(createVedleggDokumentInfo().tittel(null).kategori(null).build())
								.build())
				.build()));

		JournalpostManglerTo to = utledJournalfoeringsbehovService.utledJournalfoeringsbehov("1");

		assertThat(to.getAvsenderId(), is(MANGLER));
		assertThat(to.getAvsenderNavn(), is(MANGLER));
		assertThat(to.getArkivSak(), is(MANGLER));
		assertThat(to.getInnhold(), is(MANGLER));
		assertThat(to.getTema(), is(MANGLER));
		assertThat(to.getBruker(), is(MANGLER));
		assertThat(to.getHoveddokument().getDokumentId(), is(DOKUMENT_INFO_ID));
		assertThat(to.getHoveddokument().getTittel(), is(MANGLER));
		assertThat(to.getHoveddokument().getDokumentKategori(), is(MANGLER));
		assertThat(to.getVedlegg().get(0).getDokumentId(), is(DOKUMENT_INFO_ID_VEDLEGG));
		assertThat(to.getVedlegg().get(0).getTittel(), is(MANGLER));
		assertThat(to.getVedlegg().get(0).getDokumentKategori(), is(MANGLER));
	}

	@Test
	public void should_throw_UgyldigInputException_when_input_is_invalid() throws Exception {
		thrown.expect(UgyldigInputException.class);

		utledJournalfoeringsbehovService.utledJournalfoeringsbehov(null);
	}

	@Test
	public void should_throw_UgyldigInputException_when_journalpostId_wrong_type() throws Exception {
		thrown.expect(UgyldigInputException.class);

		utledJournalfoeringsbehovService.utledJournalfoeringsbehov("hurra");
	}

	@Test
	public void should_throw_JournalpostIkkeFunnetException_when_journalpost_not_found() throws Exception {
		thrown.expect(JournalpostIkkeFunnetException.class);

		utledJournalfoeringsbehovService.utledJournalfoeringsbehov("1");
	}

	@Test
	public void should_throw_JournalpostKanIkkeBehandlesException_when_status_is_not_midlertidig() throws Exception {
		when(repository.findById(any(Long.class))).thenReturn(Optional.of(getJournalpostBuilder()
				.journalpostType(JournalpostTypeCode.I)
				.journalStatus(JournalStatusCode.J).build()));

		thrown.expect(JournalpostKanIkkeBehandlesException.class);

		utledJournalfoeringsbehovService.utledJournalfoeringsbehov("1");
	}

	@Test
	public void should_throw_JournalpostKanIkkeBehandlesException_when_is_feilregistrert() throws Exception {
		when(repository.findById(any(Long.class))).thenReturn(Optional.of(getJournalpostBuilder()
				.journalpostType(JournalpostTypeCode.I)
				.journalStatus(JournalStatusCode.M)
				.saksrelasjon(getSaksrelasjonBuilder().feilregistrert(true).build()).build()));

		thrown.expect(JournalpostKanIkkeBehandlesException.class);

		utledJournalfoeringsbehovService.utledJournalfoeringsbehov("1");
	}

	@Test
	public void should_throw_JournalpostIkkeInngaaendeException_when_jptype_not_inngaaende() throws Exception {
		when(repository.findById(any(Long.class))).thenReturn(Optional.of(getJournalpostBuilder()
				.journalpostType(JournalpostTypeCode.U)
				.journalStatus(JournalStatusCode.M).build()));

		thrown.expect(JournalpostIkkeInngaaendeException.class);

		utledJournalfoeringsbehovService.utledJournalfoeringsbehov("1");
	}
}