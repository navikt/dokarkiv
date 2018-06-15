package no.nav.dokarkiv.dokumentproduksjoninfo.tjoark121;

import no.nav.dokarkiv.core.domain.DokumentFil;
import no.nav.dokarkiv.core.domain.DokumentInfo;
import no.nav.dokarkiv.core.domain.FilDetaljer;
import no.nav.dokarkiv.core.domain.Journalpost;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.exceptions.DokumentInfoNotFoundException;
import no.nav.dokarkiv.core.exceptions.FilDetaljerNotFoundException;
import no.nav.dokarkiv.core.exceptions.IllegalDokumentstatusException;
import no.nav.dokarkiv.core.exceptions.IllegalJournalStatusException;
import no.nav.dokarkiv.core.exceptions.IllegalVariantFormatException;
import no.nav.dokarkiv.core.exceptions.JournalpostNotFoundException;
import no.nav.dokarkiv.core.repository.DokumentFilRepository;
import no.nav.dokarkiv.core.repository.JoarkRepository;

import javax.inject.Inject;
import java.util.LinkedList;
import java.util.List;

/**
 * Service-klasse for å hente ferdigstilte dokumenter (TJOARK121)
 * 
 * 
 * @author Stig Strøm
 *
 */
public class HentFerdigstilteDokumenterService {

	@Inject
	private JoarkRepository joarkRepository;

	@Inject
	private DokumentFilRepository dokumentFilRepository;
	
	@Inject
	private HentFerdigstilteDokumenterValidator hentFerdigstilteRokumenterValidator;

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
			Journalpost journalpost = joarkRepository.findById(journalpostId).orElse(null);
			hentFerdigstilteRokumenterValidator.validateJournalpost(journalpostId, journalpost);

			for (Long dokumentInfoId : dokumentInfos) {
				DokumentInfo dokumentInfo = journalpost.findDokumentInfoById(dokumentInfoId);
				hentFerdigstilteRokumenterValidator.validateDokumentInfo(journalpostId, dokumentInfoId, dokumentInfo);

				FilDetaljer filDetaljer = dokumentInfo.findFilDetaljerByVariantFormat(VariantFormatCode.ARKIV);
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
