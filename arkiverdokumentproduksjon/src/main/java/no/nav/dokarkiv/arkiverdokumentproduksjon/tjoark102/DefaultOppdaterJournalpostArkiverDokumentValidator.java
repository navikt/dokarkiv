package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark102;

import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.AlleredeFerdigstiltException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.FeilStrukturException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.KanIkkeFerdigstillesException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.ObjektIkkeFunnetException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.UgyldigInputException;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Validation for the operation ArkiverDokumentOgFerdigstillJournalpost
 *
 * @author Torgeir Cook
 */
@Component
public class DefaultOppdaterJournalpostArkiverDokumentValidator implements OppdaterJournalpostArkiverDokumentValidator {

	@Override
	public void validate(Journalpost journalpost, OppdaterJournalpostArkiverDokumentRequestTo request) throws ObjektIkkeFunnetException, FeilStrukturException, KanIkkeFerdigstillesException, AlleredeFerdigstiltException, UgyldigInputException {
		if (journalpost == null) {
			throw new ObjektIkkeFunnetException("JournalpostId eksisterer ikke i Joark", request.getJournalpostId());
		}
		DokumentInfo dokumentInfoById = journalpost.findDokumentInfoById(request.getDokumentInfoId());
		Set<FilDetaljer> fildetaljer = request.getFildetaljer();
		validateJournalpostContainsDokumentInfoWithId(journalpost, request.getDokumentInfoId());
		validateNoDuplicateVariantFormats(fildetaljer, journalpost.getJournalpostId());
		validateDokumentInfoOrFilDetaljerContainsArkivFormat(dokumentInfoById, fildetaljer, journalpost.getJournalpostId());
		validateJournalpostTypeAndStatus(journalpost, request);
		validateJournalpostContainsOneRealtedDokumenInfoOfTypeHoveddokument(journalpost);
		validateNoDuplicateVariantFormatsExceptProduksjon(dokumentInfoById.getFildetaljerListe(), fildetaljer, journalpost.getJournalpostId());
		validateDokumentInfoIsUnderRedigering(dokumentInfoById, journalpost.getJournalpostId());
		validateThatAllDocumentStatusesAreFerdigstilltWhenFerdigstillJournalPost(journalpost,
				dokumentInfoById, request.isFerdigstillJournalpost());
		validateDatoDokument(request);
	}

	@Override
	public void validateRequest(OppdaterJournalpostArkiverDokumentRequestTo request) throws ObjektIkkeFunnetException, UgyldigInputException {
		if (request == null) {
			throw new ObjektIkkeFunnetException("OppdaterJournalpostArkiverDokumentRequest kan ikke v�re null", null);
		}
		validateRequiredFields(request);
	}

	private void validateRequiredFields(OppdaterJournalpostArkiverDokumentRequestTo request) throws UgyldigInputException {
		String message = "";
		if (request.getJournalpostId() == null) {
			message += "journalpostId";
		}
		if (request.getDokumentInfoId() == null) {
			message = message.isEmpty() ? "dokumentInfoId" : ", dokumentInfoId";
		}
		if (StringUtils.isBlank(request.getEndretAvNavn())) {
			message = message.isEmpty() ? "endretAvNavn" : ", endretAvNavn";
		}
		if (request.getFildetaljer().isEmpty()) {
			message = message.isEmpty() ? "filDetaljer" : ", filDetaljer";
		}

		if (!message.isEmpty()) {
			throw new UgyldigInputException("Mangler p�krevde attributter: " + message, request.getJournalpostId());
		}
	}

	/**
	 * Validate that all DocumentStatuses = FERDIGSTILT for all DocumentInfo not in request
	 * when ferdigstillJournalpost = true
	 *
	 * @param journalpost            List of DokumentInfo-objects on Journalpost
	 * @param requestDokumentInfoId  DokumentInfoId from request
	 * @param ferdigstillJournalpost whether to ferdigstill Journalpost
	 */
	public void validateThatAllDocumentStatusesAreFerdigstilltWhenFerdigstillJournalPost(Journalpost journalpost, DokumentInfo requestDokumentInfoId, boolean ferdigstillJournalpost) throws KanIkkeFerdigstillesException {

		if (ferdigstillJournalpost) {
			List<DokumentInfo> dokumentInfoNotInRequest = journalpost.findAllDokumentInfos();
			dokumentInfoNotInRequest.remove(requestDokumentInfoId);
			for (DokumentInfo dokumentInfo : dokumentInfoNotInRequest) {
				if (!dokumentInfo.isFerdigstilt()) {
					throw new KanIkkeFerdigstillesException("Journalposten kan ikke ferdigstilles fordi tilknyttet dokument (dokumentInfoId=" + dokumentInfo.getDokumentInfoId() + ")  ikke har status " + DokumentStatusCode.FERDIGSTILT
							.name(),
							journalpost.getJournalpostId());
				}
			}
		}
	}

