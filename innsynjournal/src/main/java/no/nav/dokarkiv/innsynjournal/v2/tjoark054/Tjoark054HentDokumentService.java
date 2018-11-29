package no.nav.dokarkiv.innsynjournal.v2.tjoark054;

import no.nav.dokarkiv.core.domain.entities.DokumentFil;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.innsynjournal.v2.exceptions.DocumentNotFoundException;
import no.nav.dokarkiv.innsynjournal.v2.exceptions.SecurityLimitationAttributeException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * The service layer class for HentDokument(TJOARK051 and TJOARK054)
 *
 * @author Stig Strøm
 * @author Thomas Kåsene, Visma Consulting AS
 */
@Component
public class Tjoark054HentDokumentService extends AbstractJournalOperations {

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

		if (StringUtils.isNotEmpty(filDetaljer.getOnDemandId()) && filDetaljer.getOnDemandInstans() != null) {
			// pga lisens tillater man ikke henting fra OnDemand
			throw new SecurityLimitationAttributeException(request.getJournalpostId(),
					request.getDokumentInfoId(),
					Collections.singletonMap("DokumentInfo.Fildetaljer.OnDemandId", filDetaljer.getOnDemandId()));
		}

		DokumentFil dokumentFil = getDocumentFromDBRepository(filDetaljer.getFilUuid());
		return dokumentFil.getFil();
	}
}
