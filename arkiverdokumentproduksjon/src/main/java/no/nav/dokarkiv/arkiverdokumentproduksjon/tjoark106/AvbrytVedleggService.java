package no.nav.service.dok.joark.nsb;

import no.nav.service.dok.joark.NoJournalpostFoundException;
import no.nav.service.dok.joark.journalbehandling.NoDokumentInfoFoundException;
import no.nav.service.dok.joark.journalbehandling.UgyldigDokumentStatusVerdiException;
import no.nav.service.dok.joark.journalbehandling.UgyldigJournalStatusVerdiException;
import no.nav.service.dok.joark.nsb.exceptions.UgyldigTilknyttetJournalpostSomVerdiException;
import no.nav.service.dok.joark.nsb.to.AvbrytVedleggRequestTo;

/**
 * Interface for operation AvbrytVedlegg
 *
 * @author Roar Bjurstrom, Visma Consulting
 */
public interface AvbrytVedleggService {

	/**
	 * Sets dokumentStatus on a Vedlegg to Avbrutt
	 *
	 * @param domainRequest the domain request
	 * @throws NoJournalpostFoundException when cannot find a journalpost in the input
	 * @throws NoDokumentInfoFoundException when cannot find dokumentinfo in the input
	 * @throws UgyldigJournalStatusVerdiException JournalStatus is not D
	 * @throws UgyldigDokumentStatusVerdiException DokumentStatus is already AVBRUTT
	 * @throws UgyldigTilknyttetJournalpostSomVerdiException TilknyttetJournalpostSomStatus is not VEDLEGG
	 */
	void avbrytVedlegg(AvbrytVedleggRequestTo domainRequest) throws NoJournalpostFoundException, NoDokumentInfoFoundException,
			UgyldigJournalStatusVerdiException, UgyldigDokumentStatusVerdiException,
			UgyldigTilknyttetJournalpostSomVerdiException;
}
