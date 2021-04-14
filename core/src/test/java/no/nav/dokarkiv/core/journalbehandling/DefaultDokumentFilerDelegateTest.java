package no.nav.dokarkiv.core.journalbehandling;

import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentFil;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.pdfValidation.PdfValidatorResponseToGrafana;
import no.nav.dokarkiv.core.repository.DokumentFilRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;

import java.util.List;

import static no.nav.dokarkiv.core.domain.builder.DokumentFilBuilder.getDokumentFilBuilder;
import static no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder.getDokumentInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder.getFilDetaljerBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for DefaultDokumentFilerDelegate.
 *
 * @author Thomas Eugen Bjørge, Visma Sirius
 */
public class DefaultDokumentFilerDelegateTest {

	@Mock
	private DokumentFilRepository dokumentFilRepositoryMock;

	@InjectMocks
	private DefaultDokumentFilerDelegate dokumentFilerDelegate;

	@Captor
	ArgumentCaptor<DokumentFil> dokumentFilCaptor;

	private final byte[] fileContent = "fileContent".getBytes();
	private Journalpost journalpost;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		dokumentFilerDelegate = new DefaultDokumentFilerDelegate();
		dokumentFilerDelegate.setDokumentFilRepository(dokumentFilRepositoryMock);
	}

	@Test
	public void shouldSaveNewDokumentFil() throws Exception {
		FilDetaljer filDetaljer = getFilDetaljerBuilder()
				.fileContent(fileContent)
				.build();

		journalpost = getJournalpostBuilder()
				.dokumentInfoRelasjoner(createDokumentInfoRelasjonWith(filDetaljer))
				.build();


		dokumentFilerDelegate.saveUpdateDokumentFiler(journalpost);

		verify(dokumentFilRepositoryMock).save(dokumentFilCaptor.capture());

		assertThat(dokumentFilCaptor.getValue().getFil(), is(fileContent));
	}

	@Test
	public void shouldUpdateExistingDokumentFil() throws Exception {
		FilDetaljer filDetaljer = createFilDetaljer();

		journalpost = getJournalpostBuilder()
				.journalpostType(JournalpostTypeCode.U)
				.journalStatus(JournalStatusCode.D)
				.dokumentInfoRelasjoner(createDokumentInfoRelasjonWith(filDetaljer))
				.build();

		DokumentFil dokumentFil = getDokumentFilBuilder()
				.filUuid(filDetaljer.getFilUuid())
				.fil("Test".getBytes())
				.build();
		when(dokumentFilRepositoryMock.findByFilUuid(filDetaljer.getFilUuid())).thenReturn(dokumentFil);

		dokumentFilerDelegate.saveUpdateDokumentFiler(journalpost);

		assertThat(dokumentFil.getFil(), is(fileContent));
		assertThat(dokumentFil.getEndretKildeNavn(), is(filDetaljer.getEndretKildeNavn()));
		assertThat(filDetaljer.getFilstorrelse(), is(String.valueOf(fileContent.length)));
	}

	@Test
	public void shouldSaveNewDokumentFilWhenExistingDokumentFilNotFound() throws Exception {
		FilDetaljer filDetaljer = createFilDetaljer();

		journalpost = getJournalpostBuilder()
				.journalpostId(231341412L)
				.journalpostType(JournalpostTypeCode.U)
				.journalStatus(JournalStatusCode.D)
				.dokumentInfoRelasjoner(createDokumentInfoRelasjonWith(filDetaljer))
				.build();

		dokumentFilerDelegate.saveUpdateDokumentFiler(journalpost);

		verify(dokumentFilRepositoryMock).save(dokumentFilCaptor.capture());
		assertThat(dokumentFilCaptor.getValue().getFil(), is(fileContent));
	}

	@Test
	public void shouldSaveValidateNewDokumentFil() throws Exception {
		FilDetaljer filDetaljer = getFilDetaljerBuilder()
				.fileContent(fileContent)
				.filUuid("filUuid123")
				.opprettetKildeNavn("SrvEnEllerAnnen")
				.filtype(FilTypeCode.PDFA)
				.build();

		journalpost = getJournalpostBuilder()
				.dokumentInfoRelasjoner(createDokumentInfoRelasjonWith(filDetaljer))
				.build();


		List<PdfValidatorResponseToGrafana> response = dokumentFilerDelegate.saveUpdateValidateDokumentFiler(journalpost);
		assertThat(response.size(), is(1));

		verify(dokumentFilRepositoryMock).save(dokumentFilCaptor.capture());

		assertThat(dokumentFilCaptor.getValue().getFil(), is(fileContent));
	}

	@Test
	public void shouldUpdateValidateExistingDokumentFiler() throws Exception {
		FilDetaljer filDetaljer = createFilDetaljerPDFA();

		journalpost = getJournalpostBuilder()
				.journalpostType(JournalpostTypeCode.U)
				.journalStatus(JournalStatusCode.D)
				.dokumentInfoRelasjoner(createDokumentInfoRelasjonWith(filDetaljer))
				.build();

		DokumentFil dokumentFil = getDokumentFilBuilder()
				.filUuid(filDetaljer.getFilUuid())
				.fil("Test".getBytes())
				.build();
		when(dokumentFilRepositoryMock.findByFilUuid(filDetaljer.getFilUuid())).thenReturn(dokumentFil);

		List<PdfValidatorResponseToGrafana> response = dokumentFilerDelegate.saveUpdateValidateDokumentFiler(journalpost);
		assertThat(response.size(), is(1));

		assertThat(dokumentFil.getFil(), is(fileContent));
		assertThat(dokumentFil.getEndretKildeNavn(), is(filDetaljer.getEndretKildeNavn()));
		assertThat(filDetaljer.getFilstorrelse(), is(String.valueOf(fileContent.length)));
	}

	@Test
	public void shouldSaveValidateNewDokumentFilWhenExistingDokumentFilNotFound() throws Exception {
		FilDetaljer filDetaljer = createFilDetaljerPDFA();

		journalpost = getJournalpostBuilder()
				.journalpostId(231341412L)
				.journalpostType(JournalpostTypeCode.U)
				.journalStatus(JournalStatusCode.D)
				.dokumentInfoRelasjoner(createDokumentInfoRelasjonWith(filDetaljer))
				.build();

		List<PdfValidatorResponseToGrafana> response =dokumentFilerDelegate.saveUpdateValidateDokumentFiler(journalpost);
		assertThat(response.size(), is(1));

		verify(dokumentFilRepositoryMock).save(dokumentFilCaptor.capture());
		assertThat(dokumentFilCaptor.getValue().getFil(), is(fileContent));
	}

	private FilDetaljer createFilDetaljer() {
		return getFilDetaljerBuilder()
				.fildetaljerId(99L)
				.fileContent(fileContent)
				.endretKildeNavn("Test")
				.build();
	}

	private FilDetaljer createFilDetaljerPDFA() {
		return getFilDetaljerBuilder()
				.fildetaljerId(99L)
				.fileContent(fileContent)
				.endretKildeNavn("Test")
				.filtype(FilTypeCode.PDFA)
				.build();
	}

	private JournalpostDokumentInfoRelasjon createDokumentInfoRelasjonWith(FilDetaljer filDetaljer) {
		return getJournalpostDokumentInfoRelasjonBuilder()
				.dokumentInfo(getDokumentInfoBuilder()
						.dokumentstatus(DokumentStatusCode.UNDER_REDIGERING)
						.filDetaljerList(filDetaljer)
						.build())
				.build();
	}

}