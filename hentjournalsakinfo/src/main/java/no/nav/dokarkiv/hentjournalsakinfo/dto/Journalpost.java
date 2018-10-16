package no.nav.dokarkiv.hentjournalsakinfo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.FaktiskDistribusjonskanalCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Data
@Builder
@AllArgsConstructor
public class Journalpost {

	private final Long journalpostId;
	private final String journalForendeEnhetId;
	private final Date journalDato;
	private final Date sendtPrintDato;
	private final Integer antallRetur;
	private final Date avsendtReturDato;
	private final String innhold;
	private final String kravtype;
	private final String merknad;
	private final String fordeling;
	private final Boolean originaltBestilt;
	private final String kanalReferanseId;
	private final FagomradeCode fagomrade;
	private final JournalStatusCode journalstatus;
	private final Date dokumentDato;
	private final String avsenderMottaker;
	private final String avsenderMottakerId;
	private final String journalfortAvNavn;
	private final Date mottattDato;
	private final MottaksKanalCode mottakskanal;
	private final UtsendingsKanalCode utsendingskanal;
	private final String land;
	private final FaktiskDistribusjonskanalCode faktiskDistribusjonskanal;
	private final boolean elektroniskDistribusjon;
	private final Date ekspedertDato;
	private final Date lestDato;
	private final Date mottattAdressatDato;
	private final JournalpostTypeCode journalposttype;
	private final boolean signatur;
	private final Saksrelasjon saksrelasjon;
	private final Date datoOpprettet;
	@Builder.Default
	private final Set<JournalpostDokumentInfoRelasjon> journalpostDokumentInfoRelasjoner = new HashSet<>();
//	private final final Set<Kryssreferanse> kryssreferanser = new HashSet<>(); TODO Trenger vi denne?
//	private final final Set<ReturInfo> returInfos = new HashSet<>(); TODO Trenger vi denne?
//	private final Behandlingsrelasjon behandlingsrelasjon; TODO Trenger vi denne?

	@Data
	@Builder
	public static class Saksrelasjon {
		private final Long saksrelasjonId;
		private final String sakId;
		private final Boolean feilregistrert;
		private final String endretAvNavn;
		private final FagsystemCode fagsystem;

	}

}
