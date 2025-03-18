package no.nav.dokarkiv.core.consumer.pdl;

import java.util.List;

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
	 * Henter alle aktørIds for ident (inkludert historiske identer)
	 *
	 * @param ident Både aktørId og folkeregisterIdent
	 * @return Liste av alle aktørIds for ident. Inkludert historiske
	 * @throws PersonIkkeFunnetException
	 */
	List<String> hentAlleAktoerIdsForIdent(final String ident) throws PersonIkkeFunnetException;

	/**
	 * Henter personens fulle navn
	 *
	 * @param id   Folkeregisterident tilhørende person
	 * @param tema Tema for tilgang til PDL
	 * @return Personens fulle navn
	 */
	String hentPersonnavn(String id, String tema);
}
