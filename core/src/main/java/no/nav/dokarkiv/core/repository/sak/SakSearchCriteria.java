package no.nav.dokarkiv.core.repository.sak;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
@Builder
@AllArgsConstructor
public class SakSearchCriteria {
	private String aktoerId;
	private String orgnr;
	private List<String> tema;
	private String fagsakNr;
	private String applikasjon;


	public Optional<String> getAktoerId() {
		return Optional.ofNullable(aktoerId);
	}

	public Optional<String> getOrgnr() {
		return Optional.ofNullable(orgnr);
	}

	List<String> getTema() {
		return tema == null ? new ArrayList<>() : tema;
	}

	Optional<String> getFagsakNr() {
		return Optional.ofNullable(fagsakNr);
	}

	public Optional<String> getApplikasjon() {
		return Optional.ofNullable(applikasjon);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append("aktoerId", aktoerId)
				.append("orgnr", orgnr)
				.append("tema", tema)
				.append("fagsakNr", fagsakNr)
				.append("applikasjon", applikasjon)
				.toString();
	}
}
