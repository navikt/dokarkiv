package no.nav.dokarkiv.hentdokument.dlf.to;

import static org.apache.logging.log4j.util.Strings.isBlank;
import static org.springframework.util.ObjectUtils.isEmpty;

import no.nav.dokarkiv.core.exceptions.InvalidArgumentException;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

/**
 * Request object for service TJOARK039 SettMetadataIDLF
 *
 * @author Per Kristian Foss, Visma Sirius
 */
public class SettMetadataIDLFRequest {

	/**
	 * ID used for serialization.
	 */
	private static final long serialVersionUID = -***gammelt_fnr***96449856L;

	private SettMetadataForUthenting settMetadataForUthenting;
	private SettMetadataForKopiering settMetadataForKopiering;
	private byte[] dlfDokument;

	/**
	 * Constructs a new SettMetadataIDLFRequest for the scenario LagringAvDok
	 *
	 * @param settMetadataForUthenting Contains properties for the settMetadataForUthenting scenario
	 * @param dlfDokument              The DLF-document to be updated
	 */
	public SettMetadataIDLFRequest(SettMetadataForUthenting settMetadataForUthenting, byte[] dlfDokument) {
		this.settMetadataForUthenting = settMetadataForUthenting;
		this.dlfDokument = dlfDokument == null ? null : dlfDokument.clone();
	}

	/**
	 * Constructs a new SettMetadataIDLFRequest for the scenario HentingAvVedlegg
	 *
	 * @param settMetadataForKopiering Contains properties for the settMetadataForKopiering scenario
	 * @param dlfDokument              The DLF-document to be updated
	 */
	public SettMetadataIDLFRequest(SettMetadataForKopiering settMetadataForKopiering, byte[] dlfDokument) {
		this.settMetadataForKopiering = settMetadataForKopiering;
		this.dlfDokument = dlfDokument;
	}

	/**
	 * Check that mandatory fields are set.
	 */
	public void validate() {
		StringBuilder missingFields = new StringBuilder();
		validateForUthenting(missingFields);
		validateForKopiering(missingFields);

		if (isEmpty(dlfDokument)) {
			missingFields.append("dlfDokument ");
		}
		if (missingFields.length() > 0) {
			throw new InvalidArgumentException("Missing parameter(s): " + missingFields.toString());
		}
	}

	/**
	 * Checks if this is a request to update metadata for copying of document.
	 *
	 * @return True if settMetadataForKopiering is set.
	 */
	public boolean isForUthenting() {
		return settMetadataForUthenting != null;
	}

	private void validateForUthenting(StringBuilder missingFields) {
		if (settMetadataForUthenting != null) {
			if (settMetadataForUthenting.getJournalpostId() == null) {
				missingFields.append("journalpostId ");
			}
			if (isBlank(settMetadataForUthenting.getFilUuid())) {
				missingFields.append("filUuid ");
			}
			if (settMetadataForUthenting.getVersjon() == null) {
				missingFields.append("versjon ");
			}
		}
	}

	private void validateForKopiering(StringBuilder missingFields) {
		if (settMetadataForKopiering != null) {
			if (settMetadataForKopiering.getJournalpostIdVedlegg() == null) {
				missingFields.append("journalpostIdVedlegg ");
			}
			if (isBlank(settMetadataForKopiering.getFilUuidVedlegg())) {
				missingFields.append("filUuidVedlegg ");
			}
			if (isEmpty(settMetadataForKopiering.getDlfHoveddokument())) {
				missingFields.append("dlfHoveddokument ");
			}
		}
	}

	/**
	 * Getter for the settMetadataForUthenting property
	 *
	 * @return the settMetadataForUthenting
	 */
	public SettMetadataForUthenting getSettMetadataForUthenting() {
		return settMetadataForUthenting;
	}

	/**
	 * Getter for the settMetadataForKopiering property
	 *
	 * @return the settMetadataForKopiering
	 */
	public SettMetadataForKopiering getSettMetadataForKopiering() {
		return settMetadataForKopiering;
	}

	/**
	 * Getter for the dlfDokument property
	 *
	 * @return the dlfDokument
	 */
	public byte[] getDlfDokument() {
		return dlfDokument == null ? null : dlfDokument.clone();
	}

	/**
	 * Setter for the dlfDokument property
	 *
	 * @param dlfDokument the dlfDokument to set
	 */
	public void setDlfDokument(byte[] dlfDokument) {
		this.dlfDokument = dlfDokument == null ? null : dlfDokument.clone();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return new ReflectionToStringBuilder(this).toString();
	}

}
