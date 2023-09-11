package no.nav.dokarkiv.safintern.views;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.Mapping;
import no.nav.dokarkiv.core.domain.codes.AvsenderMottakerIdTypeCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;

@EntityView(Journalpost.class)
public interface AvsenderMottakerView {
	@Mapping("avsenderMottakerId")
	String getId();

	@Mapping("avsenderMottakerIdType")
	AvsenderMottakerIdTypeCode getType();

	@Mapping("avsenderMottaker")
	String getNavn();

	@Mapping("land")
	String getLand();
}

