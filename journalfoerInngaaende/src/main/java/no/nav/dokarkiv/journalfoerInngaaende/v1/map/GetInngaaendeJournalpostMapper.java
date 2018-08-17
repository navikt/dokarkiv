package no.nav.dokarkiv.journalfoerInngaaende.v1.map;

import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;
import no.nav.dokarkiv.core.domain.entities.SkannetInnhold;
import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import no.nav.dokarkiv.journalfoerInngaaende.v1.to.ArkivsakTo;
import no.nav.dokarkiv.journalfoerInngaaende.v1.to.AvsenderTo;
import no.nav.dokarkiv.journalfoerInngaaende.v1.to.BrukerTo;
import no.nav.dokarkiv.journalfoerInngaaende.v1.to.DokumentTo;
import no.nav.dokarkiv.journalfoerInngaaende.v1.to.JournalpostResponseTo;
import no.nav.dokarkiv.journalfoerInngaaende.v1.to.LogiskVedleggTo;
import no.nav.dokarkiv.journalfoerInngaaende.v1.to.VariantTo;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Component
public class GetInngaaendeJournalpostMapper {

	private static final List<JournalStatusCode> MIDLERTIDIG_STATUS = Arrays.asList(JournalStatusCode.M, JournalStatusCode.MO, JournalStatusCode.UB, JournalStatusCode.OD);

	public JournalpostResponseTo map(Journalpost journalpost) {
		return JournalpostResponseTo.builder()
				.journaltilstand(mapJournaltilstand(journalpost))
				.avsender(mapAvsender(journalpost))
				.brukere(mapBrukere(journalpost.getBrukere()))
				.arkivsak(mapArkivsak(journalpost.getSaksrelasjon()))
				.tema(journalpost.getFagomrade() == null ? null : journalpost.getFagomrade().name())
				.tittel(journalpost.getInnhold())
				.kanalreferanseId(journalpost.getKanalReferanseId())
				.forsendelseMottatt(journalpost.getMottattDato() == null ? null : LocalDateTime.ofInstant(journalpost.getMottattDato()
						.toInstant(), ZoneId.systemDefault()))
				.mottakskanal(journalpost.getMottakskanal() == null ? null : journalpost.getMottakskanal().name())
				.journalfoerendeEnhet(journalpost.getJournalForendeEnhetId())
				.dokumenter(mapDokumenter(journalpost.getJournalpostDokumentInfoRelasjoner()))
				.build();
	}

	private String mapJournaltilstand(Journalpost journalpost) {
		if (journalpost.isFeilregistrert()) {
			return JournaltilstandKode.UTGAAR.name();
		} else if (journalpost.hasEndeligJournalforingStatus()) {
			return JournaltilstandKode.ENDELIG.name();
		} else if (MIDLERTIDIG_STATUS.contains(journalpost.getJournalstatus())) {
			return JournaltilstandKode.MIDLERTIDIG.name();
		} else if (journalpost.hasUtgaattJournalforingStatus()) {
			return JournaltilstandKode.UTGAAR.name();
		} else {
			throw new DokarkivFunctionalException("Ugyldig journalstatus for inngående Journalpost. journalpostId=" + journalpost
					.getJournalpostId());
		}
	}

	private enum JournaltilstandKode {
		MIDLERTIDIG,
		UTGAAR,
		ENDELIG
	}

	private ArkivsakTo mapArkivsak(Saksrelasjon saksrelasjon) {
		if (saksrelasjon == null) {
			return null;
		}
		//TODO Finn ut om dette skal valideres på
// else if (saksrelasjon.getFagsystem() != FagsystemCode.FS22 || saksrelasjon.getFagsystem() != FagsystemCode.PEN {
//			throw new DokArkivRestFunctionalException()
//		}
		else {
			return ArkivsakTo.builder().build().builder()
					.arkivsakId(saksrelasjon.getSakId())
					.arkivsaksystem(mapFagsystemtoArkivsaksystem(saksrelasjon.getFagsystem()))
					.build();
		}
	}

	private List<BrukerTo> mapBrukere(Set<Bruker> brukere) {
		if (brukere.isEmpty()) {
			return new ArrayList<>();
		} else {
			return brukere.stream().map(bruker -> BrukerTo.builder()
					.identifikator(bruker.getBrukerId())
					.type(bruker.getBrukerType().name())
					.build()).collect(Collectors.toList());
		}
	}

	private AvsenderTo mapAvsender(Journalpost journalpost) {
		if (journalpost.getAvsenderMottakerId() == null || journalpost.getAvsenderMottakerId().isEmpty()) {
			return null;
		} else {
			return AvsenderTo.builder()
					.identifikator(journalpost.getAvsenderMottakerId())
					.type(utledAvsenderType(journalpost.getAvsenderMottakerId()))
					.navn(journalpost.getAvsenderMottaker())
					.build();
		}
	}

	private List<DokumentTo> mapDokumenter(Set<JournalpostDokumentInfoRelasjon> journalpostDokumentInfoRelasjoner) {
		return journalpostDokumentInfoRelasjoner.stream().map(relasjon -> DokumentTo.builder()
				.dokumentId(relasjon.getDokumentInfo().getDokumentInfoId().toString())
				.dokumenttypeId(relasjon.getDokumentInfo().getDokumenttypeId())
				.navSkjemaId(relasjon.getDokumentInfo().getBrevkode())
				.tittel(relasjon.getDokumentInfo().getTittel())
				.dokumentkategori(relasjon.getDokumentInfo().getKategori() == null ? null : relasjon.getDokumentInfo()
						.getKategori().name())
				.tilknyttetSom(relasjon.getTilknyttetJournalpostSom().name())
				.varianter(mapVarianter(relasjon.getDokumentInfo().getFildetaljerListe()))
				.logiskeVedlegg(mapLogiskeVedlegg(relasjon.getDokumentInfo().getSkannetInnholdListe()))
				.build()).collect(Collectors.toList());
	}

	private List<VariantTo> mapVarianter(Set<FilDetaljer> fildetaljer) {
		return fildetaljer.stream().map(filDetaljer -> VariantTo.builder()
				.arkivfiltype(filDetaljer.getFiltype().name())
				.variantformat(filDetaljer.getVariantFormat().name())
				.build()).collect(Collectors.toList());

	}

	private List<LogiskVedleggTo> mapLogiskeVedlegg(Set<SkannetInnhold> skannetInnholdSet) {
		return skannetInnholdSet.stream().map(skannetInnhold -> LogiskVedleggTo.builder()
				.logiskVedleggId(skannetInnhold.getSkannetInnholdId() == null ? null : skannetInnhold.getSkannetInnholdId()
						.toString())
				.logiskVedleggTittel(skannetInnhold.getVedleggInnhold())
				.build()).collect(Collectors.toList());
	}

	private String utledAvsenderType(String avsenderId) {
		if (avsenderId == null) {
			return null;
		} else if (avsenderId != null && avsenderId.length() == 11) {
			return BrukerTypeCode.PERSON.name();
		} else {
			return BrukerTypeCode.ORGANISASJON.name();
		}
	}

	private String mapFagsystemtoArkivsaksystem(FagsystemCode fagsystemCode) {
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

}
