package no.nav.dokarkiv.hentjournalsakinfo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
@NoArgsConstructor
@AllArgsConstructor
public class Journalpost {

	private Long journalpostId;
	private String journalForendeEnhetId;
	private Date journalDato;
	private Date sendtPrintDato;
	private Integer antallRetur;
	private Date avsendtReturDato;
	private String innhold;
	private String kravtype;
	private String merknad;
	private String fordeling;
	private Boolean originaltBestilt;
	private String kanalReferanseId;
	private FagomradeCode fagomrade;
	private JournalStatusCode journalstatus;
	private Date dokumentDato;
	private String avsenderMottaker;
	private String avsenderMottakerId;
	private String journalfortAvNavn;
	private Date mottattDato;
	private MottaksKanalCode mottakskanal;
	private UtsendingsKanalCode utsendingskanal;
	private String land;
	private FaktiskDistribusjonskanalCode faktiskDistribusjonskanal;
	private Boolean elektroniskDistribusjon;
	private Date ekspedertDato;
	private Date lestDato;
	private Date mottattAdressatDato;
	private JournalpostTypeCode journalposttype;
	private Boolean signatur;
	private Saksrelasjon saksrelasjon;
	@Builder.Default
	private final Set<JournalpostDokumentInfoRelasjon> journalpostDokumentInfoRelasjoner = new HashSet<>();
//	private final Set<Kryssreferanse> kryssreferanser = new HashSet<>(); TODO Trenger vi denne?
//	private final Set<ReturInfo> returInfos = new HashSet<>(); TODO Trenger vi denne?
//	private Behandlingsrelasjon behandlingsrelasjon; TODO Trenger vi denne?

	@Data
	@Builder
	public static class Saksrelasjon {
		private Long saksrelasjonId;
		private String sakId;
		private Boolean feilregistrert;
		private String endretAvNavn;
		private FagsystemCode fagsystem;

	}

}
