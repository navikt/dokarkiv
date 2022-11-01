package no.nav.dokarkiv.journal.v3.tjoark051;

import no.nav.dokarkiv.core.domain.entities.DokumentFil;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.DocumentNotFoundException;
import no.nav.dokarkiv.core.exceptions.InvalidFilUuidException;
import no.nav.dokarkiv.core.exceptions.NoJournalpostFoundException;
import no.nav.dokarkiv.core.ondemand.HentOndemandDokument;
import no.nav.dokarkiv.core.repository.DokumentFilSkjermetRepository;
import no.nav.dokarkiv.core.repository.JoarkRepositorySkjermet;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * The service layer class for HentDokument(TJOARK051 and TJOARK054)
 */
@Component
public class Tjoark051HentDokumentService extends AbstractJournalOperations {

	private final HentOndemandDokument hentOndemandDokument;

	public Tjoark051HentDokumentService(JoarkRepositorySkjermet joarkRepository, DokumentFilSkjermetRepository dokumentFilRepository, HentOndemandDokument hentOndemandDokument) {
		super(joarkRepository, dokumentFilRepository);
		this.hentOndemandDokument = hentOndemandDokument;
	}

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
