package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark108;


import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.NoJournalpostFoundException;
import no.nav.dokarkiv.core.repository.JournalpostRepositorySkjermet;
import no.nav.dokarkiv.core.sporing.SporingPopulator;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Stream;

import static java.time.temporal.ChronoUnit.SECONDS;
import static no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder.getDokumentInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder.getFilDetaljerBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static org.assertj.core.api.Assertions.within;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test of {@link DefaultFerdigstillJournalpostService}
 */
@ExtendWith(MockitoExtension.class)
public class DefaultFerdigstillJournalpostServiceTest {
	private static final Long JOURNALPOST_ID = 42L;
	private static final String ENDRET_AV_NAVN = "endret_av";
	private static final UtsendingsKanalCode UTSENDINGS_KANAL = UtsendingsKanalCode.EESSI;
	private FerdigstillJournalpostRequestTo request;

	private FerdigstillJournalpostValidator validator = spy(DefaultFerdigstillJournalpostValidator.class);

	@Mock
	private SporingPopulator sporingPopulator;

	@Mock
	private JournalpostRepositorySkjermet journalpostRepositorySkjermetMock;

	private DefaultFerdigstillJournalpostService service;

	@BeforeEach
	public void setUp() {
		service = new DefaultFerdigstillJournalpostService(journalpostRepositorySkjermetMock, validator, sporingPopulator);
		request = new FerdigstillJournalpostRequestTo(JOURNALPOST_ID, ENDRET_AV_NAVN, UTSENDINGS_KANAL);
	}

	@Test
	public void shouldRunOk() {
		Journalpost journalpost = testJournalpost(VariantFormatCode.ARKIV);
		when(journalpostRepositorySkjermetMock.findById(JOURNALPOST_ID)).thenReturn(Optional.of(journalpost));

		service.ferdigstillJournalpost(request);

		assertThat(journalpost.getJournalstatus(), is(JournalStatusCode.FS));
		Assertions.assertThat(journalpost.getJournalDato()).isCloseTo(LocalDateTime.now(), within(3, SECONDS));
		assertThat(journalpost.getUtsendingskanal(), is(UTSENDINGS_KANAL));
		assertThat(journalpost.getJournalfortAvNavn(), is(ENDRET_AV_NAVN));

		verify(validator).validateInputRequest(request);
		verify(validator).validate(journalpost);
		verify(journalpostRepositorySkjermetMock).findById(JOURNALPOST_ID);
		verify(sporingPopulator).populateSporingInfo(journalpost, ENDRET_AV_NAVN);
	}

	@Test
	public void shouldRunOkLokalPrint() {
		Journalpost journalpost = testJournalpost(VariantFormatCode.ARKIV);
		when(journalpostRepositorySkjermetMock.findById(JOURNALPOST_ID)).thenReturn(Optional.of(journalpost));

		request.setUtsendingskanal(UtsendingsKanalCode.L);
		service.ferdigstillJournalpost(request);

		assertThat(journalpost.getJournalstatus(), is(JournalStatusCode.FL));
	}

	@Test
	public void shouldRunOkProduksjon() {
		Journalpost journalpost = testJournalpost(VariantFormatCode.ARKIV, VariantFormatCode.PRODUKSJON);
		when(journalpostRepositorySkjermetMock.findById(JOURNALPOST_ID)).thenReturn(Optional.of(journalpost));
		service.ferdigstillJournalpost(request);

		for (DokumentInfo dokumentInfo : journalpost.findAllDokumentInfos()) {
			FilDetaljer produksjonFilDetaljer = dokumentInfo.findFilDetaljerByVariantFormat(
					VariantFormatCode.PRODUKSJON);
			assertThat(produksjonFilDetaljer.getMetaforceInstanceId(), nullValue());
		}

		verify(validator).validateInputRequest(request);
		verify(validator).validate(journalpost);
		verify(journalpostRepositorySkjermetMock).findById(JOURNALPOST_ID);
		verify(sporingPopulator).populateSporingInfo(journalpost, ENDRET_AV_NAVN);
	}

	@Test
	public void shouldThrowExceptionCannotFindJournalpost() {
		when(journalpostRepositorySkjermetMock.findById(JOURNALPOST_ID)).thenReturn(Optional.empty());

		assertThrows(NoJournalpostFoundException.class,
				() -> service.ferdigstillJournalpost(request));
	}

	private Journalpost testJournalpost(VariantFormatCode... variantFormat) {
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
														Stream.of(variantFormat)
																.map(variantFormatCode ->
																		getFilDetaljerBuilder().filtype(FilTypeCode.PDF)
																				.fileContent("file".getBytes())
																				.metaforceInstanceId(21L)
																				.variantFormat(variantFormatCode).build())
																.toArray(FilDetaljer[]::new))
												.build())
								.build()).build();
	}

}
