package no.nav.dokarkiv.behandlejournal.v2.tjoark061;

import no.nav.dokarkiv.behandlejournal.SporingsMetaData;
import no.nav.dokarkiv.core.domain.builder.BrukerBuilder;
import no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder;
import no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder;
import no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder;
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.exceptions.ApplicationException;
import no.nav.dokarkiv.core.exceptions.InvalidJournalpostStructureException;
import no.nav.dokarkiv.core.exceptions.NoJournalpostFoundException;
import no.nav.dokarkiv.core.repository.DokumentFilRepository;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.core.repository.JoarkRepositorySkjermet;
import no.nav.dokarkiv.core.sporing.KildeNavnPopulator;
import no.nav.dokarkiv.core.stelvio.RequestContextSetter;
import no.nav.dokarkiv.core.stelvio.SimpleRequestContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;
import java.util.Set;

import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Test class for DefaultLagreVedleggPaaJournalpost
 * 
 * @author Rune Romundstad, Visma Consulting
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultLagreVedleggPaaJournalpostTest {
	private static final String VEDLEGG_DOKUMENT_TYPE_ID = "458212";
	private static final String SPORING_FORNAVN = "fornavn";
	private static final String SPORING_ETTERNAVN = "etternavn";
	private static final String COMPONENT_ID = "TESTER";
	private static final String TITTEL = "tittel";
	private static final String BRUKEROPPGITT_TITTEL = "brukeroppgittTittel";
	private static final Long JOURNALPOST_ID = 1L;
	private static final long DOKUMENT_ID = 100L;

	@Mock
    private JoarkRepositorySkjermet joarkRepositoryMock;
	@Mock
    private DokumentinfoRepository dokumentinfoRepositoryMock;
	@Mock
	private DokumentFilRepository dokumentFilRepositoryMock;
	@Mock
	private KildeNavnPopulator kildeNavnPopulatorMock;

	@InjectMocks
	private DefaultLagreVedleggPaaJournalpost service;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private LagreVedleggPaaJournalpostRequest lagreVedleggPaaJournalpostRequest;
	private LagreVedleggPaaJournalpostResponse lagreVedleggPaaJournalpostResponse;

	@Before
	public void init() {
		RequestContextSetter.setRequestContext(new SimpleRequestContext.Builder().componentId(COMPONENT_ID).build());
		when(dokumentinfoRepositoryMock.save(any())).thenReturn(DokumentInfo.builder().dokumentInfoId(DOKUMENT_ID).build());
		
		service.setVedleggDokumentTypeId(VEDLEGG_DOKUMENT_TYPE_ID);
	}

	@Test
	public void shouldThrowExceptionIfRequestIsNull() throws Exception {
		expectedException.expect(ApplicationException.class);
		expectedException.expectMessage("Missing parameter: lagreVedleggPaaJournalpostRequest is null");

		service.lagreVedleggPaaJournalpost(null);
	}

	@Test
	public void shouldThrowExceptionIfJournalpostIdMissingInRequest() throws Exception {
		lagreVedleggPaaJournalpostRequest = new LagreVedleggPaaJournalpostRequest(null, new DokumentInfo(),
				createSporingsMetaData());
		expectedException.expect(ApplicationException.class);
		expectedException.expectMessage("Missing parameter in request: journalpostId");

		service.lagreVedleggPaaJournalpost(lagreVedleggPaaJournalpostRequest);
	}

	@Test
	public void shouldThrowExceptionIfSporingsMetaDataMissingInRequest() throws Exception {
		lagreVedleggPaaJournalpostRequest = new LagreVedleggPaaJournalpostRequest(JOURNALPOST_ID,
				createInputDokumentInfo("test.pdf"), null);
		expectedException.expect(ApplicationException.class);
		expectedException.expectMessage("Missing parameter in request: sporingsMetaData");

		service.lagreVedleggPaaJournalpost(lagreVedleggPaaJournalpostRequest);
	}

	@Test
	public void shouldThrowExceptionIfDokumentInfoMissingInRequest() throws Exception {
		lagreVedleggPaaJournalpostRequest = new LagreVedleggPaaJournalpostRequest(1L, null, createSporingsMetaData());
		expectedException.expect(ApplicationException.class);
		expectedException.expectMessage("Missing parameter in request: dokumentInfo");

		service.lagreVedleggPaaJournalpost(lagreVedleggPaaJournalpostRequest);
	}

	@Test
	public void shouldThrowExceptionIfNoJournalpostIdInDb() throws Exception {
		lagreVedleggPaaJournalpostRequest = new LagreVedleggPaaJournalpostRequest(JOURNALPOST_ID, new DokumentInfo(),
				createSporingsMetaData());
		expectedException.expect(NoJournalpostFoundException.class);
		expectedException.expectMessage(containsString("Journalpost with id: " + JOURNALPOST_ID + " does not exist"));
		when(joarkRepositoryMock.findById(eq(JOURNALPOST_ID))).thenReturn(Optional.empty());

		service.lagreVedleggPaaJournalpost(lagreVedleggPaaJournalpostRequest);
	}

	@Test
	public void shouldThrowExceptionIfDuplicateDokumentVariants() throws Exception {
		lagreVedleggPaaJournalpostRequest = new LagreVedleggPaaJournalpostRequest(JOURNALPOST_ID,
				createDokumentInfoWithDuplicateDokumentVariant(), createSporingsMetaData());
		expectedException.expect(InvalidJournalpostStructureException.class);
		expectedException.expectMessage(containsString("cannot contain dokumentvariant duplicates"));
		when(joarkRepositoryMock.findById(eq(JOURNALPOST_ID))).thenReturn(Optional.of(createJournalpostWithHoveddokument()));

		service.lagreVedleggPaaJournalpost(lagreVedleggPaaJournalpostRequest);
	}

	@Test
	public void shouldThrowExceptionIfFilTypeMissingInDokumentInnhold() throws Exception {
		lagreVedleggPaaJournalpostRequest = new LagreVedleggPaaJournalpostRequest(JOURNALPOST_ID,
				createInputDokumentInfoWithMissingFiltype(), createSporingsMetaData());
		when(joarkRepositoryMock.findById(eq(JOURNALPOST_ID))).thenReturn(Optional.of(createJournalpostWithHoveddokument()));

		expectedException.expect(ApplicationException.class);
		expectedException.expectMessage("Filtype is missing from Fildetaljer");

		service.lagreVedleggPaaJournalpost(lagreVedleggPaaJournalpostRequest);
	}

	@Test
	public void shouldThrowExceptionIfVariantFormatMissingInDokumentInnhold() throws Exception {
		lagreVedleggPaaJournalpostRequest = new LagreVedleggPaaJournalpostRequest(JOURNALPOST_ID,
				createInputDokumentInfoWithMissingVariantFormat(), createSporingsMetaData());
		when(joarkRepositoryMock.findById(eq(JOURNALPOST_ID))).thenReturn(Optional.of(createJournalpostWithHoveddokument()));

		expectedException.expect(ApplicationException.class);
		expectedException.expectMessage("Variantformat is missing from Fildetaljer");

		service.lagreVedleggPaaJournalpost(lagreVedleggPaaJournalpostRequest);
	}

	@Test
	public void shouldThrowExceptionIfFileContentMissingInDokumentInnhold() throws Exception {
		lagreVedleggPaaJournalpostRequest = new LagreVedleggPaaJournalpostRequest(JOURNALPOST_ID,
				createInputDokumentInfoWithMissingFileContent(), createSporingsMetaData());
		when(joarkRepositoryMock.findById(eq(JOURNALPOST_ID))).thenReturn(Optional.of(createJournalpostWithHoveddokument()));

		expectedException.expect(ApplicationException.class);
		expectedException.expectMessage("Filecontent is missing from Fildetaljer");

		service.lagreVedleggPaaJournalpost(lagreVedleggPaaJournalpostRequest);
	}

	@Test
	public void shouldStoreDokumentInfoAsVedleggOnExistingJournalpost() throws Exception {
		String filnavn = "testStoreVedleggOnExistingJournalpost";
		lagreVedleggPaaJournalpostRequest = new LagreVedleggPaaJournalpostRequest(JOURNALPOST_ID,
				createInputDokumentInfo(filnavn), createSporingsMetaData());
		Journalpost journalpost = createJournalpostWithHoveddokument();
		journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().setOriginalJournalpost(journalpost);
		when(joarkRepositoryMock.findById(eq(JOURNALPOST_ID))).thenReturn(Optional.of(journalpost));

		service.lagreVedleggPaaJournalpost(lagreVedleggPaaJournalpostRequest);

		verifyDokumentInfoAddedAsVedleggOnJournalpost(journalpost, filnavn);
	}
	
	@Test
	public void shouldStoreDokumentInfoBrukeroppgittTittelAsVedleggOnExistingJournalpost() throws Exception {
		String filnavn = "testStoreVedleggOnExistingJournalpost";
		lagreVedleggPaaJournalpostRequest = new LagreVedleggPaaJournalpostRequest(JOURNALPOST_ID,
				createInputDokumentInfoBrukeroppgittTittel(filnavn), createSporingsMetaData());
		Journalpost journalpost = createJournalpostWithHoveddokument();
		journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().setOriginalJournalpost(journalpost);
		when(joarkRepositoryMock.findById(eq(JOURNALPOST_ID))).thenReturn(Optional.of(journalpost));
		
		service.lagreVedleggPaaJournalpost(lagreVedleggPaaJournalpostRequest);
		
		verifyDokumentInfoAddedAsVedleggOnJournalpost(journalpost, filnavn);
		assertThat(joarkRepositoryMock.findById(JOURNALPOST_ID).get().findDokumentInfoById(DOKUMENT_ID).getTittel(), is(BRUKEROPPGITT_TITTEL));
	}

	@Test
	public void shouldReturnJoarkResponseAfterStoringVedlegg() throws Exception {
		DokumentInfo vedlegg = createInputDokumentInfo("filnavn");
		lagreVedleggPaaJournalpostRequest = new LagreVedleggPaaJournalpostRequest(JOURNALPOST_ID, vedlegg,
				createSporingsMetaData());
		Journalpost journalpost = createJournalpostWithHoveddokument();
		journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().setOriginalJournalpost(journalpost);
		when(joarkRepositoryMock.findById(eq(JOURNALPOST_ID))).thenReturn(Optional.of(journalpost));

		lagreVedleggPaaJournalpostResponse = service.lagreVedleggPaaJournalpost(lagreVedleggPaaJournalpostRequest);

		assertThat(lagreVedleggPaaJournalpostResponse.getDokumentId(), is(vedlegg.getDokumentInfoId()));
		assertThat(joarkRepositoryMock.findById(JOURNALPOST_ID).get().findDokumentInfoById(DOKUMENT_ID).getTittel(), is(TITTEL));
	}
	
	@Test
	public void shouldReturnJoarkResponseAfterStoringVedleggBrukerOppgittTittel() throws Exception {
		DokumentInfo vedlegg = createInputDokumentInfoBrukeroppgittTittel("filnavn");
		lagreVedleggPaaJournalpostRequest = new LagreVedleggPaaJournalpostRequest(JOURNALPOST_ID, vedlegg,
				createSporingsMetaData());
		Journalpost journalpost = createJournalpostWithHoveddokument();
		journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().setOriginalJournalpost(journalpost);
		when(joarkRepositoryMock.findById(eq(JOURNALPOST_ID))).thenReturn(Optional.of(journalpost));
		
		lagreVedleggPaaJournalpostResponse = service.lagreVedleggPaaJournalpost(lagreVedleggPaaJournalpostRequest);
		

		assertThat(lagreVedleggPaaJournalpostResponse.getDokumentId(), is(vedlegg.getDokumentInfoId()));
		assertThat(joarkRepositoryMock.findById(JOURNALPOST_ID).get().findDokumentInfoById(DOKUMENT_ID).getTittel(), is(BRUKEROPPGITT_TITTEL));
	}

	private SporingsMetaData createSporingsMetaData() {
		return new SporingsMetaData(SPORING_FORNAVN, SPORING_ETTERNAVN, null);
	}

	private void verifyDokumentInfoAddedAsVedleggOnJournalpost(Journalpost journalpost, String filnavn) {
		Set<JournalpostDokumentInfoRelasjon> vedlegg = journalpost
				.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG);
		assertThat(vedlegg.size(), is(1));
		assertThat(vedlegg.iterator().next().getTilknyttetAvNavn(), is(SPORING_FORNAVN + " " + SPORING_ETTERNAVN));
		for (JournalpostDokumentInfoRelasjon relasjon : vedlegg) {
			FilDetaljer fildetaljer = relasjon.getDokumentInfo().getFildetaljerListe().iterator().next();
			assertThat(fildetaljer.getFilnavn(), is(filnavn));
		}
	}

	private DokumentInfo createInputDokumentInfo(String filnavn) {
		return DokumentInfoBuilder
				.getDokumentInfoBuilder()
				.dokumentInfoId(DOKUMENT_ID)
				.tittel(TITTEL)
				.dokumenttypeId("dokumenttypeId")
				.filDetaljerList(
						FilDetaljerBuilder.getFilDetaljerBuilder().filnavn(filnavn).filtype(FilTypeCode.PDF)
								.variantFormat(VariantFormatCode.ARKIV).fileContent("filinnhold".getBytes()).build())
				.build();
	}
	
	private DokumentInfo createInputDokumentInfoBrukeroppgittTittel(String filnavn) {
		return DokumentInfoBuilder
				.getDokumentInfoBuilder()
				.dokumentInfoId(DOKUMENT_ID)
				.tittel(TITTEL)
				.brukeroppgittTittel(BRUKEROPPGITT_TITTEL)
				.dokumenttypeId("dokumenttypeId")
				.filDetaljerList(
						FilDetaljerBuilder.getFilDetaljerBuilder().filnavn(filnavn).filtype(FilTypeCode.PDF)
								.variantFormat(VariantFormatCode.ARKIV).fileContent("filinnhold".getBytes()).build())
				.build();
	}
	
	private DokumentInfo createInputDokumentInfoWithMissingFiltype() {
		return DokumentInfoBuilder
				.getDokumentInfoBuilder()
				.dokumentInfoId(DOKUMENT_ID)
				.dokumenttypeId("dokumenttypeId")
				.filDetaljerList(
						FilDetaljerBuilder.getFilDetaljerBuilder().filnavn("test.pdf").filtype(null)
								.variantFormat(VariantFormatCode.ARKIV).fileContent("filinnhold".getBytes()).build())
				.build();
	}

	private DokumentInfo createInputDokumentInfoWithMissingVariantFormat() {
		return DokumentInfoBuilder
				.getDokumentInfoBuilder()
				.dokumentInfoId(DOKUMENT_ID)
				.dokumenttypeId("dokumenttypeId")
				.filDetaljerList(
						FilDetaljerBuilder.getFilDetaljerBuilder().filnavn("test.pdf").filtype(FilTypeCode.PDF)
								.variantFormat(null).fileContent("filinnhold".getBytes()).build()).build();
	}

	private DokumentInfo createInputDokumentInfoWithMissingFileContent() {
		return DokumentInfoBuilder
				.getDokumentInfoBuilder()
				.dokumentInfoId(DOKUMENT_ID)
				.dokumenttypeId("dokumenttypeId")
				.filDetaljerList(
						FilDetaljerBuilder.getFilDetaljerBuilder().filnavn("test.pdf").filtype(FilTypeCode.PDF)
								.variantFormat(VariantFormatCode.ARKIV).fileContent(null).build()).build();
	}

	private Journalpost createJournalpostWithHoveddokument() {
		return getJournalpostBuilder()
				.journalpostId(JOURNALPOST_ID)
				.brukere(BrukerBuilder.getBrukerBuilder().brukerId("").brukerType(BrukerTypeCode.PERSON).build())
				.journalpostType(JournalpostTypeCode.U)
				.journalStatus(JournalStatusCode.OD)
				.dokumentInfoRelasjoner(
						JournalpostDokumentInfoRelasjonBuilder
								.getJournalpostDokumentInfoRelasjonBuilder()
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
								.dokumentInfo(
										DokumentInfoBuilder
												.getDokumentInfoBuilder()
												.filDetaljerList(
														FilDetaljerBuilder.getFilDetaljerBuilder().filnavn("TestFil")
																.filtype(FilTypeCode.PDF)
																.fileContent("hoveddokument".getBytes())
																.variantFormat(VariantFormatCode.ARKIV).build())
												.build()).build()).build();
	}

	private DokumentInfo createDokumentInfoWithDuplicateDokumentVariant() {
		return DokumentInfoBuilder
				.getDokumentInfoBuilder()
				.dokumenttypeId("dokumenttypeId")
				.filDetaljerList(
						FilDetaljerBuilder.getFilDetaljerBuilder().filtype(FilTypeCode.PDF)
								.variantFormat(VariantFormatCode.PRODUKSJON).fileContent("content".getBytes()).build(),
						FilDetaljerBuilder.getFilDetaljerBuilder().filtype(FilTypeCode.PDF)
								.variantFormat(VariantFormatCode.PRODUKSJON).fileContent("content".getBytes()).build())
				.build();
	}
}
