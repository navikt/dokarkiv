package no.nav.dokarkiv.journalpost.v1.api.skjermdokument;

import jakarta.validation.constraints.NotNull;

public record SkjermDokumentRequest(@NotNull SkjermDokumentHjemmelCode hjemmel) {
}
