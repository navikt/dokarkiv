package no.nav.dokarkiv.journalpost.v1.services;

import no.nav.dokarkiv.core.consumers.saf.SafJournalpostQueryService;
import no.nav.dokarkiv.core.consumers.saf.journalpost.SafJournalpostTo;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.repository.JournalpostRepository;
import no.nav.dokarkiv.core.repository.projections.IdAndFagomradeHolder;
import no.nav.dokarkiv.journalpost.v1.api.ArsakKode;
import no.nav.dokarkiv.journalpost.v1.api.DokumentVedlegg;
import no.nav.dokarkiv.journalpost.v1.api.FeiledeDokumenter;
import no.nav.dokarkiv.journalpost.v1.api.TilknyttVedleggRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static no.nav.dokarkiv.core.domain.codes.FagomradeCode.FAR;
import static no.nav.dokarkiv.core.domain.codes.FagomradeCode.KTA;

@Service
public class AccessLookupJournalpost {

	private static final EnumSet<FagomradeCode> TEMA_SOM_TRENGER_TILGANGSKONTROLL = EnumSet.of(FAR, KTA);
	private static final int MAX_ANTALL_JOURNALPOSTER_PR_KALL = 1000;
	private final SafJournalpostQueryService safJournalpostQueryService;
	private final JournalpostRepository journalpostRepository;

	public AccessLookupJournalpost(SafJournalpostQueryService safJournalpostQueryService,
								   JournalpostRepository journalpostRepository) {
		this.safJournalpostQueryService = safJournalpostQueryService;
		this.journalpostRepository = journalpostRepository;
	}

	public AccessControlledDocuments checkDocumentsCanBeAccessedByActor(TilknyttVedleggRequest tilknyttVedleggRequest) {
		List<FeiledeDokumenter> feiledeDokumentListe = new ArrayList<>();
		List<DokumentVedlegg> dokumenterTilTilknytning = new ArrayList<>();

		Map<Long, FagomradeCode> journalposter = getFagomradeForJournalposter(tilknyttVedleggRequest.getDokument());

		for (DokumentVedlegg dokument : tilknyttVedleggRequest.getDokument()) {
			FagomradeCode fagomradeCode = journalposter.get(dokument.getKildeJournalpostId());

			if (TEMA_SOM_TRENGER_TILGANGSKONTROLL.contains(fagomradeCode)) {
				SafJournalpostTo safJournalpostTo = safJournalpostQueryService.hentJournalpost(dokument.getKildeJournalpostId());

				SafJournalpostTo.DokumentInfo dokumentInfo = getDokumentInfo(safJournalpostTo, dokument.getDokumentInfoId());

				if ((dokumentInfo == null) || (dokumentInfo.getDokumentInfoId() == null)) {
					feiledeDokumentListe.add(new FeiledeDokumenter(String.valueOf(dokument.getKildeJournalpostId()), dokument.getDokumentInfoId(), ArsakKode.IKKE_FUNNET));
				} else if (saksbehandlerHarIkkeTilgang(dokumentInfo)) {
					feiledeDokumentListe.add(new FeiledeDokumenter(String.valueOf(dokument.getKildeJournalpostId()), dokument.getDokumentInfoId(), ArsakKode.SIKKERHETSBEGRENSNING));
				} else {
					dokumenterTilTilknytning.add(dokument);
				}
			} else {
				dokumenterTilTilknytning.add(dokument);
			}
		}

		return new AccessControlledDocuments(dokumenterTilTilknytning, feiledeDokumentListe);
	}

	private Map<Long, FagomradeCode> getFagomradeForJournalposter(List<DokumentVedlegg> dokumenter) {
		Map<Long, FagomradeCode> journalposter = new HashMap<>();

		Collection<List<Long>> partisjoner = partisjonerListe(dokumenter
				.stream()
				.map(DokumentVedlegg::getKildeJournalpostId)
				.toList());

		partisjoner.forEach(partisjon ->
				journalposter.putAll(journalpostRepository.findIdAndFagomradeByJournalpostIdIn(partisjon)
						.stream()
						.collect(toMap(IdAndFagomradeHolder::id, IdAndFagomradeHolder::fagomrade))));

		return journalposter;
	}

	private Collection<List<Long>> partisjonerListe(final List<Long> list) {
		return IntStream.range(0, list.size()).boxed()
				.collect(groupingBy(partition -> (partition / MAX_ANTALL_JOURNALPOSTER_PR_KALL), mapping(list::get, toList())))
				.values();
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

	public record AccessControlledDocuments(List<DokumentVedlegg> okDocuments,
											List<FeiledeDokumenter> failedDocuments) {
	}
}
