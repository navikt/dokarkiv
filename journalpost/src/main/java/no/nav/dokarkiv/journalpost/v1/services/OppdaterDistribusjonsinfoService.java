package no.nav.dokarkiv.journalpost.v1.services;

import no.nav.dokarkiv.core.aksjonslogg.LagreAksjonsLoggService;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.repository.JoarkRepositorySkjermet;
import no.nav.dokarkiv.journalpost.v1.api.OppdaterDistribusjonsinfoRequest;
import no.nav.dokarkiv.journalpost.v1.api.bulkOppdaterDistribusjonsinfo.JournalpostResponse;
import no.nav.dokarkiv.journalpost.v1.api.bulkOppdaterDistribusjonsinfo.JournalpostWithDistribusjonsinfo;
import no.nav.dokarkiv.journalpost.v1.util.oppdaterjournalpost.ChangeTracker;
import no.nav.dokarkiv.journalpost.v1.util.oppdaterjournalpost.JournalpostUpdater;
import no.nav.dokarkiv.journalpost.v1.util.oppdaterjournalpost.JournalpostUpdaterFromBulk;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.A;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.E;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.U;
import static no.nav.dokarkiv.journalpost.v1.validators.OppdaterDistribusjonsinfoValidator.validateJournalpostKanSetteStatusEkspedert;

@Service("oppdaterDistribusjonsinfo")
public class OppdaterDistribusjonsinfoService {

	private static final List<JournalStatusCode> JOURNAL_STATUSER_INKLUDERT_IN_RESPONSEN = Arrays.asList(E, A, U);
	private final JoarkRepositorySkjermet joarkRepository;
	private final JournalpostUpdater journalpostUpdater;
	private final LagreAksjonsLoggService aksjonsLoggService;

	public OppdaterDistribusjonsinfoService(JoarkRepositorySkjermet joarkRepository,
											JournalpostUpdater journalpostUpdater,
											LagreAksjonsLoggService aksjonsLoggService) {
		this.joarkRepository = joarkRepository;
		this.journalpostUpdater = journalpostUpdater;
		this.aksjonsLoggService = aksjonsLoggService;
	}

	public void oppdaterDistribusjonsinfo(Long journalpostId, OppdaterDistribusjonsinfoRequest request) {
		Journalpost journalpost = joarkRepository.findById(journalpostId)
				.orElseThrow(() -> new JournalpostIkkeFunnetException(
						String.format("Kunne ikke finne journalpost med journalpostId=%s i joark", journalpostId)));

		if (request.getSettStatusEkspedert()) {
			validateJournalpostKanSetteStatusEkspedert(journalpost, request);
		}

		ChangeTracker trackStatusSattTilEkspedert = journalpostUpdater.updateFields(journalpost, request);

		joarkRepository.save(journalpost);

		if (!trackStatusSattTilEkspedert.getChanges().isEmpty()) {
			aksjonsLoggService.lagreAksjonsLoggForJournalpost(
					AksjonsTypeCode.EKSPEDER, journalpostId, null, "Journalposten fikk status 'ekspedert'",
					null, trackStatusSattTilEkspedert.getChanges());
		}
	}

	public JournalpostResponse oppdaterDistribusjonsinfoFromBulk(JournalpostWithDistribusjonsinfo journalpostWithDistribusjonsinfo) {
		Optional<Journalpost> journalpostOptional = joarkRepository.findById(journalpostWithDistribusjonsinfo.getJournalpostId());
		return journalpostOptional.map(journalpost -> {
			try {
				if (isFeilregistrertOrJournalStatusEorAorU(journalpost)) {
					return JournalpostResponse.ok(journalpost.getJournalpostId());
				}

				if (journalpostWithDistribusjonsinfo.getSettStatusEkspedert()) {
					validateJournalpostKanSetteStatusEkspedert(journalpost, journalpostWithDistribusjonsinfo);
				}

				ChangeTracker trackStatusSattTilEkspedert = JournalpostUpdaterFromBulk.updateFields(journalpost, journalpostWithDistribusjonsinfo);

				joarkRepository.save(journalpost);

				if (!trackStatusSattTilEkspedert.getChanges().isEmpty()) {
					aksjonsLoggService.lagreAksjonsLoggForJournalpost(
							AksjonsTypeCode.EKSPEDER, journalpost.getJournalpostId(), null, "Journalposten fikk status 'ekspedert'",
							null, trackStatusSattTilEkspedert.getChanges());
				}
				return JournalpostResponse.ok(journalpostWithDistribusjonsinfo.getJournalpostId());
			} catch (DokarkivFunctionalException e) {
				return JournalpostResponse.error(journalpostWithDistribusjonsinfo.getJournalpostId(), e.getMessage());
			}
		}).orElseGet(() -> JournalpostResponse.error(journalpostWithDistribusjonsinfo.getJournalpostId(),
				String.format("Kunne ikke finne journalpost med journalpostId=%s i joark", journalpostWithDistribusjonsinfo.getJournalpostId())));
	}

	private boolean isFeilregistrertOrJournalStatusEorAorU(Journalpost jp) {
		return (jp.getSaksrelasjon() == null || (jp.getSaksrelasjon().getFeilregistrert() != null
				&& jp.getSaksrelasjon().getFeilregistrert())) || JOURNAL_STATUSER_INKLUDERT_IN_RESPONSEN.contains(jp.getJournalstatus());
	}
}
