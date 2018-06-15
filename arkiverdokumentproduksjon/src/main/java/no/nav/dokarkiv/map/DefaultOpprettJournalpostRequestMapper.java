package no.nav.dokarkiv.map;

import no.nav.domain.dok.joark.Journalpost;
import no.nav.provider.dok.joark.nsb.map.OpprettJournalpostRequestMapper;
import no.nav.service.dok.joark.journalbehandling.KildeNavnPopulator;
import no.nav.service.dok.joark.nsb.to.OpprettJournalpostRequestTo;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.OpprettJournalpostRequest;
import no.stelvio.common.context.RequestContextHolder;
import org.dozer.Mapper;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Implementation of OpprettJournalpostRequestMapper. Does the mapping of
 * webservice requst to domain request
 * 
 * @author Stig Strøm
 *
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
