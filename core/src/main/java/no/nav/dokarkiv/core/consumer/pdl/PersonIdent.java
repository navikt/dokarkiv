package no.nav.dokarkiv.core.consumer.pdl;

import lombok.Builder;
import lombok.Getter;

import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

@Builder
@Getter
public class PersonIdent {

	private String fornavn;
	private String mellomnavn;
	private String etternavn;
	private String ident;

	public String getNavn() {
		return Stream.of(fornavn, mellomnavn, etternavn).filter(n -> n != null && !n.isEmpty()).collect(joining(" "));
	}
}
