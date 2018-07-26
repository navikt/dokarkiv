package no.nav.dokarkiv.behandleinngaaendejournal.v1.tjoark066;

import com.google.common.base.Strings;
import lombok.NoArgsConstructor;
import no.nav.dokarkiv.behandleinngaaendejournal.v1.exceptions.DokumentInfoIkkeTilknyttetJournalpostException;
import no.nav.dokarkiv.behandleinngaaendejournal.v1.exceptions.OppdaterJournalpostIkkeMuligException;
import no.nav.dokarkiv.behandleinngaaendejournal.v1.exceptions.UgyldigInputException;
import no.nav.dokarkiv.behandleinngaaendejournal.v1.tjoark066.to.DokumentInformasjonTo;
import no.nav.dokarkiv.behandleinngaaendejournal.v1.tjoark066.to.OppdaterJournalpostRequestTo;
import no.nav.dokarkiv.behandleinngaaendejournal.v1.tjoark066.to.OppdaterJournalpostTo;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeInngaaendeException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeMidlertidigException;

import java.util.List;
import java.util.Set;

/**
 * Validator for OppdaterJournalpostService
 *
 * @author Leo-Andreas Ervik, Visma Consulting. 02.06.2017.
 */
@NoArgsConstructor
public class OppdaterJournalpostValidator {
	
	public void validateInput(OppdaterJournalpostRequestTo request) {
		if (request.getOppdaterJournalpostTo() == null) {
			throw new UgyldigInputException("Tjenesten kan ikke utføres fordi OppdaterJournalpost i kallet er null.");
		}
		
		OppdaterJournalpostTo input = request.getOppdaterJournalpostTo();
		
		if (input.getJournalpostId() == null) {
			throw new UgyldigInputException("Tjenesten kan ikke utføres fordi input er ugyldig. journalpostId=null");
		}
		
		validateJournalpostId(request.getOppdaterJournalpostTo().getJournalpostId());
		validateBruker(input);
		validateArkivSak(input);
		if (!isValidHoveddokument(input)) {
			throw new UgyldigInputException("Mangler informasjon på Hoveddokument for oppdatering av journalpost. journalpostId=" + input
					.getJournalpostId());
		}
		if (!isValidVedlegg(input.getVedlegg())) {
			throw new UgyldigInputException("Mangler informasjon på Vedlegg for oppdatering av journalpost. journalpostId=" + input
					.getJournalpostId());
		}
	}
	
	
	public void validateJournalpost(Journalpost journalpost, OppdaterJournalpostTo to) {
		if (journalpost == null) {
			throw new JournalpostIkkeFunnetException("Journalpost ikke funnet. journalpostId=" + to.getJournalpostId());
		}
		
		Long journalpostId = journalpost.getJournalpostId();
		Set<JournalpostDokumentInfoRelasjon> relasjoner = journalpost.getJournalpostDokumentInfoRelasjoner();
		
		if (to.getHoveddokument() != null) {
			validateHoveddokument(to, journalpostId, relasjoner);
		}
		if (to.getVedlegg() != null && !to.getVedlegg().isEmpty()) {
			validateVedlegg(to, journalpostId, relasjoner);
		}
		
		if (!journalpost.isInngaende()) {
			throw new JournalpostIkkeInngaaendeException("Journalpost er ikke av type Inngående. journalpostId=" + journalpost.getJournalpostId());
		}
		if (!journalpost.hasMidlertidigInngaaendeJournalforingStatus()) {
			throw new JournalpostIkkeMidlertidigException("Journalpost er ikke av status Midlertidig. Status=" +
					journalpost.getJournalstatus().name() + ", journalpostId=" + journalpost.getJournalpostId());
		}
		if (journalpost.isFeilregistrert()) {
			throw new OppdaterJournalpostIkkeMuligException(
					"Journalpost saksrelasjon er markert som feilregistrert. journalpostId=" + journalpost.getJournalpostId());
		}
		validateDokumentInfo(journalpost.findAllDokumentInfos(), journalpostId, relasjoner);
	}
	
	private boolean isValidHoveddokument(OppdaterJournalpostTo input) {
		return !(input.getHoveddokument() != null && (input.getHoveddokument().getDokumentId() == null));
	}
	
