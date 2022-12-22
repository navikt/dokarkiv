package no.nav.dokarkiv.behandlejournal.v3.tjoark061;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Test class for DefaultLagreVedleggPaaJournalpost
 *
 * @author Rune Romundstad, Visma Consulting
 */
@ExtendWith(MockitoExtension.class)
public class DefaultLagreVedleggPaaJournalpostV3Test {
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
	private DefaultLagreVedleggPaaJournalpostV3 service;

	private LagreVedleggPaaJournalpostRequest lagreVedleggPaaJournalpostRequest;
	private LagreVedleggPaaJournalpostResponse lagreVedleggPaaJournalpostResponse;

	@BeforeEach
	public void init() {
		RequestContextSetter.setRequestContext(new SimpleRequestContext.Builder().componentId(COMPONENT_ID).build());
		lenient().when(dokumentinfoRepositoryMock.persist(any())).thenReturn(DokumentInfo.builder().dokumentInfoId(DOKUMENT_ID).build());
		service.setVedleggDokumentTypeId(VEDLEGG_DOKUMENT_TYPE_ID);
	}

	@Test
	public void shouldThrowExceptionIfRequestIsNull() {
		assertThrows(ApplicationException.class,
				() -> service.lagreVedleggPaaJournalpost(null),
				"Missing parameter: lagreVedleggPaaJournalpostRequest is null");
	}

	@Test
	public void shouldThrowExceptionIfJournalpostIdMissingInRequest() {
		lagreVedleggPaaJournalpostRequest = new LagreVedleggPaaJournalpostRequest(null, new DokumentInfo(),
				createSporingsMetaData());

		assertThrows(ApplicationException.class,
				() -> service.lagreVedleggPaaJournalpost(lagreVedleggPaaJournalpostRequest),
				"Missing parameter in request: journalpostId");
	}

	@Test
	public void shouldThrowExceptionIfSporingsMetaDataMissingInRequest() {
		lagreVedleggPaaJournalpostRequest = new LagreVedleggPaaJournalpostRequest(JOURNALPOST_ID,
				createInputDokumentInfo("test.pdf"), null);

		assertThrows(ApplicationException.class,
				() -> service.lagreVedleggPaaJournalpost(lagreVedleggPaaJournalpostRequest),
				"Missing parameter in request: sporingsMetaData");
	}

	@Test
	public void shouldThrowExceptionIfDokumentInfoMissingInRequest() {
		lagreVedleggPaaJournalpostRequest = new LagreVedleggPaaJournalpostRequest(1L, null, createSporingsMetaData());

		assertThrows(ApplicationException.class,
				() -> service.lagreVedleggPaaJournalpost(lagreVedleggPaaJournalpostRequest),
				"Missing parameter in request: dokumentInfo");
	}

	@Test
	public void shouldThrowExceptionIfNoJournalpostIdInDb() {
		lagreVedleggPaaJournalpostRequest = new LagreVedleggPaaJournalpostRequest(JOURNALPOST_ID, new DokumentInfo(),
				createSporingsMetaData());
		when(joarkRepositoryMock.findById(eq(JOURNALPOST_ID))).thenReturn(Optional.empty());

		assertThrows(NoJournalpostFoundException.class,
				() -> service.lagreVedleggPaaJournalpost(lagreVedleggPaaJournalpostRequest),
				"Journalpost with id: " + JOURNALPOST_ID + " does not exist");
	}

	@Test
	public void shouldThrowExceptionIfDuplicateDokumentVariants() {
		lagreVedleggPaaJournalpostRequest = new LagreVedleggPaaJournalpostRequest(JOURNALPOST_ID,
				createDokumentInfoWithDuplicateDokumentVariant(), createSporingsMetaData());
		when(joarkRepositoryMock.findById(eq(JOURNALPOST_ID))).thenReturn(Optional.of(createJournalpostWithHoveddokument()));

		assertThrows(InvalidJournalpostStructureException.class,
				() -> service.lagreVedleggPaaJournalpost(lagreVedleggPaaJournalpostRequest),
				"cannot contain dokumentvariant duplicates");
	}

	@Test
	public void shouldThrowExceptionIfFilTypeMissingInDokumentInnhold() {
		lagreVedleggPaaJournalpostRequest = new LagreVedleggPaaJournalpostRequest(JOURNALPOST_ID,
				createInputDokumentInfoWithMissingFiltype(), createSporingsMetaData());
		when(joarkRepositoryMock.findById(eq(JOURNALPOST_ID))).thenReturn(Optional.of(createJournalpostWithHoveddokument()));

		assertThrows(ApplicationException.class,
				() -> service.lagreVedleggPaaJournalpost(lagreVedleggPaaJournalpostRequest),
				"Filtype is missing from Fildetaljer");
	}

	@Test
	public void shouldThrowExceptionIfVariantFormatMissingInDokumentInnhold() {
		lagreVedleggPaaJournalpostRequest = new LagreVedleggPaaJournalpostRequest(JOURNALPOST_ID,
				createInputDokumentInfoWithMissingVariantFormat(), createSporingsMetaData());
		when(joarkRepositoryMock.findById(eq(JOURNALPOST_ID))).thenReturn(Optional.of(createJournalpostWithHoveddokument()));

		assertThrows(ApplicationException.class,
				() -> service.lagreVedleggPaaJournalpost(lagreVedleggPaaJournalpostRequest),
				"Variantformat is missing from Fildetaljer");
	}

	@Test
	public void shouldThrowExceptionIfFileContentMissingInDokumentInnhold() {
		lagreVedleggPaaJournalpostRequest = new LagreVedleggPaaJournalpostRequest(JOURNALPOST_ID,
				createInputDokumentInfoWithMissingFileContent(), createSporingsMetaData());
		when(joarkRepositoryMock.findById(eq(JOURNALPOST_ID))).thenReturn(Optional.of(createJournalpostWithHoveddokument()));

		assertThrows(ApplicationException.class,
				() -> service.lagreVedleggPaaJournalpost(lagreVedleggPaaJournalpostRequest),
				"Filecontent is missing from Fildetaljer");
	}

	@Test
	public void shouldStoreDokumentInfoAsVedleggOnExistingJournalpost() {
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
	public void shouldStoreDokumentInfoBrukeroppgittTittelAsVedleggOnExistingJournalpost() {
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
	public void shouldReturnJoarkResponseAfterStoringVedlegg() {
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
	public void shouldReturnJoarkResponseAfterStoringVedleggBrukerOppgittTittel() {
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
