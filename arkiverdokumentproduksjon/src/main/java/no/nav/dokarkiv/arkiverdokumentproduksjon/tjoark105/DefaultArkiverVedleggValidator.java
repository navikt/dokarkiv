package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark105;

import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.IllegalDocumentUpdateException;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.ApplicationException;
import no.nav.dokarkiv.core.exceptions.NoJournalpostFoundException;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.TreeSet;

import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.D;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.ARKIV;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * Default implmenentation of ArkiverVedleggValidator
 *
 * @author Magnar Brandsdal, Visma Consulting
 */
@Component
public class DefaultArkiverVedleggValidator implements ArkiverVedleggValidator {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void validate(ArkiverVedleggRequestTo arkiverVedleggRequestTo) {
		validateRequestFields(arkiverVedleggRequestTo);

		validateFilDetaljerVariantFormat(arkiverVedleggRequestTo.getDokumentInfo().getFildetaljerListe());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void validate(Journalpost journalpost, Long journalpostId) throws NoJournalpostFoundException {
		if (journalpost == null) {
			throw new NoJournalpostFoundException("Journalpost with id: " + journalpostId + " not found",
					journalpostId);
		}
		JournalStatusCode journalstatus = journalpost.getJournalstatus();

		if (!D.equals(journalstatus)) {
			throw new IllegalDocumentUpdateException("Journalpost with id: " +
					journalpost.getJournalpostId() + " can not be updated");
		}
	}

	/**
	 * Validates required fields in request
	 *
	 * @param arkiverVedleggRequestTo
	 */
	private void validateRequestFields(ArkiverVedleggRequestTo arkiverVedleggRequestTo) {
		if (arkiverVedleggRequestTo == null) {
			throw new ApplicationException("Request er null");
		}
		StringBuilder sb = new StringBuilder();

		if (arkiverVedleggRequestTo.getJournalpostId() == null || arkiverVedleggRequestTo.getJournalpostId() == 0L) {
			sb.append("JournalpostId ");
		}
		if (isEmpty(arkiverVedleggRequestTo.getEndretAvNavn())) {
			sb.append("EndretAvNavn ");
		}
		if (arkiverVedleggRequestTo.getFerdigstillDokument() == null) {
			sb.append("FerdigstillDokument ");
		}

		DokumentInfo dokumentInfo = arkiverVedleggRequestTo.getDokumentInfo();

		if (dokumentInfo.getKategori() == null) {
			sb.append("Kategori ");
		}
		if (isEmpty(dokumentInfo.getTittel())) {
			sb.append("Tittel ");
		}
		if (isEmpty(dokumentInfo.getBrevkode())) {
			sb.append("Brevkode ");
		}
		if (isEmpty(dokumentInfo.getDokumenttypeId())) {
			sb.append("DokumenttypeId ");
		}

		if (dokumentInfo.getSensitivt() == null) {
			sb.append("Sensitivt ");
		}

		Set<FilDetaljer> fildetaljerList = dokumentInfo.getFildetaljerListe();

		for (FilDetaljer filDetaljer : fildetaljerList) {
			if (filDetaljer.getFiltype() == null) {
				sb.append("FilType ");
			}
			if (filDetaljer.getVariantFormat() == null) {
				sb.append("VariantFormat ");
			}
			if (filDetaljer.getFileContent() == null || filDetaljer.getFileContent().length == 0) {
				sb.append("IkkeRedigerbartDokument ");
			}
		}

		if (sb.length() > 0) {
			throw new ApplicationException("Missing parameter(s): " + sb.toString());
		}

	}

	/**
	 * Validates that Fildetaljer contains no duplicate variantformat and has one variantformat 'ARKIV'.
	 *
	 * @param fildetaljerListe List of fildetaljer
	 */
	void validateFilDetaljerVariantFormat(Set<FilDetaljer> fildetaljerListe) {
		Set<VariantFormatCode> variantFormatCodeSet = new TreeSet<>();

		for (FilDetaljer filDetaljer : fildetaljerListe) {
			if (!variantFormatCodeSet.add(filDetaljer.getVariantFormat())) {
				throw new ApplicationException("Request can only contain one Fildetaljer with variantformat " +
						filDetaljer.getVariantFormat().name());
			}
		}

		if (!variantFormatCodeSet.contains(ARKIV)) {
			throw new ApplicationException("Request requires Fildetaljer with variantformat " + ARKIV.name());
		}
	}

}
