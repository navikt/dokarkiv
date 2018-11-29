package no.nav.dokarkiv.journal.v3.tjoark051;

import no.nav.dokarkiv.core.domain.entities.DokumentFil;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.DocumentNotFoundException;
import no.nav.dokarkiv.core.exceptions.InvalidFilUuidException;
import no.nav.dokarkiv.core.exceptions.NoJournalpostFoundException;
import no.nav.dokarkiv.core.ondemand.HentOndemandDokument;
import no.nav.tjeneste.virksomhet.journal.v3.HentDokumentSikkerhetsbegrensning;
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
public class Tjoark051HentDokumentService extends AbstractJournalOperations {

	@Inject
	private HentOndemandDokument hentOndemandDokument;

	/**
	 * Search and retrieves the document, will get documents from OnDemand as well
	 *
	 * @param request domain request with document info
	 * @return the byte array of the document
	 * @throws DocumentNotFoundException Cannot find journalpost, dokumentinfo or fildetaljer
	 */
	public byte[] hentDokument(HentDokumentRequestTo request) throws DocumentNotFoundException, HentDokumentSikkerhetsbegrensning {
		Journalpost journalpost = lookupJournalpost(request.getJournalpostId());

		DokumentInfo dokumentInfo = getDokumentInfo(journalpost, request.getDokumentInfoId());
		FilDetaljer filDetaljer = getFilDetaljer(journalpost.getJournalpostId(), dokumentInfo, request.getVariantFormat());
		generateAuditLogIfDokumentIsSensitivt(journalpost, filDetaljer, "hentDokument");

		if (dokumentInfo.getSlettet() != null && dokumentInfo.getSlettet()) {
			throw new HentDokumentSikkerhetsbegrensning("Dokument med journalpostId=" + request.getJournalpostId() + " er slettet.");
		}

		if (StringUtils.isNotEmpty(filDetaljer.getOnDemandId()) && filDetaljer.getOnDemandInstans() != null) {
			try {
				String dokumentUrl = hentOndemandDokument.createDokumentUrl(request.getJournalpostId(), filDetaljer.getFilUuid()).getDokumentUrl();
				return hentOndemandDokument.hentOndemandDokumentFromJoark(dokumentUrl);
			} catch (InvalidFilUuidException | NoJournalpostFoundException e) {
				throw new DocumentNotFoundException("Dokument med journalpostId=" + request.getJournalpostId() + ", filUuid=" + filDetaljer.getFilUuid() + " ikke funnet i OnDemand.", e);
			}
		}

		DokumentFil dokumentFil = getDocumentFromDBRepository(filDetaljer.getFilUuid());
		return dokumentFil.getFil();
	}
}
