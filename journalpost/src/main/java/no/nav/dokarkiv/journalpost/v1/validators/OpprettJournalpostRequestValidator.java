package no.nav.dokarkiv.journalpost.v1.validators;

import static no.nav.dokarkiv.journalpost.v1.api.JournalpostType.INNGAAENDE;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.isNumeric;

import no.nav.dokarkiv.core.domain.codes.Behandlingstema;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.journalpost.v1.api.AvsenderMottaker;
import no.nav.dokarkiv.journalpost.v1.api.Bruker;
import no.nav.dokarkiv.journalpost.v1.api.BrukerIdType;
import no.nav.dokarkiv.journalpost.v1.api.Dokument;
import no.nav.dokarkiv.journalpost.v1.api.DokumentVariant;
import no.nav.dokarkiv.journalpost.v1.api.Sak;
import no.nav.dokarkiv.journalpost.v1.api.opprettjournalpost.OpprettJournalpostRequest;

import java.util.Arrays;

public class OpprettJournalpostRequestValidator {

	private static final int FNR_LENGTH = 11;
	private static final int ORGNR_LENGTH = 9;

	private static final String VALIDERER_IKKE_MOT_KODEVERK = "validerer ikke mot kodeverk";

	public void validateRequest(OpprettJournalpostRequest request) {
		if (request.getAvsenderMottaker() != null) {
			validateAvsenderMottaker(request.getAvsenderMottaker());
		}
		if (request.getBruker() != null) {
			validateBruker(request.getBruker());
		}
		if(request.getTema() != null){
			validateTema(request.getTema());
		}
		if (isNotBlank(request.getBehandlingstema())) {
			validateBehandlingstema(request.getBehandlingstema());
		}
		if (isNotBlank(request.getKanal())) {
			validateKanal(request);
		}
		if (request.getSak() != null) {
			validateSak(request.getSak());
		}
		if (!request.getDokumenter().isEmpty()) {
			request.getDokumenter().forEach(this::validateDokument);
		}
	}

	private void validateAvsenderMottaker(AvsenderMottaker avsenderMottaker) {
		if (isNotBlank(avsenderMottaker.getId()) && avsenderMottaker.getIdType() == null) {
			throw new InputValideringFeiletException("AvsenderMottaker.idType må være satt når AvsenderMottaker.id er satt.");
		}
		if (avsenderMottaker.getIdType() != null && isBlank(avsenderMottaker.getId())) {
			throw new InputValideringFeiletException("AvsenderMottaker.id må være satt når AvsenderMottaker.idType er satt.");
		}
		if (avsenderMottaker.getIdType() != null) {
			switch (avsenderMottaker.getIdType()) {
				case FNR:
					if (!avsenderMottaker.getId().matches("^\\d{11}$")) {
						throw new InputValideringFeiletException("AvsenderMottaker.id må være 11 siffer når AvsenderMottaker.idType er " + avsenderMottaker.getIdType() + ".");
					}
					break;
				case ORGNR:
				case HPRNR:
					if (!avsenderMottaker.getId().matches("^\\d{9}$")) {
						throw new InputValideringFeiletException("AvsenderMottaker.id må være 9 siffer når AvsenderMottaker.idType er " + avsenderMottaker.getIdType() + ".");
					}
					break;
				default:
					// noop
					break;
			}
		}
	}

	private void validateBruker(Bruker bruker) {
		if (isBlank(bruker.getId())) {
			throw new InputValideringFeiletException("Bruker.id må være satt.");
		}
		if (!isNumeric(bruker.getId())) {
			throw new InputValideringFeiletException("Bruker.id må bestå av tall.");
		}
		if (BrukerIdType.FNR.equals(bruker.getIdType()) && bruker.getId().length() != FNR_LENGTH) {
			throw new InputValideringFeiletException("Bruker.id må være 11 siffer for FNR.");
		} else if (BrukerIdType.ORGNR.equals(bruker.getIdType()) && bruker.getId().length() != ORGNR_LENGTH) {
			throw new InputValideringFeiletException("Bruker.id må være 9 siffer for ORGNR.");
		}
	}

