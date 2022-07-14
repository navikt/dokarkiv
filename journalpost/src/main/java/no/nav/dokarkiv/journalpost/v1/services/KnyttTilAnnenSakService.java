package no.nav.dokarkiv.journalpost.v1.services;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.consumers.saf.SafJournalpostQueryService;
import no.nav.dokarkiv.core.consumers.saf.journalpost.SafJournalpostTo;
import no.nav.dokarkiv.journalpost.v1.api.Bruker;
import no.nav.dokarkiv.journalpost.v1.api.Fagsaksystem;
import no.nav.dokarkiv.journalpost.v1.api.KnyttTilAnnenSakRequest;
import no.nav.dokarkiv.journalpost.v1.api.KnyttTilAnnenSakResponse;
import no.nav.dokarkiv.journalpost.v1.api.OppdaterJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.api.Sak;
import no.nav.dokarkiv.journalpost.v1.api.Sakstype;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static no.nav.dokarkiv.journalpost.v1.util.knyttTilAnnenSak.DokumentUtils.sjekkOmAlleDokumentvarianterErGyldige;

@Slf4j
@Service
public class KnyttTilAnnenSakService {
	private final SafJournalpostQueryService safJournalpostQueryService;
	private final KopierJournalpostService kopierJournalpostService;
	private final OppdaterJournalpostService oppdaterJournalpostService;
	private final FerdigstillJournalpostService ferdigstillJournalpostService;

	@Autowired
	public KnyttTilAnnenSakService(SafJournalpostQueryService safJournalpostQueryService,
								   KopierJournalpostService kopierJournalpostService,
								   OppdaterJournalpostService oppdaterJournalpostService,
								   FerdigstillJournalpostService ferdigstillJournalpostService
	) {
		this.safJournalpostQueryService = safJournalpostQueryService;
		this.kopierJournalpostService = kopierJournalpostService;
		this.oppdaterJournalpostService = oppdaterJournalpostService;
		this.ferdigstillJournalpostService = ferdigstillJournalpostService;
	}

	public KnyttTilAnnenSakResponse knyttTilAnnenSak(KnyttTilAnnenSakRequest knyttTilAnnenSakRequest, String kildeJournalpostId, String authorizationHeader) {
		// 3. Sjekk tilgang til å knytte dokumenter på journalpost til ny sak.
		SafJournalpostTo safJournalpostFra = safJournalpostQueryService.hentJournalpost(kildeJournalpostId, authorizationHeader);
		sjekkOmAlleDokumentvarianterErGyldige(safJournalpostFra, kildeJournalpostId);

		// 4. Kopier kildejournalpost, ny journalpost vil få midlertidlig journalpostStatus = "OD"/"R"
		Long nyJournalpostId = kopierJournalpostService.kopierJournalpost(Long.parseLong(kildeJournalpostId));
		log.info("knyttTilAnnenSak har kopiert journalpost {} til ny journalpost med journalpostId={}", kildeJournalpostId, nyJournalpostId);

		// 5. Oppdater journalpost med ny sak
		oppdaterJournalpostService.knyttTilAnnenSakOppdaterJournalpost(nyJournalpostId, opprettOppdaterJournalpostRequest(knyttTilAnnenSakRequest));
		log.warn("knyttTilAnnenSak har oppdatert ny journalpost {} med parametere fra payload", nyJournalpostId);

		// 6. Ferdigstill ny journalpost, vil sette journalpost i endelig journalpostStatus.
		ferdigstillJournalpostService.ferdigstill(nyJournalpostId, knyttTilAnnenSakRequest.getJournalfoerendeEnhet());
		//ferdigstillJournalpostConsumer.ferdigstillJournalpost(nyJournalpostId, ferdigstillJournalpostRequest, authorizationHeader, navConsumerToken);
		log.info("knyttTilAnnenSak har ferdigstilt ny journalpost {}", nyJournalpostId);

		// 7. Returner ny journalpostId
		return KnyttTilAnnenSakResponse.builder().nyJournalpostId(nyJournalpostId).build();
	}

	private OppdaterJournalpostRequest opprettOppdaterJournalpostRequest(KnyttTilAnnenSakRequest knyttTilAnnenSakRequest) {
		Bruker bruker = Bruker.builder().
				id(knyttTilAnnenSakRequest.getBruker().getId()).
				idType(knyttTilAnnenSakRequest.getBruker().getIdType()).
				build();
		Sak sak = Sak.builder().
				fagsakId(knyttTilAnnenSakRequest.getFagsakId()).
				fagsaksystem(Fagsaksystem.valueOf(knyttTilAnnenSakRequest.getFagsaksystem())).
				sakstype(Sakstype.valueOf(knyttTilAnnenSakRequest.getSakstype())).
				build();
		return OppdaterJournalpostRequest.builder().
				tema(knyttTilAnnenSakRequest.getTema()).
				bruker(bruker).
				sak(sak).
				build();
	}
}
