package no.nav.dokarkiv.core.consumer.pdl;

import java.util.List;

/**
 * Interface for tjenester relatert til henting av identer.
 *
 * @author Roar Bjurstrom, Visma Consulting.
 */
public interface IdentConsumer {
	/**
	 * Henter NAV intern aktørId for folkeregisterIdent.
	 *
	 * @param folkeregisterIdent Folkeregisterident tilhørende person
	 * @param tema for journalpost
	 * @return NAV intern aktørId
	 * @throws PersonIkkeFunnetException Finner ikke person
	 */
	PersonIdent hentAktoer(final String folkeregisterIdent, final String tema) throws PersonIkkeFunnetException;

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
}
