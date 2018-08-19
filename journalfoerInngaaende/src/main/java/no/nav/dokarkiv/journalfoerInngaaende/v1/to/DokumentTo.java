package no.nav.dokarkiv.journalfoerInngaaende.v1.to;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Data
@Builder
public class DokumentTo {
	private String dokumentId;
	private String dokumenttypeId;
	private String navSkjemaId;
	private String tittel;
	private String dokumentkategori;
	private String tilknyttetSom;
	private List<VariantTo> varianter;
	private List<LogiskVedleggTo> logiskeVedlegg;
}
