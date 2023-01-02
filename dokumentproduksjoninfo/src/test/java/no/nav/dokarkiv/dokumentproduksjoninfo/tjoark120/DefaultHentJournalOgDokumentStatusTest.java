package no.nav.dokarkiv.dokumentproduksjoninfo.tjoark120;

import no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder;
import no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder;
import no.nav.dokarkiv.core.domain.builder.JournalpostBuilder;
import no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.InvalidArgumentException;
import no.nav.dokarkiv.core.exceptions.NoDokumentInfoFoundException;
import no.nav.dokarkiv.core.exceptions.NoJournalpostFoundException;
import no.nav.dokarkiv.core.repository.JournalpostRepositorySkjermet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * Unit tests for DefaultHentJournalOgDokumentStatus.
 *
 * @author Thomas Eugen Bjørge, Visma Consulting
 */
@ExtendWith(MockitoExtension.class)
public class DefaultHentJournalOgDokumentStatusTest {

	private static final long JOURNALPOST_ID = 1L;
	private static final long DOKUMENT_INFO_ID = 1L;
	private static final long METAFORCE_INSTANCE_ID = 9L;
	private static final JournalStatusCode JOURNAL_STATUS = JournalStatusCode.D;
	private static final DokumentStatusCode DOKUMENT_STATUS = DokumentStatusCode.UNDER_REDIGERING;

	@Mock
	private JournalpostRepositorySkjermet journalpostRepositorySkjermetMock;

	@InjectMocks
	private DefaultHentJournalOgDokumentStatus hentJournalOgDokumentStatus;

	private HentJournalOgDokumentStatusRequestTo request;

	@BeforeEach
	public void setUp() {
		request = new HentJournalOgDokumentStatusRequestTo();
		request.setJournalpostId(JOURNALPOST_ID);
		request.setDokumentInfoId(DOKUMENT_INFO_ID);
	}

	@Test
	public void shouldFailValidationWhenRequestIsNull() throws Exception {
		assertThrows(InvalidArgumentException.class,
				() -> hentJournalOgDokumentStatus.hentJournalOgDokumentStatus(null),
				"HentJournalOgDokumentStatusRequestTo is null");
	}

	@Test
	public void shouldFailValidationWhenJournalpostIdIsNull() throws Exception {
		request.setJournalpostId(null);

		assertThrows(InvalidArgumentException.class,
				() -> hentJournalOgDokumentStatus.hentJournalOgDokumentStatus(request),
				"Missing parameter journalpostId");
	}

	@Test
	public void shouldFailValidationWhenJournalpostIdIsZero() throws Exception {
		request.setJournalpostId(0L);

		assertThrows(InvalidArgumentException.class,
				() -> hentJournalOgDokumentStatus.hentJournalOgDokumentStatus(request),
				"Missing parameter journalpostId");
	}

	@Test
	public void shouldThrowExceptionForJournalpostNotFound() throws Exception {
		assertThrows(NoJournalpostFoundException.class,
				() -> hentJournalOgDokumentStatus.hentJournalOgDokumentStatus(request),
				"Journalpost with id: " + JOURNALPOST_ID + " does not exist");
	}

	@Test
	public void shouldThrowExceptionWhenDokumentInfoNotFoundOnJournalpost() throws Exception {
		Journalpost journalpost = createJournalPost();
		DokumentInfo dokumentInfo = new DokumentInfo(10L, 0);
		journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().setDokumentInfo(dokumentInfo);

		when(journalpostRepositorySkjermetMock.findById(JOURNALPOST_ID)).thenReturn(Optional.of(journalpost));

		assertThrows(NoDokumentInfoFoundException.class,
				() -> hentJournalOgDokumentStatus.hentJournalOgDokumentStatus(request),
				"Journalpost, journalpostId=" + JOURNALPOST_ID + ",  has no DokumentInfo with id: " + DOKUMENT_INFO_ID);
	}

	@Test
	public void shouldReturnNullForMetaforceInstanceIdWhenFildetaljerNotFound() throws Exception {
		Journalpost journalpost = createJournalPost();
		journalpost.findAllFilDetaljer().iterator().next().setVariantFormat(VariantFormatCode.ARKIV);

		when(journalpostRepositorySkjermetMock.findById(JOURNALPOST_ID)).thenReturn(Optional.of(journalpost));

		HentJournalOgDokumentStatusResponseTo response = hentJournalOgDokumentStatus.hentJournalOgDokumentStatus(request);

		assertThat(response.getMetaforceInstanceId(), is(nullValue()));
	}

	@Test
	public void shouldReturnNullForMetaforceInstanceIdWhenIdNotFound() throws Exception {
		Journalpost journalpost = createJournalPost();
		journalpost.findAllFilDetaljer().iterator().next().setMetaforceInstanceId(null);

		when(journalpostRepositorySkjermetMock.findById(JOURNALPOST_ID)).thenReturn(Optional.of(journalpost));

		HentJournalOgDokumentStatusResponseTo response = hentJournalOgDokumentStatus.hentJournalOgDokumentStatus(request);

		assertThat(response.getMetaforceInstanceId(), is(nullValue()));
	}

	@Test
	public void shouldReturnJournalStatusDokumentStatusAndMetaforceInstanceId() throws Exception {
		when(journalpostRepositorySkjermetMock.findById(JOURNALPOST_ID)).thenReturn(Optional.of(createJournalPost()));

		HentJournalOgDokumentStatusResponseTo response = hentJournalOgDokumentStatus.hentJournalOgDokumentStatus(request);

		assertThat(response.getJournalStatus(), is(JOURNAL_STATUS));
		assertThat(response.getDokumentStatus(), is(DOKUMENT_STATUS));
		assertThat(response.getMetaforceInstanceId(), is(METAFORCE_INSTANCE_ID));
	}

	@Test
	public void shouldCompleteWhenDokumentInfoIdIsMissing() throws Exception {
		when(journalpostRepositorySkjermetMock.findById(JOURNALPOST_ID)).thenReturn(Optional.of(createJournalPost()));
		request.setDokumentInfoId(0L);

		HentJournalOgDokumentStatusResponseTo response = hentJournalOgDokumentStatus.hentJournalOgDokumentStatus(request);

		assertThat(response.getJournalStatus(), is(JOURNAL_STATUS));
		assertThat(response.getDokumentStatus(), nullValue());
		assertThat(response.getMetaforceInstanceId(), nullValue());
	}

	private Journalpost createJournalPost() {
		return JournalpostBuilder.getJournalpostBuilder()
				.journalpostId(JOURNALPOST_ID)
				.journalStatus(JOURNAL_STATUS)
				.dokumentInfoRelasjoner(JournalpostDokumentInfoRelasjonBuilder
						.getJournalpostDokumentInfoRelasjonBuilder()
						.dokumentInfo(DokumentInfoBuilder.getDokumentInfoBuilder()
								.dokumentInfoId(DOKUMENT_INFO_ID)
								.dokumentstatus(DOKUMENT_STATUS)
								.filDetaljerList(FilDetaljerBuilder.getFilDetaljerBuilder()
										.metaforceInstanceId(METAFORCE_INSTANCE_ID)
										.variantFormat(VariantFormatCode.PRODUKSJON)
										.build())
								.build())
						.build())
				.build();
	}
}
