package no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v1;


import com.google.common.collect.ImmutableMap;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;
import no.nav.dokarkiv.core.journalbehandling.DokumentFilerDelegate;
import no.nav.dokarkiv.core.repository.JoarkRepositorySkjermet;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Optional;

import static no.nav.dokarkiv.arkiverdokumentmottak.ArkiverDokumentmottakConstants.FORSENDELSE_MOTTAK_ID_KEY;
import static no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder.getDokumentInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(org.mockito.junit.MockitoJUnitRunner.class)
public class JournalforInngaaendeForsendelseServiceTest {
	private static final String DOKUMENT_TYPE_VEDLEGG = "dokumentTypeVedlegg";
	private static final String FORSENDLESE_MOTTAKS_ID = "forsendleseMottaksId";
	private static final String OPPRETTET_AV = "opprettetAv";
	private static final Long JOURNALPOST_ID = 42L;
	private static final Long DOKUMENTINFO_ID = 56L;
	private static final Long DOKUMENTINFO_VEDLEGG = 78L;

	@Rule
	public ExpectedException expected = ExpectedException.none();
	@Mock
    private JoarkRepositorySkjermet repositoryMock;
	@Mock
	private DokumentFilerDelegate dokumentFilerDelegateMock;
	@Mock
	private JournalforInngaaendeForsendelseValidator validator;
	@InjectMocks
	public JournalforInngaaendeForsendelseService service;

	@Test
	public void shouldJournalforNewInngaaendeForsendelse() throws Exception {
		JournalforInngaaendeForsendelseRequestTo requestTo = new JournalforInngaaendeForsendelseRequestTo(createJournalpost(null, null, null));
		when(repositoryMock.save(any(Journalpost.class))).thenReturn(createJournalpost(JOURNALPOST_ID, DOKUMENTINFO_ID, DOKUMENTINFO_VEDLEGG));
		when(repositoryMock.findJournalpostIdByTilleggsopplysningerNokkelAndVerdi(FORSENDELSE_MOTTAK_ID_KEY, FORSENDLESE_MOTTAKS_ID))
				.thenReturn(null);

		JournalforInngaaendeForsendelseResponseTo response = service.journalforInngaaendeForsendelse(requestTo);

		assertThat(response.getJournalpostId(), is(JOURNALPOST_ID));
		assertThat(response.getDokumentInfoIdHoveddokument(), is(DOKUMENTINFO_ID));
		assertThat(response.getDokumentInfoIdVedleggTo(), hasSize(1));

		verify(repositoryMock).save(any(Journalpost.class));
		verify(repositoryMock).findJournalpostIdByTilleggsopplysningerNokkelAndVerdi(FORSENDELSE_MOTTAK_ID_KEY, FORSENDLESE_MOTTAKS_ID);
		verifyNoMoreInteractions(repositoryMock);
		verify(validator).validate(any(Journalpost.class));
		verifyNoMoreInteractions(validator);
		verify(dokumentFilerDelegateMock).saveUpdateDokumentFiler(any(Journalpost.class));
		verifyNoMoreInteractions(dokumentFilerDelegateMock);
	}

	@Test
	public void shouldJournalforNewInngaaendeForsendelseMissingTilleggsopplysning() throws Exception {
		JournalforInngaaendeForsendelseRequestTo requestTo = new JournalforInngaaendeForsendelseRequestTo(createJournalpost(null, null, null));
		requestTo.getJournalpost().setTilleggsopplysninger(null);
		when(repositoryMock.save(any(Journalpost.class))).thenReturn(createJournalpost(JOURNALPOST_ID, DOKUMENTINFO_ID, DOKUMENTINFO_VEDLEGG));

		JournalforInngaaendeForsendelseResponseTo response = service.journalforInngaaendeForsendelse(requestTo);

		assertThat(response.getJournalpostId(), is(JOURNALPOST_ID));
		assertThat(response.getDokumentInfoIdHoveddokument(), is(DOKUMENTINFO_ID));
		assertThat(response.getDokumentInfoIdVedleggTo(), hasSize(1));
		assertThat(response.getDokumentInfoIdVedleggTo().get(0).getDokumentInfoId(), is(DOKUMENTINFO_VEDLEGG));
		assertThat(response.getDokumentInfoIdVedleggTo().get(0).getDokumentTypeId(), is(DOKUMENT_TYPE_VEDLEGG));

		verify(repositoryMock).save(any(Journalpost.class));
		verifyNoMoreInteractions(repositoryMock);
		verify(validator).validate(any(Journalpost.class));
		verifyNoMoreInteractions(validator);
		verify(dokumentFilerDelegateMock).saveUpdateDokumentFiler(any(Journalpost.class));
		verifyNoMoreInteractions(dokumentFilerDelegateMock);
	}

