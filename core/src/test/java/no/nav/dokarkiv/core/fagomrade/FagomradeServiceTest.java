package no.nav.dokarkiv.core.fagomrade;

import no.nav.dokarkiv.core.domain.codes.Fagomrade;
import no.nav.dokarkiv.core.repository.FagomradeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FagomradeServiceTest {

	private static final String AKTIVT_FAGOMRADE = "FOR";
	private static final String INAKTIVT_FAGOMRADE = "UKJ";
	private static final String UGYLDIG_FAGOMRADE = "ABC";

	@Mock
	FagomradeRepository fagomradeRepository;

	@InjectMocks
	FagomradeService fagomradeService;

	@Test
	void skalReturnereInaktivtFagomrade() {
		Fagomrade ugyldigFagomrade = Fagomrade.builder()
				.kode(INAKTIVT_FAGOMRADE).erGyldig(false).datoTilOgMed(LocalDate.now().minusDays(1))
				.build();
		when(fagomradeRepository.findById(INAKTIVT_FAGOMRADE)).thenReturn(Optional.of(ugyldigFagomrade));

		boolean fagomradetErUgyldig = fagomradeService.erFagomradetInaktivt(INAKTIVT_FAGOMRADE);

		assertThat(fagomradetErUgyldig).isTrue();
	}

	@Test
	void skalReturnereInaktivtFagomradeForUgyldigFagomradekode() {
		when(fagomradeRepository.findById(UGYLDIG_FAGOMRADE)).thenReturn(Optional.empty());

		boolean fagomradetErUgyldig = fagomradeService.erFagomradetInaktivt(UGYLDIG_FAGOMRADE);

		assertThat(fagomradetErUgyldig).isTrue();
	}

	@Test
	void skalReturnereAktivtFagomrade() {
		Fagomrade gyldigFagomrade = Fagomrade.builder().kode(AKTIVT_FAGOMRADE).erGyldig(true).datoTilOgMed(null).build();
		when(fagomradeRepository.findById(AKTIVT_FAGOMRADE)).thenReturn(Optional.of(gyldigFagomrade));

		boolean fagomradetErUgyldig = fagomradeService.erFagomradetInaktivt(AKTIVT_FAGOMRADE);

		assertThat(fagomradetErUgyldig).isFalse();
	}

}