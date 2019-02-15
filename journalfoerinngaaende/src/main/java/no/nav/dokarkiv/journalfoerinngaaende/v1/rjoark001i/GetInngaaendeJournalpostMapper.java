package no.nav.dokarkiv.journalfoerinngaaende.v1.rjoark001i;

import no.nav.dok.tjenester.journalfoerinngaaende.ArkivSakNoArkivsakSystemEnum;
import no.nav.dok.tjenester.journalfoerinngaaende.Avsender;
import no.nav.dok.tjenester.journalfoerinngaaende.Bruker;
import no.nav.dok.tjenester.journalfoerinngaaende.Dokument;
import no.nav.dok.tjenester.journalfoerinngaaende.GetJournalpostResponse;
import no.nav.dok.tjenester.journalfoerinngaaende.LogiskVedlegg;
import no.nav.dok.tjenester.journalfoerinngaaende.Variant;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;
import no.nav.dokarkiv.core.domain.entities.SkannetInnhold;
import no.nav.dokarkiv.core.exceptions.UgyldigJournalStatusException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Component
public class GetInngaaendeJournalpostMapper {

	private static final List<JournalStatusCode> MIDLERTIDIG_STATUS = Arrays.asList(JournalStatusCode.M, JournalStatusCode.MO, JournalStatusCode.UB, JournalStatusCode.OD);

	public GetJournalpostResponse map(Journalpost journalpost) {
		return new GetJournalpostResponse()
				.withJournalTilstand(mapJournaltilstand(journalpost))
				.withAvsender(mapAvsender(journalpost))
				.withBrukerListe(mapBrukere(journalpost.getBrukere()))
				.withArkivSak(mapArkivsak(journalpost.getSaksrelasjon()))
				.withTema(journalpost.getFagomrade() == null ? null : journalpost.getFagomrade().name())
				.withTittel(journalpost.getInnhold())
				.withKanalReferanseId(journalpost.getKanalReferanseId())
				.withForsendelseMottatt(journalpost.getMottattDato())
				.withMottaksKanal(journalpost.getMottakskanal() == null ? null : journalpost.getMottakskanal().name())
				.withJournalfEnhet(journalpost.getJournalForendeEnhetId())
				.withDokumentListe(mapDokumenter(journalpost.getJournalpostDokumentInfoRelasjoner()));
	}

	private GetJournalpostResponse.JournalTilstand mapJournaltilstand(Journalpost journalpost) {
		GetJournalpostResponse.JournalTilstand journaltilstand;
		if (journalpost.isFeilregistrert()) {
			journaltilstand = GetJournalpostResponse.JournalTilstand.UTGAAR;
		} else if (journalpost.hasEndeligJournalforingStatus()) {
			journaltilstand = GetJournalpostResponse.JournalTilstand.ENDELIG;
		} else if (MIDLERTIDIG_STATUS.contains(journalpost.getJournalstatus())) {
			journaltilstand = GetJournalpostResponse.JournalTilstand.MIDLERTIDIG;
		} else if (journalpost.hasUtgaattJournalforingStatus()) {
			journaltilstand = GetJournalpostResponse.JournalTilstand.UTGAAR;
		} else {
			throw new UgyldigJournalStatusException(String.format("Journalstatus=%s er ugyldig status for inngaaende journalpost med journalpostId=%s", journalpost
					.getJournalstatus(), journalpost.getJournalpostId()));
		}
		return journaltilstand;
	}

	private ArkivSakNoArkivsakSystemEnum mapArkivsak(Saksrelasjon saksrelasjon) {
		if (saksrelasjon == null) {
			return null;
		} else {
			return new ArkivSakNoArkivsakSystemEnum()
					.withArkivSakId(saksrelasjon.getSakId())
					.withArkivSakSystem(mapFagsystemCodeToArkivSakSystem(saksrelasjon.getFagsystem()));
		}
	}

	private List<Bruker> mapBrukere(Set<no.nav.dokarkiv.core.domain.entities.Bruker> brukere) {
		if (brukere.isEmpty()) {
			return new ArrayList<>();
		} else {
			return brukere.stream().map(bruker -> new Bruker()
					.withIdentifikator(bruker.getBrukerId())
					.withBrukerType(utledBrukerType(bruker.getBrukerId())))
					.collect(Collectors.toList());
		}
	}

