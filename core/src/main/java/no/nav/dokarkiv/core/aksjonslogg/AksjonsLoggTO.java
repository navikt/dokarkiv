package no.nav.dokarkiv.core.aksjonslogg;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AksjonsLoggTO {
	private Long journalpostId;
	private Long dokumentInfoId;
	@NotNull
	private AksjonsTypeCode aksjon;
	private String hjemmel;
	private String bruker;
	private String melding;
	private String utfoertAv;
	private List<ArkivElementEndringTO> arkivElementEndringTOList;
}
