package no.nav.service.dok.joark.nsb;

import no.nav.service.dok.joark.nsb.exceptions.DokumentInfoInnskrenketPartsinnsynException;
import no.nav.service.dok.joark.nsb.exceptions.DokumentInfoIsOrganInterntException;
import no.nav.service.dok.joark.nsb.exceptions.DokumentInfoNotFoundException;
import no.nav.service.dok.joark.nsb.exceptions.DokumentInfoSlettetException;
import no.nav.service.dok.joark.nsb.exceptions.FeilregistrertSaksrelasjonException;
import no.nav.service.dok.joark.nsb.exceptions.FilDetaljerOnDemandException;
import no.nav.service.dok.joark.nsb.exceptions.IllegalDokumentstatusException;
import no.nav.service.dok.joark.nsb.exceptions.IllegalFagomraadeException;
import no.nav.service.dok.joark.nsb.exceptions.IllegalJournalStatusException;
import no.nav.service.dok.joark.nsb.exceptions.IllegalTilleggsopplysningerException;
import no.nav.service.dok.joark.nsb.exceptions.IllegalVariantFormatException;
import no.nav.service.dok.joark.nsb.exceptions.JournalpostNotFoundException;
import no.nav.service.dok.joark.nsb.to.KnyttDokumentTilJournalpostSomVedleggRequestTo;

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
