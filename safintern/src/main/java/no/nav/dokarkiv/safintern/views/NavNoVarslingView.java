package no.nav.dokarkiv.safintern.views;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.Mapping;
import no.nav.dokarkiv.core.domain.entities.UtsendingsInfo;

@EntityView(UtsendingsInfo.NavNoVarsling.class)
public interface NavNoVarslingView {

	@Mapping("kontaktinformasjon")
	String getVarselSendtTil();

	@Mapping("varslingstekst")
	String getVarseltekst();
}
