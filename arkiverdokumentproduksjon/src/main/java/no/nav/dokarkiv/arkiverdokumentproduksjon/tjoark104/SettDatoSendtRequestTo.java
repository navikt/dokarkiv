package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark104;

import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.ApplicationException;
import org.apache.commons.lang.StringUtils;

import java.util.Date;
import java.util.List;

/**
 * Domain transfer object for settDatoSendt
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public class SettDatoSendtRequestTo {
	private List<Long> journalpostIds;
	private String endretAvNavn;
	private Date datoSendtPrint;

	public SettDatoSendtRequestTo(List<Long> journalpostIds, String endretAvNavn, Date datoSendtPrint) {
		this.journalpostIds = journalpostIds;
		this.endretAvNavn = endretAvNavn;
		this.datoSendtPrint = datoSendtPrint;
	}

	public List<Long> getJournalpostIds() {
		return journalpostIds;
	}

	public String getEndretAvNavn() {
		return endretAvNavn;
	}

	public Date getDatoSendtPrint() {
		return datoSendtPrint;
	}

	public void validate() {
		if (journalpostIds == null || journalpostIds.isEmpty()) {
			throw new ApplicationException("journalpostIds was null or empty");
		}

		if (StringUtils.isBlank(endretAvNavn)) {
			throw new ApplicationException("endretAvNavn was null or empty");
		}

		if (datoSendtPrint == null) {
			throw new ApplicationException("datoSendtPrint was null");
		}
	}
}
