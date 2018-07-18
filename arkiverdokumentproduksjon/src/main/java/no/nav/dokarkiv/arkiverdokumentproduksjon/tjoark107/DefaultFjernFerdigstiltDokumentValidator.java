package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark107;

import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.NoDokumentInfoFoundException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.NoJournalpostFoundException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.UgyldigDokumentStatusVerdiException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.UgyldigJournalStatusVerdiException;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.ApplicationException;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * Implementation of the interface {@link FjernFerdigstiltDokumentValidator}
 *
 * @author Stig Strøm
 */
@Component
public class DefaultFjernFerdigstiltDokumentValidator implements FjernFerdigstiltDokumentValidator {
	private static final DokumentStatusCode UNDER_REDIGERING = DokumentStatusCode.UNDER_REDIGERING;
	private static final DokumentStatusCode AVBRUTT = DokumentStatusCode.AVBRUTT;

	@Override
	public void validateInputRequest(final FjernFerdigstiltDokumentRequestTo request) {
		if (request.getJournalpostId() == null || request.getJournalpostId() == 0) {
			throw new IllegalArgumentException("JournalpostId cannot be empty or missing. " + request);
		}
		
		if (request.getDokumentInfoId() == null || request.getDokumentInfoId() == 0) {
			throw new IllegalArgumentException("DokumentInfoId cannot be empty or missing. " + request);
		}
		Assert.hasText(request.getEndretAvNavn(), "EndretAvNavn cannot be empty or missing. " + request);
	}
	
	@Override
	public void validate(final Journalpost journalpost, final FjernFerdigstiltDokumentRequestTo request)
			throws UgyldigJournalStatusVerdiException, NoDokumentInfoFoundException, UgyldigDokumentStatusVerdiException,
			NoJournalpostFoundException {

		validateJournalpost(journalpost, request);
		validateDokumentInfoAndFildetaljer(journalpost, request);
	}
	

	private void validateJournalpost(final Journalpost journalpost, final FjernFerdigstiltDokumentRequestTo request)
			throws UgyldigJournalStatusVerdiException {		
		if (!JournalStatusCode.D.equals(journalpost.getJournalstatus())) {
			throw new UgyldigJournalStatusVerdiException("Invalid JournalStatus for journalpostId="
					+ request.getJournalpostId(), journalpost.getJournalstatus());
		}
	}

	private void validateDokumentInfoAndFildetaljer(final Journalpost journalpost,
			final FjernFerdigstiltDokumentRequestTo request) throws NoDokumentInfoFoundException,
			UgyldigDokumentStatusVerdiException {
		DokumentInfo dokumentInfo = findDokumentInfoOnJournalpost(journalpost, request.getDokumentInfoId());
		switch (dokumentInfo.getDokumentstatus()) {
		case UNDER_REDIGERING:
			throw new UgyldigDokumentStatusVerdiException("DokumentInfoId=" + dokumentInfo.getDokumentInfoId()
					+ " is already Under redigering", UNDER_REDIGERING);
		case AVBRUTT:
			throw new UgyldigDokumentStatusVerdiException("DokumentInfoId=" + dokumentInfo.getDokumentInfoId()
					+ " is already Avbrutt", AVBRUTT);
		default:
			break;
		}

		FilDetaljer arkivFilDetaljer = dokumentInfo.findFilDetaljerByVariantFormat(VariantFormatCode.ARKIV);
		if (arkivFilDetaljer == null) {
			throw new ApplicationException("Cannot find a Fildetaljer with VariantFormat ARKIV for dokumentInfoId="
					+ dokumentInfo.getDokumentInfoId());
		}
	}
	
	private DokumentInfo findDokumentInfoOnJournalpost(final Journalpost journalpost, final Long dokumentInfoId)
			throws NoDokumentInfoFoundException {
		DokumentInfo dokumentInfo = journalpost.findDokumentInfoById(dokumentInfoId);
		if (dokumentInfo == null) {
			throw new NoDokumentInfoFoundException("Journalpost missing DokumentInfo with dokumentInfoId=" + dokumentInfoId,
					dokumentInfoId);
		}
		return dokumentInfo;
	}

}
