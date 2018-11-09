package no.nav.dokarkiv.behandlejournal.v2.tjoark061;

import static no.nav.dokarkiv.core.domain.builder.BidragMellomlagringBuilder.getBidragMellomlagringBuilder;
import static no.nav.dokarkiv.core.domain.builder.BidragMellomlagringDokumentBuilder.getBidragMellomlagringDokumentBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
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
import no.nav.dokarkiv.core.domain.entities.bidrag.BidragMellomlagring;
import no.nav.dokarkiv.core.domain.entities.bidrag.BidragMellomlagringDokument;
import no.nav.dokarkiv.core.domain.entities.bidrag.BidragMellomlagringDokumentType;
import no.nav.dokarkiv.core.domain.entities.bidrag.BidragMellomlagringStatus;
import no.nav.dokarkiv.core.exceptions.ApplicationException;
import no.nav.dokarkiv.core.exceptions.InvalidJournalpostStructureException;
import no.nav.dokarkiv.core.exceptions.NoJournalpostFoundException;
import no.nav.dokarkiv.core.repository.BidragMellomlagringDokumentRepository;
import no.nav.dokarkiv.core.repository.BidragMellomlagringRepository;
import no.nav.dokarkiv.core.repository.DokumentFilRepository;
import no.nav.dokarkiv.core.repository.DokumentinfoRepositoryBegrenset;
import no.nav.dokarkiv.core.repository.JoarkRepositoryBegrenset;
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
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.Optional;
import java.util.Set;

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
	private static final Long BIDRAG_MELLOMLAGRING_ID = 1L;
	private static final Long BIDRAG_JOURNALPOST_ID = ***gammelt_fnr***1L;
	private static final Long VEDLEGG_BIDRAG_MELLOMLAGRING_ID = 1001L;

	@Mock
    private JoarkRepositoryBegrenset joarkRepositoryMock;
	@Mock
    private DokumentinfoRepositoryBegrenset dokumentinfoRepositoryMock;
	@Mock
	private BidragMellomlagringRepository bidragMellomlagringRepository;
	@Mock
	private BidragMellomlagringDokumentRepository bidragMellomlagringDokumentRepositoryMock;
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
		when(bidragMellomlagringDokumentRepositoryMock.save(any()))
				.thenReturn(BidragMellomlagringDokument.builder().bidragMellomlagringDokumentId(VEDLEGG_BIDRAG_MELLOMLAGRING_ID).build());
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
	public void shouldThrowExceptionIfFilTypeMissingInDokumentInnholdForBidragsdokument() throws Exception {
		lagreVedleggPaaJournalpostRequest = new LagreVedleggPaaJournalpostRequest(BIDRAG_JOURNALPOST_ID,
				createInputDokumentInfoWithMissingFiltype(), createSporingsMetaData());

		expectedException.expect(ApplicationException.class);
		expectedException.expectMessage("Filtype is missing from Fildetaljer");

		service.lagreVedleggPaaJournalpost(lagreVedleggPaaJournalpostRequest);
	}

	@Test
	public void shouldThrowExceptionIfVariantFormatMissingInDokumentInnholdForBidragsdokument() throws Exception {
		lagreVedleggPaaJournalpostRequest = new LagreVedleggPaaJournalpostRequest(BIDRAG_JOURNALPOST_ID,
				createInputDokumentInfoWithMissingVariantFormat(), createSporingsMetaData());

		expectedException.expect(ApplicationException.class);
		expectedException.expectMessage("Variantformat is missing from Fildetaljer");

		service.lagreVedleggPaaJournalpost(lagreVedleggPaaJournalpostRequest);
	}

	@Test
	public void shouldThrowExceptionIfFileContentMissingInDokumentInnholdForBidragsdokument() throws Exception {
		lagreVedleggPaaJournalpostRequest = new LagreVedleggPaaJournalpostRequest(BIDRAG_JOURNALPOST_ID,
				createInputDokumentInfoWithMissingFileContent(), createSporingsMetaData());

		expectedException.expect(ApplicationException.class);
		expectedException.expectMessage("Filecontent is missing from Fildetaljer");

		service.lagreVedleggPaaJournalpost(lagreVedleggPaaJournalpostRequest);
	}
	
	@Test
	public void shouldThrowExceptionIfDuplicateDokumentVariantsForBidragsdokument() throws Exception {
		lagreVedleggPaaJournalpostRequest = new LagreVedleggPaaJournalpostRequest(BIDRAG_JOURNALPOST_ID,
				createDokumentInfoWithDuplicateDokumentVariant(), createSporingsMetaData());
		expectedException.expect(InvalidJournalpostStructureException.class);
		expectedException.expectMessage(containsString("cannot contain dokumentvariant duplicates"));

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
	public void shouldThrowExceptionIfNoBidragMellomlagringIdInDb() throws Exception {
		lagreVedleggPaaJournalpostRequest = new LagreVedleggPaaJournalpostRequest(BIDRAG_JOURNALPOST_ID,
				DokumentInfo.builder().build(), createSporingsMetaData());
		expectedException.expect(ApplicationException.class);
		expectedException.expectMessage("BidragMellomlagring with id: " + BIDRAG_MELLOMLAGRING_ID + " does not exist");
		when(bidragMellomlagringRepository.findById(eq(BIDRAG_MELLOMLAGRING_ID))).thenReturn(Optional.empty());

		service.lagreVedleggPaaJournalpost(lagreVedleggPaaJournalpostRequest);
	}

	@Test
	public void shouldStoreDokumentInfoAsVedleggOnExistingBidragMellomlagring() throws Exception {
		lagreVedleggPaaJournalpostRequest = new LagreVedleggPaaJournalpostRequest(BIDRAG_JOURNALPOST_ID,
				createInputDokumentInfo("filnavn"), createSporingsMetaData());
		BidragMellomlagring bidragMellomlagring = createBidragMellomlagringWithHoveddokument();
		when(bidragMellomlagringRepository.findById(BIDRAG_MELLOMLAGRING_ID)).thenReturn(Optional.of(bidragMellomlagring));

		service.lagreVedleggPaaJournalpost(lagreVedleggPaaJournalpostRequest);

		assertThat(bidragMellomlagring.getBidragMellomlagringDokuments().size(), is(2));
	}
	
	@Test
	public void shouldStoreKvitteringAsVedleggKvitteringOnExistingBidragMellomlagring() throws Exception {
		String vedleggBrevkode = VEDLEGG_DOKUMENT_TYPE_ID;
		DokumentInfo dokumentInfo = createInputDokumentInfo("filnavn");
		dokumentInfo.setBrevkode(vedleggBrevkode);
		
		lagreVedleggPaaJournalpostRequest = new LagreVedleggPaaJournalpostRequest(BIDRAG_JOURNALPOST_ID,
				dokumentInfo, createSporingsMetaData());
		BidragMellomlagring bidragMellomlagring = createBidragMellomlagringWithHoveddokument();
		when(bidragMellomlagringRepository.findById(BIDRAG_MELLOMLAGRING_ID)).thenReturn(Optional.of(bidragMellomlagring));
		
		service.lagreVedleggPaaJournalpost(lagreVedleggPaaJournalpostRequest);
		
		assertThatBidragMellomlagringHasKvitteringVedlegg(bidragMellomlagring);
	}

	private void assertThatBidragMellomlagringHasKvitteringVedlegg(BidragMellomlagring bidragMellomlagring) {
		assertTrue(Iterables.any(bidragMellomlagring.getBidragMellomlagringDokuments(),
				new Predicate<BidragMellomlagringDokument>() {
					@Override
					public boolean apply(BidragMellomlagringDokument input) {
						return input.getDokumentType() == BidragMellomlagringDokumentType.VEDLEGG_KVITTERING;
					}
				}));
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


	@Test
	public void shouldReturnBidragResponseAfterStoringVedlegg() throws Exception {
		lagreVedleggPaaJournalpostRequest = new LagreVedleggPaaJournalpostRequest(BIDRAG_JOURNALPOST_ID,
				createInputDokumentInfo("filnavn"), createSporingsMetaData());
		BidragMellomlagring bidragMellomlagring = createBidragMellomlagringWithHoveddokument();
		when(bidragMellomlagringRepository.findById(eq(BIDRAG_MELLOMLAGRING_ID))).thenReturn(Optional.of(bidragMellomlagring));
		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				BidragMellomlagring answerBidragMellomlagring = (BidragMellomlagring) invocation.getArguments()[0];
				for (BidragMellomlagringDokument dokument : answerBidragMellomlagring.getBidragMellomlagringDokuments()) {
					if (dokument.getDokumentType() == BidragMellomlagringDokumentType.VEDLEGG) {
						changeBidragMellomlagringDokumentId(dokument, VEDLEGG_BIDRAG_MELLOMLAGRING_ID);
					}
				}
				return null;
			}
		}).when(bidragMellomlagringRepository).save((BidragMellomlagring) any());

		lagreVedleggPaaJournalpostResponse = service.lagreVedleggPaaJournalpost(lagreVedleggPaaJournalpostRequest);

		assertThat(lagreVedleggPaaJournalpostResponse.getDokumentId(), is(VEDLEGG_BIDRAG_MELLOMLAGRING_ID));
	}

	private void changeBidragMellomlagringDokumentId(BidragMellomlagringDokument object, Long value) throws Throwable {
		Field bidragMellomlagringDokumentIdField = object.getClass().getDeclaredField("bidragMellomlagringDokumentId");
		bidragMellomlagringDokumentIdField.setAccessible(true);
		bidragMellomlagringDokumentIdField.set(object, value);
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

	private BidragMellomlagring createBidragMellomlagringWithHoveddokument() {
		return getBidragMellomlagringBuilder()
				.bidragMellomlagringId(DOKUMENT_ID)
				.avsenderFnr("***gammelt_fnr***")
				.mottattDato(new Date())
				.status(BidragMellomlagringStatus.DOKUMENTOPPLASTING)
				.bidragMellomlagringDokuments(
						getBidragMellomlagringDokumentBuilder().bidragMellomlagringDokumentId(1000L)
								.dokumentType(BidragMellomlagringDokumentType.HOVEDDOKUMENT)
								.dokument("Hoveddokument".getBytes()).build()).build();
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
