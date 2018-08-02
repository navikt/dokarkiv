package no.nav.dokarkiv.journal.v3.tjoark051;

import no.nav.dokarkiv.core.domain.entities.DokumentFil;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.InvalidFilUuidException;
import no.nav.dokarkiv.core.exceptions.NoJournalpostFoundException;
import no.nav.dokarkiv.core.ondemand.HentOndemandDokument;
import no.nav.dokarkiv.journal.v3.exceptions.DocumentNotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * The service layer class for HentDokument(TJOARK051 and TJOARK054)
 *
 * @author Stig Strøm
 * @author Thomas Kåsene, Visma Consulting AS
 */
@Component
public class HentDokumentService extends AbstractJournalOperations {

	@Inject
	private HentOndemandDokument hentOndemandDokument;

	/**
	 * Search and retrieves the document, will get documents from OnDemand as well
	 *
	 * @param request domain request with document info
	 * @return the byte array of the document
	 * @throws DocumentNotFoundException Cannot find journalpost, dokumentinfo or fildetaljer
	 */
	public byte[] hentDokument(HentDokumentRequestTo request) throws DocumentNotFoundException {
		Journalpost journalpost = lookupJournalpost(request.getJournalpostId());

		DokumentInfo dokumentInfo = getDokumentInfo(journalpost, request.getDokumentInfoId());
		FilDetaljer filDetaljer = getFilDetaljer(dokumentInfo, request.getVariantFormat());
		generateAuditLogIfDokumentIsSensitivt(journalpost, filDetaljer, "hentDokument");

		if (StringUtils.isNotEmpty(filDetaljer.getOnDemandId())) {
			try {
				return hentOndemandDokument.hentOndemandDokumentFromJoark(request.getJournalpostId(), filDetaljer.getFilUuid());
			} catch (InvalidFilUuidException | NoJournalpostFoundException e) {
				throw new DocumentNotFoundException("Dokument med journalpostId=" + request.getJournalpostId() + ", filUuid=" + filDetaljer.getFilUuid() + " ikke funnet i OnDemand.", e);
			}
		}

		DokumentFil dokumentFil = getDocumentFromDBRepository(filDetaljer.getFilUuid());
		return dokumentFil.getFil();
	}
}
