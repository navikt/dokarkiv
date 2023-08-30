package no.nav.dokarkiv.safintern.views;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.FetchStrategy;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.MappingCorrelatedSimple;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.entities.Sak;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;

@EntityView(Saksrelasjon.class)
public interface SaksrelasjonView {
	@Mapping("sakId")
	Long getSakId();

	@Mapping("fagsystem")
	FagsystemCode getFagsystem();

	@Mapping("feilregistrert")
	Boolean getFeilregistrert();

	@MappingCorrelatedSimple(
			correlationBasis = "sakId",
			correlated = Sak.class,
			correlationExpression = "sakId IN correlationKey",
			fetch = FetchStrategy.JOIN
	)
	SakView getSak();
}
