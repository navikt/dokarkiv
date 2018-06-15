package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark108;

import static no.nav.domain.dok.joark.codestable.DokumentStatusCode.UNDER_REDIGERING;
import static no.nav.domain.dok.joark.codestable.JournalStatusCode.D;
import static no.nav.domain.dok.joark.codestable.JournalStatusCode.FL;
import static no.nav.domain.dok.joark.codestable.JournalStatusCode.FS;

import no.nav.domain.dok.joark.DokumentInfo;
import no.nav.domain.dok.joark.FilDetaljer;
import no.nav.domain.dok.joark.Journalpost;
import no.nav.domain.dok.joark.JournalpostDokumentInfoRelasjon;
import no.nav.domain.dok.joark.codestable.JournalStatusCode;
import no.nav.domain.dok.joark.codestable.VariantFormatCode;
import no.nav.modig.core.exception.ApplicationException;
import no.nav.service.dok.joark.journalbehandling.UgyldigDokumentStatusVerdiException;
import no.nav.service.dok.joark.journalbehandling.UgyldigJournalStatusVerdiException;
import no.nav.service.dok.joark.nsb.to.FerdigstillJournalpostRequestTo;
import org.springframework.util.Assert;

/**
 * Implementation for the {@link FerdigstillJournalpostValidator} interface
 * 
 * @author Stig Strøm
 *
 */
public class DefaultFerdigstillJournalpostValidator implements FerdigstillJournalpostValidator {

	@Override
	public void validateInputRequest(FerdigstillJournalpostRequestTo request) {
		if (request.getJournalpostId() == null || request.getJournalpostId() == 0) {
			throw new IllegalArgumentException("JournalpostId cannot be empty or missing. " + request);
		}
		
		Assert.notNull(request.getUtsendingskanal(), "Utsendingskanal cannot be empty or missing. " + request);
		Assert.hasText(request.getEndretAvNavn(), "EndretAvNavn cannot be empty or missing. " + request);
	}
	
	@Override
	public void validate(final Journalpost journalpost) throws UgyldigJournalStatusVerdiException,
			UgyldigDokumentStatusVerdiException {
		validateIfJournalpostHasHoveddokument(journalpost);
        validateIfJournalpostHasCorrectStatus(journalpost);
        validateDokumentInfoAndFildetaljer(journalpost);
	}
	
	private void validateIfJournalpostHasHoveddokument(final Journalpost journalpost) {
		JournalpostDokumentInfoRelasjon hoveddokumentDokumentInfoRelasjon = journalpost.findHoveddokumentDokumentInfoRelasjon();
		if (hoveddokumentDokumentInfoRelasjon == null) {
			throw new ApplicationException("Cannot find hoveddokument for journalpostId=" + journalpost.getJournalpostId());
		}
	}

    private void validateIfJournalpostHasCorrectStatus(final Journalpost journalpost) throws UgyldigJournalStatusVerdiException {
        JournalStatusCode journalstatus = journalpost.getJournalstatus();

        if (journalstatus != D && journalstatus != FS && journalstatus != FL) {
            throw new UgyldigJournalStatusVerdiException("Expected one of Journalstatus.D or Journalstatus.FS or Journalstatus.FL for journalpostId="
                    + journalpost.getJournalpostId(), journalstatus);
        }
	}
	
	private void validateDokumentInfoAndFildetaljer(final Journalpost journalpost) throws UgyldigDokumentStatusVerdiException {
		for (DokumentInfo dokumentInfo : journalpost.findAllDokumentInfos()) {
			validateDokumentInfoIsNotUnderRedigering(journalpost, dokumentInfo);			
			validateThatFildetaljerIsArchived(journalpost, dokumentInfo);
		}
	}

	private void validateThatFildetaljerIsArchived(final Journalpost journalpost, DokumentInfo dokumentInfo) {
		FilDetaljer arkivFilDetaljer = dokumentInfo.findFilDetaljerByVariantFormat(VariantFormatCode.ARKIV);
		if (arkivFilDetaljer == null) {
			throw new ApplicationException("Found Fildetaljer without VariantFormat ARKIV for journalpostId="
					+ journalpost.getJournalpostId() + " dokumentInfoId=" + dokumentInfo.getDokumentInfoId());
		}
	}

	private void validateDokumentInfoIsNotUnderRedigering(final Journalpost journalpost, DokumentInfo dokumentInfo)
			throws UgyldigDokumentStatusVerdiException {
		if (UNDER_REDIGERING.equals(dokumentInfo.getDokumentstatus())) {
			throw new UgyldigDokumentStatusVerdiException("Illegal dokument status for dokumentInfoId="
					+ dokumentInfo.getDokumentInfoId() + ",journalpostId=" + journalpost.getJournalpostId(),
					UNDER_REDIGERING);
		}
	}

}