	private void validateTema(String tema) {
		try {
			FagomradeCode.valueOf(tema);
		} catch (IllegalArgumentException e) {
			throw new InputValideringFeiletException(String.format("Oppgitt tema=%s %s", tema, VALIDERER_IKKE_MOT_KODEVERK));
		}
	}

	private void validateBehandlingstema(String behandlingstema) {
		try {
			Behandlingstema.valueOf(behandlingstema);
		} catch (IllegalArgumentException e) {
			throw new InputValideringFeiletException(String.format("Oppgitt behandlingstema=%s %s", behandlingstema, VALIDERER_IKKE_MOT_KODEVERK));
		}
	}

	private void validateKanal(OpprettJournalpostRequest request) {
		if (INNGAAENDE.equals(request.getJournalpostType())) {
			try {
				MottaksKanalCode.valueOf(request.getKanal());
			} catch (IllegalArgumentException e) {
				throw new InputValideringFeiletException(String.format("Oppgitt kanal=%s %s", request.getKanal(), VALIDERER_IKKE_MOT_KODEVERK));
			}

			if (MottaksKanalCode.valueOf(request.getKanal()) == MottaksKanalCode.NAV_NO_UINNLOGGET && !request.getTema().equalsIgnoreCase(FagomradeCode.SER.name())) {
				throw new InputValideringFeiletException("Det er kun mulig å arkivere med mottakskanal NAV_NO_UINNLOGGET dersom tema=SER.");
			}

		} else {
			try {
				UtsendingsKanalCode.valueOf(request.getKanal());
			} catch (IllegalArgumentException e) {
				throw new InputValideringFeiletException(String.format("Oppgitt kanal=%s %s", request.getKanal(), VALIDERER_IKKE_MOT_KODEVERK));
			}
		}
	}

	private void validateSak(Sak sak) {
		if (isBlank(sak.getArkivsaksnummer())) {
			throw new InputValideringFeiletException("Sak.arkivsaksnummer må være satt");
		}
		if(!isNumeric(sak.getArkivsaksnummer())) {
			throw new InputValideringFeiletException("Sak.arkivsaksnummer skal være opprettet i GSAK/PSAK og må være et numerisk heltall.");
		}
	}

	private void validateDokument(Dokument dokument) {
		if (isNotBlank(dokument.getDokumentKategori())) {
			try {
				DokumentKategoriCode.valueOf(dokument.getDokumentKategori());
			} catch (IllegalArgumentException e) {
				throw new InputValideringFeiletException(String.format("Dokument.dokumentkategori %s", VALIDERER_IKKE_MOT_KODEVERK));
			}
		}
		dokument.getDokumentvarianter().forEach(this::validateDokumentVariant);
	}

	private void validateDokumentVariant(DokumentVariant dokumentVariant) {
		if (isBlank(dokumentVariant.getFiltype())) {
			throw new InputValideringFeiletException("Dokument.dokumentvariant.filtype må være satt");
		}
		try {
			FilTypeCode.valueOf(dokumentVariant.getFiltype());
		} catch (IllegalArgumentException e) {
			throw new InputValideringFeiletException(String.format("Dokument.dokumentvariant.filtype %s", VALIDERER_IKKE_MOT_KODEVERK));
		}
		if (isBlank(dokumentVariant.getVariantformat())) {
			throw new InputValideringFeiletException("Dokument.dokumentvariant.variantformat må være satt");
		}
		try {
			VariantFormatCode.valueOf(dokumentVariant.getVariantformat());
		} catch (IllegalArgumentException e) {
			throw new InputValideringFeiletException(String.format("Dokument.dokumentvariant.variantformat %s", VALIDERER_IKKE_MOT_KODEVERK));
		}
		if (dokumentVariant.getVariantformat().equals(VariantFormatCode.ARKIV.name())
				&& !Arrays.asList(FilTypeCode.PDF, FilTypeCode.PDFA).contains(FilTypeCode.valueOf(dokumentVariant.getFiltype()))) {
			throw new InputValideringFeiletException("Dokument.dokumentvariant.filtype på være PDF eller PDFA for Dokument.dokumentvariant.variantformat=ARKIV.");
		}
	}
}