package no.nav.dokarkiv.journalpost.v1.validators;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.Sak;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;
import no.nav.dokarkiv.core.exceptions.DokumentUnderRedigeringException;
import no.nav.dokarkiv.core.exceptions.InvalidJournalpostStructureException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeMidlertidigException;
import no.nav.dokarkiv.core.exceptions.KanIkkeFerdigstilleException;
import no.nav.dokarkiv.journalpost.v1.api.FerdigstillJournalpostRequest;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static java.lang.String.format;
import static no.nav.dokarkiv.core.domain.codes.FagsystemCode.FS22;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.A;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.D;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.FL;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.FS;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.M;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.MO;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.OD;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.R;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.UB;
import static no.nav.dokarkiv.core.domain.codes.SakStatusCode.AAPEN;
import static no.nav.dokarkiv.journalpost.v1.validators.CommonValidator.isConsumerFagsystemArgus;
import static no.nav.dokarkiv.journalpost.v1.validators.CommonValidator.validateJournalfoerendeEnhet;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
public class FerdigstillJournalpostValidator {

	private static final EnumSet<JournalStatusCode> MIDLERTIDIG_JOURNALSTATUS = EnumSet.of(M, MO, UB, D, R, OD, FL, FS, A);

	public FerdigstillJournalpostValidator() {
		// Vi setter ingenting her
	}

	public void validateRequest(FerdigstillJournalpostRequest request) {
		validateJournalfoerendeEnhet(request.getJournalfoerendeEnhet(), "journalfoerendeEnhet");
	}

	public void validateJournalpostTilstand(Journalpost journalpost) {
		verifyMidlertidigJournalfoert(journalpost);
		verifyDokumenttilstand(journalpost);
	}

	public void validateSakrelasjon(final Journalpost journalpost, final Sak sak) {
		if (FS22.equals(journalpost.getSaksrelasjon().getFagsystem())) {
			var sakStatus = sak.getSakStatus();
			if (sakStatus != null && sakStatus != AAPEN) {
				throw new KanIkkeFerdigstilleException(format(
						"Journalposten kan ikke ferdigstilles som generell sak eller fagsak med sakstatus=%s. Sakstatus må være=%s eller null".formatted(sakStatus, AAPEN)));
			}
		}
	}

	public void validateJournalpostStruktur(Journalpost journalpost) {
		verifyExactlyOneHoveddokument(journalpost);
		verifyFildetaljerVariantFormat(journalpost);
		verifyAtLeastOneBrukerExists(journalpost);
		verifySaksrelasjonIsPresent(journalpost);
	}

	public void validatePaakrevdeFelter(Journalpost journalpost) {
		List<String> manglendePaakrevdeFelter = new ArrayList<>();
		verifyPaakrevdeFelterJournalpost(journalpost, manglendePaakrevdeFelter);
		verifyPaakrevdeFelterSaksrelasjon(journalpost.getSaksrelasjon(), manglendePaakrevdeFelter);
		verifyPaakrevdeFelterBruker(journalpost, manglendePaakrevdeFelter);
		verifyPaakrevdeFelterDokumentInfo(journalpost, manglendePaakrevdeFelter);
		if (!manglendePaakrevdeFelter.isEmpty()) {
			throw new KanIkkeFerdigstilleException(format(
					"Journalposten mangler følgende felter: %s", String.join(", ", manglendePaakrevdeFelter)));
		}
	}

	private void verifyMidlertidigJournalfoert(Journalpost jp) {

		List<String> feilmeldinger = new ArrayList<>();

		if (!MIDLERTIDIG_JOURNALSTATUS.contains(jp.getJournalstatus())) {
			feilmeldinger.add(format(
					"Den har journalstatus=%s (midlertidig journalførte journalposter har en av følgende journalstatuser=%s)",
					jp.getJournalstatus(), MIDLERTIDIG_JOURNALSTATUS));
		}

		if (Boolean.TRUE.equals(jp.getSaksrelasjon() == null ? Boolean.FALSE : jp.getSaksrelasjon().getFeilregistrert())) {
			feilmeldinger.add("Den er feilregistrert");
		}

		if (!feilmeldinger.isEmpty()) {
			throw new JournalpostIkkeMidlertidigException(format(
					"Journalposten er ikke ansett som midlertidig journalført av følgende grunn(er): %s",
					String.join(", ", feilmeldinger)));
		}
	}

	private void verifyDokumenttilstand(Journalpost jp) {
		try {
			jp.verifyNoDokumentInfosUnderRedigering();
		} catch (InvalidJournalpostStructureException e) {
			throw new DokumentUnderRedigeringException("Ett eller flere av dokumentene på journalposten er under redigering");
		}
	}

	private void verifyExactlyOneHoveddokument(Journalpost jp) {
		try {
			jp.verifyOnlyOneHoveddokument();
		} catch (InvalidJournalpostStructureException e) {
			throw new KanIkkeFerdigstilleException("Journalposten inneholder ingen eller flere enn ett hoveddokument");
		}
	}

