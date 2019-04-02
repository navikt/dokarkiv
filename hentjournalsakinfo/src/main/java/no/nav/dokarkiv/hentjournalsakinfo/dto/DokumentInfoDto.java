package no.nav.dokarkiv.hentjournalsakinfo.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Value;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;

import java.util.Date;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
public class DokumentInfoDto {
	private final Long dokumentInfoId;
	@JsonIgnore
	private final String tilknyttetSom;
	private final DokumentStatusCode dokumentstatus;
	private final Date datoFerdigstilt;
	private final String brevkode;
	private final List<VariantDto> varianter;
	private final String tittel;
	private final SkjermingTypeCode skjerming;
	private final Long origJournalpostId;
	private final List<LogiskVedleggDto> logiske;
}
