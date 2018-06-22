package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark107;


import static no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder.getDokumentInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.NoJournalpostFoundException;
import no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.repository.DokumentFilRepository;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.core.sporing.SporingPopulator;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

/**
 * Test of {@link DefaultFjernFerdigstiltDokumentService}
 *
 * @author Stig Strøm
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultFjernFerdigstiltDokumentServiceTest {
	private static final Long JOURNALPOST_ID = 42L;
	private static final Long DOKUMENTINFO_ID = 1L;
	private static final String ENDRET_AV_NAVN = "endret_av";
	private FjernFerdigstiltDokumentRequestTo request = new FjernFerdigstiltDokumentRequestTo(JOURNALPOST_ID, DOKUMENTINFO_ID,
			ENDRET_AV_NAVN);

	@Mock
	private FjernFerdigstiltDokumentValidator validator;

	@Mock
	private SporingPopulator sporingPopulator;

	@Mock
	private JoarkRepository joarkRepository;

	@Mock
	private DokumentFilRepository dokumentFilRepository;

	@InjectMocks
	private DefaultFjernFerdigstiltDokumentService service;
	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void shouldRunOk() throws Exception {
		Journalpost journalpost = testJournalpost();
		when(joarkRepository.findById(JOURNALPOST_ID)).thenReturn(Optional.of(journalpost));
		service.fjernFerdigstiltDokument(request);

		verify(validator).validateInputRequest(request);
		verify(validator).validate(journalpost, request);
		verify(joarkRepository).findById(JOURNALPOST_ID);
		verify(sporingPopulator).populateSporingInfo(journalpost, ENDRET_AV_NAVN);
		verify(dokumentFilRepository).deleteByFilUuid("filuid");
	}


	@Test
	public void shouldThrowException_cannotFindJournalpost() throws Exception {
		expectedException.expect(NoJournalpostFoundException.class);
		when(joarkRepository.findById(JOURNALPOST_ID)).thenReturn(Optional.ofNullable(null));
		service.fjernFerdigstiltDokument(request);
	}

	private Journalpost testJournalpost() {
		return getJournalpostBuilder()
				.journalpostId(JOURNALPOST_ID)
				.dokumentInfoRelasjoner(
						getJournalpostDokumentInfoRelasjonBuilder()
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
								.dokumentInfo(
										getDokumentInfoBuilder().
												dokumentInfoId(DOKUMENTINFO_ID)
												.filDetaljerList(
														FilDetaljerBuilder.getFilDetaljerBuilder()
																.filUuid("filuid")
																.variantFormat(VariantFormatCode.ARKIV).build()).build())
								.build())
				.build();
	}

}
