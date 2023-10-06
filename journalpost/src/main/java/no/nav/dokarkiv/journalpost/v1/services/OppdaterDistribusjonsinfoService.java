package no.nav.dokarkiv.journalpost.v1.services;

import no.nav.dokarkiv.core.aksjonslogg.LagreAksjonsLoggService;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import no.nav.dokarkiv.core.exceptions.DokarkivTechnicalException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.repository.JournalpostRepository;
import no.nav.dokarkiv.journalpost.v1.api.OppdaterDistribusjonsinfoRequest;
import no.nav.dokarkiv.journalpost.v1.api.bulkOppdaterDistribusjonsinfo.JournalpostResponse;
import no.nav.dokarkiv.journalpost.v1.api.bulkOppdaterDistribusjonsinfo.JournalpostWithDistribusjonsinfo;
import no.nav.dokarkiv.journalpost.v1.util.oppdaterjournalpost.ChangeTracker;
import no.nav.dokarkiv.journalpost.v1.util.oppdaterjournalpost.JournalpostUpdater;
import no.nav.dokarkiv.journalpost.v1.util.oppdaterjournalpost.JournalpostUpdaterFromBulk;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.Optional;

import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.A;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.E;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.U;
import static no.nav.dokarkiv.journalpost.v1.validators.OppdaterDistribusjonsinfoValidator.validateJournalpostKanSetteStatusEkspedert;

@Service("oppdaterDistribusjonsinfo")
public class OppdaterDistribusjonsinfoService {

	private static final EnumSet<JournalStatusCode> IKKE_OPPDATER_MED_JOURNALSTATUS = EnumSet.of(E, A, U);
	private final JournalpostRepository journalpostRepository;
	private final JournalpostUpdater journalpostUpdater;
	private final JournalpostUpdaterFromBulk journalpostUpdaterFromBulk;
	private final LagreAksjonsLoggService aksjonsLoggService;

	public OppdaterDistribusjonsinfoService(JournalpostRepository journalpostRepository,
											JournalpostUpdater journalpostUpdater,
											JournalpostUpdaterFromBulk journalpostUpdaterFromBulk,
											LagreAksjonsLoggService aksjonsLoggService) {
		this.journalpostRepository = journalpostRepository;
		this.journalpostUpdater = journalpostUpdater;
		this.journalpostUpdaterFromBulk = journalpostUpdaterFromBulk;
		this.aksjonsLoggService = aksjonsLoggService;
	}

	public void oppdaterDistribusjonsinfo(Long journalpostId, OppdaterDistribusjonsinfoRequest request) {
		Journalpost journalpost = journalpostRepository.findById(journalpostId)
				.orElseThrow(() -> new JournalpostIkkeFunnetException(
						String.format("Kunne ikke finne journalpost med journalpostId=%s i joark", journalpostId)));

		if (request.getSettStatusEkspedert()) {
			validateJournalpostKanSetteStatusEkspedert(journalpost, request);
		}

		ChangeTracker trackStatusSattTilEkspedert = journalpostUpdater.updateFields(journalpost, request);

		if (!trackStatusSattTilEkspedert.getChanges().isEmpty()) {
			aksjonsLoggService.lagreAksjonsLoggForJournalpost(
					AksjonsTypeCode.EKSPEDER, journalpostId, null, "Journalposten fikk status 'ekspedert'",
					null, trackStatusSattTilEkspedert.getChanges());
		}
	}

	public JournalpostResponse oppdaterDistribusjonsinfoFromBulk(JournalpostWithDistribusjonsinfo journalpostWithDistribusjonsinfo) {
		Optional<Journalpost> journalpostOptional = journalpostRepository.findById(journalpostWithDistribusjonsinfo.getJournalpostId());
		return journalpostOptional.map(journalpost -> {
			try {
				if (isFeilregistrertOrJournalStatusEorAorU(journalpost)) {
					return JournalpostResponse.ok(journalpost.getJournalpostId());
				}

				if (journalpostWithDistribusjonsinfo.getSettStatusEkspedert()) {
					validateJournalpostKanSetteStatusEkspedert(journalpost, journalpostWithDistribusjonsinfo);
				}

				ChangeTracker trackStatusSattTilEkspedert = journalpostUpdaterFromBulk.updateFields(journalpost, journalpostWithDistribusjonsinfo);

				if (!trackStatusSattTilEkspedert.getChanges().isEmpty()) {
					aksjonsLoggService.lagreAksjonsLoggForJournalpost(
							AksjonsTypeCode.EKSPEDER, journalpost.getJournalpostId(), null, "Journalposten fikk status 'ekspedert'",
							null, trackStatusSattTilEkspedert.getChanges());
				}
				return JournalpostResponse.ok(journalpostWithDistribusjonsinfo.getJournalpostId());
			} catch (NullPointerException e) {
				throw new DokarkivTechnicalException("Klarte ikke opprette UtsendingsInfo for journalpostId=" + journalpost.getJournalpostId(), e);
			} catch (DokarkivFunctionalException e) {
				return JournalpostResponse.error(journalpostWithDistribusjonsinfo.getJournalpostId(), e.getMessage());
			}
		}).orElseGet(() -> JournalpostResponse.error(journalpostWithDistribusjonsinfo.getJournalpostId(),
				String.format("Kunne ikke finne journalpost med journalpostId=%s i joark", journalpostWithDistribusjonsinfo.getJournalpostId())));
	}

	private boolean isFeilregistrertOrJournalStatusEorAorU(Journalpost jp) {
		return jp.isFeilregistrert() || IKKE_OPPDATER_MED_JOURNALSTATUS.contains(jp.getJournalstatus());
	}
}
