package no.nav.dokarkiv.innsynjournal.v2.hentdokument;

import no.nav.dokarkiv.core.domain.codes.OnDemandInstansCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentFil;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.InvalidArgumentException;
import no.nav.dokarkiv.core.repository.DokumentFilRepository;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.core.repository.ondemand.OnDemandRepository;
import no.nav.dokarkiv.innsynjournal.v2.exceptions.DocumentNotFoundException;
import no.nav.dokarkiv.innsynjournal.v2.exceptions.InvalidFilUuidException;
import no.nav.dokarkiv.innsynjournal.v2.exceptions.NoDokumentInfoFoundException;
import no.nav.dokarkiv.innsynjournal.v2.exceptions.NoJournalpostFoundException;
import org.springframework.util.Assert;

import javax.inject.Inject;

/**
 * Common operations for the different functions in JournalV1 and JournalV2
 * (TJOARK050,TJOARK051,TJOARK052)
 *
 * @author Stig Strøm
 */
public abstract class AbstractJournalOperations {

	@Inject
	private JoarkRepository joarkRepository;

	@Inject
	private DokumentFilRepository dokumentFilRepository;

	@Inject
	private OnDemandRepository onDemandRepository;

	/**
	 * Finds the journalpost for a journalpostId
	 *
	 * @param journalpostId the id of a journalpost
	 * @return journalpost
	 * @throws DocumentNotFoundException with root cause NoJournalpostFoundException
	 */
	protected Journalpost lookupJournalpost(Long journalpostId) throws DocumentNotFoundException {
		return joarkRepository.findById(journalpostId).orElseThrow(() ->
				new DocumentNotFoundException(new NoJournalpostFoundException("Journalpost with journalpostId= " + journalpostId + " does not exist", journalpostId)));
	}

	/**
	 * Gets a dokumentInfo for a journalpost
	 *
	 * @param journalpost    the journalpost
	 * @param dokumentInfoId the dokumentInfoId of the dokumentInfoId
	 * @return dokumentInfo
	 * @throws DocumentNotFoundException with the root cause NoDokumentInfoFoundException
	 */
	protected DokumentInfo getDokumentInfo(Journalpost journalpost, Long dokumentInfoId) throws DocumentNotFoundException {
		DokumentInfo dokumentInfo = journalpost.findDokumentInfoById(dokumentInfoId);
		if (dokumentInfo == null) {
			throw new DocumentNotFoundException(new NoDokumentInfoFoundException(
					"Journalpost has no DokumentInfo with dokumentInfoId= " + dokumentInfoId, dokumentInfoId));
		}
		return dokumentInfo;
	}

	/**
	 * Get fildetaljer for a journalpost
	 *
	 * @param dokumentInfo  the dokumentInfo searching for variant
	 * @param variantFormat the variant we are trying to find
	 * @return filDetaljer
	 * @throws DocumentNotFoundException with the root cause InvalidArgumentException
	 */
	protected FilDetaljer getFilDetaljer(DokumentInfo dokumentInfo, VariantFormatCode variantFormat)
			throws DocumentNotFoundException {
		FilDetaljer filDetaljer = dokumentInfo.findFilDetaljerByVariantFormat(variantFormat);
		if (filDetaljer == null) {
			throw new DocumentNotFoundException(new InvalidArgumentException("DokumentInfo with dokumentInfoId="
					+ dokumentInfo.getDokumentInfoId() + " has no FilDetaljer with variant: " + variantFormat));
		}
		return filDetaljer;
	}

	/**
	 * Retrieves a physical document from the database.
	 *
	 * @param filUuid The filuuid of the DokumentFil.
	 * @return The DokumentFil.
	 * @throws DocumentNotFoundException with the root cause InvalidFilUuidException
	 */
	protected DokumentFil getDocumentFromDBRepository(String filUuid)
			throws DocumentNotFoundException {
		DokumentFil dokumentFil = dokumentFilRepository.findByFilUuid(filUuid);
		if (dokumentFil == null) {
			throw new DocumentNotFoundException(new InvalidFilUuidException("Could not find DokumentFil with filUuid="
					+ filUuid, filUuid));
		}
		return dokumentFil;
	}

	/**
	 * Check if a dokument is sensitivt and log access if it is.
	 *
	 * @param journalpost   The Journalpost.
	 * @param fildetaljer   The FilDetaljer.
	 * @param operationName The operation generating the log.
	 */
	protected void generateAuditLogIfDokumentIsSensitivt(Journalpost journalpost, FilDetaljer fildetaljer,
														 String operationName) {
		Boolean sensitivt = fildetaljer.getDokumentInfo().getSensitivt();
//		if (BooleanUtils.isTrue(sensitivt) && AuditLogUtil.AUDIT_READ.isInfoEnabled()) {
//			AuditLogUtil.generateAuditLog(operationName, journalpost, fildetaljer);
//		} FIXME need arcsight integrasjon
	}

	/**
	 * Retrieves a document from OnDemand
	 *
	 * @param onDemandId      the OnDemandId
	 * @param onDemandInstans the OnDemandInstans
	 * @return the document
	 */
	protected byte[] hentDokumentFromOnDemand(String onDemandId, OnDemandInstansCode onDemandInstans) {
		Assert.notNull(onDemandInstans, "DokumentInfo.Fildetaljer.OnDemandInstans null for OnDemandId=" + onDemandId);
		return onDemandRepository.getDocument(onDemandId, onDemandInstans);
	}
}
