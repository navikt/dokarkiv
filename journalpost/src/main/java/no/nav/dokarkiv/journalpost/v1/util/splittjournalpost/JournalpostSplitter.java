package no.nav.dokarkiv.journalpost.v1.util.splittjournalpost;

import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggTO;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.exceptions.DokumentInfoIkkeFunnetException;
import no.nav.dokarkiv.journalpost.v1.api.DokumentVariant;
import no.nav.dokarkiv.journalpost.v1.api.splittJournalpost.SplittJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.api.splittJournalpost.SplittJournalpostRequest.SplittDokument;
import org.slf4j.MDC;
import org.springframework.data.util.Pair;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_NAME;
import static no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode.ENDRE_DOKUMENT;
import static no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode.KOPIER_DOKUMENT;
import static no.nav.dokarkiv.core.domain.codes.BrukerTypeCode.ORGANISASJON;
import static no.nav.dokarkiv.core.domain.codes.BrukerTypeCode.PERSON;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.M;
import static no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode.HOVEDDOKUMENT;
import static no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode.VEDLEGG;
import static no.nav.dokarkiv.journalpost.v1.api.BrukerIdType.FNR;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class JournalpostSplitter {

	public static final String SPLITT_JOURNALPOST_FILNAVN = "splittet_%s_%s.%s";

	private JournalpostSplitter() {
	}

	public record SplittResultat(Journalpost nyJournalpost, List<AksjonsLoggTO> aksjoner) {
	}

	public static SplittResultat splitt(Journalpost originalJournalpost, SplittJournalpostRequest request) {
		Journalpost journalpost = originalJournalpost.toBuilder()
				.journalpostId(null)
				.opprettetAvNavn(MDC.get(MDC_USER_NAME))
				.tilleggsopplysninger(new HashMap<>(originalJournalpost.getTilleggsopplysninger()))
				.kanalReferanseId(request.eksternReferanseId())
				.saksrelasjon(null)
				.journalDato(LocalDateTime.now())
				.journalstatus(M)
				.build();

		String tittel = isNotBlank(request.tittel()) ? request.tittel() : originalJournalpost.getInnhold();
		String journalfoerendeEnhet = isNotBlank(request.journalfoerendeEnhet()) ? request.journalfoerendeEnhet() : null;
		FagomradeCode fagomrade = isNotBlank(request.tema()) ? FagomradeCode.valueOf(request.tema()) : originalJournalpost.getFagomrade();

		journalpost.setInnhold(tittel);
		journalpost.setJournalForendeEnhetId(journalfoerendeEnhet);
		journalpost.setFagomrade(fagomrade);

		if (request.bruker() != null) {
			journalpost.addBruker(opprettBruker(request));
		} else {
			originalJournalpost.getBrukere().forEach(bruker -> {
				var nyBruker = bruker.toBuilder()
						.brukerInfoId(null)
						.build();
				nyBruker.setOpprettetKildeNavn(MDC.get(MDC_CONSUMER_ID));
				journalpost.addBruker(nyBruker);
			});
		}

		originalJournalpost.getKryssreferanser().forEach(journalpost::addKryssReferanse);

		journalpost.setOpprettetKildeNavn(MDC.get(MDC_CONSUMER_ID));

		List<Pair<JournalpostDokumentInfoRelasjon, AksjonsLoggTO>> dokumentRelasjonerMedAksjoner = opprettDokumentInfoRelasjoner(originalJournalpost, request.dokumenter());

		dokumentRelasjonerMedAksjoner.forEach(pair -> journalpost.addJournalpostDokumentInfoRelasjon(pair.getFirst()));

		List<AksjonsLoggTO> aksjoner = dokumentRelasjonerMedAksjoner.stream()
				.map(Pair::getSecond)
				.toList();

		return new SplittResultat(journalpost, aksjoner);
	}

	private static Bruker opprettBruker(SplittJournalpostRequest request) {
		Bruker bruker = Bruker.builder()
				.brukerId(request.bruker().getId())
				.brukerType(request.bruker().getIdType() == FNR ? PERSON : ORGANISASJON)
				.build();

		bruker.setOpprettetKildeNavn(MDC.get(MDC_CONSUMER_ID));

		return bruker;
	}

	private static List<Pair<JournalpostDokumentInfoRelasjon, AksjonsLoggTO>> opprettDokumentInfoRelasjoner(
			Journalpost journalpost,
			List<SplittDokument> splittDokumenter) {

		Pair<JournalpostDokumentInfoRelasjon, AksjonsLoggTO> hoveddokument =
				kopierRelasjon(journalpost, splittDokumenter.getFirst(), HOVEDDOKUMENT);

		List<Pair<JournalpostDokumentInfoRelasjon, AksjonsLoggTO>> vedlegg = splittDokumenter.stream()
				.skip(1)
				.map(dokument -> kopierRelasjon(journalpost, dokument, VEDLEGG))
				.toList();

		return Stream.concat(Stream.of(hoveddokument), vedlegg.stream()).toList();
	}

	private static Pair<JournalpostDokumentInfoRelasjon, AksjonsLoggTO> kopierRelasjon(
			Journalpost journalpost,
			SplittDokument splittDokument,
			TilknyttetJournalpostSomCode tilknyttetSom) {

		JournalpostDokumentInfoRelasjon eksisterendeRelasjon = journalpost.getJournalpostDokumentInfoRelasjoner().stream()
				.filter(relasjon -> relasjon.getDokumentInfo().getDokumentInfoId().equals(splittDokument.dokumentInfoId()))
				.findFirst()
				.orElseThrow(() -> new DokumentInfoIkkeFunnetException("Fant ikke relasjon med dokumentInfoId=%s i journalpost med journalpostId=%s"
						.formatted(splittDokument.dokumentInfoId(), journalpost.getJournalpostId())));

		JournalpostDokumentInfoRelasjon nyRelasjon = eksisterendeRelasjon.toBuilder()
				.journalpostDokumentInfoRelasjonId(null)
				.tilknyttetJournalpostSom(tilknyttetSom)
				.build();

		nyRelasjon.setOpprettetKildeNavn(MDC.get(MDC_CONSUMER_ID));

		if (!splittDokument.kopierUtenEndringer()) {
			nyRelasjon.setDokumentInfo(opprettDokumentInfo(journalpost, splittDokument, eksisterendeRelasjon));
		}

		AksjonsLoggTO aksjon = AksjonsLoggTO.builder()
				.aksjon(splittDokument.kopierUtenEndringer() ? KOPIER_DOKUMENT : ENDRE_DOKUMENT)
				.journalpostId(eksisterendeRelasjon.getJournalpost().getJournalpostId())
				.dokumentInfoId(eksisterendeRelasjon.getDokumentInfo().getDokumentInfoId())
				.build();

		return Pair.of(nyRelasjon, aksjon);
	}

	private static DokumentInfo opprettDokumentInfo(
			Journalpost originalJournalpost,
			SplittDokument splittDokument,
			JournalpostDokumentInfoRelasjon eksisterendeRelasjon) {

		DokumentInfo nyDokumentInfo = DokumentInfo.builder()
				.originalJournalpost(originalJournalpost)
				.tittel(eksisterendeRelasjon.getDokumentInfo().getTittel())
				.build();

		nyDokumentInfo.setOpprettetKildeNavn(MDC.get(MDC_CONSUMER_ID));

		splittDokument.dokumentvarianter().stream()
				.map(variant -> mapDokumentvariant(variant, eksisterendeRelasjon))
				.forEach(nyDokumentInfo::addFilDetaljer);

		return nyDokumentInfo;
	}

	private static FilDetaljer mapDokumentvariant(DokumentVariant variant, JournalpostDokumentInfoRelasjon eksisterendeRelasjon) {
		FilDetaljer fildetaljer = FilDetaljer.builder()
				.filtype(FilTypeCode.valueOf(variant.getFiltype()))
				.variantFormat(VariantFormatCode.valueOf(variant.getVariantformat()))
				.fileContent(variant.getFysiskDokument())
				.filUuid(FilDetaljer.generateUuid())
				.filnavn(genererFilnavn(eksisterendeRelasjon, variant.getFiltype()))
				.build();

		fildetaljer.setOpprettetKildeNavn(MDC.get(MDC_CONSUMER_ID));

		return fildetaljer;
	}

	private static String genererFilnavn(JournalpostDokumentInfoRelasjon eksisterendeRelasjon, String filtype) {
		return SPLITT_JOURNALPOST_FILNAVN.formatted(
				eksisterendeRelasjon.getDokumentInfo().getDokumentInfoId(),
				eksisterendeRelasjon.getJournalpost().getJournalpostId(),
				filtype.toLowerCase());
	}
}
