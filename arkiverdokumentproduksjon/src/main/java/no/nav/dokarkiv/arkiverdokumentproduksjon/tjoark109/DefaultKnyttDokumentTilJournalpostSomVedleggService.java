package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark109;

import static java.util.Arrays.asList;
import static no.nav.dokarkiv.core.domain.codes.DokumentStatusCode.FERDIGSTILT;
import static no.nav.dokarkiv.core.domain.codes.FagomradeCode.GEN;
import static no.nav.dokarkiv.core.domain.codes.FagomradeCode.OPP;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.D;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.E;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.FL;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.FS;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.J;
import static no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode.VEDLEGG;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.ARKIV;
import static org.apache.commons.lang3.BooleanUtils.isTrue;
import static org.springframework.util.StringUtils.collectionToDelimitedString;

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
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.JournalpostIkkeFerdigstiltException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.JournalpostNotFoundException;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.core.sporing.SporingPopulator;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.EnumSet;

/**
 * @author Thomas Kåsene, Visma Consulting AS
 */
@Component
public class DefaultKnyttDokumentTilJournalpostSomVedleggService implements KnyttDokumentTilJournalpostSomVedleggService {

	private static final String KILDENAVN = "Dokumentproduksjon";

	private static final String EKSTERNE_VEDLEGG_KEY = "EksterneVedlegg";

	private static final String NO_DOKUMENT_INFO_FOUND_FORMAT = "Journalpost with journalpostId=%d has no DokumentInfo with dokumentInfoId=%d";
	private static final String FEILREGISTRERT_SAKSRELASJON_FORMAT = "Journalpost with journalpostId=%d cannot have saksrelasjon that is feilregistrert";
	private static final String JOURNALPOST_NOT_FERDIGSTILT_FORMAT = "Journalpost with journalpostId=%d must have one of the following journalStatus: %s";
	private static final String JOURNAL_STATUS_FORMAT = "Journalpost with journalpostId=%d must have journalStatus '%s'";
	private static final String TILLEGGSOPPLYSNING_FORMAT = "Journalpost with journalpostId=%d needs hoveddokument with tilleggsopplysning '%s'='%s'";
	private static final String REQUIRED_DOKUMENTSTATUS_FORMAT = "DokumentInfo with dokumentInfoId=%d must have dokumentstatus '%s' or undefined";
	private static final String DELETED_DOKUMENT_INFO_FORMAT = "DokumentInfo with dokumentInfoId=%d must not be deleted";
	private static final String ORGAN_INTERN_DOKUMENT_INFO_FORMAT = "DokumentInfo with dokumentInfoId=%d cannot be organ intern";
	private static final String PARTSINNSYN_DOKUMENT_INFO_FORMAT = "DokumentInfo with dokumentInfoId=%d cannot have innskrenket partsinnsyn";
	private static final String PARTSINNSYN_TREDJEPART_DOKUMENT_INFO_FORMAT = "DokumentInfo with dokumentInfoId=%d cannot have innskrenket partsinnsyn"
			+ " fra tredjepart";
	private static final String ON_DEMAND_FIL_DETALJER_FORMAT = "DokumentInfo with dokumentInfoId=%d cannot have fildetaljer with onDemandId defined";
	private static final String VARIANT_FORMAT_FORMAT = "DokumentInfo with dokumentInfoId=%d requires at least one fildetalj with variantFormat '%s'";
	private static final String FAGOMRAADE_MESSAGE = "Journalpost source with journalpostId=%d must have fagomrade 'OPP' or 'GEN', or it must be equal"
			+ " to fagomrade on the target journalpost (journalpostId=%d)";

	@Inject
	private KnyttDokumentTilJournalpostSomVedleggValidator validator;

	@Inject
	private JoarkRepository joarkRepository;

