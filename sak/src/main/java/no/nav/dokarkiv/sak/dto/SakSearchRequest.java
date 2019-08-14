package no.nav.dokarkiv.sak.dto;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.dokarkiv.sak.repository.SakSearchCriteria;
import no.nav.dokarkiv.sak.validering.AtLeastOneOf;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@AtLeastOneOf(fields = {"aktoerId", "orgnr", "fagsakNr"})
public class SakSearchRequest {
	@ApiParam("Filtrering på saker opprettet for en aktør (person)")
	private String aktoerId;
	@ApiParam("Filtrering på saker opprettet for en organisasjon")
	private String orgnr;
	@ApiParam("Filtrering på applikasjon (iht felles kodeverk)")
	private String applikasjon;
	@ApiParam("Filtrering på tema (iht felles kodeverk)")
	private List<String> tema;
	@ApiParam("Filtrering på fagsakNr")
	private String fagsakNr;

	public String getAktoerId() {
		return aktoerId;
	}


	public SakSearchCriteria toCriteria() {
		return SakSearchCriteria.builder()
				.aktoerId(aktoerId)
				.orgnr(orgnr)
				.applikasjon(applikasjon)
				.tema(tema)
				.fagsakNr(fagsakNr).build();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
				.append("aktoerId", aktoerId)
				.append("orgnr", orgnr)
				.append("applikasjon", applikasjon)
				.append("tema", tema)
				.append("fagsaknr", fagsakNr)
				.toString();
	}
}
