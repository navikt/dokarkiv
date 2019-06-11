package no.nav.dokarkiv.core.akjsonslogg;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggTO;
import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.aksjonslogg.JournalpostDokumentInfoPair;
import no.nav.dokarkiv.core.aksjonslogg.LagreAksjonsLoggService;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */

@RunWith(MockitoJUnitRunner.class)
public class LagreAksjonsLoggServiceTest {

	@Mock
	AksjonsLoggService aksjonsLoggService;

	@Mock
	JournalpostDokumentInfoRelasjonRepository relasjonRepository;

	@InjectMocks
	LagreAksjonsLoggService lagreAksjonsLoggService;

	@Captor
	ArgumentCaptor<AksjonsLoggTO> captorAksjonsLogg;

	@Captor
	ArgumentCaptor<List<ArkivElementEndringTO>> captorArkivElementListe;

	private final String HJEMMEL="POL";
	private final String UTFOERT_AV="MEG";
	private final String MELDING="HEI";

	@Test
	public void skalLageNyEllerLeggeTilEndringerTilEksisterendeAksjonsLoggHvisJournalpostErNull() throws UgyldigAksjonsLoggException {
		Map<JournalpostDokumentInfoPair, List<ArkivElementEndringTO>> aksjonsLoggMap = new HashMap<>();
		aksjonsLoggMap.put(JournalpostDokumentInfoPair.of(1L, 1L), new ArrayList<>(Arrays.asList(ArkivElementEndringTO.builder().arkivElement("test1").build())));
		aksjonsLoggMap.put(JournalpostDokumentInfoPair.of(3L, 1L), new ArrayList<>(Arrays.asList(ArkivElementEndringTO.builder().arkivElement("test3").build())));
		aksjonsLoggMap.put(JournalpostDokumentInfoPair.of(null, 1L), new ArrayList<>(Arrays.asList(ArkivElementEndringTO.builder().arkivElement("test2").build())));

		when(relasjonRepository.findAllByDokumentInfoDokumentInfoId(1L)).thenReturn(Arrays.asList(
				JournalpostDokumentInfoRelasjon.builder().journalpost(Journalpost.builder().journalpostId(2L).build()).build(),
				JournalpostDokumentInfoRelasjon.builder().journalpost(Journalpost.builder().journalpostId(1L).build()).build(),
				JournalpostDokumentInfoRelasjon.builder().journalpost(Journalpost.builder().journalpostId(3L).build()).build()
		));
		lagreAksjonsLoggService.lagreAksjonsLogg(AksjonsTypeCode.ARKIVERING, aksjonsLoggMap, HJEMMEL, MELDING, UTFOERT_AV);

		verify(aksjonsLoggService, times(3)).validateAndSaveAksjonsLogg(captorAksjonsLogg.capture(), captorArkivElementListe.capture());

		assertThat(captorAksjonsLogg.getAllValues().get(0).getJournalpostId(), is(2L));
		assertThat(captorAksjonsLogg.getAllValues().get(0).getDokumentInfoId(), is(1L));
		assertThat(captorAksjonsLogg.getAllValues().get(0).getHjemmel(), is(HJEMMEL));
		assertThat(captorAksjonsLogg.getAllValues().get(0).getMelding(), is(MELDING));
		assertThat(captorAksjonsLogg.getAllValues().get(0).getUtfoertAv(), is(UTFOERT_AV));
		assertThat(captorArkivElementListe.getAllValues().get(0).size(), is(1));
		assertThat(captorArkivElementListe.getAllValues().get(0).get(0).getArkivElement(), is("test2"));

		assertThat(captorAksjonsLogg.getAllValues().get(1).getJournalpostId(), is(1L));
		assertThat(captorAksjonsLogg.getAllValues().get(1).getDokumentInfoId(), is(1L));
		assertThat(captorAksjonsLogg.getAllValues().get(1).getHjemmel(), is(HJEMMEL));
		assertThat(captorAksjonsLogg.getAllValues().get(1).getMelding(), is(MELDING));
		assertThat(captorAksjonsLogg.getAllValues().get(1).getUtfoertAv(), is(UTFOERT_AV));
		assertThat(captorArkivElementListe.getAllValues().get(1).size(), is(2));
		assertThat(captorArkivElementListe.getAllValues().get(1).get(0).getArkivElement(), is("test1"));
		assertThat(captorArkivElementListe.getAllValues().get(1).get(1).getArkivElement(), is("test2"));

		assertThat(captorAksjonsLogg.getAllValues().get(2).getJournalpostId(), is(3L));
		assertThat(captorAksjonsLogg.getAllValues().get(2).getDokumentInfoId(), is(1L));
		assertThat(captorAksjonsLogg.getAllValues().get(2).getHjemmel(), is(HJEMMEL));
		assertThat(captorAksjonsLogg.getAllValues().get(2).getMelding(), is(MELDING));
		assertThat(captorAksjonsLogg.getAllValues().get(2).getUtfoertAv(), is(UTFOERT_AV));
		assertThat(captorArkivElementListe.getAllValues().get(2).size(), is(2));
		assertThat(captorArkivElementListe.getAllValues().get(2).get(0).getArkivElement(), is("test3"));
		assertThat(captorArkivElementListe.getAllValues().get(2).get(1).getArkivElement(), is("test2"));

	}

