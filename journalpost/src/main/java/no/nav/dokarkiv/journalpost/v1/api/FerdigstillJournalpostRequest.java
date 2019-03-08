package no.nav.dokarkiv.journalpost.v1.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FerdigstillJournalpostRequest {

	@ApiModelProperty(
			value = "NAV-enheten som personen som utfører journalføring jobber for. Ved automatisk journalføring uten mennesker involvert, skal enhet settes til \"9999\".",
			required = true,
			example = "9999")
	private String journalfEnhet;
}
