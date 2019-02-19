package no.nav.dokarkiv.inngaaendejournal.v1.tjoark056;

import com.google.common.collect.Ordering;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;
import no.nav.dokarkiv.core.domain.service.SkjermingService;
import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import no.nav.dokarkiv.inngaaendejournal.v1.tjoark056.to.AktoerTo;
import no.nav.dokarkiv.inngaaendejournal.v1.tjoark056.to.ArkivSakTo;
import no.nav.dokarkiv.inngaaendejournal.v1.tjoark056.to.DokumentInnholdTo;
import no.nav.dokarkiv.inngaaendejournal.v1.tjoark056.to.DokumentinformasjonTo;
import no.nav.dokarkiv.inngaaendejournal.v1.tjoark056.to.DokumenttilstandTo;
import no.nav.dokarkiv.inngaaendejournal.v1.tjoark056.to.InngaaendeJournalpostTo;
import no.nav.dokarkiv.inngaaendejournal.v1.tjoark056.to.JournaltilstandTo;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public final class InngaaendeJournalpostToMapper {

	private static final List<JournalStatusCode> MIDLERTIDIG_STATUS = Arrays.asList(JournalStatusCode.M, JournalStatusCode.MO, JournalStatusCode.UB, JournalStatusCode.OD);

	public InngaaendeJournalpostTo map(Journalpost journalpost) {
		try {
			return doMap(journalpost);
		} catch(Exception e) {
			throw new DokarkivFunctionalException("Kunne ikke mappe Journalpost. journalpostId=" + journalpost.getJournalpostId(), e);
		}
	}

	private InngaaendeJournalpostTo doMap(Journalpost journalpost) {
		return InngaaendeJournalpostTo.builder()
				.avsenderId(journalpost.getAvsenderMottakerId())
				.forsendelseMottatt(journalpost.getMottattDato() == null ? null : LocalDateTime.ofInstant(journalpost.getMottattDato().toInstant(), ZoneId.systemDefault()))
				.mottakskanal(journalpost.getMottakskanal())
				.tema(journalpost.getFagomrade())
				.kanalReferanseId(journalpost.getKanalReferanseId())
				.journaltilstand(mapJournaltilstand(journalpost))
				.journalfEnhet(journalpost.getJournalForendeEnhetId())
				.arkivSak(mapArkivSak(journalpost.getSaksrelasjon()))
				.brukere(mapBrukere(journalpost.getBrukere()))
				.hoveddokument(mapHoveddokument(journalpost.findHoveddokumentDokumentInfoRelasjon()))
				.vedlegg(mapVedlegg(journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)))
				.build();
	}

	private JournaltilstandTo mapJournaltilstand(Journalpost journalpost) {
		if(journalpost.isFeilregistrert()) {
			return JournaltilstandTo.UTGAAR;
		} else if(journalpost.hasEndeligJournalforingStatus()) {
			return JournaltilstandTo.ENDELIG;
		} else if(MIDLERTIDIG_STATUS.contains(journalpost.getJournalstatus())) {
			return JournaltilstandTo.MIDLERTIDIG;
		} else if(journalpost.hasUtgaattJournalforingStatus()) {
			return JournaltilstandTo.UTGAAR;
		} else {
			throw new DokarkivFunctionalException("Ugyldig journalstatus for inngående Journalpost. journalpostId=" + journalpost.getJournalpostId());
		}
	}

	private ArkivSakTo mapArkivSak(Saksrelasjon saksrelasjon) {
		if(saksrelasjon == null) {
			return null;
		} else {
			return ArkivSakTo.builder()
					.arkivSakId(saksrelasjon.getSakId())
					.fagsystem(saksrelasjon.getFagsystem())
					.build();
		}
	}

	private List<AktoerTo> mapBrukere(Set<Bruker> brukere) {
		if(brukere.isEmpty()) {
			return new ArrayList<>();
		} else {
			return brukere.stream().map(bruker -> AktoerTo.builder()
					.aktoerId(bruker.getBrukerId())
					.aktoerType(bruker.getBrukerType())
					.build()).collect(Collectors.toList());
		}
	}

	private DokumentinformasjonTo mapHoveddokument(JournalpostDokumentInfoRelasjon hoveddokumentDokumentInfoRelasjon) {
		DokumentInfo dokumentInfo = hoveddokumentDokumentInfoRelasjon.getDokumentInfo();
		return DokumentinformasjonTo.builder()
				.dokumentkategori(dokumentInfo.getKategori())
				.dokumenttypeId(dokumentInfo.getDokumenttypeId())
				.dokumentId(dokumentInfo.getDokumentInfoId())
				.dokumenttilstand(mapDokumenttilstand(dokumentInfo))
				.dokumentInnhold(mapDokumentinnhold(dokumentInfo.getFildetaljerListe()))
				.build();
	}

	private DokumenttilstandTo mapDokumenttilstand(DokumentInfo dokumentInfo) {
		if(dokumentInfo.isFerdigstilt()) {
			return DokumenttilstandTo.FERDIGSTILT;
		} else if(dokumentInfo.isAvbrutt()) {
			return DokumenttilstandTo.AVBRUTT;
		} else if(dokumentInfo.isUnderRedigering()) {
			return DokumenttilstandTo.UNDER_REDIGERING;
		} else {
			return null;
		}
	}

	private List<DokumentInnholdTo> mapDokumentinnhold(Set<FilDetaljer> filDetaljers) {
		if(filDetaljers.isEmpty()) {
			return Collections.emptyList();
		} else {
				return filDetaljers.stream()
						.map(filDetaljer -> DokumentInnholdTo.builder()
								.arkivFiltype(filDetaljer.getFiltype())
								.variantFormat(filDetaljer.getVariantFormat())
								.build()).collect(Collectors.toList());
			}
	}

	private List<DokumentinformasjonTo> mapVedlegg(Set<JournalpostDokumentInfoRelasjon> vedlegg) {
		if(vedlegg.isEmpty()) {
			return Collections.emptyList();
		} else {
			List<JournalpostDokumentInfoRelasjon> sortedCopy = Ordering.from(new Comparator<JournalpostDokumentInfoRelasjon>() {
				@Override
				public int compare(JournalpostDokumentInfoRelasjon o1, JournalpostDokumentInfoRelasjon o2) {
					return LocalDateTime.ofInstant(o1.getDokumentInfo().getChangeStamp().getCreatedDate().toInstant(), ZoneId.systemDefault())
							.compareTo(LocalDateTime.ofInstant(o2.getDokumentInfo().getChangeStamp().getCreatedDate().toInstant(), ZoneId.systemDefault()));
				}
			}).sortedCopy(vedlegg);
			return sortedCopy.stream().map(relasjon -> {
				DokumentInfo dokumentInfo = relasjon.getDokumentInfo();
				return DokumentinformasjonTo.builder()
						.dokumentkategori(dokumentInfo.getKategori())
						.dokumenttypeId(dokumentInfo.getDokumenttypeId())
						.dokumentId(dokumentInfo.getDokumentInfoId())
						.dokumenttilstand(mapDokumenttilstand(dokumentInfo))
						.dokumentInnhold(mapDokumentinnhold(dokumentInfo.getFildetaljerListe()))
						.build();
			}).collect(Collectors.toList());
		}
	}
}