	@Test
	public void shouldJournalforNewInngaaendeForsendelseUtenVedlegg() throws Exception {
		JournalforInngaaendeForsendelseRequestTo requestTo = new JournalforInngaaendeForsendelseRequestTo(createJournalpostUtenVedlegg(null, null));
		when(repositoryMock.save(any(Journalpost.class))).thenReturn(createJournalpostUtenVedlegg(JOURNALPOST_ID, DOKUMENTINFO_ID));
		when(repositoryMock.findJournalpostIdByTilleggsopplysningerNokkelAndVerdi(FORSENDELSE_MOTTAK_ID_KEY, FORSENDLESE_MOTTAKS_ID))
				.thenReturn(null);

		JournalforInngaaendeForsendelseResponseTo response = service.journalforInngaaendeForsendelse(requestTo);
		assertThat(response.getJournalpostId(), is(JOURNALPOST_ID));
		assertThat(response.getDokumentInfoIdHoveddokument(), is(DOKUMENTINFO_ID));
		assertThat(response.getDokumentInfoIdVedleggTo(), is(empty()));

		verify(repositoryMock).save(any(Journalpost.class));
		verify(repositoryMock).findJournalpostIdByTilleggsopplysningerNokkelAndVerdi(FORSENDELSE_MOTTAK_ID_KEY, FORSENDLESE_MOTTAKS_ID);
		verifyNoMoreInteractions(repositoryMock);
		verify(validator).validate(any(Journalpost.class));
		verifyNoMoreInteractions(validator);
		verify(dokumentFilerDelegateMock).saveUpdateDokumentFiler(any(Journalpost.class));
		verifyNoMoreInteractions(dokumentFilerDelegateMock);
	}

	@Test
	public void shouldReturnAlreadyJournalfoertForsendelse() throws Exception {
		JournalforInngaaendeForsendelseRequestTo requestTo = new JournalforInngaaendeForsendelseRequestTo(createJournalpostUtenVedlegg(null, null));
		when(repositoryMock.findJournalpostIdByTilleggsopplysningerNokkelAndVerdi(FORSENDELSE_MOTTAK_ID_KEY, FORSENDLESE_MOTTAKS_ID))
				.thenReturn(JOURNALPOST_ID);
		when(repositoryMock.findById(JOURNALPOST_ID)).thenReturn(Optional.ofNullable(createJournalpostUtenVedlegg(JOURNALPOST_ID, DOKUMENTINFO_ID)));

		JournalforInngaaendeForsendelseResponseTo response = service.journalforInngaaendeForsendelse(requestTo);
		assertThat(response.getJournalpostId(), is(JOURNALPOST_ID));
		assertThat(response.getDokumentInfoIdHoveddokument(), is(DOKUMENTINFO_ID));

		verify(repositoryMock).findJournalpostIdByTilleggsopplysningerNokkelAndVerdi(FORSENDELSE_MOTTAK_ID_KEY, FORSENDLESE_MOTTAKS_ID);
		verify(repositoryMock).findById(JOURNALPOST_ID);
		verifyNoMoreInteractions(repositoryMock);
		verifyNoMoreInteractions(validator);
		verifyNoMoreInteractions(dokumentFilerDelegateMock);
	}

	private Journalpost createJournalpost(Long journalpostId, Long dokumentInfoIdHoveddokument, Long dokumentInfoIdVedlegg1) {
		return getJournalpostBuilder()
				.journalpostId(journalpostId)
				.opprettetAvNavn(OPPRETTET_AV)
				.saksrelasjon(new Saksrelasjon())
				.brukere(new Bruker())
				.tilleggsopplysninger(ImmutableMap.of(FORSENDELSE_MOTTAK_ID_KEY, FORSENDLESE_MOTTAKS_ID))
				.dokumentInfoRelasjoner(
						getJournalpostDokumentInfoRelasjonBuilder()
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
								.dokumentInfo(
										getDokumentInfoBuilder()
												.dokumentInfoId(dokumentInfoIdHoveddokument)
												.filDetaljerList(new FilDetaljer())
												.build())
								.build(),
						getJournalpostDokumentInfoRelasjonBuilder()
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
								.dokumentInfo(
										getDokumentInfoBuilder()
												.dokumentInfoId(dokumentInfoIdVedlegg1)
												.dokumenttypeId(DOKUMENT_TYPE_VEDLEGG)
												.filDetaljerList(new FilDetaljer())
												.build())
								.build())
				.build();
	}

	private Journalpost createJournalpostUtenVedlegg(Long journalpostId, Long dokumentInfoIdHoveddokument) {
		return getJournalpostBuilder()
				.journalpostId(journalpostId)
				.opprettetAvNavn(OPPRETTET_AV)
				.saksrelasjon(new Saksrelasjon())
				.brukere(new Bruker())
				.tilleggsopplysninger(ImmutableMap.of(FORSENDELSE_MOTTAK_ID_KEY, FORSENDLESE_MOTTAKS_ID))
				.dokumentInfoRelasjoner(
						getJournalpostDokumentInfoRelasjonBuilder()
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
								.dokumentInfo(
										getDokumentInfoBuilder()
												.dokumentInfoId(dokumentInfoIdHoveddokument)
												.filDetaljerList(new FilDetaljer())
												.build())
								.build())
				.build();
	}
}
