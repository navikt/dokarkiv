package no.nav.dokarkiv.journal.v3.tjoark051;

import static no.nav.dokarkiv.core.domain.builder.DokumentFilBuilder.getDokumentFilBuilder;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import no.nav.dokarkiv.core.dokumenturl.HentDokumentUrlResponse;
import no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder;
import no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder;
import no.nav.dokarkiv.core.domain.builder.JournalpostBuilder;
import no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.OnDemandInstansCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentFil;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.InvalidArgumentException;
import no.nav.dokarkiv.core.exceptions.InvalidFilUuidException;
import no.nav.dokarkiv.core.exceptions.NoDokumentInfoFoundException;
import no.nav.dokarkiv.core.exceptions.NoJournalpostFoundException;
import no.nav.dokarkiv.core.ondemand.HentOndemandDokument;
import no.nav.dokarkiv.core.repository.DokumentFilRepository;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.core.exceptions.DocumentNotFoundException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

/**
 * Unit tests for HentDokumentService
 *
 * @author Stig Strøm
 */
@RunWith(MockitoJUnitRunner.class)
public class Tjoark051HentDokumentServiceTest {

	private static final long JOURNALPOST_ID = 1L;
	private static final long DOKUMENT_INFO_ID = 1L;
	private static final VariantFormatCode VARIANT_FORMAT = VariantFormatCode.ARKIV;
	private static final String FIL_UUID = "456b166e-5f9f-430f-8e35-09a732156562";

	private static final OnDemandInstansCode ON_DEMAND_INSTANS = OnDemandInstansCode.PESYS;
	private static final String ON_DEMAND_ID = "onDemandId";
	private static final byte[] BYTES = "fil".getBytes();
	private static final String DOKUMENTURL = "http://hentdokument";

	private HentDokumentRequestTo request = new HentDokumentRequestTo(JOURNALPOST_ID, DOKUMENT_INFO_ID, VARIANT_FORMAT);

	@Mock
	private JoarkRepository joarkRepositoryMock;
	@Mock
	private DokumentFilRepository dokumentFilRepository;
	@Mock
	private HentOndemandDokument hentOndemandDokument;

	@InjectMocks
	private Tjoark051HentDokumentService service;

	@Test
	public void shouldThrowExceptionForJournalpostNotFound() throws Exception {
		try {
			service.hentDokument(request);
			fail("Expected exception");
		} catch (DocumentNotFoundException e) {
			assertThat(e.getCause(), is(instanceOf(NoJournalpostFoundException.class)));
		}
	}

	@Test
	public void shouldThrowExceptionWhenDokumentInfoNotFoundOnJournalpost() throws Exception {
		Journalpost journalpost = createJournalPost();
		DokumentInfo dokumentInfo = new DokumentInfo(10L, 0);
		journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().setDokumentInfo(dokumentInfo);

		when(joarkRepositoryMock.findById(JOURNALPOST_ID)).thenReturn(Optional.of(journalpost));

		try {
			service.hentDokument(request);
			fail("Expected exception");
		} catch (DocumentNotFoundException e) {
			assertThat(e.getCause(), is(instanceOf(NoDokumentInfoFoundException.class)));
		}
	}

	@Test
	public void shouldThrowExceptionWhenFilDetaljerWithGivenVariantNotFound() throws Exception {
		Journalpost journalpost = createJournalPost();
		journalpost.findFilDetaljerByFilUuid(FIL_UUID).setVariantFormat(VariantFormatCode.PRODUKSJON);

		when(joarkRepositoryMock.findById(JOURNALPOST_ID)).thenReturn(Optional.of(journalpost));

		try {
			service.hentDokument(request);
			fail("Expected exception");
		} catch (DocumentNotFoundException e) {
			assertThat(e.getCause(), is(instanceOf(InvalidArgumentException.class)));
		}
	}

	@Test
	public void shouldThrowExceptionWhenDokumentFilNotFound() throws Exception {
		Journalpost journalpost = createJournalPost();

		when(joarkRepositoryMock.findById(JOURNALPOST_ID)).thenReturn(Optional.of(journalpost));
		when(dokumentFilRepository.findByFilUuid(FIL_UUID)).thenReturn(null);
		try {
			service.hentDokument(request);
			fail("Expected exception");
		} catch (DocumentNotFoundException e) {
			assertThat(e.getCause(), is(instanceOf(InvalidFilUuidException.class)));
		}
	}

	@Test
	public void shouldReturnDokument() throws Exception {
		DokumentFil dokumentFil = getDokumentFilBuilder().fil(BYTES).build();

		when(joarkRepositoryMock.findById(JOURNALPOST_ID)).thenReturn(Optional.of(createJournalPost()));
		when(dokumentFilRepository.findByFilUuid(FIL_UUID)).thenReturn(dokumentFil);

		byte[] dokument = service.hentDokument(request);

		assertThat(dokument, is(BYTES));
	}

	@Test
	public void shouldReturnOnDemandDokument() throws Exception {
		Journalpost journalPost = createWithOndemand(ON_DEMAND_ID, ON_DEMAND_INSTANS);

		when(joarkRepositoryMock.findById(JOURNALPOST_ID)).thenReturn(Optional.of(journalPost));
		when(hentOndemandDokument.createDokumentUrl(JOURNALPOST_ID, FIL_UUID)).thenReturn(new HentDokumentUrlResponse(DOKUMENTURL));
		when(hentOndemandDokument.hentOndemandDokumentFromJoark(DOKUMENTURL)).thenReturn(BYTES);

		byte[] dokument = service.hentDokument(request);

		assertThat(dokument, is(BYTES));
	}

	private Journalpost createWithOndemand(String onDemandId, OnDemandInstansCode onDemandInstans) {
		Journalpost journalPost = createJournalPost();
		FilDetaljer filDetaljer = journalPost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo().findFilDetaljerByFilUuid(FIL_UUID);
		filDetaljer.setOnDemandId(onDemandId);
		filDetaljer.setOnDemandInstans(onDemandInstans);
		return journalPost;
	}

	private Journalpost createJournalPost() {
		return JournalpostBuilder
				.getJournalpostBuilder()
				.journalpostId(JOURNALPOST_ID)
				.mottakskanal(MottaksKanalCode.NAV_NO)
				.dokumentInfoRelasjoner(
						JournalpostDokumentInfoRelasjonBuilder
								.getJournalpostDokumentInfoRelasjonBuilder()
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
								.dokumentInfo(
										DokumentInfoBuilder
												.getDokumentInfoBuilder()
												.dokumentInfoId(DOKUMENT_INFO_ID)
												.filDetaljerList(
														FilDetaljerBuilder.getFilDetaljerBuilder().filUuid(FIL_UUID)
																.variantFormat(VARIANT_FORMAT).build()).build()).build())
				.build();
	}
}