	private void validateBruker(OppdaterJournalpostTo input) {
		if (input.getAktoerTo() != null && (input.getAktoerTo()
				.getBrukerTypeCode() == null || Strings.isNullOrEmpty(input.getAktoerTo().getAktoerId()))) {
			throw new UgyldigInputException("Mangler informasjon på Aktoer for oppdatering av journalpost. journalpostId=" + input
					.getJournalpostId());
		}
	}
	
	private void validateArkivSak(OppdaterJournalpostTo input) {
		if (input.getArkivSak() != null && (Strings.isNullOrEmpty(input.getArkivSak().getArkivSakId()) || input.getArkivSak()
				.getArkivSakSystem() == null)) {
			throw new UgyldigInputException("Mangler informasjon på ArkivSak for oppdatering av journalpost. journalpostId=" + input
					.getJournalpostId());
		}
	}
	
	private void validateHoveddokument(OppdaterJournalpostTo to, Long journalpostId, Set<JournalpostDokumentInfoRelasjon> relasjoner) {
		if (!requestDokumentIdExists(to.getHoveddokument().getDokumentId(), relasjoner)) {
			throw new DokumentInfoIkkeTilknyttetJournalpostException(
					"Innsendt hoveddokument er ikke knyttet til journalposten. journalpostId=" + journalpostId);
		}
	}
	
	private void validateVedlegg(OppdaterJournalpostTo to, Long journalpostId, Set<JournalpostDokumentInfoRelasjon> relasjoner) {
		for (DokumentInformasjonTo dokTo : to.getVedlegg()) {
			if (!requestDokumentIdExists(dokTo.getDokumentId(), relasjoner)) {
				throw new DokumentInfoIkkeTilknyttetJournalpostException(
						"Ett eller flere innsendte vedlegg er ikke knyttet til journalposten. journalpostId=" + journalpostId);
			}
		}
	}
	
	private void validateDokumentInfo(List<DokumentInfo> dokumentInfoList, Long journalpostId, Set<JournalpostDokumentInfoRelasjon> relasjoner) {
		for (DokumentInfo dokumentInfo : dokumentInfoList) {
			if (!requestDokumentIdExists(dokumentInfo.getDokumentInfoId(), relasjoner)) {
				throw new DokumentInfoIkkeTilknyttetJournalpostException(
						"Innsendt vedlegg er ikke knyttet til journalposten. journalpostId=" + journalpostId);
			}
			if (dokumentInfo.isFunksjoneltSlettet()) {
				throw new OppdaterJournalpostIkkeMuligException("Dokumentet som forsøkes oppdatert er slettet. journalpostId="
						+ journalpostId + ",dokumentInfoId=" + dokumentInfo.getDokumentInfoId());
			}
			if (dokumentInfo.getDokumentstatus() != null && (dokumentInfo.isUnderRedigering() || dokumentInfo.isAvbrutt())) {
				throw new OppdaterJournalpostIkkeMuligException(
						"Dokument har ugyldig status for oppdatering. dokumentStatus="
								+ dokumentInfo.getDokumentstatus() + ",journalpostId=" + journalpostId
								+ ",dokumentInfoId=" + dokumentInfo.getDokumentInfoId());
			}
		}
	}
	
	private void validateJournalpostId(String journalpostId) {
		try {
			Long.valueOf(journalpostId);
		} catch (NumberFormatException e) {
			throw new UgyldigInputException("JournalpostId må være et nummer. journalpostId=" + journalpostId);
		}
	}
	
	private boolean isValidVedlegg(List<DokumentInformasjonTo> dokInfoList) {
		if (!dokInfoList.isEmpty()) {
			for (DokumentInformasjonTo dokTo : dokInfoList) {
				if (dokTo.getDokumentId() == null) {
					return false;
				}
			}
		}
		return true;
	}
	
	private boolean requestDokumentIdExists(Long dokumentId, Set<JournalpostDokumentInfoRelasjon> relasjoner) {
		for (JournalpostDokumentInfoRelasjon relasjon : relasjoner) {
			if (dokumentId.equals(relasjon.getDokumentInfo().getId())) {
				return true;
			}
		}
		return false;
	}
}
