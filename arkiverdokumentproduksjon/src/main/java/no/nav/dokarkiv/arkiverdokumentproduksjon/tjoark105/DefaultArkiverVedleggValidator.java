package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark105;

import static no.nav.domain.dok.joark.codestable.JournalStatusCode.D;
import static no.nav.domain.dok.joark.codestable.VariantFormatCode.ARKIV;

import no.nav.domain.dok.joark.DokumentInfo;
import no.nav.domain.dok.joark.FilDetaljer;
import no.nav.domain.dok.joark.Journalpost;
import no.nav.domain.dok.joark.codestable.JournalStatusCode;
import no.nav.domain.dok.joark.codestable.VariantFormatCode;
import no.nav.modig.core.exception.ApplicationException;
import no.nav.service.dok.joark.NoJournalpostFoundException;
import no.nav.service.dok.joark.journalbehandling.IllegalDocumentUpdateException;
import no.nav.service.dok.joark.nsb.to.ArkiverVedleggRequestTo;
import org.springframework.util.StringUtils;

import java.util.Set;
import java.util.TreeSet;

/**
 * Default implmenentation of ArkiverVedleggValidator
 *
 * @author Magnar Brandsdal, Visma Consulting
 */
public class DefaultArkiverVedleggValidator implements ArkiverVedleggValidator {

	/** {@inheritDoc} */
	@Override
	public void validate(ArkiverVedleggRequestTo arkiverVedleggRequestTo) {
		validateRequestFields(arkiverVedleggRequestTo);

		validateFilDetaljerVariantFormat(arkiverVedleggRequestTo.getDokumentInfo().getFildetaljerListe());
	}

	/** {@inheritDoc} */
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
		if (StringUtils.isEmpty(arkiverVedleggRequestTo.getEndretAvNavn())) {
			sb.append("EndretAvNavn ");
		}
		if (arkiverVedleggRequestTo.getFerdigstillDokument() == null) {
			sb.append("FerdigstillDokument ");
		}

		DokumentInfo dokumentInfo = arkiverVedleggRequestTo.getDokumentInfo();

		if (dokumentInfo.getKategori() == null) {
			sb.append("Kategori ");
		}
		if (StringUtils.isEmpty(dokumentInfo.getTittel())) {
			sb.append("Tittel ");
		}
		if (StringUtils.isEmpty(dokumentInfo.getBrevkode())) {
			sb.append("Brevkode ");
		}
		if (StringUtils.isEmpty(dokumentInfo.getDokumenttypeId())) {
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