	@Inject
	private SporingPopulator sporingPopulator;

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void knyttDokumentTilJournalpostSomVedlegg(KnyttDokumentTilJournalpostSomVedleggRequestTo request) throws
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
			FeilregistrertSaksrelasjonException {

		validator.validate(request);

		Journalpost journalpostTarget = joarkRepository.findById(request.getKnyttesTilJournalpostId())
				.orElseThrow(() -> new JournalpostNotFoundException("Journalpost with journalpostId=" + request.getKnyttesTilJournalpostId() + " does not exist"));
		checkIfJournalpostIsUnderProduksjon(journalpostTarget);
		checkIfJournalpostAllowsEksterneVedlegg(journalpostTarget);

		Journalpost journalpostSource = joarkRepository.findById(request.getKnyttesFraJournalpostId())
				.orElseThrow(() -> new JournalpostNotFoundException("Journalpost with journalpostId=" + request.getKnyttesFraJournalpostId() + " does not exist"));
		checkIfJournalpostHasAllowedJournalstatus(journalpostSource);
		checkIfJournalpostHasFeilregistrertSaksrelasjon(journalpostSource);

		checkIfJournalpostsHaveLegalFagomrader(journalpostSource, journalpostTarget);

		DokumentInfo dokumentInfo = findRelevantDokumentInfo(request.getDokumentInfoId(), journalpostSource);
		checkIfDokumentInfoHasLegalDokumentStatus(dokumentInfo);
		checkIfDokumentInfoIsSlettet(dokumentInfo);
		checkIfDokumentInfoIsOrganIntern(dokumentInfo);
		checkIfDokumentInfoHasInnskrenketPartsinnsyn(dokumentInfo);
		checkIfDokumentInfoHasInnskrenketPartsinnsynFraTredjepart(dokumentInfo);
		checkIfDokumentInfoDoesNotHaveFildetaljerWithOnDemandId(dokumentInfo);
		checkIfDokumentInfoHasFildetaljerWithArkivVariantFormat(dokumentInfo);

		JournalpostDokumentInfoRelasjon relasjon = createJournalpostDokumentInfoRelasjon(request.getEndretAvNavn(), dokumentInfo);

		journalpostTarget.addJournalpostDokumentInfoRelasjon(relasjon);

		if (dokumentInfo.getDokumentstatus() == null) {
			dokumentInfo.setDokumentstatus(FERDIGSTILT);
		}

		sporingPopulator.populateSporingInfo(journalpostTarget, KILDENAVN);
	}

	private void checkIfJournalpostIsUnderProduksjon(Journalpost journalpost) throws IllegalJournalStatusException {
		if (journalpost.getJournalstatus() != D) {
			String message = String.format(JOURNAL_STATUS_FORMAT, journalpost.getJournalpostId(), D.name());
			throw new IllegalJournalStatusException(message);
		}
	}

	private void checkIfJournalpostAllowsEksterneVedlegg(Journalpost journalpost) throws IllegalTilleggsopplysningerException {
		DokumentInfo hoveddokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		if (!Boolean.valueOf(hoveddokumentInfo.getTilleggsopplysninger().get(EKSTERNE_VEDLEGG_KEY))) {
			String message = String.format(TILLEGGSOPPLYSNING_FORMAT, journalpost.getJournalpostId(), EKSTERNE_VEDLEGG_KEY, true);
			throw new IllegalTilleggsopplysningerException(message);
		}
	}

	private void checkIfJournalpostHasAllowedJournalstatus(Journalpost journalpost) throws JournalpostIkkeFerdigstiltException {
		EnumSet<JournalStatusCode> legalJournalStatusCodes = EnumSet.of(J, FS, FL, E);
		if (!legalJournalStatusCodes.contains(journalpost.getJournalstatus())) {
			String legalJournalStatuses = collectionToDelimitedString(asList(J.name(), FS.name(), FL.name(), E.name()), ", ");
			String message = String.format(JOURNALPOST_NOT_FERDIGSTILT_FORMAT, journalpost.getJournalpostId(), legalJournalStatuses);
			throw new JournalpostIkkeFerdigstiltException(message);
		}
	}

	private void checkIfJournalpostHasFeilregistrertSaksrelasjon(Journalpost journalpost) throws FeilregistrertSaksrelasjonException {
		if (isTrue(journalpost.getSaksrelasjon().getFeilregistrert())) {
			String message = String.format(FEILREGISTRERT_SAKSRELASJON_FORMAT, journalpost.getJournalpostId());
			throw new FeilregistrertSaksrelasjonException(message);
		}
	}

	private void checkIfJournalpostsHaveLegalFagomrader(Journalpost journalpostSource, Journalpost journalpostTarget) throws IllegalFagomraadeException {
		EnumSet<FagomradeCode> legalSourceFagomraader = EnumSet.of(OPP, GEN, journalpostTarget.getFagomrade());
		if (!legalSourceFagomraader.contains(journalpostSource.getFagomrade())) {
			String message = String.format(FAGOMRAADE_MESSAGE, journalpostSource.getJournalpostId(), journalpostTarget.getJournalpostId());
			throw new IllegalFagomraadeException(message);
		}
	}

