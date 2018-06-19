package no.nav.dokarkiv.dokumentproduksjoninfo.tjoark121;

import static no.nav.dokarkiv.core.domain.builder.DokumentFilBuilder.getDokumentFilBuilder;
import static no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder.getDokumentInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder.getFilDetaljerBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentFil;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.repository.DokumentFilRepository;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.dokumentproduksjoninfo.exceptions.FilDetaljerNotFoundException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Junit test for {@link HentFerdigstilteDokumenterService}
 *
 * @author Stig Strøm
 */
@RunWith(MockitoJUnitRunner.class)
public class HentFerdigstilteDokumenterServiceTest {

	private static final String FILUUID_1 = "filuuid1";
	private static final String FILUUID_2 = "filuuid2";
	private static final String FILCONTENT_1 = "filcontent1";
	private static final String FILCONTENT_2 = "filcontent1";
	private static final String TITTEL_1 = "brevtittel1";
	private static final String TITTEL_2 = "brevtittel2";
	private static final long JOURNALPOST_ID = 42L;
	private static final long DOKUMENT_1 = 1L;
	private static final long DOKUMENT_2 = 2L;

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Mock
	private JoarkRepository joarkRepository;

	@Mock
	private DokumentFilRepository dokumentFilRepository;

	@Mock
	private HentFerdigstilteDokumenterValidator hentFerdigstilteRokumenterValidator;

	@InjectMocks
	private HentFerdigstilteDokumenterService service;

	@Test
	public void shouldFetchFerdigstilteDokumenter() throws Exception {
		when(joarkRepository.findById(JOURNALPOST_ID)).thenReturn(Optional.of(createJournalpost()));
		when(dokumentFilRepository.findByFilUuid(FILUUID_1)).thenReturn(createFildetaljer(FILCONTENT_1));
		when(dokumentFilRepository.findByFilUuid(FILUUID_2)).thenReturn(createFildetaljer(FILCONTENT_2));

		List<HentFerdigstilteDokumenterResponseTo> hentFerdigstilteDokumenter = service.hentFerdigstilteDokumenter(
				JOURNALPOST_ID, Arrays.asList(DOKUMENT_1, DOKUMENT_2));

		assertThat(hentFerdigstilteDokumenter.size(), is(2));
		assertThat(hentFerdigstilteDokumenter.get(0).getDokumentInfoId(), is(DOKUMENT_1));
		assertThat(hentFerdigstilteDokumenter.get(0).getFil(), is(FILCONTENT_1.getBytes()));
		assertThat(hentFerdigstilteDokumenter.get(0).getTittel(), is(TITTEL_1));
		assertThat(hentFerdigstilteDokumenter.get(1).getDokumentInfoId(), is(DOKUMENT_2));
		assertThat(hentFerdigstilteDokumenter.get(1).getFil(), is(FILCONTENT_2.getBytes()));
		assertThat(hentFerdigstilteDokumenter.get(1).getTittel(), is(TITTEL_2));
	}

	@Test
	public void shouldThrowException_dokumentNotAvailable() throws Exception {
		exception.expect(FilDetaljerNotFoundException.class);
		when(joarkRepository.findById(JOURNALPOST_ID)).thenReturn(Optional.of(createJournalpost()));

		service.hentFerdigstilteDokumenter(JOURNALPOST_ID, Arrays.asList(DOKUMENT_1));
	}

	private DokumentFil createFildetaljer(String filContent) {
		return getDokumentFilBuilder().fil(filContent.getBytes()).build();
	}

	private Journalpost createJournalpost() {
		return getJournalpostBuilder()
				.journalpostId(JOURNALPOST_ID)
				.dokumentInfoRelasjoner(
						getJournalpostDokumentInfoRelasjonBuilder()
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
								.dokumentInfo(
										getDokumentInfoBuilder()
												.dokumentInfoId(DOKUMENT_1)
												.tittel(TITTEL_1)
												.filDetaljerList(
														getFilDetaljerBuilder().filUuid(FILUUID_1)
																.variantFormat(VariantFormatCode.ARKIV).build()).build())
								.build())
				.dokumentInfoRelasjoner(
						getJournalpostDokumentInfoRelasjonBuilder()
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
								.dokumentInfo(
										getDokumentInfoBuilder()
												.dokumentInfoId(DOKUMENT_2)
												.tittel(TITTEL_2)
												.filDetaljerList(
														getFilDetaljerBuilder()
																.filUuid(FILUUID_2)
																.variantFormat(VariantFormatCode.ARKIV)
																.build())
												.build())
								.build()
				)
				.build();
	}

}
