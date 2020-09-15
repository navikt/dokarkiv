package no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions;

import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Exception that is thrown when {@link TilknyttetJournalpostSomCode} has an illegal status
 *
 * @author Roar Bjurstrom, Visma Consulting
 */
public class UgyldigTilknyttetJournalpostSomVerdiException extends DokarkivFunctionalException {

	private static final long serialVersionUID = 2992512874669188758L;

	private final TilknyttetJournalpostSomCode tilknyttetJournalpostSomCode;

	public UgyldigTilknyttetJournalpostSomVerdiException(String message, TilknyttetJournalpostSomCode
			tilknyttetJournalpostSomCode) {
		super(message);
		this.tilknyttetJournalpostSomCode = tilknyttetJournalpostSomCode;
	}

	public TilknyttetJournalpostSomCode getTilknyttetJournalpostSomCode() {
		return tilknyttetJournalpostSomCode;
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.appendSuper(super.toString());
		builder.append("tilknyttetJournalpostSomCode", tilknyttetJournalpostSomCode);
		return builder.toString();
	}

}
