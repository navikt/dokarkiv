package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark109;

import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.DokumentInfoInnskrenketPartsinnsynException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.DokumentInfoIsOrganInterntException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.DokumentInfoNotFoundException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.DokumentInfoSlettetException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.FeilregistrertSaksrelasjonException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.FilDetaljerOnDemandException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.IllegalDokumentstatusException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.IllegalFagomraadeException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.IllegalJournalStatusException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.IllegalTilleggsopplysningerException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.IllegalVariantFormatException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.JournalpostNotFoundException;

/**
 * @author Thomas Kåsene, Visma Consulting AS
 */
public interface KnyttDokumentTilJournalpostSomVedleggService {

    void knyttDokumentTilJournalpostSomVedlegg(KnyttDokumentTilJournalpostSomVedleggRequestTo request) throws
            JournalpostNotFoundException,
            DokumentInfoNotFoundException,
            DokumentInfoInnskrenketPartsinnsynException,
            IllegalDokumentstatusException,
            DokumentInfoSlettetException,
            DokumentInfoIsOrganInterntException,
            IllegalFagomraadeException,
            FilDetaljerOnDemandException,
            IllegalVariantFormatException,
            IllegalJournalStatusException,
            IllegalTilleggsopplysningerException,
            FeilregistrertSaksrelasjonException;
}