	/**
	 * Validates that filDetaljer does not contain duplicate variant formats.
	 *
	 * @param filDetaljer to be validated.
	 */
	public void validateNoDuplicateVariantFormats(Set<FilDetaljer> filDetaljer, Long journalpostId) throws FeilStrukturException {
		List<VariantFormatCode> variantList = getVariantFormatList(filDetaljer);
		Set<VariantFormatCode> uniqueSet = new HashSet<>(variantList);
		for (VariantFormatCode variantFormatCode : uniqueSet) {
			if (Collections.frequency(variantList, variantFormatCode) > 1) {
				throw new FeilStrukturException("Input til tjenesten inneholder flere fildetaljer med samme variantformat", journalpostId);
			}
		}
	}

	/**
	 * Validates that journalpost type is not Inng�ende dokument.
	 * Validates that journalpost status is not Dokument under produksjon.
	 * https://confluence.adeo.no/x/RLJlBQ step 3
	 *
	 * @param journalpost to be validated.
	 * @param request     input request with dokument payloads for dokumentinfo
	 */
	public void validateJournalpostTypeAndStatus(Journalpost journalpost, OppdaterJournalpostArkiverDokumentRequestTo request) throws KanIkkeFerdigstillesException, AlleredeFerdigstiltException, UgyldigInputException {
		boolean ferdigstillJournalpost = request.isFerdigstillJournalpost();
		if (journalpost.isInngaende()) {
			throw new UgyldigInputException("Journalpost kan ikke v�re av typen INNG�ENDE", journalpost.getJournalpostId());
		} else {
			DokumentInfo dokumentInfoById = journalpost.findDokumentInfoById(request.getDokumentInfoId());
			if (journalpost.getJournalstatus() == JournalStatusCode.FS
					&& UtsendingsKanalCode.L != request.getUtsendingskanal()
					&& DokumentStatusCode.FERDIGSTILT == dokumentInfoById.getDokumentstatus()) {
				throw new AlleredeFerdigstiltException("Journalpost med dokument er allerede ferdigstilt", journalpost.getJournalpostId());
			} else if (journalpost.getJournalstatus() == JournalStatusCode.FL
					&& UtsendingsKanalCode.L == request.getUtsendingskanal()
					&& DokumentStatusCode.FERDIGSTILT == dokumentInfoById.getDokumentstatus()
					&& ferdigstillJournalpost) {
				throw new AlleredeFerdigstiltException("Journalpost med dokument er allerede ferdigstilt lokalprint", journalpost.getJournalpostId());
			} else if (journalpost.getJournalstatus() == JournalStatusCode.D
					&& DokumentStatusCode.FERDIGSTILT == dokumentInfoById.getDokumentstatus()
					&& !ferdigstillJournalpost) {
				throw new AlleredeFerdigstiltException("Dokument er allerede ferdigstilt for journalpost under arbeid", journalpost.getJournalpostId());
			} else if (journalpost.getJournalstatus() != JournalStatusCode.D
					|| DokumentStatusCode.UNDER_REDIGERING != dokumentInfoById.getDokumentstatus()) {
				throw new KanIkkeFerdigstillesException("Journal- og/eller dokumentstatus er ulik \"under arbeid\"", journalpost.getJournalpostId());
			}
		}
	}

	/**
	 * Validates that dokumentInfo has status Under redigering.
	 *
	 * @param dokumentInfo to be validated.
	 */
	public void validateDokumentInfoIsUnderRedigering(DokumentInfo dokumentInfo, Long journalpostId) throws KanIkkeFerdigstillesException {
		if (!dokumentInfo.isUnderRedigering()) {
			throw new KanIkkeFerdigstillesException("DokumentInfo [" + dokumentInfo.getDokumentInfoId() + "] krever status UNDER REDIGERING", journalpostId);
		}
	}

	/**
	 * Validates that journalpost contains dokument info with dokumentInfoId.
	 *
	 * @param journalpost    to be validated.
	 * @param dokumentInfoId used for .
	 */
	public void validateJournalpostContainsDokumentInfoWithId(Journalpost journalpost, Long dokumentInfoId) throws ObjektIkkeFunnetException {
		Set<Long> dokInfoIdSet = getDokumentInfoIdSet(journalpost);

		if (!dokInfoIdSet.contains(dokumentInfoId)) {
			throw new ObjektIkkeFunnetException("DokumentInfoId [" + dokumentInfoId + "] finnes ikke p� angitt journalpost", journalpost
					.getJournalpostId());
		}
	}

	/**
	 * Validates that dokumentInfo or filDetaljer contains a variant format of the type Format arkiv.
	 *
	 * @param dokumentInfo to be validated.
	 * @param filDetaljer  to be validated.
	 */
	public void validateDokumentInfoOrFilDetaljerContainsArkivFormat(DokumentInfo dokumentInfo, Set<FilDetaljer> filDetaljer, Long journalpostId) throws FeilStrukturException {
		Set<FilDetaljer> concatFilDetaljer = new HashSet<>();
		concatFilDetaljer.addAll(dokumentInfo.getFildetaljerListe());
		concatFilDetaljer.addAll(filDetaljer);
		validateFilDetaljerContainVariantFormat(concatFilDetaljer, VariantFormatCode.ARKIV, journalpostId);
	}

