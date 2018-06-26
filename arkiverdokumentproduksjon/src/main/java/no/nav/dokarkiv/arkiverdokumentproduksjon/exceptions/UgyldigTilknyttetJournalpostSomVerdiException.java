package no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions;

import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.stelvio.FunctionalRecoverableException;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Exception that is thrown when {@link TilknyttetJournalpostSomCode} has an illegal status
 *
 * @author Roar Bjurstrom, Visma Consulting
 */
public class UgyldigTilknyttetJournalpostSomVerdiException extends FunctionalRecoverableException {

	private static final long serialVersionUID = ***gammelt_fnr***69188758L;

	private TilknyttetJournalpostSomCode tilknyttetJournalpostSomCode;

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
