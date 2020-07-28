package no.nav.dokarkiv.journalpost.v1.validators;

import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.journalpost.v1.api.DokumentVariant;
import no.nav.dokarkiv.journalpost.v1.api.MottaDokumentUtgaaendeSkanningRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MottaDokumentUtgaaendeSkanningValidator {
    public Optional<String> validateRequest(MottaDokumentUtgaaendeSkanningRequest request) {

        List<String> errors = new ArrayList<>();

        if (request.getMottakskanal() == null) {
            errors.add("mottakskanal kan ikke være null");
        } else {
            try {
                MottaksKanalCode.valueOf(request.getMottakskanal());
            } catch (IllegalArgumentException e) {
                errors.add("mottakskanal er ugyldig");
            }
        }

        if (request.getDokumentvarianter() != null) {
            errors.addAll(IntStream.range(0, request.getDokumentvarianter().size())
                    .mapToObj(i -> validateDokumentVariant(request.getDokumentvarianter().get(i), i))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList()));
        }

        if(errors.size() == 0){ return Optional.empty(); }
        return Optional.of("Kan ikke validere request: " + String.join("; ", errors));
    }

    public Optional<String> validateJournalpostHasAllElements(Journalpost journalpost) {
        ArrayList<String> errors = new ArrayList<>();

        List<DokumentInfo> dokumentInfos = journalpost.findAllDokumentInfos();

        if (dokumentInfos.isEmpty()) {
            errors.add("Mangler DokumentInfo");
        }
        if (!journalpost.hasHoveddokumentRelasjon()) {
            errors.add("Har ikke hoveddokument");
        }

        if (errors.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of("Kan ikke validere journalpost: " + String.join("; ", errors));
    }

    public Optional<String> validateJournalpostMetadata(Journalpost journalpost) {
        ArrayList<String> errors = new ArrayList<>();

        JournalStatusCode journalStatusCode = journalpost.getJournalstatus();
        JournalpostTypeCode journalpostTypeCode = journalpost.getJournalposttype();
        List<DokumentInfo> dokumentInfos = journalpost.findAllDokumentInfos();
        long journalpostId = journalpost.getId();

        if (!hasValidJournalPostType(journalpostTypeCode) || !hasValidJournalpostStatus(journalStatusCode)) {
            errors.add("Ugyldig journalpostId. JournalpostId=" + journalpostId + " har journalposttype=" + journalpostTypeCode +
                    " og status=" + journalStatusCode);
        }
        if (dokumentInfos.size() > 1) {
            errors.add("Ugyldig journalpostId. JournalpostId=" + journalpostId + " har mer enn ett " +
                    "DokumentInfo-objekt");
        }
        if (!dokumentInfos.isEmpty() && !dokumentInfos.get(0).getFildetaljerListe().isEmpty()) {
            errors.add("JournalpostId=" + journalpostId + " har allerede fildetaljer og kan ikke oppdateres. " +
                    "JournalpostId er ugyldig eller samme førsteside er benyttet flere ganger.");
        }

        if(errors.isEmpty()){ return Optional.empty(); }

        return Optional.of("Kan ikke validere journalpost: " + String.join("; ", errors));
    }

    private boolean hasValidJournalpostStatus(JournalStatusCode journalStatusCode) {
        return journalStatusCode == JournalStatusCode.R;
    }

    private boolean hasValidJournalPostType(JournalpostTypeCode journalpostTypeCode) {
        return journalpostTypeCode == JournalpostTypeCode.U || journalpostTypeCode == JournalpostTypeCode.N;
    }

    private Optional<String> validateDokumentVariant(DokumentVariant dokumentVariant, int index) {
        ArrayList<String> errors = new ArrayList<>();
        if (isNullOrEmpty(dokumentVariant.getFiltype())) {
            errors.add("mangler filtype");
        } else {
            try {
                FilTypeCode.valueOf(dokumentVariant.getFiltype());
            } catch (IllegalArgumentException e) {
                errors.add("har ugyldig filtype " + dokumentVariant.getFiltype());
            }
        }

        if (isNullOrEmpty(dokumentVariant.getVariantformat())) {
            errors.add("mangler variantformat");
        } else {
            try {
                VariantFormatCode.valueOf(dokumentVariant.getVariantformat());
            } catch (IllegalArgumentException e) {
                errors.add("har ugyldig variantformat " + dokumentVariant.getVariantformat());
            }
        }
        if (isNullOrEmpty(dokumentVariant.getFysiskDokument())) {
            errors.add("mangler fysiskDokument");
        }

        if(errors.size() == 0){ return Optional.empty(); }

        return (Optional.of("dokumentvarianter[" + index + "] " + String.join(", ", errors)));
    }

    private boolean isNullOrEmpty(String string) {
        return string == null || string.isBlank();
    }

    private boolean isNullOrEmpty(byte[] byteArray) {
        return byteArray == null || byteArray.length == 0;
    }
}