	/**
	 * Validates that filDetaljer does not contain any variant format codes present in dokumentInfo.
	 * Duplicate variant format of the type Produksjon is however allowed.
	 *
	 * @param jpFileDetaljer to be validated.
	 * @param filDetaljer    to be validated.
	 */
	public void validateNoDuplicateVariantFormatsExceptProduksjon(Set<FilDetaljer> jpFileDetaljer, Set<FilDetaljer> filDetaljer, Long journalpostId) throws FeilStrukturException {
		for (FilDetaljer filDetalj : filDetaljer) {
			VariantFormatCode variantFormatCode = filDetalj.getVariantFormat();
			if (!variantFormatCode.equals(VariantFormatCode.PRODUKSJON)) {
				validateFilDetaljerDoesNotContainVariantFormat(jpFileDetaljer, variantFormatCode, journalpostId);
			}
		}
	}

	/**
	 * Validates that journalpost contains exactly one dokument info relation of the type Hoveddokument.
	 *
	 * @param journalpost to be validated.
	 */
	public void validateJournalpostContainsOneRealtedDokumenInfoOfTypeHoveddokument(Journalpost journalpost) throws FeilStrukturException {
		Set<JournalpostDokumentInfoRelasjon> infoRels =
				journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT);
		int count = infoRels.size();

		if (count == 0 || count > 1) {
			throw new FeilStrukturException("Journalpost har ikke korrekt struktur", journalpost.getJournalpostId());
		}
	}

	/**
	 * Validates that request contains a datoDokument
	 *
	 * @param request to be validated.
	 */
	public void validateDatoDokument(OppdaterJournalpostArkiverDokumentRequestTo request) throws UgyldigInputException {
		if (request.getDatoDokument() == null) {
			throw new UgyldigInputException("Mangler p�krevd felt datoDokument", request.getJournalpostId());
		}
	}

	private void validateFilDetaljerDoesNotContainVariantFormat(Set<FilDetaljer> jpFileDetaljerSet, VariantFormatCode variantFormatCode, Long journalpostId) throws FeilStrukturException {
		for (FilDetaljer filDetaljer : jpFileDetaljerSet) {
			if (filDetaljer.getVariantFormat().equals(variantFormatCode)) {
				throw new FeilStrukturException("Variantformat som for�kes lagt til eksisterer allerede. variantFormatCode=" + variantFormatCode, journalpostId);
			}
		}
	}

	private void validateFilDetaljerContainVariantFormat(Set<FilDetaljer> filDetaljerSet, VariantFormatCode variantFormatCode, Long journalpostId) throws FeilStrukturException {
		List<VariantFormatCode> variantFormatCodes = getVariantFormatList(filDetaljerSet);
		if (!variantFormatCodes.contains(variantFormatCode)) {
			throw new FeilStrukturException("Arkivvariant av dokument mangler, kan ikke ferdigstille journalpost. variantFormatCode=" + variantFormatCode, journalpostId);
		}
	}

	private Set<Long> getDokumentInfoIdSet(Journalpost journalpost) {
		Set<Long> dokumentInfoIds = new HashSet<>();
		Set<JournalpostDokumentInfoRelasjon> dokumentInfoRelasjoner = journalpost.getJournalpostDokumentInfoRelasjoner();

		for (JournalpostDokumentInfoRelasjon infoRel : dokumentInfoRelasjoner) {
			dokumentInfoIds.add(infoRel.getDokumentInfo().getId());
		}

		return dokumentInfoIds;
	}

	private List<VariantFormatCode> getVariantFormatList(Set<FilDetaljer> filDetaljerList) {
		List<VariantFormatCode> variantList = new ArrayList<>();
		for (FilDetaljer filDetaljer : filDetaljerList) {
			variantList.add(filDetaljer.getVariantFormat());
		}
		return variantList;
	}

	private boolean journalpostIsFerdigstiltSentralPrint(Journalpost journalpost) {
		return journalpost.getJournalstatus()
				.equals(JournalStatusCode.FS) && dokumentInfoIsFerdigstilt(journalpost.findAllDokumentInfos());
	}

	private boolean journalpostIsFerdigstiltLokalPrint(Journalpost journalpost) {
		return journalpost.getJournalstatus()
				.equals(JournalStatusCode.FL) && dokumentInfoIsFerdigstilt(journalpost.findAllDokumentInfos());
	}

	private boolean dokumentInfoIsFerdigstilt(List<DokumentInfo> dokumentInfos) {
		for (DokumentInfo dokumentInfo : dokumentInfos) {
			if (!dokumentInfo.isFerdigstilt()) {
				return false;
			}
		}
		return true;
	}
}
