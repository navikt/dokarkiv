package no.nav.dokarkiv.journalpost.v1.util.oppdaterjournalpost;

import no.nav.dokarkiv.core.consumer.pdl.IdentConsumer;
import no.nav.dokarkiv.core.domain.codes.AvsenderMottakerIdTypeCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.journalpost.v1.api.AvsenderMottaker;
import no.nav.dokarkiv.journalpost.v1.api.AvsenderMottakerIdType;
import no.nav.dokarkiv.journalpost.v1.api.OppdaterJournalpostRequest;
import org.springframework.stereotype.Component;

import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.JOURNALPOST_AVSENDER_MOTTAKER;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.JOURNALPOST_AVSENDER_MOTTAKER_ID;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.JOURNALPOST_AVSENDER_MOTTAKER_ID_TYPE;
import static org.apache.logging.log4j.util.Strings.isNotBlank;

@Component
public class AvsenderMottakerUpdater {
	public static final String DELETE_MARKER = " ";
	private final IdentConsumer identConsumer;

	public AvsenderMottakerUpdater(IdentConsumer identConsumer) {
		this.identConsumer = identConsumer;
	}

	void updateAvsenderMottaker(Journalpost journalpost, OppdaterJournalpostRequest oppdaterJournalpostRequest, ChangeTracker endret) {
		if (oppdaterJournalpostRequest.getAvsenderMottaker() != null) {
			AvsenderMottaker ny = oppdaterJournalpostRequest.getAvsenderMottaker();
			if (ny.getId() != null) {
				oppdaterAvsenderMottakerIdOgIdType(journalpost, endret, ny);
			}
			oppdaterAvsenderMottakerNavn(journalpost, oppdaterJournalpostRequest, endret, ny);
			if (isNotBlank(ny.getLand())) {
				oppdaterAvsenderMottakerLand(journalpost, endret, ny);
			}
		}
	}

	private void oppdaterAvsenderMottakerIdOgIdType(Journalpost journalpost, ChangeTracker endret, AvsenderMottaker ny) {
		final String nyId = ny.getId();
		AvsenderMottakerIdTypeCode nyIdTypeCode = oversettAvsenderMottakerIdType(ny.getIdType());
		if (nyIdTypeCode != journalpost.getAvsenderMottakerIdType()) {
			oppdaterAvsenderMottakerIdType(endret, journalpost, nyIdTypeCode);
		}
		if (!nyId.equalsIgnoreCase(journalpost.getAvsenderMottakerId()) && !nyId.isBlank()) {
			oppdaterAvsenderMottakerId(endret, journalpost, nyId);
		}
		if (DELETE_MARKER.equals(nyId)) {
			nullUtAvsenderMottakerIdOgAvsenderMottakerIdType(journalpost, endret);
		}
	}

	private void oppdaterAvsenderMottakerNavn(Journalpost journalpost, OppdaterJournalpostRequest oppdaterJournalpostRequest, ChangeTracker endret, AvsenderMottaker ny) {
		if (DELETE_MARKER.equals(ny.getNavn())) {
			nullUtAvsenderMottakerNavn(journalpost, endret);
		}

		if (isNotBlank(ny.getNavn())) {
			oppdaterAvsenderMottakerNavn(endret, journalpost, ny.getNavn());
		} else if (isIdOgIdTypeFnrSatt(ny)) {
			String navn = identConsumer.hentPersonnavn(ny.getId(), oppdaterJournalpostRequest.getTema());
			oppdaterAvsenderMottakerNavn(endret, journalpost, navn);
		}
	}

	private static void oppdaterAvsenderMottakerLand(Journalpost journalpost, ChangeTracker endret, AvsenderMottaker ny) {
		journalpost.setLand(ny.getLand());
		endret.setEndretFlagg(true);
	}

	private boolean isIdOgIdTypeFnrSatt(AvsenderMottaker ny) {
		return ny.getId() != null && !DELETE_MARKER.equals(ny.getId()) && AvsenderMottakerIdTypeCode.FNR.equals(oversettAvsenderMottakerIdType(ny.getIdType()));
	}

	private static void nullUtAvsenderMottakerIdOgAvsenderMottakerIdType(Journalpost journalpost, ChangeTracker endret) {
		oppdaterAvsenderMottakerId(endret, journalpost, null);
		oppdaterAvsenderMottakerIdType(endret, journalpost, null);
	}

	private static void nullUtAvsenderMottakerNavn(Journalpost journalpost, ChangeTracker endret) {
		oppdaterAvsenderMottakerNavn(endret, journalpost, null);
	}

	private static void oppdaterAvsenderMottakerNavn(ChangeTracker endret, Journalpost journalpost, String navn) {
		endret.add(
				JOURNALPOST_AVSENDER_MOTTAKER,
				journalpost.getAvsenderMottaker(),
				navn
		);
		journalpost.setAvsenderMottaker(navn);
	}

	private static void oppdaterAvsenderMottakerId(ChangeTracker endret, Journalpost journalpost, String id) {
		endret.add(
				JOURNALPOST_AVSENDER_MOTTAKER_ID,
				journalpost.getAvsenderMottakerId(),
				id
		);
		journalpost.setAvsenderMottakerId(id);
	}

	private static void oppdaterAvsenderMottakerIdType(ChangeTracker endret, Journalpost journalpost, AvsenderMottakerIdTypeCode idType) {
		String gammelIdType = journalpost.getAvsenderMottakerIdType() == null ? null : journalpost.getAvsenderMottakerIdType().toString();
		String nyIdType = idType == null ? null : idType.toString();
		endret.add(
				JOURNALPOST_AVSENDER_MOTTAKER_ID_TYPE,
				gammelIdType,
				nyIdType
		);
		journalpost.setAvsenderMottakerIdType(idType);
	}

	private AvsenderMottakerIdTypeCode oversettAvsenderMottakerIdType(AvsenderMottakerIdType idType) {
		if (idType == null) {
			return null;
		}
		return switch (idType) {
			case FNR -> AvsenderMottakerIdTypeCode.FNR;
			case HPRNR -> AvsenderMottakerIdTypeCode.HPRNR;
			case ORGNR -> AvsenderMottakerIdTypeCode.ORGNR;
			case UTL_ORG -> AvsenderMottakerIdTypeCode.UTL_ORG;
		};
	}
}
