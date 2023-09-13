package no.nav.dokarkiv.safintern.views;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.Mapping;
import com.fasterxml.jackson.annotation.JsonInclude;
import no.nav.dokarkiv.core.domain.entities.UtsendingsInfo;

@JsonInclude(JsonInclude.Include.NON_NULL)
@EntityView(UtsendingsInfo.DigitalPostadresse.class)
public interface DigitalPostadresseView {
	@Mapping("adresse")
	String getAdresse();

	@Mapping("postkasseLeverandor")
	String getLeverandoer();
}