	private DokumentInfo findRelevantDokumentInfo(Long dokumentInfoId, Journalpost journalpost) throws DokumentInfoNotFoundException {
		DokumentInfo dokumentInfo = journalpost.findDokumentInfoById(dokumentInfoId);

		if (dokumentInfo == null) {
			String message = String.format(NO_DOKUMENT_INFO_FOUND_FORMAT, journalpost.getJournalpostId(), dokumentInfoId);
			throw new DokumentInfoNotFoundException(message);
		}

		return dokumentInfo;
	}

	private void checkIfDokumentInfoHasLegalDokumentStatus(DokumentInfo dokumentInfo) throws IllegalDokumentstatusException {
		if (dokumentInfo.getDokumentstatus() != null && dokumentInfo.getDokumentstatus() != FERDIGSTILT) {
			String message = String.format(REQUIRED_DOKUMENTSTATUS_FORMAT, dokumentInfo.getDokumentInfoId(), FERDIGSTILT.name());
			throw new IllegalDokumentstatusException(message);
		}
	}

	private void checkIfDokumentInfoIsSlettet(DokumentInfo dokumentInfo) throws DokumentInfoSlettetException {
		if (isTrue(dokumentInfo.getSlettet())) {
			String message = String.format(DELETED_DOKUMENT_INFO_FORMAT, dokumentInfo.getDokumentInfoId());
			throw new DokumentInfoSlettetException(message);
		}
	}

	private void checkIfDokumentInfoIsOrganIntern(DokumentInfo dokumentInfo) throws DokumentInfoIsOrganInterntException {
		if (isTrue(dokumentInfo.getOrganInternt())) {
			String message = String.format(ORGAN_INTERN_DOKUMENT_INFO_FORMAT, dokumentInfo.getDokumentInfoId());
			throw new DokumentInfoIsOrganInterntException(message);
		}
	}

	private void checkIfDokumentInfoHasInnskrenketPartsinnsyn(DokumentInfo dokumentInfo) throws DokumentInfoInnskrenketPartsinnsynException {
		if (isTrue(dokumentInfo.getInnskrenketPartsinnsyn())) {
			String message = String.format(PARTSINNSYN_DOKUMENT_INFO_FORMAT, dokumentInfo.getDokumentInfoId());
			throw new DokumentInfoInnskrenketPartsinnsynException(message);
		}
	}

	private void checkIfDokumentInfoHasInnskrenketPartsinnsynFraTredjepart(DokumentInfo dokumentInfo) throws DokumentInfoInnskrenketPartsinnsynException {
		if (isTrue(dokumentInfo.getInnskrenketPartsinnsynFraTredjepart())) {
			String message = String.format(PARTSINNSYN_TREDJEPART_DOKUMENT_INFO_FORMAT, dokumentInfo.getDokumentInfoId());
			throw new DokumentInfoInnskrenketPartsinnsynException(message);
		}
	}

	private void checkIfDokumentInfoDoesNotHaveFildetaljerWithOnDemandId(DokumentInfo dokumentInfo) throws FilDetaljerOnDemandException {
		for (FilDetaljer detaljer : dokumentInfo.getFildetaljerListe()) {
			if (detaljer.getOnDemandId() != null) {
				String message = String.format(ON_DEMAND_FIL_DETALJER_FORMAT, dokumentInfo.getDokumentInfoId());
				throw new FilDetaljerOnDemandException(message);
			}
		}
	}

	private void checkIfDokumentInfoHasFildetaljerWithArkivVariantFormat(DokumentInfo dokumentInfo) throws IllegalVariantFormatException {
		if (dokumentInfo.findFilDetaljerByVariantFormat(ARKIV) == null) {
			String message = String.format(VARIANT_FORMAT_FORMAT, dokumentInfo.getDokumentInfoId(), ARKIV.name());
			throw new IllegalVariantFormatException(message);
		}
	}

	private JournalpostDokumentInfoRelasjon createJournalpostDokumentInfoRelasjon(String tilknyttetAvNavn, DokumentInfo dokumentInfo) {
		JournalpostDokumentInfoRelasjon relasjon = new JournalpostDokumentInfoRelasjon();
		relasjon.setDokumentInfo(dokumentInfo);
		relasjon.setTilknyttetJournalpostSom(VEDLEGG);
		relasjon.setTilknyttetAvNavn(tilknyttetAvNavn);
		return relasjon;
	}
}
