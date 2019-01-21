package no.nav.dokarkiv.dokumentproduksjoninfo.tjoark120;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

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
import no.nav.dokarkiv.core.repository.JoarkRepositorySkjermet;
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
 * Unit tests for DefaultHentJournalOgDokumentStatus.
 *
 * @author Thomas Eugen Bjørge, Visma Consulting
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultHentJournalOgDokumentStatusTest {

	private static final long JOURNALPOST_ID = 1L;
	private static final long DOKUMENT_INFO_ID = 1L;
	private static final long METAFORCE_INSTANCE_ID = 9L;
	private static final JournalStatusCode JOURNAL_STATUS = JournalStatusCode.D;
	private static final DokumentStatusCode DOKUMENT_STATUS = DokumentStatusCode.UNDER_REDIGERING;
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	@Mock
    private JoarkRepositorySkjermet joarkRepositoryMock;

	@InjectMocks
	private DefaultHentJournalOgDokumentStatus hentJournalOgDokumentStatus;	
	
	private HentJournalOgDokumentStatusRequestTo request;
	
	@Before
	public void setUp() {
		request = new HentJournalOgDokumentStatusRequestTo();
		request.setJournalpostId(JOURNALPOST_ID);
		request.setDokumentInfoId(DOKUMENT_INFO_ID);
	}
	
	@Test
	public void shouldFailValidationWhenRequestIsNull() throws Exception {
		expectedException.expect(InvalidArgumentException.class);
		expectedException.expectMessage("HentJournalOgDokumentStatusRequestTo is null");
		
		hentJournalOgDokumentStatus.hentJournalOgDokumentStatus(null);
	}
	
	@Test
	public void shouldFailValidationWhenJournalpostIdIsNull() throws Exception {
		expectedException.expect(InvalidArgumentException.class);
		expectedException.expectMessage("Missing parameter journalpostId");
		request.setJournalpostId(null);
		
		hentJournalOgDokumentStatus.hentJournalOgDokumentStatus(request);
	}

	@Test
	public void shouldFailValidationWhenJournalpostIdIsZero() throws Exception {
		expectedException.expect(InvalidArgumentException.class);
		expectedException.expectMessage("Missing parameter journalpostId");
		request.setJournalpostId(0L);
		
		hentJournalOgDokumentStatus.hentJournalOgDokumentStatus(request);
	}

	@Test
	public void shouldThrowExceptionForJournalpostNotFound() throws Exception {
		expectedException.expect(NoJournalpostFoundException.class);
		expectedException.expectMessage("Journalpost with id: " + JOURNALPOST_ID + " does not exist");
		
		hentJournalOgDokumentStatus.hentJournalOgDokumentStatus(request);
	}
	
	@Test
	public void shouldThrowExceptionWhenDokumentInfoNotFoundOnJournalpost() throws Exception {
		Journalpost journalpost = createJournalPost();
		DokumentInfo dokumentInfo = new DokumentInfo(10L, 0);
		journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().setDokumentInfo(dokumentInfo);
		
		when(joarkRepositoryMock.findById(JOURNALPOST_ID)).thenReturn(Optional.of(journalpost));
		
		expectedException.expect(NoDokumentInfoFoundException.class);
		expectedException.expectMessage("Journalpost, journalpostId="+JOURNALPOST_ID+",  has no DokumentInfo with id: " + DOKUMENT_INFO_ID);
		
		hentJournalOgDokumentStatus.hentJournalOgDokumentStatus(request);
	}
	
	@Test
	public void shouldReturnNullForMetaforceInstanceIdWhenFildetaljerNotFound() throws Exception {
		Journalpost journalpost = createJournalPost();
		journalpost.findAllFilDetaljer().iterator().next().setVariantFormat(VariantFormatCode.ARKIV);

		when(joarkRepositoryMock.findById(JOURNALPOST_ID)).thenReturn(Optional.of(journalpost));
		
		HentJournalOgDokumentStatusResponseTo response = hentJournalOgDokumentStatus.hentJournalOgDokumentStatus(request);
		
		assertThat(response.getMetaforceInstanceId(), is(nullValue()));
	}
	
	@Test
	public void shouldReturnNullForMetaforceInstanceIdWhenIdNotFound() throws Exception {
		Journalpost journalpost = createJournalPost();
		journalpost.findAllFilDetaljer().iterator().next().setMetaforceInstanceId(null);

		when(joarkRepositoryMock.findById(JOURNALPOST_ID)).thenReturn(Optional.of(journalpost));
		
		HentJournalOgDokumentStatusResponseTo response = hentJournalOgDokumentStatus.hentJournalOgDokumentStatus(request);
		
		assertThat(response.getMetaforceInstanceId(), is(nullValue()));
	}
	
	@Test
	public void shouldReturnJournalStatusDokumentStatusAndMetaforceInstanceId() throws Exception {
		when(joarkRepositoryMock.findById(JOURNALPOST_ID)).thenReturn(Optional.of(createJournalPost()));
		
		HentJournalOgDokumentStatusResponseTo response = hentJournalOgDokumentStatus.hentJournalOgDokumentStatus(request);
		
		assertThat(response.getJournalStatus(), is(JOURNAL_STATUS));
		assertThat(response.getDokumentStatus(), is(DOKUMENT_STATUS));
		assertThat(response.getMetaforceInstanceId(), is(METAFORCE_INSTANCE_ID));
	}

	@Test
	public void shouldCompleteWhenDokumentInfoIdIsMissing() throws Exception {
		when(joarkRepositoryMock.findById(JOURNALPOST_ID)).thenReturn(Optional.of(createJournalPost()));
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
