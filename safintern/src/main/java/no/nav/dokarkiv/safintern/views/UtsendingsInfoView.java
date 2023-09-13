package no.nav.dokarkiv.safintern.views;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRawValue;
import no.nav.dokarkiv.core.domain.entities.UtsendingsInfo;

@JsonInclude(JsonInclude.Include.NON_NULL)
@EntityView(UtsendingsInfo.class)
public interface UtsendingsInfoView {
	@JsonIgnore
	@IdMapping
	Long getJournalpostId();

	@Mapping("fysiskPostadresse")
	FysiskpostadresseView getFysiskPostadresse();

	@Mapping("navNoVarsling")
	NavNoVarslingView getNavNoVarsling();

	@Mapping("digitalPostadresse")
	DigitalPostadresseView getDigitalPostadresse();

	@JsonRawValue
	@Mapping("epostVarsler.epostvarsel")
	String getEpostVarsel();

	@JsonRawValue
	@Mapping("smsVarsler.smsvarsel")
	String getSmsVarsel();
}
