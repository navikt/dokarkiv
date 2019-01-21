package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark106;

import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.UgyldigDokumentStatusVerdiException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.UgyldigJournalStatusVerdiException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.UgyldigTilknyttetJournalpostSomVerdiException;
import no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder;
import no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.exceptions.NoDokumentInfoFoundException;
import no.nav.dokarkiv.core.exceptions.NoJournalpostFoundException;
import no.nav.dokarkiv.core.repository.JoarkRepositorySkjermet;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import no.nav.dokarkiv.core.sporing.SporingPopulator;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultAvbrytVedleggServiceTest {

	private static final Long JOURNALPOST_ID = 51L;
	private static final Long DOKUMENTINFO_ID = 100L;
	private static final String ENDRET_AV_NAVN = "endret_av";

	@Mock
	private AvbrytVedleggValidator validator;

	@Mock
	private JoarkRepositorySkjermet joarkRepository;

	@Mock
	private JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;

	@Mock
	private SporingPopulator sporingPopulator;

	@InjectMocks
	private DefaultAvbrytVedleggService avbrytVedleggService;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private Journalpost journalpost;

	@Before
	public void setUp() {
		journalpost = createJournalpostWithDokumentInfo();
		when(joarkRepository.findById(JOURNALPOST_ID)).thenReturn(Optional.of(journalpost));
	}

	@Test
	public void shouldValidateInput() throws Exception {
		AvbrytVedleggRequestTo request = createRequest();
		avbrytVedleggService.avbrytVedlegg(request);

		ArgumentCaptor<AvbrytVedleggRequestTo> captor = ArgumentCaptor.forClass(AvbrytVedleggRequestTo.class);
		verify(validator).validateInputRequest(captor.capture());

		assertThat(captor.getValue(), is(request));
	}

	@Test
	public void shouldFindJournalpostBasedOnResponse() throws Exception {
		AvbrytVedleggRequestTo request = createRequest();
		avbrytVedleggService.avbrytVedlegg(request);

		verify(joarkRepository).findById(request.getJournalpostId());
	}

	@Test
	public void shouldValidateJournalpost() throws Exception {
		when(joarkRepository.findById(JOURNALPOST_ID)).thenReturn(Optional.of(journalpost));
		avbrytVedleggService.avbrytVedlegg(createRequest());

		verify(validator).validateJournalpost(journalpost, JOURNALPOST_ID);
	}

	@Test
	public void shouldRethrowNoJournalpostFoundException() throws Exception {
		thrown.expect(NoJournalpostFoundException.class);
		doThrow(new NoJournalpostFoundException("", JOURNALPOST_ID))
				.when(validator).validateJournalpost(journalpost, JOURNALPOST_ID);

		avbrytVedleggService.avbrytVedlegg(createRequest());
	}

	@Test
	public void shouldRethrowUgyldigJournalStatusVerdiException() throws Exception {
		thrown.expect(UgyldigJournalStatusVerdiException.class);
		doThrow(new UgyldigJournalStatusVerdiException("", JournalStatusCode.J))
				.when(validator).validateJournalpost(journalpost, JOURNALPOST_ID);

		avbrytVedleggService.avbrytVedlegg(createRequest());
	}

	@Test
	public void shouldValidateDocument() throws Exception {
		avbrytVedleggService.avbrytVedlegg(createRequest());

		verify(validator).validateDokumentInfo(journalpost.findDokumentInfoById(DOKUMENTINFO_ID), DOKUMENTINFO_ID);
	}

	@Test
	public void shouldRethrowNoDokumentInfoFoundException() throws Exception {
		thrown.expect(NoDokumentInfoFoundException.class);
		doThrow(new NoDokumentInfoFoundException("", DOKUMENTINFO_ID))
				.when(validator).validateDokumentInfo(journalpost.findDokumentInfoById(DOKUMENTINFO_ID), DOKUMENTINFO_ID);

		avbrytVedleggService.avbrytVedlegg(createRequest());
	}

	@Test
	public void shouldRethrowUgyldigDokumentStatusVerdiException() throws Exception {
		thrown.expect(UgyldigDokumentStatusVerdiException.class);
		doThrow(new UgyldigDokumentStatusVerdiException("", DokumentStatusCode.AVBRUTT))
				.when(validator).validateDokumentInfo(journalpost.findDokumentInfoById(DOKUMENTINFO_ID), DOKUMENTINFO_ID);

		avbrytVedleggService.avbrytVedlegg(createRequest());
	}

	@Test
	public void shouldValidateJournalpostDokumentInfoRelasjon() throws Exception {
		avbrytVedleggService.avbrytVedlegg(createRequest());

		verify(validator).validateJournalpostDokumentInfoRelasjon(
				journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next());
	}

	@Test
	public void shouldRethrowUgyldigTilknyttetJournalpostSomVerdiException() throws Exception {
		thrown.expect(UgyldigTilknyttetJournalpostSomVerdiException.class);
		doThrow(new UgyldigTilknyttetJournalpostSomVerdiException("", TilknyttetJournalpostSomCode.HOVEDDOKUMENT))
				.when(validator).validateJournalpostDokumentInfoRelasjon(
				journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next());

		avbrytVedleggService.avbrytVedlegg(createRequest());
	}

	@Test
	public void shouldSetSporingsinformasjon() throws Exception {
		avbrytVedleggService.avbrytVedlegg(createRequest());

		verify(sporingPopulator).populateSporingInfo(journalpost, ENDRET_AV_NAVN);
	}

	@Test
	public void shouldSetDokumentstatusToAvbruttWhenDokumentBelongsToASingelJournalpost() throws Exception {
		avbrytVedleggService.avbrytVedlegg(createRequest());

		DokumentInfo dokumentInfo = journalpost.findDokumentInfoById(DOKUMENTINFO_ID);
		assertThat(dokumentInfo.getDokumentstatus(), is(DokumentStatusCode.AVBRUTT));
	}

	@Test
	public void shouldNotChangeDokumentstatusWhenDokumentBelongsToMultipleJournalposts() throws Exception {
		DokumentInfo dokumentInfo = createDokumentInfo();
		dokumentInfo.setDokumentstatus(DokumentStatusCode.FERDIGSTILT);

		Journalpost jp = createJournalpost(JOURNALPOST_ID, dokumentInfo);

		//Create another journalpost related to dokumentInfo
		createJournalpost(JOURNALPOST_ID + 1, dokumentInfo);

		when(joarkRepository.findById(JOURNALPOST_ID)).thenReturn(Optional.of(jp));

		avbrytVedleggService.avbrytVedlegg(createRequest());

		assertThat(dokumentInfo.getDokumentstatus(), is(DokumentStatusCode.FERDIGSTILT));
	}

	@Test
	public void shouldDeleteJournalpostInfoRelasjonWhenDokumentBelongsToMultipleJournalposts() throws Exception {
		DokumentInfo dokumentInfo = createDokumentInfo();
		dokumentInfo.setDokumentstatus(DokumentStatusCode.FERDIGSTILT);

		Journalpost jp = createJournalpost(JOURNALPOST_ID, dokumentInfo);
		JournalpostDokumentInfoRelasjon relasjon = jp.getJournalpostDokumentInfoRelasjoner().iterator().next();

		//Create another journalpost related to dokumentInfo
		createJournalpost(JOURNALPOST_ID + 1, dokumentInfo);

		when(joarkRepository.findById(JOURNALPOST_ID)).thenReturn(Optional.of(jp));

		avbrytVedleggService.avbrytVedlegg(createRequest());

		verify(journalpostDokumentInfoRelasjonRepository).delete(relasjon);
	}

	private Journalpost createJournalpost(Long journalPostId, DokumentInfo dokumentInfo) {
		return getJournalpostBuilder()
				.journalpostId(journalPostId)
				.dokumentInfoRelasjoner(createJournalpostDokumentInfoRelasjon(dokumentInfo))
				.build();
	}

	private JournalpostDokumentInfoRelasjon createJournalpostDokumentInfoRelasjon(DokumentInfo dokumentInfo) {
		return JournalpostDokumentInfoRelasjonBuilder
				.getJournalpostDokumentInfoRelasjonBuilder()
				.dokumentInfo(dokumentInfo)
				.build();
	}

	private Journalpost createJournalpostWithDokumentInfo() {
		return getJournalpostBuilder()
				.journalpostId(JOURNALPOST_ID)
				.dokumentInfoRelasjoner(createJournalpostDokumentInfoRelasjon(createDokumentInfo()))
				.build();
	}

	private DokumentInfo createDokumentInfo() {
		return DokumentInfoBuilder
				.getDokumentInfoBuilder()
				.dokumentInfoId(DOKUMENTINFO_ID)
				.build();
	}

	private AvbrytVedleggRequestTo createRequest() {
		return new AvbrytVedleggRequestTo(JOURNALPOST_ID, DOKUMENTINFO_ID, ENDRET_AV_NAVN);
	}
}