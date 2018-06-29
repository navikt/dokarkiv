package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark108;


import static no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder.getDokumentInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder.getFilDetaljerBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.NoJournalpostFoundException;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.util.DateProvider;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.core.sporing.SporingPopulator;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

/**
 * Test of {@link DefaultFerdigstillJournalpostService}
 *
 * @author Stig Strøm
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultFerdigstillJournalpostServiceTest {
	private static final Long JOURNALPOST_ID = 42L;
	private static final String ENDRET_AV_NAVN = "endret_av";
	private static final UtsendingsKanalCode UTSENDINGS_KANAL = UtsendingsKanalCode.ALTINN;
	private static final String MOCK_DATE = "2018-06-20T14:31:54.767";
	private FerdigstillJournalpostRequestTo request;

	@Mock
	private FerdigstillJournalpostValidator validator;

	@Mock
	private SporingPopulator sporingPopulator;

	@Mock
	private JoarkRepository joarkRepository;

	@InjectMocks
	private DefaultFerdigstillJournalpostService service;
	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Before
	public void setUp() throws Exception {
		request = new FerdigstillJournalpostRequestTo(JOURNALPOST_ID, ENDRET_AV_NAVN, UTSENDINGS_KANAL);
		DateProvider.configure(true, MOCK_DATE);
	}

	@Test
	public void shouldRunOk() throws Exception {
		Journalpost journalpost = testJournalpost(VariantFormatCode.ARKIV);
		when(joarkRepository.findById(JOURNALPOST_ID)).thenReturn(Optional.of(journalpost));

		service.ferdigstillJournalpost(request);

		assertThat(journalpost.getJournalstatus(), is(JournalStatusCode.FS));
		assertThat(journalpost.getJournalDato(), is(LocalDateTime.parse(MOCK_DATE.replace(" ", "T")).toDate()));
		assertThat(journalpost.getUtsendingskanal(), is(UTSENDINGS_KANAL));
		assertThat(journalpost.getJournalfortAvNavn(), is(ENDRET_AV_NAVN));

		verify(validator).validateInputRequest(request);
		verify(validator).validate(journalpost);
		verify(joarkRepository).findById(JOURNALPOST_ID);
		verify(sporingPopulator).populateSporingInfo(journalpost, ENDRET_AV_NAVN);
	}

	@Test
	public void shouldRunOkLokalPrint() throws Exception {
		Journalpost journalpost = testJournalpost(VariantFormatCode.ARKIV);
		when(joarkRepository.findById(JOURNALPOST_ID)).thenReturn(Optional.of(journalpost));

		request.setUtsendingskanal(UtsendingsKanalCode.L);
		service.ferdigstillJournalpost(request);

		assertThat(journalpost.getJournalstatus(), is(JournalStatusCode.FL));
	}

	@Test
	public void shouldRunOkProduksjon() throws Exception {
		Journalpost journalpost = testJournalpost(VariantFormatCode.PRODUKSJON);
		when(joarkRepository.findById(JOURNALPOST_ID)).thenReturn(Optional.of(journalpost));
		service.ferdigstillJournalpost(request);

		for (DokumentInfo dokumentInfo : journalpost.findAllDokumentInfos()) {
			FilDetaljer produksjonFilDetaljer = dokumentInfo.findFilDetaljerByVariantFormat(
					VariantFormatCode.PRODUKSJON);
			assertThat(produksjonFilDetaljer.getMetaforceInstanceId(), nullValue());
		}

		verify(validator).validateInputRequest(request);
		verify(validator).validate(journalpost);
		verify(joarkRepository).findById(JOURNALPOST_ID);
		verify(sporingPopulator).populateSporingInfo(journalpost, ENDRET_AV_NAVN);
	}

	@Test
	public void shouldThrowException_cannotFindJournalpost() throws Exception {
		expectedException.expect(NoJournalpostFoundException.class);
		when(joarkRepository.findById(JOURNALPOST_ID)).thenReturn(Optional.ofNullable(null));
		service.ferdigstillJournalpost(request);
	}

	private Journalpost testJournalpost(VariantFormatCode variantFormat) {
		return getJournalpostBuilder()
				.journalpostId(JOURNALPOST_ID)
				.journalStatus(JournalStatusCode.D)
				.dokumentInfoRelasjoner(
						getJournalpostDokumentInfoRelasjonBuilder()
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
								.dokumentInfo(
										getDokumentInfoBuilder()
												.dokumentstatus(DokumentStatusCode.FERDIGSTILT)
												.filDetaljerList(
														getFilDetaljerBuilder().filtype(FilTypeCode.PDF)
																.fileContent("file".getBytes())
																.metaforceInstanceId(21L)
																.variantFormat(variantFormat).build()).build())
								.build()).build();
	}

}
