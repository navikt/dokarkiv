package no.nav.dokarkiv.journalpost.v1.services;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.journalpost.v1.api.AvsluttAlleSakerPaaTemaRequest;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AvsluttAlleSakerPaaTemaService {

	public void avsluttAlleSakerPaaTema(AvsluttAlleSakerPaaTemaRequest request) {
		log.info("Inni avsluttAlleSakerPaaTemaService");
	}
}