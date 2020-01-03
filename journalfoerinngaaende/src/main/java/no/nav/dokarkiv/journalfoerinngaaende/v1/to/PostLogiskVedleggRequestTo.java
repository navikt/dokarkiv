package no.nav.dokarkiv.journalfoerinngaaende.v1.to;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostLogiskVedleggRequestTo {
	private String tittel;
}
