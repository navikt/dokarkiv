package no.nav.dokarkiv.journalpost.v1.validators;

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
import no.nav.dokarkiv.journalpost.v1.api.Sakstype;
import no.nav.dokarkiv.journalpost.v1.api.opprettjournalpost.OpprettJournalpostRequest;

import java.util.Arrays;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.isNumeric;
import static org.apache.cxf.common.util.CollectionUtils.isEmpty;

public class OpprettJournalpostRequestValidator {

	private static final int FNR_LENGTH = 11;
	private static final int AKTOERID_LENGTH = 13;
	private static final int ORGNR_LENGTH = 9;
	public static final String MASKINELL_JOURNALFOERENDE_ENHET = "9999";
	public static final String JOURNALPOST_FERDIGSTILT = "false";

	private static final String VALIDERER_IKKE_MOT_KODEVERK = "validerer ikke mot kodeverk";

	public void validateRequest(OpprettJournalpostRequest request, String journalpostFerdigstilt) {
		if (request.getAvsenderMottaker() != null) {
			validateAvsenderMottaker(request.getAvsenderMottaker());
		}
		if (request.getBruker() != null) {
			validateBruker(request.getBruker());
		}
		if (request.getTema() != null) {
			validateTema(request.getTema());
		}
		if (isNotBlank(request.getBehandlingstema())) {
			validateBehandlingstema(request.getBehandlingstema());
		}
		if (isNotBlank(request.getKanal())) {
			validateKanal(request);
		}
		if (request.getSak() != null) {
			validateSak(request.getSak(), request.getBruker(), request.getTema());
		}
		if(isNotBlank(request.getJournalfoerendeEnhet())) {
			validateJournalpost(journalpostFerdigstilt,request.getJournalfoerendeEnhet());
		}
		if (!request.getDokumenter().isEmpty()) {
			request.getDokumenter().forEach(this::validateDokument);
		} else {
			throw new InputValideringFeiletException("Kan ikke opprette journalpost uten dokumenter.");
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
						throw new InputValideringFeiletException("AvsenderMottaker.id må være 11 siffer når AvsenderMottaker.idType er " + avsenderMottaker
								.getIdType() + ".");
					}
					break;
				case ORGNR:
				case HPRNR:
					if (!avsenderMottaker.getId().matches("^\\d{9}$")) {
						throw new InputValideringFeiletException("AvsenderMottaker.id må være 9 siffer når AvsenderMottaker.idType er " + avsenderMottaker
								.getIdType() + ".");
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
		} else if (BrukerIdType.AKTOERID.equals(bruker.getIdType()) && bruker.getId().length() != AKTOERID_LENGTH) {
			throw new InputValideringFeiletException("Bruker.id må være 11 siffer for AKTOERID.");
		}
	}

	private void validateTema(String tema) {
		try {
			FagomradeCode.valueOf(tema);
		} catch (IllegalArgumentException e) {
			throw new InputValideringFeiletException(format("Oppgitt tema=%s %s", tema, VALIDERER_IKKE_MOT_KODEVERK));
		}
	}

	private void validateJournalpost(String journalpostFerdigstilt, String journalfoerendeEnhet) {
		if(JOURNALPOST_FERDIGSTILT.equals(journalpostFerdigstilt) && MASKINELL_JOURNALFOERENDE_ENHET.equals(journalfoerendeEnhet)) {
			throw new InputValideringFeiletException(format("Ikke mulig å opprette journalpost på journalfoerendeEnhet=%s", MASKINELL_JOURNALFOERENDE_ENHET));
		}
	}

	private void validateBehandlingstema(String behandlingstema) {
		if (behandlingstema.length() != 6 || !behandlingstema.startsWith("ab")) {
			throw new InputValideringFeiletException(format("Oppgitt behandlingstema=%s er ikke på formatet ´ab + fire siffer´." , behandlingstema));
		}
	}

	private void validateKanal(OpprettJournalpostRequest request) {
		if (request.isInngaaende()) {
			try {
				MottaksKanalCode.valueOf(request.getKanal());
			} catch (IllegalArgumentException e) {
				throw new InputValideringFeiletException(format("Oppgitt kanal=%s %s", request.getKanal(), VALIDERER_IKKE_MOT_KODEVERK));
			}

			if (MottaksKanalCode.valueOf(request.getKanal()) == MottaksKanalCode.NAV_NO_UINNLOGGET && !request.getTema()
					.equalsIgnoreCase(FagomradeCode.SER.name())) {
				throw new InputValideringFeiletException("Det er kun mulig å arkivere med mottakskanal NAV_NO_UINNLOGGET dersom tema=SER.");
			}

		} else {
			try {
				UtsendingsKanalCode.valueOf(request.getKanal());
			} catch (IllegalArgumentException e) {
				throw new InputValideringFeiletException(format("Oppgitt kanal=%s %s", request.getKanal(), VALIDERER_IKKE_MOT_KODEVERK));
			}
		}
	}

	private void validateSak(Sak sak, Bruker bruker, String tema) {
		if (Sakstype.FAGSAK.equals(sak.getSakstype())) {
			validateFagsak(sak, bruker, tema);
		}

		if (Sakstype.GENERELL_SAK.equals(sak.getSakstype())) {
			validateGenerellSak(sak, bruker, tema);
		}

		if (Sakstype.ARKIVSAK.equals(sak.getSakstype()) || sak.getSakstype() == null) {
			validateArkivsak(sak);

		}
	}

