package no.nav.dokarkiv.safintern.views;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.Mapping;
import no.nav.dokarkiv.core.domain.entities.UtsendingsInfo;

@EntityView(UtsendingsInfo.DigitalPostadresse.class)
public interface DigitalPostadresseView {
	@Mapping("adresse")
	String getAdresse();

	@Mapping("postkasseLeverandor")
	String getLeverandoer();
}
