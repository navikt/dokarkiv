package no.nav.dokarkiv.journalpost.v1.services;

import no.nav.dokarkiv.core.domain.codes.SlettebestillingTypeCode;
import no.nav.dokarkiv.core.domain.entities.Slettebestilling;
import no.nav.dokarkiv.core.repository.DokumentInfoRepository;
import no.nav.dokarkiv.core.repository.SlettebestillingRepository;
import no.nav.dokarkiv.journalpost.v1.api.SlettebestillingRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.stream.Stream;

import static no.nav.dokarkiv.core.CoreConfig.ZONEID_NORGE;
import static no.nav.dokarkiv.core.domain.codes.SlettebestillingArsakCode.ENKELTSLETTING;
import static no.nav.dokarkiv.core.domain.codes.SlettebestillingHjemmelCode.ARK;
import static no.nav.dokarkiv.core.domain.codes.SlettebestillingTypeCode.DOKUMENT;
import static org.assertj.core.api.Assertions.assertThat;
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

	public static Stream<Arguments> shouldCreateCorrectDeletiontime() {
		return Stream.of(
				Arguments.of(DOKUMENT, LocalDate.of(2025, 12, 30))
		);
	}

	@ParameterizedTest
	@MethodSource
	public void shouldCreateCorrectDeletiontime(SlettebestillingTypeCode type, LocalDate expectedDate) {
		when(slettebestillingRepository.persist(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArguments()[0]);
		ArgumentCaptor<Slettebestilling> slettebestillingArgumentCaptor = ArgumentCaptor.forClass(Slettebestilling.class);

		var request = new SlettebestillingRequest(type.name(), 12L, ENKELTSLETTING.name(), ARK.name(), "begrunnelse");

		slettebestillingService.bestillSletting(request);

		verify(slettebestillingRepository).persist(slettebestillingArgumentCaptor.capture());
		assertThat(slettebestillingArgumentCaptor.getValue().getDatoUtfores()).isEqualTo(expectedDate);
	}

}