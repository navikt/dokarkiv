package no.nav.dokarkiv.journalpost.v1.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@ApiModel
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Dokumenter {

	@ApiModelProperty(
			value = "Id til et dokumentInfo-objekt som peker på det arkiverte dokumentet."
	)
	String dokumentInfoId;
}
