package no.nav.dokarkiv.core.domain.builder;

import no.nav.dokarkiv.core.domain.ChangeStamp;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FaktiskDistribusjonskanalCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.domain.entities.Behandlingsrelasjon;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.entities.Kryssreferanse;
import no.nav.dokarkiv.core.domain.entities.ReturInfo;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Builder for Journalpost.
 *
 * @author Thomas Eugen Bjørge, Sirius IT
 */
@Deprecated // bruk lombok builder istedet
public class JournalpostBuilder extends Builder<Journalpost> {

	private JournalpostBuilder() {
	}
	
	public static JournalpostBuilder getJournalpostBuilder() {
		return new JournalpostBuilder();
	}
	
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
	private String opprettetAvNavn;
	private String endretAvNavn;
	private String kanalReferanseId;
	private FagomradeCode fagomrade;
	private JournalStatusCode journalStatus;
	private Map<String, String> tilleggsopplysninger = new HashMap<>();
	private Set<Bruker> brukere = new HashSet<Bruker>();
	private Saksrelasjon saksrelasjon;
	private Set<JournalpostDokumentInfoRelasjon> dokumentInfoRelasjoner = new HashSet<JournalpostDokumentInfoRelasjon>();
	private Set<Kryssreferanse> kryssreferanser = new HashSet<Kryssreferanse>();
	private Set<ReturInfo> returInfos = new HashSet<ReturInfo>();
	private Behandlingsrelasjon behandlingsrelasjon;
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
	private Boolean signatur;
	private Date ekspedertDato;
	private Date lestDato;
	private Date mottattAdressatDato;
	private String opprettetKildeNavn;
	private String endretKildeNavn;
	private JournalpostTypeCode journalpostType;
	private ChangeStamp changeStamp;

	public JournalpostBuilder journalpostId(Long value) { this.journalpostId = value; return this; }
	public JournalpostBuilder journalForendeEnhetId(String value) { this.journalForendeEnhetId = value; return this; }
	public JournalpostBuilder journalDato(Date value) { this.journalDato = value; return this; }
	public JournalpostBuilder sendtPrintDato(Date value) { this.sendtPrintDato = value; return this; }
	public JournalpostBuilder antallRetur(Integer value) { this.antallRetur = value; return this; }
	public JournalpostBuilder avsendtReturDato(Date value) { this.avsendtReturDato = value; return this; }
	public JournalpostBuilder innhold(String value) { this.innhold = value; return this; }
	public JournalpostBuilder kravtype(String value) { this.kravtype = value; return this; }
	public JournalpostBuilder merknad(String value) { this.merknad = value; return this; }
	public JournalpostBuilder fordeling(String value) { this.fordeling = value; return this; }
	public JournalpostBuilder originaltBestilt(Boolean value) { this.originaltBestilt = value; return this; }
	public JournalpostBuilder opprettetAvNavn(String value) { this.opprettetAvNavn = value; return this; }
	public JournalpostBuilder endretAvNavn(String value) { this.endretAvNavn = value; return this; }
	public JournalpostBuilder kanalReferanseId(String value) { this.kanalReferanseId = value; return this; }
	public JournalpostBuilder fagomrade(FagomradeCode value) { this.fagomrade = value; return this; }
	public JournalpostBuilder journalStatus(JournalStatusCode value) { this.journalStatus = value; return this; }
	public JournalpostBuilder tilleggsopplysninger(Map<String, String> value) {	this.tilleggsopplysninger = value; return this; }
	public JournalpostBuilder brukere(Bruker... value) { this.brukere.addAll(Arrays.asList(value)); return this; }
	public JournalpostBuilder saksrelasjon(Saksrelasjon value) { this.saksrelasjon = value; return this; }
	public JournalpostBuilder dokumentInfoRelasjoner(JournalpostDokumentInfoRelasjon... value) { 
		this.dokumentInfoRelasjoner.addAll(Arrays.asList(value)); return this; }
	public JournalpostBuilder kryssReferanser(Kryssreferanse... value) {
		this.kryssreferanser.addAll(Arrays.asList(value)); return this; }
	public JournalpostBuilder returInfos(ReturInfo... value) { this.returInfos.addAll(Arrays.asList(value)); return this; }
	public JournalpostBuilder dokumentDato(Date value) { this.dokumentDato = value; return this; }
	public JournalpostBuilder avsenderMottaker(String value) { this.avsenderMottaker = value; return this; }
	public JournalpostBuilder avsenderMottakerId(String value) { this.avsenderMottakerId = value; return this; }
	public JournalpostBuilder journalfortAvNavn(String value) { this.journalfortAvNavn = value; return this; }
	public JournalpostBuilder mottattDato(Date value) { this.mottattDato = value; return this; }
	public JournalpostBuilder mottakskanal(MottaksKanalCode value) { this.mottakskanal = value; return this; }
	public JournalpostBuilder utsendingskanal(UtsendingsKanalCode value) { this.utsendingskanal = value; return this; }
	public JournalpostBuilder land(String value) { this.land = value; return this; }
	public JournalpostBuilder faktiskDistribusjonskanal(FaktiskDistribusjonskanalCode value) {
		this.faktiskDistribusjonskanal = value; return this; }
	public JournalpostBuilder elektroniskDistribusjon(Boolean value) { this.elektroniskDistribusjon = value; return this; }
	public JournalpostBuilder ekspedertDato(Date value) { this.ekspedertDato = value; return this; }
	public JournalpostBuilder lestDato(Date value) { this.lestDato = value; return this; }
	public JournalpostBuilder mottattAdressatDato(Date value) { this.mottattAdressatDato = value; return this; }
	public JournalpostBuilder opprettetKildeNavn(String value) { this.opprettetKildeNavn = value; return this; }
	public JournalpostBuilder endretKildeNavn(String value) { this.endretKildeNavn = value; return this; }
	public JournalpostBuilder journalpostType(JournalpostTypeCode value) { this.journalpostType = value; return this; }
	public JournalpostBuilder changeStamp(ChangeStamp value) { this.changeStamp = value; return this; }
	public JournalpostBuilder signatur(Boolean value) {this.signatur = value; return this; }
	public JournalpostBuilder behandlingsrelasjon(Behandlingsrelasjon value) {this.behandlingsrelasjon = value; return this; }
	
