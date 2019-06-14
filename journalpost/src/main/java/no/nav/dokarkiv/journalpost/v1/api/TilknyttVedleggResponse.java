package no.nav.dokarkiv.journalpost.v1.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Olav Røstvold Thorsen, Visma Consulting.
 */

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TilknyttVedleggResponse {

	@Builder.Default
	@ApiModelProperty(
			value = "Liste med dokumenter som ikke kunne knyttes til journalpostId",
			required = false)
	private List<FeiletDokument> feiletDokument = new ArrayList<>();
}
