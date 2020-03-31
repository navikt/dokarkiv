package no.nav.dokarkiv.journalpost.v1.validators;

import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.journalpost.v1.api.DokumentVariant;
import no.nav.dokarkiv.journalpost.v1.api.MottaDokumentUtgaaendeSkanningRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MottaDokumentUtgaaendeSkanningValidator {
    public Optional<String> validateRequest(MottaDokumentUtgaaendeSkanningRequest request){
        List<String> errors = request.getDokumentvarianter().stream()
                .map(this::validateDokumentVariant)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        if(errors.size() == 0){ return Optional.empty(); }
        return Optional.of(String.join("\n", errors));
    }
    public Optional<String> validateJournalpost(Journalpost journalpost){
        ArrayList<String> errors = new ArrayList<>();

        JournalpostTypeCode journalpostTypeCode = journalpost.getJournalposttype();
        JournalStatusCode journalStatusCode = journalpost.getJournalstatus();
        List<DokumentInfo> dokumentInfos = journalpost.findAllDokumentInfos();

        if(journalpostTypeCode != JournalpostTypeCode.U && journalpostTypeCode != JournalpostTypeCode.N){
            errors.add("JournalpostType er ikke U eller N");
        }
        if(journalStatusCode != JournalStatusCode.R) {
            errors.add("JournalStatus er ikke R");
        }
        if(dokumentInfos.isEmpty()) {
            errors.add("Har ikke ett hoveddokument");
        }
        if(dokumentInfos.size() > 1) {
            errors.add("Har mer enn ett dokument");
        }
        if(!journalpost.hasHoveddokumentRelasjon()) {
            errors.add("Har ikke hoveddokument");
        }
        if(!dokumentInfos.isEmpty() && !dokumentInfos.get(0).getFildetaljerListe().isEmpty()){
            errors.add("Har tilknyttede fildetaljer");
        }

        if(errors.isEmpty()){ return Optional.empty(); }

        return Optional.of("Kan ikke validere journalpost: " + String.join(", ", errors));
    }

    private Optional<String> validateDokumentVariant(DokumentVariant dokumentVariant) {
        ArrayList<String> errors = new ArrayList<>();
        if(isNullOrEmpty(dokumentVariant.getFiltype())){
            errors.add("mangler filtype");
        } else {
            try {
                FilTypeCode.valueOf(dokumentVariant.getFiltype());
            } catch (IllegalArgumentException e) {
                errors.add("ugyldig filtype " + dokumentVariant.getFiltype());
            }
        }

        if(isNullOrEmpty(dokumentVariant.getVariantformat())){
            errors.add("mangler variantformat");
        } else {
            try {
                VariantFormatCode.valueOf(dokumentVariant.getVariantformat());
            } catch (IllegalArgumentException e) {
                errors.add("ugyldig variantformat " + dokumentVariant.getVariantformat());
            }
        }
        if(isNullOrEmpty(dokumentVariant.getFysiskDokument())){
            errors.add("mangler fysiskDokument");
        }

        if(errors.size() == 0){ return Optional.empty(); }

        return(Optional.of("DokumentVariant i request kan ikke valideres: " + String.join(", ", errors)));
    }

    private boolean isNullOrEmpty(String string) {
        return string == null || string.isBlank();
    }

    private boolean isNullOrEmpty(byte[] byteArray){
        return byteArray == null || byteArray.length == 0;
    }
}