	@Override
	public Journalpost build() {
		Journalpost journalpost = new Journalpost(journalpostId, 1);
		journalpost.setJournalForendeEnhetId(journalForendeEnhetId);
		journalpost.setJournalDato(journalDato);
		journalpost.setSendtPrintDato(sendtPrintDato);
		journalpost.setAntallRetur(antallRetur);
		journalpost.setAvsendtReturDato(avsendtReturDato);
		journalpost.setInnhold(innhold);
		journalpost.setKravtype(kravtype);
		journalpost.setMerknad(merknad);
		journalpost.setFordeling(fordeling);
		journalpost.setOriginaltBestilt(originaltBestilt);
		journalpost.setOpprettetAvNavn(opprettetAvNavn);
		journalpost.setEndretAvNavn(endretAvNavn);
		journalpost.setKanalReferanseId(kanalReferanseId);
		journalpost.setFagomrade(fagomrade);
		journalpost.setJournalstatus(journalStatus);
		journalpost.setSaksrelasjon(saksrelasjon);
		journalpost.setTilleggsopplysninger(tilleggsopplysninger);
		for (Bruker bruker : brukere) {
			journalpost.addBruker(bruker);
		}
		for (JournalpostDokumentInfoRelasjon dokumentInfoRelasjon : dokumentInfoRelasjoner) {
			journalpost.addJournalpostDokumentInfoRelasjon(dokumentInfoRelasjon);
		}
		for (Kryssreferanse kryssreferanse : kryssreferanser) {
			journalpost.addKryssReferanse(kryssreferanse);
		}
		for (ReturInfo returInfo : returInfos) {
			journalpost.addReturInfo(returInfo);
		}
		journalpost.getJournalpostDokumentInfoRelasjoner().forEach(relasjon -> {
			if (relasjon.getDokumentInfo() != null) {
				relasjon.getDokumentInfo().setOriginalJournalpost(journalpost);
			}
		});
		journalpost.setBehandlingsrelasjon(behandlingsrelasjon);
		journalpost.setDokumentDato(dokumentDato);
		journalpost.setAvsenderMottaker(avsenderMottaker);
		journalpost.setAvsenderMottakerId(avsenderMottakerId);
		journalpost.setJournalfortAvNavn(journalfortAvNavn);
		journalpost.setMottattDato(mottattDato);
		journalpost.setMottakskanal(mottakskanal);
		journalpost.setUtsendingskanal(utsendingskanal);
		journalpost.setLand(land);
		journalpost.setFaktiskDistribusjonskanal(faktiskDistribusjonskanal);
		journalpost.setElektroniskDistribusjon(elektroniskDistribusjon);
		journalpost.setEkspedertDato(ekspedertDato);
		journalpost.setLestDato(lestDato);
		journalpost.setMottattAdressatDato(mottattAdressatDato);
		journalpost.setOpprettetKildeNavn(opprettetKildeNavn);
		journalpost.setEndretKildeNavn(endretKildeNavn);
		journalpost.setJournalposttype(journalpostType);
		journalpost.setChangeStamp(changeStamp);
		journalpost.setSignatur(signatur);
		return journalpost;
	}

}
