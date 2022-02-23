package no.nav.dokarkiv.arkiverdokumentmottak;

import lombok.Builder;
import lombok.Data;

/**
 * Support class for DokumentInfoIdVedleggListe
 * when mapping to response.
 *
 * @author Leo-Andreas Ervik, Visma Consulting. 21.02.2017
 */
@Builder
@Data
public class DokumentInfoIdVedleggTo {
	private final long dokumentInfoId;
	private final String dokumentTypeId;
}
