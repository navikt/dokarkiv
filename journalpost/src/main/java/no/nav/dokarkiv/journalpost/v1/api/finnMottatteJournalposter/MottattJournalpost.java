package no.nav.dokarkiv.journalpost.v1.api.finnMottatteJournalposter;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(value = NON_NULL)
public class MottattJournalpost {

	@Schema(
			description = "ID til journalpost i Joark",
			example = "22345678"
	)
	private long journalpostId;

	@Schema(
			description = "Status på journalposten i joark. Journalstatusen gir en indikasjon på hvor i journalførings- eller dokumentproduksjonsprosessen journalposten befinner seg.",
			example = "M"
	)
	private String journalStatus;

	@Schema(
			description = "Kanalen dokumentene ble mottatt i",
			example = "NAV_NO"
	)
	private String mottaksKanal;

	@Hidden
	@Schema(
			description = """
					Person eller organisasjon som har et forhold til NAV, f.eks. som mottaker av tjenester eller ytelser.
					Returneres bare internt i teamdokumentløsninger.
					"""
	)
	private MottattJournalpostBruker bruker;

	@Schema(
			description = "Temaet/Fagområdet som en journalpost og tilhørende sak tilhører",
			example = "PEN"
	)
	private String tema;

	@Schema(
			description = "Detaljering av tema på journalpost og tilhørende sak",
			example = "ab0072"
	)
	private String behandlingstema;

	@Schema(
			description = " NAV-enheten som har journalført forsendelsen",
			example = "0001"
	)
	private String journalforendeEnhet;

	@Schema(
			description = "Datoen journalposten ble opprettet i arkivet. Datoen settes automatisk og kan ikke overskrives.",
			example = "2019-12-04T11:07:25.596+0000"
	)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
	private Date datoOpprettet;
}
