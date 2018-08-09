package no.nav.dokarkiv.journalfoerInngaaende.v1.to;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DokumentinfoTo {
	String dokumentId;
	String dokumenttypeId;
	//TODO NAV-skjemaID
	String tittel;
	String dokumentkategori;
	String dokumenttilstand;
	String tilknyttetSom;
	List<VariantTo> varianter;
	List<LogiskVedleggTo> logiskeVedlegg;
}