	@Test
	public void skalLageNyEllerLeggeTilEndringerTilEksisterendeAksjonsLoggHvisDokumentInfoIdErNull() throws UgyldigAksjonsLoggException {
		Map<JournalpostDokumentInfoPair, List<ArkivElementEndringTO>> aksjonsLoggMap = new HashMap<>();
		aksjonsLoggMap.put(JournalpostDokumentInfoPair.of(1L, 1L), new ArrayList<>(Arrays.asList(ArkivElementEndringTO.builder().arkivElement("test1").build())));
		aksjonsLoggMap.put(JournalpostDokumentInfoPair.of(3L, 2L), new ArrayList<>(Arrays.asList(ArkivElementEndringTO.builder().arkivElement("test3").build())));
		aksjonsLoggMap.put(JournalpostDokumentInfoPair.of(1L, null), new ArrayList<>(Arrays.asList(ArkivElementEndringTO.builder().arkivElement("test2").build())));

		when(relasjonRepository.findAllByJournalpostJournalpostId(1L)).thenReturn(Arrays.asList(
				JournalpostDokumentInfoRelasjon.builder().dokumentInfo(DokumentInfo.builder().dokumentInfoId(3L).build()).build(),
				JournalpostDokumentInfoRelasjon.builder().dokumentInfo(DokumentInfo.builder().dokumentInfoId(1L).build()).build()
		));
		lagreAksjonsLoggService.lagreAksjonsLogg(AksjonsTypeCode.ARKIVERING, aksjonsLoggMap, HJEMMEL, MELDING, UTFOERT_AV);

		verify(aksjonsLoggService, times(3)).validateAndSaveAksjonsLogg(captorAksjonsLogg.capture(), captorArkivElementListe.capture());

		assertThat(captorAksjonsLogg.getAllValues().get(0).getJournalpostId(), is(1L));
		assertThat(captorAksjonsLogg.getAllValues().get(0).getDokumentInfoId(), is(1L));
		assertThat(captorAksjonsLogg.getAllValues().get(0).getHjemmel(), is(HJEMMEL));
		assertThat(captorAksjonsLogg.getAllValues().get(0).getMelding(), is(MELDING));
		assertThat(captorAksjonsLogg.getAllValues().get(0).getUtfoertAv(), is(UTFOERT_AV));
		assertThat(captorArkivElementListe.getAllValues().get(0).size(), is(2));
		assertThat(captorArkivElementListe.getAllValues().get(0).get(0).getArkivElement(), is("test1"));
		assertThat(captorArkivElementListe.getAllValues().get(0).get(1).getArkivElement(), is("test2"));

		assertThat(captorAksjonsLogg.getAllValues().get(1).getJournalpostId(), is(1L));
		assertThat(captorAksjonsLogg.getAllValues().get(1).getDokumentInfoId(), is(3L));
		assertThat(captorAksjonsLogg.getAllValues().get(1).getHjemmel(), is(HJEMMEL));
		assertThat(captorAksjonsLogg.getAllValues().get(1).getMelding(), is(MELDING));
		assertThat(captorAksjonsLogg.getAllValues().get(1).getUtfoertAv(), is(UTFOERT_AV));
		assertThat(captorArkivElementListe.getAllValues().get(1).size(), is(1));
		assertThat(captorArkivElementListe.getAllValues().get(0).get(1).getArkivElement(), is("test2"));

		assertThat(captorAksjonsLogg.getAllValues().get(2).getJournalpostId(), is(3L));
		assertThat(captorAksjonsLogg.getAllValues().get(2).getDokumentInfoId(), is(2L));
		assertThat(captorAksjonsLogg.getAllValues().get(2).getHjemmel(), is(HJEMMEL));
		assertThat(captorAksjonsLogg.getAllValues().get(2).getMelding(), is(MELDING));
		assertThat(captorAksjonsLogg.getAllValues().get(2).getUtfoertAv(), is(UTFOERT_AV));
		assertThat(captorArkivElementListe.getAllValues().get(2).size(), is(1));
		assertThat(captorArkivElementListe.getAllValues().get(2).get(0).getArkivElement(), is("test3"));

	}

