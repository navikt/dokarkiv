package no.nav.dokarkiv.safintern.views;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;
import com.fasterxml.jackson.annotation.JsonIgnore;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;

@EntityView(FilDetaljer.class)
public interface FildetaljerView {
	@IdMapping
	@JsonIgnore
	Long getFildetaljerId();

	@Mapping("skjermingType")
	SkjermingTypeCode getSkjerming();

	@Mapping("variantFormat")
	VariantFormatCode getFormat();

	@Mapping("filUuid")
	String getUuid();

	@Mapping("filnavn")
	String getNavn();

	@Mapping("filtype")
	FilTypeCode getType();

	@Mapping("filstorrelse")
	String getStoerrelse();
}