	private Avsender mapAvsender(Journalpost journalpost) {
		if (journalpost.getAvsenderMottakerId() == null || journalpost.getAvsenderMottakerId().isEmpty()) {
			return null;
		} else {
			return new Avsender()
					.withIdentifikator(journalpost.getAvsenderMottakerId())
					.withAvsenderType(utledAvsenderType(journalpost.getAvsenderMottakerId()))
					.withNavn(journalpost.getAvsenderMottaker());
		}
	}

	private List<Dokument> mapDokumenter(Set<JournalpostDokumentInfoRelasjon> journalpostDokumentInfoRelasjoner) {
		List<Dokument> dokumentList = new ArrayList<>();

		dokumentList.addAll(journalpostDokumentInfoRelasjoner.stream()
				.filter(relasjon -> relasjon.getTilknyttetJournalpostSom().equals(TilknyttetJournalpostSomCode.HOVEDDOKUMENT))
				.map(this::mapDokumentinfoRelasjonToDokument)
				.collect(Collectors.toList()));

		dokumentList.addAll(journalpostDokumentInfoRelasjoner.stream()
				.filter(relasjon -> relasjon.getTilknyttetJournalpostSom().equals(TilknyttetJournalpostSomCode.VEDLEGG))
				.sorted(Comparator.comparing(relasjon -> relasjon.getDokumentInfo().getChangeStamp().getCreatedDate()))
				.map(this::mapDokumentinfoRelasjonToDokument)
				.collect(Collectors.toList()));

		return dokumentList;
	}

	private Dokument mapDokumentinfoRelasjonToDokument(JournalpostDokumentInfoRelasjon relasjon) {
		return new Dokument()
				.withDokumentId(relasjon.getDokumentInfo().getDokumentInfoId().toString())
				.withDokumentTypeId(relasjon.getDokumentInfo().getDokumenttypeId())
				.withNavSkjemaId(relasjon.getDokumentInfo().getBrevkode())
				.withTittel(relasjon.getDokumentInfo().getTittel())
				.withDokumentKategori(relasjon.getDokumentInfo().getKategori() == null ? null : relasjon.getDokumentInfo()
						.getKategori().name())
				.withVariant(mapVarianter(relasjon.getDokumentInfo().getFildetaljerListe()))
				.withLogiskVedleggListe(mapLogiskeVedlegg(relasjon.getDokumentInfo().getSkannetInnholdListe()));
	}

	private List<Variant> mapVarianter(Set<FilDetaljer> fildetaljer) {
		return fildetaljer.stream()
				.map(filDetaljer -> new Variant()
				.withArkivFilType(filDetaljer.getFiltype().name())
				.withVariantFormat(filDetaljer.getVariantFormat().name()))
				.collect(Collectors.toList());
	}

	private List<LogiskVedlegg> mapLogiskeVedlegg(Set<SkannetInnhold> skannetInnholdSet) {
		return skannetInnholdSet.stream().map(skannetInnhold -> new LogiskVedlegg()
				.withLogiskVedleggId(skannetInnhold.getSkannetInnholdId() == null ? null : skannetInnhold.getSkannetInnholdId()
						.toString())
				.withLogiskVedleggTittel(skannetInnhold.getVedleggInnhold()))
				.collect(Collectors.toList());
	}

	private Avsender.AvsenderType utledAvsenderType(String avsenderId) {
		if (avsenderId == null) {
			return null;
		} else if (avsenderId.length() == 11) {
			return Avsender.AvsenderType.PERSON;
		} else {
			return Avsender.AvsenderType.ORGANISASJON;
		}
	}

	private Bruker.BrukerType utledBrukerType(String brukerId) {
		if (brukerId == null) {
			return null;
		} else if (brukerId.length() == 11) {
			return Bruker.BrukerType.PERSON;
		} else {
			return Bruker.BrukerType.ORGANISASJON;
		}
	}

	protected String mapFagsystemCodeToArkivSakSystem(FagsystemCode fagsystemCode) {
		if (fagsystemCode.equals(FagsystemCode.FS22)) {
			return ArkivsystemKode.GSAK.name();
		} else if (fagsystemCode.equals(FagsystemCode.PEN)) {
			return ArkivsystemKode.PSAK.name();
		} else {
			return fagsystemCode.name();
		}
	}

	private enum ArkivsystemKode {
		GSAK,
		PSAK
	}

	protected FagsystemCode mapArkivSakSystemToFagsystemCode(String arkivSakSystem) {
		if (ArkivsystemKode.GSAK.name().equals(arkivSakSystem)) {
			return FagsystemCode.FS22;
		} else {
			return FagsystemCode.PEN;
		}
	}

}
