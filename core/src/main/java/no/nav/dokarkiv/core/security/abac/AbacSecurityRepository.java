package no.nav.dokarkiv.core.security.abac;

/**
 * Defines Joark repository for extracting objects used for evaluating ABAC policies in axiomatics
 *
 * @author Martin Burheim Tingstad, Visma Consulting
 */
public interface AbacSecurityRepository {
    AbacResources findAbacResources(Long journalpostId);
}
