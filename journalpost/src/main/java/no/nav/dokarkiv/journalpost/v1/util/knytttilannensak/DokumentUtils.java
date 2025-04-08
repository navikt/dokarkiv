package no.nav.dokarkiv.journalpost.v1.util.knytttilannensak;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.consumers.saf.exceptions.saf.SafJournalpostUnauthorizedException;
import no.nav.dokarkiv.core.consumers.saf.journalpost.SafJournalpostTo;
import no.nav.dokarkiv.core.consumers.saf.journalpost.SafJournalpostTo.DokumentInfo;
import no.nav.dokarkiv.core.consumers.saf.journalpost.SafJournalpostTo.Dokumentvariant;
import no.nav.dokarkiv.core.exceptions.JournalpostDokumentInfoRelasjonIkkeFunnetException;
import no.nav.dokarkiv.journalpost.v1.api.knytttilannensak.KnyttTilAnnenSakRequest;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.ARKIV;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.SLADDET;

@Slf4j
public class DokumentUtils {

	public static void sjekkOmAlleDokumenterEksistererPaaJournalposten(KnyttTilAnnenSakRequest request,
																	   SafJournalpostTo safJournalpost,
																	   long journalpostId) {
		Set<Long> journalpostDokumenter = safJournalpost.getDokumenter().stream()
				.map(DokumentInfo::getDokumentInfoId)
				.map(Long::parseLong)
				.collect(Collectors.toSet());

		List<Long> ugyldigeDokumenter = request.getDokumenter().stream()
				.filter(dokument -> !journalpostDokumenter.contains(dokument))
				.toList();

		if (!ugyldigeDokumenter.isEmpty()) {
			throw new JournalpostDokumentInfoRelasjonIkkeFunnetException("Dokument(er) med id %s finnes ikke paa journalpost med journalpostId=%s"
					.formatted(ugyldigeDokumenter, journalpostId));
		}
	}

	/*
	 * For hvert dokument
	 * 		Sjekk at det finnes et dokument hvor (arkivvariant == ARKIV || SLADDET) && isSaksbehandlerHarTilgang == true
	 */
	public static void sjekkOmAlleDokumentvarianterErGyldige(SafJournalpostTo safJournalpost, long journalpostId) {
		List<SafJournalpostTo.DokumentInfo> dokumenter = safJournalpost.getDokumenter();
		if(dokumenter.isEmpty()){
			return;
		}
		for(DokumentInfo dokument : dokumenter){
			if(!harSaksbehandlerTilgangTilDokumentet(dokument)){
				throw new SafJournalpostUnauthorizedException(String.format("Dokumentvariant har ikke variantformat 'ARKIV' eller 'SLADDET' der saksbehandlerHarTilgang = TRUE for journalpostId=%s", journalpostId));
			}
		}
	}

	private static boolean harSaksbehandlerTilgangTilDokumentet(DokumentInfo dokument){
		for(Dokumentvariant variant : dokument.getDokumentvarianter()){
			if(isDokumentVariantArkivOrSladdet(variant) && variant.isSaksbehandlerHarTilgang()){
				return true;
			}
		}
		return false;
	}

	private static boolean isDokumentVariantArkivOrSladdet(Dokumentvariant dokumentvariant) {
		return (ARKIV.name().equals(dokumentvariant.getVariantformat())
				|| (SLADDET.name()).equals(dokumentvariant.getVariantformat()));
	}
}