	private void validateFagsak(Sak sak, Bruker bruker, String tema) {
		if (isBlank(tema)) {
			throw new InputValideringFeiletException("tema må være satt dersom sakstype=FAGSAK");
		}
		if (bruker == null) {
			throw new InputValideringFeiletException("Bruker må være satt dersom sakstype=FAGSAK");
		}
		if (isBlank(sak.getFagsakId())) {
			throw new InputValideringFeiletException("Sak.fagsakId må være satt dersom sakstype=FAGSAK");
		}
		if (sak.getFagsaksystem() == null) {
			throw new InputValideringFeiletException("Sak.fagsaksystem må være satt dersom sakstype=FAGSAK");
		}
		if (isNotBlank(sak.getArkivsaksnummer())) {
			throw new InputValideringFeiletException("Sak.arkivsaksnummer skal ikke være satt dersom sakstype=FAGSAK");
		}
		if (sak.getArkivsaksystem() != null) {
			throw new InputValideringFeiletException("Sak.arkivsaksystem skal ikke være satt dersom sakstype=FAGSAK");
		}
	}

	private void validateGenerellSak(Sak sak, Bruker bruker, String tema) {
		if (isBlank(tema)) {
			throw new InputValideringFeiletException("tema må være satt dersom sakstype=GENERELL_SAK");
		}
		if (bruker == null) {
			throw new InputValideringFeiletException("Bruker må være satt dersom sakstype=GENERELL_SAK");
		}
		if (isNotBlank(sak.getFagsakId())) {
			throw new InputValideringFeiletException("Sak.fagsakId skal ikke være satt dersom sakstype=GENERELL_SAK");
		}
		if (sak.getFagsaksystem() != null) {
			throw new InputValideringFeiletException("Sak.fagsaksystem skal ikke være satt dersom sakstype=GENERELL_SAK");
		}
		if (isNotBlank(sak.getArkivsaksnummer())) {
			throw new InputValideringFeiletException("Sak.arkivsaksnummer skal ikke være satt dersom sakstype=GENERELL_SAK");
		}
		if (sak.getArkivsaksystem() != null) {
			throw new InputValideringFeiletException("Sak.arkivsaksystem skal ikke være satt dersom sakstype=GENERELL_SAK");
		}
	}

	private void validateArkivsak(Sak sak) {
		if (isNotBlank(sak.getFagsakId())) {
			throw new InputValideringFeiletException("Sak.fagsakId skal ikke være satt dersom sakstype=ARKIVSAK");
		}
		if (sak.getFagsaksystem() != null) {
			throw new InputValideringFeiletException("Sak.fagsaksystem skal ikke være satt dersom sakstype=ARKIVSAK");
		}
		if (isBlank(sak.getArkivsaksnummer())) {
			throw new InputValideringFeiletException("Sak.arkivsaksnummer må være satt dersom sakstype=GENERELL_SAK");
		}
		if (sak.getArkivsaksystem() == null) {
			throw new InputValideringFeiletException("Sak.arkivsaksystem må være satt dersom sakstype=GENERELL_SAK");
		}
		if (!isNumeric(sak.getArkivsaksnummer())) {
			throw new InputValideringFeiletException("Sak.arkivsaksnummer skal være opprettet i GSAK/PSAK og må være et numerisk heltall.");
		}
	}

	private void validateDokument(Dokument dokument) {
		if (isNotBlank(dokument.getDokumentKategori())) {
			try {
				DokumentKategoriCode.valueOf(dokument.getDokumentKategori());
			} catch (IllegalArgumentException e) {
				throw new InputValideringFeiletException(format("Dokument.dokumentkategori %s", VALIDERER_IKKE_MOT_KODEVERK));
			}
		}
		if (!isEmpty(dokument.getDokumentvarianter())) {
			dokument.getDokumentvarianter().forEach(this::validateDokumentVariant);
		}
	}

	private void validateDokumentVariant(DokumentVariant dokumentVariant) {
		if (isBlank(dokumentVariant.getFiltype())) {
			throw new InputValideringFeiletException("Dokument.dokumentvariant.filtype må være satt");
		}
		try {
			FilTypeCode.valueOf(dokumentVariant.getFiltype());
		} catch (IllegalArgumentException e) {
			throw new InputValideringFeiletException(format("Dokument.dokumentvariant.filtype %s", VALIDERER_IKKE_MOT_KODEVERK));
		}
		if (isBlank(dokumentVariant.getVariantformat())) {
			throw new InputValideringFeiletException("Dokument.dokumentvariant.variantformat må være satt");
		}
		try {
			VariantFormatCode.valueOf(dokumentVariant.getVariantformat());
		} catch (IllegalArgumentException e) {
			throw new InputValideringFeiletException(format("Dokument.dokumentvariant.variantformat %s", VALIDERER_IKKE_MOT_KODEVERK));
		}
		if (dokumentVariant.getVariantformat().equals(VariantFormatCode.ARKIV.name())
				&& !Arrays.asList(FilTypeCode.PDF, FilTypeCode.PDFA, FilTypeCode.TIFF, FilTypeCode.PNG, FilTypeCode.JPEG )
				.contains(FilTypeCode.valueOf(dokumentVariant.getFiltype()))) {
			throw new InputValideringFeiletException("Dokument.dokumentvariant.filtype på være PDF, PDFA, TIFF, PNG eller JPEG for Dokument.dokumentvariant.variantformat=ARKIV.");
		}
	}
}