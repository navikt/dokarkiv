package no.nav.dokarkiv.sak.repository;

import no.nav.dokarkiv.core.domain.entities.Sak;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

import java.time.LocalDateTime;

public class SakTestData {
	private static final String[] gyldigeOrgnr = {"999263550", "991012133", "895106402"};
	private String tema = RandomStringUtils.randomAlphabetic(3);
	private String aktoerId = RandomStringUtils.randomAlphabetic(9);
	private String orgnr;
	private String fagsakNr;
	private String applikasjon;
	private String opprettetAv = RandomStringUtils.randomAlphabetic(8);
	private LocalDateTime opprettetTidspunkt = LocalDateTime.now();


	public Sak build() {
		return Sak.builder()
				.aktoerId(aktoerId)
				.orgnr(orgnr)
				.tema(tema)
				.fagsakNr(fagsakNr)
				.applikasjon(applikasjon)
				.opprettetAv(opprettetAv)
				.opprettetTidspunkt(opprettetTidspunkt)
				.build();
	}


	public SakTestData aktoerId(String aktoerId) {
		this.aktoerId = aktoerId;
		this.orgnr = null;
		return this;
	}

	public SakTestData orgnr(String orgnr) {
		this.orgnr = orgnr;
		this.aktoerId = null;
		return this;
	}

	public SakTestData tema(String tema) {
		this.tema = tema;
		return this;
	}

	public SakTestData fagsakNr(String fagsakNr) {
		this.fagsakNr = fagsakNr;
		return this;
	}

	public SakTestData applikasjon(String applikasjon) {
		this.applikasjon = applikasjon;
		return this;
	}

	public SakTestData opprettettidspunkt(LocalDateTime opprettetTidspunkt) {
		this.opprettetTidspunkt = opprettetTidspunkt;
		return this;
	}

	public static String generateValidOrgnr() {
		return gyldigeOrgnr[RandomUtils.nextInt(0, 3)];
	}
}
