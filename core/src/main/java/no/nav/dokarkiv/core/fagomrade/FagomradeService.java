package no.nav.dokarkiv.core.fagomrade;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.codes.Fagomrade;
import no.nav.dokarkiv.core.repository.FagomradeRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static java.time.LocalDate.now;
import static no.nav.dokarkiv.core.cache.CacheConfig.FAGOMRADE_CACHE;

@Service
@Slf4j
public class FagomradeService {

	private final FagomradeRepository fagomradeRepository;

	public FagomradeService(FagomradeRepository fagomradeRepository) {
		this.fagomradeRepository = fagomradeRepository;
	}

	@Cacheable(FAGOMRADE_CACHE)
	public boolean erFagomradetInaktivt(String fagomradekode) {
		Optional<Fagomrade> fagomrade = fagomradeRepository.findById(fagomradekode);

		if (fagomrade.isEmpty()) {
			return true;
		}

		boolean fagomradetErInaktivt = fagomrade.filter(
				value -> !value.getErGyldig() && value.getDatoTilOgMed() != null && now().isAfter(value.getDatoTilOgMed())
		).isPresent();

		// TODO: Nedgrader loggmelding til warn etter 1. juli 2023 for å luke ut feil fra kallende systemer
		if (fagomradetErInaktivt) {
			log.error("Fagomrade={} er ikke lenger aktivt etter {}", fagomradekode, fagomrade.get().getDatoTilOgMed());
		}

		return fagomradetErInaktivt;
	}
}
