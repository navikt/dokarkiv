package no.nav.dokarkiv.safintern.views;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.Mapping;
import com.fasterxml.jackson.annotation.JsonInclude;
import no.nav.dokarkiv.core.domain.entities.UtsendingsInfo;

@JsonInclude(JsonInclude.Include.NON_NULL)
@EntityView(UtsendingsInfo.NavNoVarsling.class)
public interface NavNoVarslingView {
	@Mapping("kontaktinformasjon")
	String getVarselSendtTil();

	@Mapping("varslingstekst")
	String getVarseltekst();
}
