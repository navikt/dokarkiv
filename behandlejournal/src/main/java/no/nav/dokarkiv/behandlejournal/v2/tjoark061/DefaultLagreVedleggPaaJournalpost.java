package no.nav.dokarkiv.behandlejournal.v2.tjoark061;

import com.google.common.collect.Lists;
import no.nav.dokarkiv.behandlejournal.SporingUtil;
import no.nav.dokarkiv.behandlejournal.SporingsMetaData;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.DokumentFil;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.exceptions.ApplicationException;
import no.nav.dokarkiv.core.exceptions.NoJournalpostFoundException;
import no.nav.dokarkiv.core.repository.DokumentFilRepository;
import no.nav.dokarkiv.core.repository.DokumentInfoRepository;
import no.nav.dokarkiv.core.repository.JoarkRepositorySkjermet;
import no.nav.dokarkiv.core.sporing.KildeNavnPopulator;
import no.nav.dokarkiv.core.stelvio.RequestContextHolder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class DefaultLagreVedleggPaaJournalpost implements LagreVedleggPaaJournalpost {

	private final JoarkRepositorySkjermet joarkRepository;
	private final DokumentInfoRepository dokumentInfoRepository;
	private final DokumentFilRepository dokumentFilRepository;
	private final KildeNavnPopulator kildeNavnPopulator;
	private String vedleggDokumentTypeId;

	public DefaultLagreVedleggPaaJournalpost(JoarkRepositorySkjermet joarkRepository, DokumentInfoRepository dokumentInfoRepository, DokumentFilRepository dokumentFilRepository, KildeNavnPopulator kildeNavnPopulator, @Value("${behandlejournal.v2.lagreVedleggPaaJournalpost.vedleggDokumentTypeId}") String vedleggDokumentTypeId) {
		this.joarkRepository = joarkRepository;
		this.dokumentInfoRepository = dokumentInfoRepository;
		this.dokumentFilRepository = dokumentFilRepository;
		this.kildeNavnPopulator = kildeNavnPopulator;
		this.vedleggDokumentTypeId = vedleggDokumentTypeId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public LagreVedleggPaaJournalpostResponse lagreVedleggPaaJournalpost(
			LagreVedleggPaaJournalpostRequest lagreVedleggPaaJournalpostRequest) throws NoJournalpostFoundException {
		validateRequiredInput(lagreVedleggPaaJournalpostRequest);
		DokumentInfo dokumentInfo = lagreVedleggPaaJournalpostRequest.getDokumentInfo();
		Long journalpostId = lagreVedleggPaaJournalpostRequest.getJournalpostId();
		SporingsMetaData sporingsMetaData = lagreVedleggPaaJournalpostRequest.getSporingsMetaData();

		return handleIncomingDokumentvedlegg(journalpostId, dokumentInfo, sporingsMetaData);
	}

	private LagreVedleggPaaJournalpostResponse handleIncomingDokumentvedlegg(Long journalpostId,
																			 DokumentInfo dokumentInfo, SporingsMetaData sporingsMetaData) throws NoJournalpostFoundException {
		return handleIncomingJoarkvedlegg(journalpostId, dokumentInfo, sporingsMetaData);
	}

	private LagreVedleggPaaJournalpostResponse handleIncomingJoarkvedlegg(Long journalpostId,
																		  DokumentInfo dokumentInfo, SporingsMetaData sporingsMetaData) throws NoJournalpostFoundException {
		Journalpost journalpost = getPersistedJournalpost(journalpostId);

		validateJournalpostAndDokumentInfo(journalpost, dokumentInfo);
		updateJournalpostAndDokumentInfoValues(journalpost, dokumentInfo, sporingsMetaData);

		persistDokumenter(dokumentInfo);
		// FIXME
		DokumentInfo savedDokumentInfo = dokumentInfoRepository.persist(dokumentInfo);
		joarkRepository.save(journalpost);
		return new LagreVedleggPaaJournalpostResponse(savedDokumentInfo.getDokumentInfoId());
	}

	private Journalpost getPersistedJournalpost(Long journalpostId) throws NoJournalpostFoundException {
		Journalpost journalpost = joarkRepository.findById(journalpostId).orElse(null);
		if (journalpost == null) {
			throw new NoJournalpostFoundException("Journalpost with id: " + journalpostId + " does not exist",
					journalpostId);
		}
		return journalpost;
	}

	private void validateJournalpostAndDokumentInfo(Journalpost journalpost, DokumentInfo dokumentInfo) {
		validateJournalpostHasStatusForDokumentopplasting(journalpost);
		validateDokumentInfo(dokumentInfo);
		validateAndSetOrginalJournalpost(journalpost, dokumentInfo);
	}

	private void updateJournalpostAndDokumentInfoValues(Journalpost journalpost, DokumentInfo dokumentInfo,
														SporingsMetaData sporingsMetaData) {
		if (FagomradeCode.PEN.equals(journalpost.getFagomrade())) {
			dokumentInfo.setKategori(DokumentKategoriCode.IS);
		}
		updateJournalpostvaluesIfNotInngaaende(journalpost, dokumentInfo);
		addJournalpostDokumentInfoRelasjon(journalpost, dokumentInfo, sporingsMetaData);
		addKildeNavnForJournalpostStructure(journalpost);
	}

	private void updateJournalpostvaluesIfNotInngaaende(Journalpost journalpost, DokumentInfo dokumentInfo) {
		if (!journalpost.isInngaende()) {
			dokumentInfo.setDokumentstatus(DokumentStatusCode.FERDIGSTILT);
			dokumentInfo.setDokumentFerdigDato(new Date());
		}
	}

	private void addKildeNavnForJournalpostStructure(Journalpost journalpost) {
		kildeNavnPopulator.populateKildeNavnForEntireJournalStructure(journalpost, RequestContextHolder
				.currentRequestContext().getComponentId());
	}

	private void addJournalpostDokumentInfoRelasjon(Journalpost journalpost, DokumentInfo dokumentInfo,
													SporingsMetaData sporingsMetaData) {
		JournalpostDokumentInfoRelasjon dokumentInfoRelasjon = new JournalpostDokumentInfoRelasjon();
		dokumentInfoRelasjon.setTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG);
		dokumentInfoRelasjon.setTilknyttetAvNavn(SporingUtil.decideSporingNavn(sporingsMetaData));
		dokumentInfoRelasjon.setDokumentInfo(dokumentInfo);

		journalpost.addJournalpostDokumentInfoRelasjon(dokumentInfoRelasjon);
	}

	private void validateDokumentInfo(DokumentInfo dokumentInfo) {
		for (FilDetaljer filDetaljer : dokumentInfo.getFildetaljerListe()) {
			validateFilDetaljer(filDetaljer);
		}
		dokumentInfo.verifyNoVariantDuplicates();
	}

	private void validateFilDetaljer(FilDetaljer filDetaljer) {
		if (filDetaljer.getFiltype() == null) {
			throw new ApplicationException("Filtype is missing from Fildetaljer");
		}
		if (filDetaljer.getVariantFormat() == null) {
			throw new ApplicationException("Variantformat is missing from Fildetaljer");
		}
		if (!filDetaljer.hasFileContent()) {
			throw new ApplicationException("Filecontent is missing from Fildetaljer");
		}
	}

	private void validateJournalpostHasStatusForDokumentopplasting(Journalpost journalpost) {
		if (!journalpost.getJournalstatus().equals(JournalStatusCode.OD)) {
			throw new ApplicationException("JournalpostStatus is not OD. Dokumentopplasting not allowed.");
		}
	}

	private void validateAndSetOrginalJournalpost(Journalpost journalpost, DokumentInfo dokumentInfo) {
		long journalpostId = journalpost.getJournalpostId();
		DokumentInfo hoveddokumentDokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		if (hoveddokumentDokumentInfo.getOriginalJournalpost().getJournalpostId() == journalpostId) {
			dokumentInfo.setOriginalJournalpost(journalpost);
		} else {
			throw new ApplicationException(
					"OriginalJournalpost property on retrieved Journalpost not the same as input journalpostId");
		}
	}

	private void persistDokumenter(DokumentInfo dokumentInfo) {
		List<DokumentFil> dokumentFiler = createDokumentFilerFromFildetaljer(dokumentInfo);
		for (DokumentFil dokumentFil : dokumentFiler) {
			dokumentFilRepository.save(dokumentFil);
		}
	}

	private List<DokumentFil> createDokumentFilerFromFildetaljer(DokumentInfo dokumentInfo) {
		List<DokumentFil> dokumentFiler = Lists.newArrayList();
		for (FilDetaljer fildetaljer : dokumentInfo.getFildetaljerListe()) {
			if (fildetaljer.hasFileContent()) {
				dokumentFiler.add(fildetaljer.createDokumentFil());
			}
		}
		return dokumentFiler;
	}

	private void validateRequiredInput(LagreVedleggPaaJournalpostRequest lagreVedleggPaaJournalpostRequest) {
		if (lagreVedleggPaaJournalpostRequest == null) {
			throw new ApplicationException("Missing parameter: lagreVedleggPaaJournalpostRequest is null");
		}
		lagreVedleggPaaJournalpostRequest.validate();
	}

	void setVedleggDokumentTypeId(String vedleggDokumentTypeId) {
		this.vedleggDokumentTypeId = vedleggDokumentTypeId;
	}
}
