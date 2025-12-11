package no.nav.dokarkiv.journalpost.v1.services;

import no.nav.dokarkiv.core.domain.codes.SlettebestillingHjemmelCode;
import no.nav.dokarkiv.core.domain.codes.SlettebestillingTypeCode;
import no.nav.dokarkiv.core.domain.entities.Slettebestilling;
import no.nav.dokarkiv.core.exceptions.UnauthorizedForSlettebestillingException;
import no.nav.dokarkiv.core.repository.DokumentInfoRepository;
import no.nav.dokarkiv.core.repository.SakRepository;
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
import java.util.Optional;
import java.util.stream.Stream;

import static no.nav.dokarkiv.core.CoreConfig.ZONEID_NORGE;
import static no.nav.dokarkiv.core.domain.codes.SlettebestillingArsakCode.BEVARINGSTID;
import static no.nav.dokarkiv.core.domain.codes.SlettebestillingArsakCode.ENKELTSLETTING;
import static no.nav.dokarkiv.core.domain.codes.SlettebestillingTypeCode.DOKUMENT;
import static no.nav.dokarkiv.core.domain.codes.SlettebestillingTypeCode.DOKUMENTER_PA_SAK;
import static no.nav.dokarkiv.core.domain.codes.SlettebestillingTypeCode.SAK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

public class SlettebestillingServiceTest {
	private static final String AZP_NAME_GOSYS = "prod-fss:isa:gosys";
	private static final String AZP_NAME_TEAMDOKUMENTHANDTERING = "dev-fss:teamdokumenthandtering:saf-q1";

	@Mock
	SlettebestillingRepository slettebestillingRepository;
	@Mock
	SakRepository sakRepository;
	@Mock
	DokumentInfoRepository dokumentInfoRepository;

	SlettebestillingService slettebestillingService;

	static Clock clock = Clock.fixed(Instant.parse("2025-12-09T16:00:00.000Z"), ZONEID_NORGE);

	@BeforeEach
	void setup() {
		openMocks(this);
		when(sakRepository.existsById(any())).thenReturn(true);
		when(dokumentInfoRepository.existsById(any())).thenReturn(true);
		slettebestillingService = new SlettebestillingService(sakRepository, dokumentInfoRepository, slettebestillingRepository, clock);
	}

	public static Stream<Arguments> shouldValidateAccessBasedOnAzpName() {
		return Stream.of(
				Arguments.of(DOKUMENT, null),
				Arguments.of(SAK, null),
				Arguments.of(DOKUMENT, AZP_NAME_TEAMDOKUMENTHANDTERING),
				Arguments.of(SAK, AZP_NAME_GOSYS),
				Arguments.of(DOKUMENT, ""),
				Arguments.of(SAK, "prod-fss:teamdokumenthandtering-not:en-app"),
				Arguments.of(DOKUMENT, "dev-fss:teamluring:luringapp-ikke-gosys")
		);
	}

	@ParameterizedTest
	@MethodSource
	public void shouldValidateAccessBasedOnAzpName(SlettebestillingTypeCode type, String azpName) {
		var request = new SlettebestillingRequest(type, 12L, 21L, SlettebestillingHjemmelCode.ARK, ENKELTSLETTING, "begrunnelse");

		assertThrows(UnauthorizedForSlettebestillingException.class, () ->
				slettebestillingService.bestillSletting(request, Optional.ofNullable(azpName)));
	}

	public static Stream<Arguments> shouldCreateCorrectDeletiontime() {
		return Stream.of(
				Arguments.of(DOKUMENT, LocalDate.of(2025, 12, 30)),
				Arguments.of(DOKUMENTER_PA_SAK, LocalDate.of(2026, 12, 9)),
				Arguments.of(SAK, LocalDate.of(2026, 12, 9))
		);
	}

	@ParameterizedTest
	@MethodSource
	public void shouldCreateCorrectDeletiontime(SlettebestillingTypeCode type, LocalDate expectedDate) {
		when(slettebestillingRepository.persist(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArguments()[0]);
		ArgumentCaptor<Slettebestilling> slettebestillingArgumentCaptor = ArgumentCaptor.forClass(Slettebestilling.class);

		var request = new SlettebestillingRequest(type, 12L, 21L, SlettebestillingHjemmelCode.ARK, type == DOKUMENT ? ENKELTSLETTING : BEVARINGSTID, "begrunnelse");

		slettebestillingService.bestillSletting(request, Optional.of(type == DOKUMENT ? AZP_NAME_GOSYS : AZP_NAME_TEAMDOKUMENTHANDTERING));

		verify(slettebestillingRepository).persist(slettebestillingArgumentCaptor.capture());
		assertThat(slettebestillingArgumentCaptor.getValue().getDatoUtfores()).isEqualTo(expectedDate);
	}

}