	@Test
	public void skalLageAksjonsloggForAlleJournalpostRelasjoner() throws UgyldigAksjonsLoggException {
		when(relasjonRepository.findAllByDokumentInfoDokumentInfoId(1L)).thenReturn(Arrays.asList(
				JournalpostDokumentInfoRelasjon.builder().journalpost(Journalpost.builder().journalpostId(2L).build()).build(),
				JournalpostDokumentInfoRelasjon.builder().journalpost(Journalpost.builder().journalpostId(1L).build()).build(),
				JournalpostDokumentInfoRelasjon.builder().journalpost(Journalpost.builder().journalpostId(3L).build()).build()
		));
		lagreAksjonsLoggService.lagreAksjonsLogg(AksjonsTypeCode.ARKIVERING, 1L, HJEMMEL, MELDING, UTFOERT_AV, new ArrayList<>(Arrays.asList(
				ArkivElementEndringTO.builder().arkivElement("test1").build(),
				ArkivElementEndringTO.builder().arkivElement("test2").build()
		)));

		verify(aksjonsLoggService, times(3)).validateAndSaveAksjonsLogg(captorAksjonsLogg.capture(), captorArkivElementListe.capture());

		assertThat(captorAksjonsLogg.getAllValues().get(0).getJournalpostId(), is(2L));
		assertThat(captorAksjonsLogg.getAllValues().get(0).getDokumentInfoId(), is(1L));
		assertThat(captorAksjonsLogg.getAllValues().get(0).getHjemmel(), is(HJEMMEL));
		assertThat(captorAksjonsLogg.getAllValues().get(0).getMelding(), is(MELDING));
		assertThat(captorAksjonsLogg.getAllValues().get(0).getUtfoertAv(), is(UTFOERT_AV));
		assertThat(captorArkivElementListe.getAllValues().get(0).size(), is(2));
		assertThat(captorArkivElementListe.getAllValues().get(0).get(0).getArkivElement(), is("test1"));
		assertThat(captorArkivElementListe.getAllValues().get(0).get(1).getArkivElement(), is("test2"));

		assertThat(captorAksjonsLogg.getAllValues().get(1).getJournalpostId(), is(1L));
		assertThat(captorAksjonsLogg.getAllValues().get(1).getDokumentInfoId(), is(1L));
		assertThat(captorAksjonsLogg.getAllValues().get(1).getHjemmel(), is(HJEMMEL));
		assertThat(captorAksjonsLogg.getAllValues().get(1).getMelding(), is(MELDING));
		assertThat(captorAksjonsLogg.getAllValues().get(1).getUtfoertAv(), is(UTFOERT_AV));
		assertThat(captorArkivElementListe.getAllValues().get(1).size(), is(2));
		assertThat(captorArkivElementListe.getAllValues().get(1).get(0).getArkivElement(), is("test1"));
		assertThat(captorArkivElementListe.getAllValues().get(1).get(1).getArkivElement(), is("test2"));

		assertThat(captorAksjonsLogg.getAllValues().get(2).getJournalpostId(), is(3L));
		assertThat(captorAksjonsLogg.getAllValues().get(2).getDokumentInfoId(), is(1L));
		assertThat(captorAksjonsLogg.getAllValues().get(2).getHjemmel(), is(HJEMMEL));
		assertThat(captorAksjonsLogg.getAllValues().get(2).getMelding(), is(MELDING));
		assertThat(captorAksjonsLogg.getAllValues().get(2).getUtfoertAv(), is(UTFOERT_AV));
		assertThat(captorArkivElementListe.getAllValues().get(2).size(), is(2));
		assertThat(captorArkivElementListe.getAllValues().get(2).get(0).getArkivElement(), is("test1"));
		assertThat(captorArkivElementListe.getAllValues().get(2).get(1).getArkivElement(), is("test2"));

	}
}