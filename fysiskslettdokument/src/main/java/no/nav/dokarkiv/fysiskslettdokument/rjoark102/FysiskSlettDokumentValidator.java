package no.nav.dokarkiv.fysiskslettdokument.rjoark102;

import static org.apache.commons.lang3.BooleanUtils.isNotTrue;

import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.exceptions.DokumentIkkeLogiskSlettetException;
import no.nav.dokarkiv.core.exceptions.ForMangeJournalpostDokumentInfoRelasjonerException;
import no.nav.dokarkiv.core.exceptions.IngenRelasjonMellomJournalpostIdOgDokumentInfoIdException;
import no.nav.dokarkiv.core.exceptions.JournalpostDokumentInfoRelasjonIkkeFunnetException;
import no.nav.dokarkiv.fysiskslettdokument.exceptions.DokumentErIkkeHoveddokumentException;
import no.nav.dokarkiv.fysiskslettdokument.exceptions.DokumentErIkkeVedleggException;
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
					String.format("Kan ikke finne journalpostDokumentInfoRelasjon med dokumentInfoId=%s",
							dokumentInfoId));
		}
	}

	private void validerAtRelasjonFoundByDokumentInfoIdHarKunEnRelasjon(
			List<JournalpostDokumentInfoRelasjon> listFoundByDokumentInfoId,
			Long dokumentInfoId) {
		if (listFoundByDokumentInfoId.size() > 1) {
			throw new ForMangeJournalpostDokumentInfoRelasjonerException(String.format(
					"Kan ikke slette dokument med dokumentInfoId=%s fordi dokumentet er knyttet til flere journalposter.",
					dokumentInfoId));
		}
	}

	private void validerAtRelasjonMellomInputParametereFinnes(
			JournalpostDokumentInfoRelasjon relasjonFoundByDokumentInfoId,
			FysiskSlettDokumentRequestTo requestTo) {
		if (isNotTrue(relasjonFoundByDokumentInfoId.getJournalpost().getJournalpostId().equals(requestTo.getJournalpostId()))) {
			throw new IngenRelasjonMellomJournalpostIdOgDokumentInfoIdException(String.format(
					"Kan ikke finne noen relasjon mellom journalpost med journalpostId=%s og dokument med dokumentInfoId=%s",
					requestTo.getJournalpostId(),
					requestTo.getDokumentInfoId()));
		}
	}

	private static void validerAtRelasjonErTilknyttetSomVedlegg(
			JournalpostDokumentInfoRelasjon jpDokInfoRelSomSkalSlettes,
			FysiskSlettDokumentRequestTo requestTo) {
		if (isNotTrue(jpDokInfoRelSomSkalSlettes.isVedlegg())) {
			throw new DokumentErIkkeVedleggException(String.format(
					"Kan ikke slette dokument med dokumentInfoId=%s og journalpostId=%s som vedlegg " +
							"fordi dokumentet er tilknyttet som %s",
					requestTo.getDokumentInfoId(),
					requestTo.getJournalpostId(),
					jpDokInfoRelSomSkalSlettes.getTilknyttetJournalpostSom().toString().toLowerCase()));
		}
	}

	private static void validerAtRelasjonErTilknyttetSomHoveddokument(
			JournalpostDokumentInfoRelasjon jpDokInfoRelSomSkalSlettes,
			FysiskSlettDokumentRequestTo requestTo) {
		if (isNotTrue(jpDokInfoRelSomSkalSlettes.isHoveddokument())) {
			throw new DokumentErIkkeHoveddokumentException(String.format(
					"Kan ikke slette dokument med dokumentInfoId=%s og journalpostId=%s som hoveddokument " +
							"fordi dokumentet er tilknyttet som %s.",
					requestTo.getDokumentInfoId(),
					requestTo.getJournalpostId(),
					jpDokInfoRelSomSkalSlettes.getTilknyttetJournalpostSom().toString().toLowerCase()));
		}
	}

	private static void validerAtDokumentErLogiskSlettet(
			DokumentInfo dokumentInfo,
			FysiskSlettDokumentRequestTo requestTo) {
		if (isNotTrue(dokumentInfo.getSlettet())) {
			throw new DokumentIkkeLogiskSlettetException(String.format(
					"Kan ikke fysisk slette dokument som ikke har blitt logisk slettet først. dokumenInfoId=%s, journalpostId=%s",
					requestTo.getDokumentInfoId(),
					requestTo.getJournalpostId()));
		}
	}
}
