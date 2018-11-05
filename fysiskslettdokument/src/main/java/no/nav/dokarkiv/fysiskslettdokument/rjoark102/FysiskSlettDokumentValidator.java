package no.nav.dokarkiv.fysiskslettdokument.rjoark102;

import static org.apache.commons.lang3.BooleanUtils.isNotTrue;

import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.exceptions.DokumentIkkeLogiskSlettetException;
import no.nav.dokarkiv.core.exceptions.ForMangeJournalpostDokumentInfoRelasjonerException;
import no.nav.dokarkiv.core.exceptions.IngenRelasjonMellomJournalpostIdOgDokumentInfoIdException;
import no.nav.dokarkiv.core.exceptions.JournalpostDokumentInfoRelasjonIkkeFunnetException;
import no.nav.dokarkiv.fysiskslettdokument.exceptions.DokumentErIkkeHoveddokumentException;
import no.nav.dokarkiv.fysiskslettdokument.exceptions.DokumentErIkkeVedleggException;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FysiskSlettDokumentValidator {

	public void validerFysiskSlettEtVedleggKnyttetEnJP(
			List<JournalpostDokumentInfoRelasjon> listFoundByDokumentInfoId,
			FysiskSlettDokumentRequestTo requestTo) {
		validerKunEnGyldigRelasjonFoundByDokumentInfoId(listFoundByDokumentInfoId, requestTo);
		validerAtRelasjonErTilknyttetSomVedlegg(listFoundByDokumentInfoId.get(0), requestTo);
	}

	public void validerFysiskSlettEtHoveddokumentKnyttetEnJP(
			List<JournalpostDokumentInfoRelasjon> listFoundByDokumentInfoId,
			FysiskSlettDokumentRequestTo requestTo) {
		validerKunEnGyldigRelasjonFoundByDokumentInfoId(listFoundByDokumentInfoId, requestTo);
		validerAtRelasjonErTilknyttetSomHoveddokument(listFoundByDokumentInfoId.get(0), requestTo);
	}

	//private
	private void validerKunEnGyldigRelasjonFoundByDokumentInfoId(
			List<JournalpostDokumentInfoRelasjon> listFoundByDokumentInfoId,
			FysiskSlettDokumentRequestTo requestTo) {
		validerAtRelasjonFoundByDokumentInfoIdFinnes(listFoundByDokumentInfoId, requestTo.getDokumentInfoId());
		validerAtRelasjonFoundByDokumentInfoIdHarKunEnRelasjon(listFoundByDokumentInfoId, requestTo.getDokumentInfoId());

		JournalpostDokumentInfoRelasjon relasjonSomValideres = listFoundByDokumentInfoId.get(0);

		validerAtRelasjonMellomInputParametereFinnes(relasjonSomValideres, requestTo);
		validerAtDokumentErLogiskSlettet(relasjonSomValideres.getDokumentInfo(), requestTo);
	}


	private void validerAtRelasjonFoundByDokumentInfoIdFinnes(
			List<JournalpostDokumentInfoRelasjon> listFoundByDokumentInfoId,
			Long dokumentInfoId) {
		if (listFoundByDokumentInfoId.isEmpty()) {
			throw new JournalpostDokumentInfoRelasjonIkkeFunnetException(
					String.format("%s kan ikke finne journalpostDokumentInfoRelasjon med dokumentInfoId=%s",
							MDC.get(MDCConstants.MDC_REQUEST_ID),
							dokumentInfoId));
		}
	}

	private void validerAtRelasjonFoundByDokumentInfoIdHarKunEnRelasjon(
			List<JournalpostDokumentInfoRelasjon> listFoundByDokumentInfoId,
			Long dokumentInfoId) {
		if (listFoundByDokumentInfoId.size() > 1) {
			throw new ForMangeJournalpostDokumentInfoRelasjonerException(
					String.format("%s kan ikke slette et dokument som er knyttet til flere journalposter. " +
									"dokumentInfoId=%s har relasjoner med %s journalposter.",
							MDC.get(MDCConstants.MDC_REQUEST_ID),
							dokumentInfoId,
							listFoundByDokumentInfoId.size()));
		}
	}

	private void validerAtRelasjonMellomInputParametereFinnes(
			JournalpostDokumentInfoRelasjon relasjonFoundByDokumentInfoId,
			FysiskSlettDokumentRequestTo requestTo) {
		if (isNotTrue(relasjonFoundByDokumentInfoId.getJournalpost().getJournalpostId().equals(requestTo.getJournalpostId()))) {
			throw new IngenRelasjonMellomJournalpostIdOgDokumentInfoIdException(
					String.format("%s finner ingen journalpostDokumentInfoRelasjon mellom journalpostId=%s og dokumentInfoId=%s",
							MDC.get(MDCConstants.MDC_REQUEST_ID),
							requestTo.getJournalpostId(),
							requestTo.getDokumentInfoId()));
		}
	}

	private static void validerAtRelasjonErTilknyttetSomVedlegg(
			JournalpostDokumentInfoRelasjon jpDokInfoRelSomSkalSlettes,
			FysiskSlettDokumentRequestTo requestTo) {
		if (isNotTrue(jpDokInfoRelSomSkalSlettes.isVedlegg())) {
			throw new DokumentErIkkeVedleggException(
					String.format("%s kan ikke slette dokument som ikke er et vedlegg når hjemmel=%s er brukt. " +
									"dokumentInfoId=%s, journalpostId=%s",
							MDC.get(MDCConstants.MDC_REQUEST_ID),
							requestTo.getHjemmel(),
							requestTo.getDokumentInfoId(),
							requestTo.getJournalpostId()));
		}
	}

	private static void validerAtRelasjonErTilknyttetSomHoveddokument(
			JournalpostDokumentInfoRelasjon jpDokInfoRelSomSkalSlettes,
			FysiskSlettDokumentRequestTo requestTo) {
		if (isNotTrue(jpDokInfoRelSomSkalSlettes.isHoveddokument())) {
			throw new DokumentErIkkeHoveddokumentException(
					String.format("%s kan ikke slette dokument som ikke er hoveddokument når hjemmel=%s er brukt. " +
									"dokumentInfoId=%s, journalpostId=%s",
							MDC.get(MDCConstants.MDC_REQUEST_ID),
							requestTo.getHjemmel(),
							requestTo.getDokumentInfoId(),
							requestTo.getJournalpostId()));
		}
	}

	private static void validerAtDokumentErLogiskSlettet(
			DokumentInfo dokumentInfo,
			FysiskSlettDokumentRequestTo requestTo) {
		if (isNotTrue(dokumentInfo.getSlettet())) {
			throw new DokumentIkkeLogiskSlettetException(String.format(
					"%s kan ikke fysisk slette dokument som ikke er logisk slettet. dokumenInfoId=%s, journalpostId=%s",
					MDC.get(MDCConstants.MDC_REQUEST_ID),
					requestTo.getDokumentInfoId(),
					requestTo.getJournalpostId()));
		}
	}


/**
 // MULIG SLETTING

 public void validerFysiskSlettAvEtVedlegg(
 JournalpostDokumentInfoRelasjon relasjonSomSkalSlettes,
 FysiskSlettDokumentRequestTo requestTo) {
 validerAtDokumentErLogiskSlettet(relasjonSomSkalSlettes.getDokumentInfo(), requestTo);
 validerAtRelasjonErTilknyttetSomVedlegg(relasjonSomSkalSlettes, requestTo);
 }

 public void validerAtKunEtVedleggSkalSlettes(
 JournalpostDokumentInfoRelasjon jpDokInfoRelSomSkalSlettes,
 FysiskSlettDokumentRequestTo requestTo) {
 validerAtRelasjonFoundByDokumentInfoIdFinnes(jpDokInfoRelSomSkalSlettes, requestTo.getJournalpostId());
 validerAtDokumentErLogiskSlettet(jpDokInfoRelSomSkalSlettes.getDokumentInfo(), requestTo);
//		validerAtRelasjonErTilknyttetSomVedlegg(jpDokInfoRelSomSkalSlettes, requestTo);
 }

 public void validerFysiskSlettAvEtHoveddokument(
 JournalpostDokumentInfoRelasjon relasjonSomSkalSlettes,
 FysiskSlettDokumentRequestTo requestTo) {
 validerAtDokumentErLogiskSlettet(relasjonSomSkalSlettes.getDokumentInfo(), requestTo);
 validerAtRelasjonErTilknyttetSomHoveddokument(relasjonSomSkalSlettes, requestTo);
 }

 public void validerAtKunEtHoveddokumentSkalSlettes(
 List<JournalpostDokumentInfoRelasjon> listFoundByJournalpostId,
 List<JournalpostDokumentInfoRelasjon> listFoundByDokumentInfoId,
 FysiskSlettDokumentRequestTo requestTo) {
 validerKunEnGyldigRelasjonFunnet(listFoundByJournalpostId, listFoundByDokumentInfoId, requestTo);
 validerFysiskSlettAvEtHoveddokument(listFoundByJournalpostId.get(0), requestTo);
 }

 // Private ---------------

 private void validerKunEnGyldigRelasjonFunnet(
 List<JournalpostDokumentInfoRelasjon> listFoundByJournalpostId,
 List<JournalpostDokumentInfoRelasjon> listFoundByDokumentInfoId,
 FysiskSlettDokumentRequestTo requestTo) {
 validerAtJournalpostDokumentInfoRelasjonerFinnes(listFoundByJournalpostId, listFoundByDokumentInfoId, requestTo);
 validerAtJournalpostDokumentInfoRelasjonKunErKnyttetTilEnRelasjon(listFoundByJournalpostId, listFoundByDokumentInfoId, requestTo);
 validerAtJournalpostIdOgDokumentInfoIdFraInputPekerPaaSammeRelasjon(listFoundByJournalpostId, listFoundByDokumentInfoId, requestTo);
 }

 private void validerAtJournalpostDokumentInfoRelasjonerFinnes(
 List<JournalpostDokumentInfoRelasjon> listFoundByJournalpostId,
 List<JournalpostDokumentInfoRelasjon> listFoundByDokumentInfoId,
 FysiskSlettDokumentRequestTo requestTo
 ) {
 if (listFoundByJournalpostId.isEmpty()) {
 throw new JournalpostDokumentInfoRelasjonIkkeFunnetException(
 String.format("%s kan ikke finne journalpostDokumentInfoRelasjon med journalpostId=%s",
 MDC.get(MDCConstants.MDC_REQUEST_ID),
 requestTo.getJournalpostId()));
 }
 if (listFoundByDokumentInfoId.isEmpty()) {
 throw new JournalpostDokumentInfoRelasjonIkkeFunnetException(
 String.format("%s kan ikke finne journalpostDokumentInfoRelasjon med dokumentInfoId=%s",
 MDC.get(MDCConstants.MDC_REQUEST_ID),
 requestTo.getDokumentInfoId()));
 }
 }

 private void validerAtJournalpostDokumentInfoRelasjonKunErKnyttetTilEnRelasjon(
 List<JournalpostDokumentInfoRelasjon> listFoundByJournalpostId,
 List<JournalpostDokumentInfoRelasjon> listFoundByDokumentInfoId,
 FysiskSlettDokumentRequestTo requestTo) {
 if (listFoundByJournalpostId.size() > 1) {
 throw new ForMangeJournalpostDokumentInfoRelasjonerException(
 String.format("%s kan ikke slette en journalpost som har relasjoner med flere dokumenter. " +
 "JournalpostId=%s har relasjoner med %s dokumenter.",
 MDC.get(MDCConstants.MDC_REQUEST_ID),
 requestTo.getJournalpostId(),
 listFoundByJournalpostId.size()));
 }
 if (listFoundByDokumentInfoId.size() > 1) {
 throw new ForMangeJournalpostDokumentInfoRelasjonerException(
 String.format("%s kan ikke slette et dokument som har relasjoner med flere journalposter. " +
 "DokumentInfoId=%s har relasjoner med %s journalposter.",
 MDC.get(MDCConstants.MDC_REQUEST_ID),
 requestTo.getDokumentInfoId(),
 listFoundByDokumentInfoId.size()));
 }
 }

 private void validerAtJournalpostIdOgDokumentInfoIdFraInputPekerPaaSammeRelasjon(
 List<JournalpostDokumentInfoRelasjon> listFoundByJournalpostId,
 List<JournalpostDokumentInfoRelasjon> listFoundByDokumentInfoId,
 FysiskSlettDokumentRequestTo requestTo) {
 Long relasjonsIdFraJournalpostId = listFoundByJournalpostId.get(0).getJournalpostDokumentInfoRelasjonId();
 Long relasjonsIdFraDokumentInfoId = listFoundByDokumentInfoId.get(0).getJournalpostDokumentInfoRelasjonId();

 if (isFalse(relasjonsIdFraJournalpostId.equals(relasjonsIdFraDokumentInfoId))) {
 throw new IngenRelasjonMellomJournalpostIdOgDokumentInfoIdException(
 String.format("%s finner ingen journalpostDokumentInfoRelasjon mellom journalpostId=%s og dokumentInfoId=%s",
 MDC.get(MDCConstants.MDC_REQUEST_ID),
 requestTo.getJournalpostId(),
 requestTo.getDokumentInfoId()));
 }
 }






 //Endre eller slett --------------------------------------

 private void validerAtHoveddokumentIkkeHarVedlegg(
 JournalpostDokumentInfoRelasjon jpDokInfoRelSomSkalSlettes,
 FysiskSlettDokumentRequestTo requestTo) {
 Set<JournalpostDokumentInfoRelasjon> jpDokInfoRelasjoner = jpDokInfoRelSomSkalSlettes.getDokumentInfo()
 .getJournalpostRelasjoner();
 if (jpDokInfoRelasjoner.size() > 1) {
 throw new ForMangeJournalpostDokumentInfoRelasjonerException(
 String.format("s kan ikke slette et hoveddokument som har vedlegg når hjemmel=%s brukes. " +
 "dokumentinfoId=%s, journalpostId=%s",
 MDC.get(MDCConstants.MDC_REQUEST_ID),
 requestTo.getDokumentInfoId(),
 requestTo.getJournalpostId()));
 }
 }

 */
}
