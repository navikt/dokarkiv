package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark106;

import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.UgyldigDokumentStatusVerdiException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.UgyldigJournalStatusVerdiException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.UgyldigTilknyttetJournalpostSomVerdiException;
import no.nav.dokarkiv.core.exceptions.NoDokumentInfoFoundException;
import no.nav.dokarkiv.core.exceptions.NoJournalpostFoundException;

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
	 * @throws NoJournalpostFoundException                   when cannot find a journalpost in the input
	 * @throws NoDokumentInfoFoundException                  when cannot find dokumentinfo in the input
	 * @throws UgyldigJournalStatusVerdiException            JournalStatus is not D
	 * @throws UgyldigDokumentStatusVerdiException           DokumentStatus is already AVBRUTT
	 * @throws UgyldigTilknyttetJournalpostSomVerdiException TilknyttetJournalpostSomStatus is not VEDLEGG
	 */
	void avbrytVedlegg(AvbrytVedleggRequestTo domainRequest) throws NoJournalpostFoundException, NoDokumentInfoFoundException,
			UgyldigJournalStatusVerdiException, UgyldigDokumentStatusVerdiException,
			UgyldigTilknyttetJournalpostSomVerdiException;
}
