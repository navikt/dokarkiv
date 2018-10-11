package no.nav.dokarkiv.hentjournalsakinfo.tjoarkxyz;

import no.nav.dokarkiv.hentjournalsakinfo.dto.DokumentInfo;
import no.nav.dokarkiv.hentjournalsakinfo.dto.Journalpost;
import no.nav.dokarkiv.hentjournalsakinfo.dto.JournalpostDokumentInfoRelasjon;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Component
public class JournalpostMapper {

	public Journalpost map(no.nav.dokarkiv.core.domain.entities.Journalpost jp) {
		return Journalpost.builder()
				.journalpostId(jp.getJournalpostId())
				.journalForendeEnhetId(jp.getJournalForendeEnhetId())
				.journalDato(jp.getJournalDato())
				.sendtPrintDato(jp.getSendtPrintDato())
				.antallRetur(jp.getAntallRetur())
				.avsendtReturDato(jp.getAvsendtReturDato())
				.innhold(jp.getInnhold())
				.kravtype(jp.getKravtype())
				.merknad(jp.getMerknad())
				.fordeling(jp.getFordeling())
				.originaltBestilt(jp.getOriginaltBestilt())
				.kanalReferanseId(jp.getKanalReferanseId())
				.fagomrade(jp.getFagomrade())
				.journalstatus(jp.getJournalstatus())
				.dokumentDato(jp.getDokumentDato())
				.avsenderMottaker(jp.getAvsenderMottaker())
				.avsenderMottakerId(jp.getAvsenderMottakerId())
				.journalfortAvNavn(jp.getJournalfortAvNavn())
				.mottattDato(jp.getMottattDato())
				.mottakskanal(jp.getMottakskanal())
				.utsendingskanal(jp.getUtsendingskanal())
				.land(jp.getLand())
				.faktiskDistribusjonskanal(jp.getFaktiskDistribusjonskanal())
				.elektroniskDistribusjon(jp.getElektroniskDistribusjon())
				.ekspedertDato(jp.getEkspedertDato())
				.lestDato(jp.getLestDato())
				.mottattAdressatDato(jp.getMottattAdressatDato())
				.journalposttype(jp.getJournalposttype())
				.signatur(jp.getSignatur())
				.saksrelasjon(Journalpost.Saksrelasjon.builder()
						.saksrelasjonId(jp.getSaksrelasjon().getSaksrelasjonId())
						.sakId(jp.getSaksrelasjon().getSakId())
						.feilregistrert(jp.getSaksrelasjon().getFeilregistrert())
						.endretAvNavn(jp.getSaksrelasjon().getEndretAvNavn())
						.fagsystem(jp.getSaksrelasjon().getFagsystem())
						.build())
				.journalpostDokumentInfoRelasjoner(new HashSet<>(
						jp.getJournalpostDokumentInfoRelasjoner().stream().map(relasjon ->
								JournalpostDokumentInfoRelasjon.builder()
										.journalpostDokumentInfoRelasjonId(relasjon.getJournalpostDokumentInfoRelasjonId())
										.tilknyttetAvNavn(relasjon.getTilknyttetAvNavn())
										.tilknyttetJournalpostSom(relasjon.getTilknyttetJournalpostSom())
										.dokumentInfo(DokumentInfo.builder()
												.dokumentInfoId(relasjon.getDokumentInfo().getDokumentInfoId())
												.brevkode(relasjon.getDokumentInfo().getBrevkode())
												.brevgruppe(relasjon.getDokumentInfo().getBrevgruppe())
												.konvertertFraSystem(relasjon.getDokumentInfo().getKonvertertFraSystem())
												.sensitivt(relasjon.getDokumentInfo().getSensitivt())
												.slettet(relasjon.getDokumentInfo().getSlettet())
												.endretAvNavn(relasjon.getDokumentInfo().getEndretAvNavn())
												.kategori(relasjon.getDokumentInfo().getKategori())
												.dokumentstatus(relasjon.getDokumentInfo().getDokumentstatus())
												.dokumentFerdigDato(relasjon.getDokumentInfo().getDokumentFerdigDato())
												.tittel(relasjon.getDokumentInfo().getTittel())
												.konfidensialitet(relasjon.getDokumentInfo().getKonfidensialitet())
												.integritet(relasjon.getDokumentInfo().getIntegritet())
												.tilgjengelighet(relasjon.getDokumentInfo().getTilgjengelighet())
												.innskrenketPartsinnsyn(relasjon.getDokumentInfo().getInnskrenketPartsinnsyn())
												.innskrenketPartsinnsynFraTredjepart(relasjon.getDokumentInfo()
														.getInnskrenketPartsinnsynFraTredjepart())
												.organInternt(relasjon.getDokumentInfo().getOrganInternt())
												.originalJournalpostId(relasjon.getDokumentInfo()
														.getOriginalJournalpost()
														.getJournalpostId())
												.dokumenttypeId(relasjon.getDokumentInfo().getDokumenttypeId())
												.build())
										.build()).collect(Collectors.toSet())))
				.build();

	}

}
