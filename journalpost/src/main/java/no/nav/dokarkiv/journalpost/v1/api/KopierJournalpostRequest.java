package no.nav.dokarkiv.journalpost.v1.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class KopierJournalpostRequest {

	@Builder.Default
	@ApiModelProperty(
			value = "",
			required = false,
			example = ""
	)
	private List<String> gjenbrukDokumenter = new ArrayList<>();
}
