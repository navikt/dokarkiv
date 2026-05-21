package no.nav.dokarkiv.journalpost.v1.services;

import no.nav.dokarkiv.core.domain.codes.SlettebestillingStatusCode;
import no.nav.dokarkiv.core.exceptions.SlettebestillingIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.UgyldigSlettebestillingException;
import no.nav.dokarkiv.core.domain.entities.Slettebestilling;
import no.nav.dokarkiv.core.repository.DokumentInfoRepository;
import no.nav.dokarkiv.core.repository.SlettebestillingRepository;
import no.nav.dokarkiv.journalpost.v1.api.SlettebestillingRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static no.nav.dokarkiv.core.CoreConfig.ZONEID_NORGE;
import static no.nav.dokarkiv.core.domain.codes.SlettebestillingArsakCode.ENKELTSLETTING;
import static no.nav.dokarkiv.core.domain.codes.SlettebestillingHjemmelCode.ARK;
import static no.nav.dokarkiv.core.domain.codes.SlettebestillingStatusCode.AVBRUTT;
import static no.nav.dokarkiv.core.domain.codes.SlettebestillingStatusCode.FERDIGSTILT;
import static no.nav.dokarkiv.core.domain.codes.SlettebestillingStatusCode.OPPRETTET;
import static no.nav.dokarkiv.core.domain.codes.SlettebestillingTypeCode.DOKUMENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

public class SlettebestillingServiceTest {
	@Mock
	SlettebestillingRepository slettebestillingRepository;
	@Mock
	DokumentInfoRepository dokumentInfoRepository;

	SlettebestillingService slettebestillingService;

	static Clock clock = Clock.fixed(Instant.parse("2025-12-09T16:00:00.000Z"), ZONEID_NORGE);

	@BeforeEach
	void setup() {
		openMocks(this);
		when(dokumentInfoRepository.existsById(any())).thenReturn(true);
		slettebestillingService = new SlettebestillingService(dokumentInfoRepository, slettebestillingRepository, clock);
	}

	@Test
	public void shouldCreateCorrectDeletiontime() {
		when(slettebestillingRepository.persist(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArguments()[0]);
		ArgumentCaptor<Slettebestilling> slettebestillingArgumentCaptor = ArgumentCaptor.forClass(Slettebestilling.class);

		long dokumentInfoId = 12L;
		var request = new SlettebestillingRequest(ARK.name(), "begrunnelse");

		slettebestillingService.bestillSletting(dokumentInfoId, request);

		verify(slettebestillingRepository).persist(slettebestillingArgumentCaptor.capture());
		assertThat(slettebestillingArgumentCaptor.getValue().getDatoUtfores()).isEqualTo(LocalDate.of(2025, 12, 30));
	}

	@Test
	void shouldAvbrytOpprettetSlettebestilling() {
		var slettebestilling = lagSlettebestilling(OPPRETTET);
		when(slettebestillingRepository.findByDokumentInfoId(1L)).thenReturn(List.of(slettebestilling));

		slettebestillingService.opphevBestillSletting(1L);

		assertEquals(AVBRUTT, slettebestilling.getSlettebestillingStatus());
	}

	@Test
	void shouldThrowWhenFerdigstiltExists() {
		var slettebestilling = lagSlettebestilling(FERDIGSTILT);
		when(slettebestillingRepository.findByDokumentInfoId(1L)).thenReturn(List.of(slettebestilling));

		assertThatThrownBy(() -> slettebestillingService.opphevBestillSletting(1L))
				.isInstanceOf(UgyldigSlettebestillingException.class)
				.hasMessageContaining("Kan ikke oppheve sletting som allerede er gjennomført");
	}

	@Test
	void shouldThrowWhenOnlyAvbruttSlettebestillingerExists() {
		var slettebestilling = lagSlettebestilling(AVBRUTT);
		when(slettebestillingRepository.findByDokumentInfoId(1L)).thenReturn(List.of(slettebestilling));

		assertThatThrownBy(() -> slettebestillingService.opphevBestillSletting(1L))
				.isInstanceOf(SlettebestillingIkkeFunnetException.class)
				.hasMessageContaining("Fant ingen slettebestillinger som kunne avbrytes");
	}

	@Test
	void shouldOnlyAvbrytOpprettetWhenMixedStatuses() {
		var opprettet = lagSlettebestilling(OPPRETTET);
		var avbrutt = lagSlettebestilling(AVBRUTT);
		when(slettebestillingRepository.findByDokumentInfoId(1L)).thenReturn(List.of(opprettet, avbrutt));

		slettebestillingService.opphevBestillSletting(1L);

		assertEquals(AVBRUTT, opprettet.getSlettebestillingStatus());
		assertEquals(AVBRUTT, avbrutt.getSlettebestillingStatus());
	}

	@Test
	void shouldThrowWhenNoSlettebestillingerExists() {
		when(slettebestillingRepository.findByDokumentInfoId(1L)).thenReturn(List.of());

		assertThatThrownBy(() -> slettebestillingService.opphevBestillSletting(1L))
				.isInstanceOf(SlettebestillingIkkeFunnetException.class)
				.hasMessageContaining("Fant ingen slettebestillinger som kunne avbrytes");
	}

	private static Slettebestilling lagSlettebestilling(SlettebestillingStatusCode status) {
		return Slettebestilling.builder()
				.slettebestillingType(DOKUMENT)
				.slettebestillingStatus(status)
				.slettebestillingHjemmel(ARK)
				.slettebestillingArsak(ENKELTSLETTING)
				.dokumentInfoId(1L)
				.begrunnelse("test")
				.datoUtfores(LocalDate.now())
				.opprettetAvNavn("test")
				.endretAvNavn("test")
				.build();
	}
}
