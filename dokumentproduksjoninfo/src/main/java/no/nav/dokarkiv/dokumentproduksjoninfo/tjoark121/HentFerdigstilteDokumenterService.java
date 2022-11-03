package no.nav.dokarkiv.dokumentproduksjoninfo.tjoark121;

import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentFil;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.service.SkjermingService;
import no.nav.dokarkiv.core.repository.DokumentFilSkjermetRepository;
import no.nav.dokarkiv.core.repository.JoarkRepositorySkjermet;
import no.nav.dokarkiv.dokumentproduksjoninfo.exceptions.DokumentInfoNotFoundException;
import no.nav.dokarkiv.dokumentproduksjoninfo.exceptions.FilDetaljerNotFoundException;
import no.nav.dokarkiv.dokumentproduksjoninfo.exceptions.IllegalDokumentstatusException;
import no.nav.dokarkiv.dokumentproduksjoninfo.exceptions.IllegalJournalStatusException;
import no.nav.dokarkiv.dokumentproduksjoninfo.exceptions.IllegalVariantFormatException;
import no.nav.dokarkiv.dokumentproduksjoninfo.exceptions.JournalpostNotFoundException;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

@Service
public class HentFerdigstilteDokumenterService {

	private final JoarkRepositorySkjermet joarkRepository;
	private final DokumentFilSkjermetRepository dokumentFilRepository;
	private final HentFerdigstilteDokumenterValidator hentFerdigstilteRokumenterValidator;
	private final SkjermingService skjermingService;

	public HentFerdigstilteDokumenterService(JoarkRepositorySkjermet joarkRepository, DokumentFilSkjermetRepository dokumentFilRepository, HentFerdigstilteDokumenterValidator hentFerdigstilteRokumenterValidator, SkjermingService skjermingService) {
		this.joarkRepository = joarkRepository;
		this.dokumentFilRepository = dokumentFilRepository;
		this.hentFerdigstilteRokumenterValidator = hentFerdigstilteRokumenterValidator;
		this.skjermingService = skjermingService;
	}

	/**
	 * 
	 * @param journalpostId
	 *            journalpost til dokumentene
	 * @param dokumentInfos
	 *            liste med dokumentInfoId som man skal hente ut
	 * @return
	 */
	public List<HentFerdigstilteDokumenterResponseTo> hentFerdigstilteDokumenter(Long journalpostId, List<Long> dokumentInfos)
			throws FilDetaljerNotFoundException, JournalpostNotFoundException, IllegalJournalStatusException, IllegalDokumentstatusException
			, DokumentInfoNotFoundException, IllegalVariantFormatException {
		List<HentFerdigstilteDokumenterResponseTo> returnValue = new LinkedList<>();
			Journalpost journalpost = joarkRepository.findById(journalpostId).orElseThrow(() -> new JournalpostNotFoundException("journalpostId=" + journalpostId + " eksisterer ikke"));
			hentFerdigstilteRokumenterValidator.validateJournalpost(journalpostId, journalpost);

			for (Long dokumentInfoId : dokumentInfos) {
				DokumentInfo dokumentInfo = journalpost.findDokumentInfoById(dokumentInfoId);
				hentFerdigstilteRokumenterValidator.validateDokumentInfo(journalpostId, dokumentInfoId, dokumentInfo);

				FilDetaljer filDetaljer = dokumentInfo.findFilDetaljerByVariantFormat(VariantFormatCode.ARKIV);
				if (filDetaljer == null) {
					throw new FilDetaljerNotFoundException("Fildetaljer ikke funnet for journalpostId=" + journalpostId + ", + dokumentInfoId=" + dokumentInfoId
							+ ",variant=" + VariantFormatCode.ARKIV.name());
				}

				hentFerdigstilteRokumenterValidator.validateFildetaljer(dokumentInfoId, filDetaljer);

				DokumentFil dokumentFil = dokumentFilRepository.findByFilUuid(filDetaljer.getFilUuid());
				if (dokumentFil == null) {
					throw new FilDetaljerNotFoundException("Fildetaljer ikke funnet for journalpostId=" + journalpostId + ", + dokumentInfoId=" + dokumentInfoId
							+ ",filUuid=" + filDetaljer.getFilUuid());
				}

				HentFerdigstilteDokumenterResponseTo response = new HentFerdigstilteDokumenterResponseTo(dokumentInfoId,
						dokumentFil.getFil(), dokumentInfo.getTittel());
				returnValue.add(response);
			}
		return returnValue;
	}
}
