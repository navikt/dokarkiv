package no.nav.dokarkiv.core.repository.sak;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import no.nav.dokarkiv.core.domain.codes.SakStatusCode;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class SakSearchCriteria {
	private List<String> aktoerId;
	private String orgnr;
	private List<String> tema;
	private String fagsakNr;
	private String applikasjon;
	private List<SakStatusCode> statuser;
	private Boolean soekNullStatus;


	public List<String> getAktoerId() {	return aktoerId == null ? emptyList() : aktoerId;}

	public Optional<String> getOrgnr() {
		return Optional.ofNullable(orgnr);
	}

	List<String> getTema() {
		return tema == null ? emptyList() : tema;
	}

	Optional<String> getFagsakNr() {
		return Optional.ofNullable(fagsakNr);
	}

	public Optional<String> getApplikasjon() {
		return Optional.ofNullable(applikasjon);
	}

	List<SakStatusCode> getStatuser() {return statuser == null ? emptyList() : statuser;}

	Optional<Boolean> getSoekNullStatus() {return Optional.ofNullable(soekNullStatus);}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append("aktoerId", aktoerId)
				.append("orgnr", orgnr)
				.append("tema", tema)
				.append("fagsakNr", fagsakNr)
				.append("applikasjon", applikasjon)
				.append("SakStatusCode", statuser)
				.append("soekNullStatus", soekNullStatus)
				.toString();
	}
}
