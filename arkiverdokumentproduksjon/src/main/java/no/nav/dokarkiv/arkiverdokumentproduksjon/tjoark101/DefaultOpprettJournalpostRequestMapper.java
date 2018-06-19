package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark101;

import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.sporing.KildeNavnPopulator;
import no.nav.dokarkiv.core.stelvio.RequestContextHolder;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.OpprettJournalpostRequest;
import org.dozer.Mapper;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Implementation of OpprettJournalpostRequestMapper. Does the mapping of
 * webservice requst to domain request
 *
 * @author Stig Strøm
 */
public class DefaultOpprettJournalpostRequestMapper implements
		OpprettJournalpostRequestMapper {

	private Mapper dozerMapper;

	@Inject
	private KildeNavnPopulator kildeNavnPopulator;

	@Override
	public OpprettJournalpostRequestTo map(OpprettJournalpostRequest wsRequest) {
		Journalpost domainJournalpost = dozerMapper.map(wsRequest.getJournalpost(), Journalpost.class);
		kildeNavnPopulator.populateKildeNavnForEntireJournalStructure(domainJournalpost, RequestContextHolder
				.currentRequestContext().getComponentId());
		return new OpprettJournalpostRequestTo(domainJournalpost);
	}


	@Inject
	@Named("dozerMapper")
	public void setDozerMapper(Mapper dozerMapper) {
		this.dozerMapper = dozerMapper;
	}

}