	private void verifyFildetaljerVariantFormat(Journalpost jp) {
		try {
			if (isConsumerFagsystemArgus()) {
				verifyVarianterFagsystemArgus(jp);
			} else {
				jp.verifyArkivVariantOfAllDocuments();
			}
		} catch (InvalidJournalpostStructureException e) {
			throw new KanIkkeFerdigstilleException("Journalposten mangler arkivvariant");
		}
		jp.getJournalpostDokumentInfoRelasjoner().forEach(dr -> {
			try {
				dr.getDokumentInfo().verifyNoVariantDuplicates();
			} catch (InvalidJournalpostStructureException e) {
				throw new KanIkkeFerdigstilleException("Journalposten inneholder flere dokumentvarianter med samme variantformat. Følgende duplikate varianter ble funnet: " + e.getMessage());
			}
		});
	}

	// For å støtte arkivering av Excel-filer fra Argus uten ARKIV-variant
	private void verifyVarianterFagsystemArgus(Journalpost journalpost) {
		for (DokumentInfo dokumentInfo : journalpost.findAllDokumentInfos()) {
			if (!dokumentInfo.hasArkivFormat() && !dokumentInfo.hasOriginalFormat()) {
				throw new KanIkkeFerdigstilleException("Argus-arkivering må ha hoveddokument med minst en original-variant");
			}
		}
	}

	private void verifyAtLeastOneBrukerExists(Journalpost jp) {
		if (jp.getBrukere().isEmpty()) {
			throw new KanIkkeFerdigstilleException("Journalposten er ikke knyttet til en bruker");
		}
	}

	private void verifySaksrelasjonIsPresent(Journalpost jp) {
		if (jp.getSaksrelasjon() == null) {
			throw new KanIkkeFerdigstilleException(String.format("Kunne ikke ferdigstille: Journalpost med journalpostId=%s må ha en saksrelasjon", jp.getJournalpostId()));
		}
	}

	private void verifyPaakrevdeFelterJournalpost(Journalpost journalpost, List<String> manglendePaakrevdeFelter) {
		verifyFieldNotNull(journalpost.getFagomrade(), "Journalpost.fagomrade", manglendePaakrevdeFelter);
		verifyStringNotBlank(journalpost.getInnhold(), "Journalpost.innhold", manglendePaakrevdeFelter);
		if (!JournalpostTypeCode.N.equals(journalpost.getJournalposttype())) {
			verifyStringNotBlank(journalpost.getAvsenderMottaker(), "Journalpost.avsendMottaker.navn", manglendePaakrevdeFelter);
		}
		if (JournalpostTypeCode.I.equals(journalpost.getJournalposttype())) {
			verifyFieldNotNull(journalpost.getMottakskanal(), "Journalpost.mottakskanal", manglendePaakrevdeFelter);
		}
	}

	private void verifyPaakrevdeFelterDokumentInfo(Journalpost
														   journalpost, List<String> manglendePaakrevdeFelter) {
		journalpost.getJournalpostDokumentInfoRelasjoner()
				.forEach(journalpostDokumentInfoRelasjon -> verifyMandatoryFelterDokumentinfo(journalpostDokumentInfoRelasjon.getDokumentInfo(), manglendePaakrevdeFelter));
	}

	private void verifyPaakrevdeFelterBruker(Journalpost journalpost, List<String> manglendePaakrevdeFelter) {
		journalpost.getBrukere().forEach(bruker -> {
			verifyStringNotBlank(bruker.getBrukerId(), "Bruker.brukerId", manglendePaakrevdeFelter);
			verifyFieldNotNull(bruker.getBrukerType(), "Bruker.brukerType", manglendePaakrevdeFelter);
		});
	}

	private void verifyPaakrevdeFelterSaksrelasjon(Saksrelasjon
														   saksrelasjon, List<String> manglendePaakrevdeFelter) {
		if (saksrelasjon != null) {
			verifyFieldNotNull(saksrelasjon.getSakId(), "Saksrelasjon.sakId", manglendePaakrevdeFelter);
			verifyFieldNotNull(saksrelasjon.getFagsystem(), "Saksrelasjon.fagsystem", manglendePaakrevdeFelter);
		}
	}

	private void verifyMandatoryFelterDokumentinfo(DokumentInfo
														   dokumentInfo, List<String> manglendePaakrevdeFelter) {
		verifyStringNotBlank(dokumentInfo.getTittel(), "DokumentInfo.tittel", manglendePaakrevdeFelter);
	}

	/**
	 * Checks that a field is not null.
	 *
	 * @param fieldValue The value to check.
	 * @param fieldName  The fieldName.
	 */
	private void verifyFieldNotNull(Object fieldValue, String fieldName, List<String> manglendePaakrevdeFelter) {
		if (fieldValue == null) {
			manglendePaakrevdeFelter.add(fieldName);
		}
	}

	/**
	 * Checks that a String is not null or empty.
	 *
	 * @param fieldValue The String to check.
	 * @param fieldName  The fieldName.
	 */
	private void verifyStringNotBlank(String fieldValue, String fieldName, List<String> manglendePaakrevdeFelter) {
		if (isBlank(fieldValue)) {
			manglendePaakrevdeFelter.add(fieldName);
		}
	}
}
