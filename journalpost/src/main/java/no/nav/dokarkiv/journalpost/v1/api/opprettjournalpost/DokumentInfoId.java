package no.nav.dokarkiv.journalpost.v1.api.opprettjournalpost;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DokumentInfoId {
	@Schema(
			description = "Id til et dokumentInfo-objekt som peker på det arkiverte dokumentet.",
			example = "123"
	)
	String dokumentInfoId;
}