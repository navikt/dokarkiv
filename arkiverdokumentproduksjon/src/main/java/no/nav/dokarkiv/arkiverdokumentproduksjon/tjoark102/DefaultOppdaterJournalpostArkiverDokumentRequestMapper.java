package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark102;

import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.UgyldigInputException;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.OppdaterJournalpostArkiverDokumentRequest;
import org.dozer.Mapper;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Implementation of OppdaterJournalpostArkiverDokumentRequestMapper
 *
 * @author Torgeir Cook
 */
public class DefaultOppdaterJournalpostArkiverDokumentRequestMapper implements
		OppdaterJournalpostArkiverDokumentRequestMapper {

	private Mapper dozerMapper;

	@Override
	public OppdaterJournalpostArkiverDokumentRequestTo map(OppdaterJournalpostArkiverDokumentRequest wsRequest) throws UgyldigInputException {
		try {
			OppdaterJournalpostArkiverDokumentRequestTo domainRequest =
					dozerMapper.map(wsRequest, OppdaterJournalpostArkiverDokumentRequestTo.class);
			return domainRequest;
		} catch (Exception e) {
			throw new UgyldigInputException(e.getMessage(), wsRequest.getJournalpostId());
		}
	}

	@Inject
	@Named("dozerMapper")
	public void setDozerMapper(Mapper dozerMapper) {
		this.dozerMapper = dozerMapper;
	}
}
