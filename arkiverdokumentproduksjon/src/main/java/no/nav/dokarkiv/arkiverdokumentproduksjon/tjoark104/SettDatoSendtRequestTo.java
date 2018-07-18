package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark104;

import lombok.Data;
import no.nav.dokarkiv.core.exceptions.ApplicationException;
import org.apache.commons.lang.StringUtils;

import java.util.Date;
import java.util.List;

/**
 * Domain transfer object for settDatoSendt
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
@Data
public class SettDatoSendtRequestTo {
	private final List<Long> journalpostIds;
	private final String endretAvNavn;
	private final Date datoSendtPrint;

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
