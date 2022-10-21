package no.nav.dokarkiv.journalpost.v1.services;

import no.nav.dokarkiv.core.consumers.saf.SafJournalpostQueryService;
import no.nav.dokarkiv.core.consumers.saf.journalpost.SafJournalpostTo;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.journalpost.v1.api.ArsakKode;
import no.nav.dokarkiv.journalpost.v1.api.DokumentVedlegg;
import no.nav.dokarkiv.journalpost.v1.api.FeiledeDokumenter;
import no.nav.dokarkiv.journalpost.v1.api.TilknyttVedleggRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AccessLookupJournalpost {

	private final SafJournalpostQueryService safJournalpostQueryService;

	public AccessLookupJournalpost(SafJournalpostQueryService safJournalpostQueryService) {
		this.safJournalpostQueryService = safJournalpostQueryService;
	}

	public AccessControlledDocuments checkDocumentsCanBeAccessedByActor(long targetJournalpostId, TilknyttVedleggRequest tilknyttVedleggRequest, String authorizationHeader) {
		safJournalpostQueryService.hentJournalpost(targetJournalpostId, authorizationHeader);

		List<FeiledeDokumenter> feiledeDokumentListe = new ArrayList<>();
		List<DokumentVedlegg> dokumenterTilTilknytning = new ArrayList<>();

		for (DokumentVedlegg dokument : tilknyttVedleggRequest.getDokument()) {
			SafJournalpostTo safJournalpostTo = safJournalpostQueryService.hentJournalpost(dokument.getKildeJournalpostId(), authorizationHeader);

			SafJournalpostTo.DokumentInfo dokumentInfo = getDokumentInfo(safJournalpostTo, dokument.getDokumentInfoId());

			if ((dokumentInfo == null) || (dokumentInfo.getDokumentInfoId() == null)) {
				feiledeDokumentListe.add(new FeiledeDokumenter(String.valueOf(dokument.getKildeJournalpostId()), dokument.getDokumentInfoId(), ArsakKode.IKKE_FUNNET));
			} else if (saksbehandlerHarIkkeTilgang(dokumentInfo)) {
				feiledeDokumentListe.add(new FeiledeDokumenter(String.valueOf(dokument.getKildeJournalpostId()), dokument.getDokumentInfoId(), ArsakKode.SIKKERHETSBEGRENSNING));
			} else {
				dokumenterTilTilknytning.add(dokument);
			}
		}

		return new AccessControlledDocuments(dokumenterTilTilknytning, feiledeDokumentListe);
	}

	private boolean saksbehandlerHarIkkeTilgang(SafJournalpostTo.DokumentInfo dokumentInfo) {
		return dokumentInfo.getDokumentvarianter()
				.stream()
				.noneMatch(dokumentvariant -> (dokumentvariant.isSaksbehandlerHarTilgang() && isDokumentVariantArkivOrSladdet(dokumentvariant)));
	}

	private boolean isDokumentVariantArkivOrSladdet(SafJournalpostTo.Dokumentvariant dokumentvariant) {
		return (VariantFormatCode.ARKIV.name().equals(dokumentvariant.getVariantformat())
				|| (VariantFormatCode.SLADDET.name()).equals(dokumentvariant.getVariantformat()));
	}

	private SafJournalpostTo.DokumentInfo getDokumentInfo(SafJournalpostTo safJournalpostTo, String dokumentInfoId) {
		return safJournalpostTo.getDokumenter()
				.stream()
				.filter(dokument -> dokumentInfoId.equals(dokument.getDokumentInfoId()))
				.findAny()
				.orElse(null);
	}
	public record AccessControlledDocuments(List<DokumentVedlegg> okDocuments, List<FeiledeDokumenter> failedDocuments) {};
}
