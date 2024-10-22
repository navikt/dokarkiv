package no.nav.dokarkiv.core.consumer.pdl;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.client.HttpServerErrorException;

import java.util.List;

import static no.nav.dokarkiv.core.storage.RetryConstants.DELAY_SHORT;
import static no.nav.dokarkiv.core.storage.RetryConstants.MULTIPLIER_SHORT;

/**
 * Interface for tjenester relatert til henting av identer.
 */
public interface IdentConsumer {
	/**
	 * Henter NAV intern aktørId for folkeregisterIdent.
	 *
	 * @param folkeregisterIdent Folkeregisterident tilhørende person
	 * @return NAV intern aktørId
	 * @throws PersonIkkeFunnetException Finner ikke person
	 */
	String hentAktoerId(final String folkeregisterIdent) throws PersonIkkeFunnetException;

	/**
	 * Henter Folkeregisterets fødselsnummer for NAV intern aktørId
	 *
	 * @param aktoerId NAV intern aktørId
	 * @return Folkeregister ident
	 * @throws PersonIkkeFunnetException Finner ikke person
	 */
	String hentFolkeregisterIdent(final String aktoerId) throws PersonIkkeFunnetException;

	/**
	 * Henter historiske folkeregister identer for folkeregisterIdent.
	 * Et typisk tilfelle er at en person har fått ett D-nummer og deretter et fødselsnummer i folkeregisteret.
	 *
	 * @param folkeregisterIdent Folkeregisterident tilhørende person
	 * @return Liste av historiske folkeregisteridenter tilhørende person
	 * @throws PersonIkkeFunnetException Finner ikke person
	 */
	List<String> hentHistoriskeFolkeregisterIdenter(final String folkeregisterIdent) throws PersonIkkeFunnetException;

	List<String> hentHistoriskeAktoerIdsForAktoerId(String folkeregisterIdent) throws PersonIkkeFunnetException;


	/**
	 * Henter personens fulle navn
	 *
	 * @param id   Folkeregisterident tilhørende person
	 * @param tema Tema for tilgang til PDL
	 * @return Personens fulle navn
	 */
	String hentPersonnavn(String id, String tema);
}
