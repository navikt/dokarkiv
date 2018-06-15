package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark106;

import no.nav.domain.dok.joark.DokumentInfo;
import no.nav.domain.dok.joark.Journalpost;
import no.nav.domain.dok.joark.JournalpostDokumentInfoRelasjon;
import no.nav.service.dok.joark.NoJournalpostFoundException;
import no.nav.service.dok.joark.journalbehandling.NoDokumentInfoFoundException;
import no.nav.service.dok.joark.journalbehandling.UgyldigDokumentStatusVerdiException;
import no.nav.service.dok.joark.journalbehandling.UgyldigJournalStatusVerdiException;
import no.nav.service.dok.joark.nsb.exceptions.UgyldigTilknyttetJournalpostSomVerdiException;
import no.nav.service.dok.joark.nsb.to.AvbrytVedleggRequestTo;

/**
 * Interface for AvbrytVedleggtValidator. Used for validating request and that the journalpost is in correct state.
 *
 * @author Roar Bjurstrom, Visma Consulting
 */
public interface AvbrytVedleggValidator {

	/**
	 * Validates the Journalpost used in AvbrytVedleggService
	 *
	 * @param journalpost The journalpost to validate.
	 * @param journalpostId The journalpostId of the requested journalpost. Equal to
	 * the passed journalpost.journalpostId if journalpost is not null
	 * @throws UgyldigJournalStatusVerdiException JournalpostStatus is not D (Under arbeid)
	 * @throws NoJournalpostFoundException A journalpost with the requested journalpostId does not exist. Journalpost is null.
	 */
	void validateJournalpost(Journalpost journalpost, Long journalpostId) throws UgyldigJournalStatusVerdiException,
			NoJournalpostFoundException;

	/**
	 * Validates the DokumentInfo used in AvbrytVedleggService
	 *
	 * @param dokumentInfo The dokumentInfo to validate
	 * @param dokumentInfoId The dokumentInfoId of the requested DokumentInfo. Should be equal to
	 * the passed dokumentInfo.dokumentInfoId if DokumentInfo is not null
	 * @throws NoDokumentInfoFoundException A DokumentInfo with the requested dokumentInfoId does
	 * not exist. DokumentInfo is null.
	 * @throws UgyldigDokumentStatusVerdiException DokumentStatusVerdi is not AVBRUTT
	 */
	void validateDokumentInfo(DokumentInfo dokumentInfo, Long dokumentInfoId) throws NoDokumentInfoFoundException,
			UgyldigDokumentStatusVerdiException;

	/**
	 * Validates the JournalpostDokumentInfoRelasjon between Journalpost and DokumentInfo
	 *
	 * @param journalpostDokumentInfoRelasjon the JournalpostDokumentInforRelasjon to validate
	 * @throws UgyldigTilknyttetJournalpostSomVerdiException TilknyttetJournalpostSom is not VEDLEGG
	 */
	void validateJournalpostDokumentInfoRelasjon(JournalpostDokumentInfoRelasjon journalpostDokumentInfoRelasjon) throws
			UgyldigTilknyttetJournalpostSomVerdiException;

	/**
	 * Validates input request.
	 *
	 * @param request The request to validate
	 */
	void validateInputRequest(AvbrytVedleggRequestTo request);
}
