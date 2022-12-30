package no.nav.dokarkiv.core.journalbehandling;

import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentFil;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.repository.DokumentFilRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static no.nav.dokarkiv.core.domain.builder.DokumentFilBuilder.getDokumentFilBuilder;
import static no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder.getDokumentInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder.getFilDetaljerBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DefaultDokumentFilerDelegateTest {

	@Mock
	private DokumentFilRepository dokumentFilRepositoryMock;

	@InjectMocks
	private DefaultDokumentFilerDelegate dokumentFilerDelegate;

	@Captor
	ArgumentCaptor<DokumentFil> dokumentFilCaptor;

	private final byte[] fileContent = "fileContent".getBytes();
	private Journalpost journalpost;

	@Test
	public void shouldSaveNewDokumentFil() {
		FilDetaljer filDetaljer = getFilDetaljerBuilder()
				.fileContent(fileContent)
				.build();

		journalpost = getJournalpostBuilder()
				.dokumentInfoRelasjoner(createDokumentInfoRelasjonWith(filDetaljer))
				.build();

		dokumentFilerDelegate.saveUpdateDokumentFiler(journalpost);

		verify(dokumentFilRepositoryMock).persist(dokumentFilCaptor.capture());

		assertThat(dokumentFilCaptor.getValue().getFil(), is(fileContent));
	}

	@Test
	public void shouldUpdateExistingDokumentFil() {
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
	public void shouldSaveNewDokumentFilWhenExistingDokumentFilNotFound() {
		FilDetaljer filDetaljer = createFilDetaljer();

		journalpost = getJournalpostBuilder()
				.journalpostType(JournalpostTypeCode.U)
				.journalStatus(JournalStatusCode.D)
				.dokumentInfoRelasjoner(createDokumentInfoRelasjonWith(filDetaljer))
				.build();

		dokumentFilerDelegate.saveUpdateDokumentFiler(journalpost);

		verify(dokumentFilRepositoryMock).persist(dokumentFilCaptor.capture());
		assertThat(dokumentFilCaptor.getValue().getFil(), is(fileContent));
	}

	private FilDetaljer createFilDetaljer() {
		return getFilDetaljerBuilder()
				.fildetaljerId(99L)
				.fileContent(fileContent)
				.endretKildeNavn("Test")
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