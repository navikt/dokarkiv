package no.nav.dokarkiv.journalpost.v1.rjoark203.support;

import no.nav.dokarkiv.core.domain.entities.Journalpost;

import java.util.List;

public class KopierService {

	public Journalpost copyFrom(Journalpost original, List<String> dokumentInfoIder) {
		Journalpost ny = Journalpost.builder().build();

		//TODO: implement
		return ny;
	}
}
