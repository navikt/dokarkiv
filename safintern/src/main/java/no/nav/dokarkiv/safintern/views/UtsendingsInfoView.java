package no.nav.dokarkiv.safintern.views;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRawValue;
import no.nav.dokarkiv.core.domain.entities.UtsendingsInfo;

@EntityView(UtsendingsInfo.class)
public interface UtsendingsInfoView {
	@IdMapping
	@JsonIgnore
	Long getJournalpostId();

	@Mapping("fysiskPostadresse")
	FysiskpostadresseView getFysiskPostadresse();

	@Mapping("navNoVarsling")
	NavNoVarslingView getNavNoVarsling();

	@Mapping("digitalPostadresse")
	DigitalPostadresseView getDigitalPostadresse();

	@Mapping("epostVarsler.epostvarsel")
	@JsonRawValue
	String getEpostVarsel();

	@Mapping("smsVarsler.smsvarsel")
	@JsonRawValue
	String getSmsVarsel();
}
