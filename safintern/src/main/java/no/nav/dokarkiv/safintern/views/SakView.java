package no.nav.dokarkiv.safintern.views;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import no.nav.dokarkiv.core.domain.codes.SakStatusCode;
import no.nav.dokarkiv.core.domain.entities.Sak;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@EntityView(Sak.class)
public interface SakView {
	@JsonIgnore
	@IdMapping("sakId")
	Long getSakId();

	@Mapping("tema")
	String getTema();

	@Mapping("aktoerId")
	String getAktoerId();

	@Mapping("orgnr")
	String getOrgNr();

	@Mapping("fagsakNr")
	String getFagsakNr();

	@Mapping("applikasjon")
	String getApplikasjon();

	@Mapping("opprettetTidspunkt")
	LocalDateTime getOpprettetTid();

	@Mapping("sakStatus")
	SakStatusCode getSakStatus();
}
