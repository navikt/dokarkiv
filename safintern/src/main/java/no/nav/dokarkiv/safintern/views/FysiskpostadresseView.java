package no.nav.dokarkiv.safintern.views;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.Mapping;
import com.fasterxml.jackson.annotation.JsonInclude;
import no.nav.dokarkiv.core.domain.entities.UtsendingsInfo;

@JsonInclude(JsonInclude.Include.NON_NULL)
@EntityView(UtsendingsInfo.FysiskPostadresse.class)
public interface FysiskpostadresseView {
	@Mapping("adresselinje1")
	String getAdresselinje1();

	@Mapping("adresselinje2")
	String getAdresselinje2();

	@Mapping("adresselinje3")
	String getAdresselinje3();

	@Mapping("postnummer")
	String getPostnummer();

	@Mapping("poststed")
	String getPoststed();

	@Mapping("landkode")
	String getLandkode();
}
