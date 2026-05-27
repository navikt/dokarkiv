package no.nav.dokarkiv.internal.settbrevdata;

import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.exceptions.DokumentIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.InputValideringBadMetadataException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.journalbehandling.DokumentFilerDelegate;
import no.nav.dokarkiv.core.repository.DokumentFilRepository;
import no.nav.dokarkiv.core.repository.JournalpostRepository;
import no.nav.dokarkiv.core.sporing.SporingPopulator;
import org.jetbrains.annotations.NotNull;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_NAME;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.D;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.R;
import static no.nav.dokarkiv.internal.settbrevdata.SettBrevdata.Handling.INGEN;
import static no.nav.dokarkiv.internal.settbrevdata.SettBrevdata.Handling.OPPDATERT_DOKUMENT;
import static no.nav.dokarkiv.internal.settbrevdata.SettBrevdata.Handling.OPPRETTET_DOKUMENT;

@Component
public class SettBrevdataService {

	private static final EnumSet<JournalStatusCode> GYLDIGE_JOURNALSTATUSER = EnumSet.of(R, D);
	private final JournalpostRepository journalpostRepository;
	private final DokumentFilRepository dokumentFilRepository;
	private final DokumentFilerDelegate dokumentFilerDelegate;
	private final SporingPopulator sporingPopulator;

	public SettBrevdataService(JournalpostRepository journalpostRepository,
							   DokumentFilRepository dokumentFilRepository,
							   DokumentFilerDelegate dokumentFilerDelegate,
							   SporingPopulator sporingPopulator) {
		this.journalpostRepository = journalpostRepository;
		this.dokumentFilRepository = dokumentFilRepository;
		this.dokumentFilerDelegate = dokumentFilerDelegate;
		this.sporingPopulator = sporingPopulator;
	}

	@Transactional
	public SettBrevdata settBrevdata(long journalpostId, VariantFormatCode variantFormat, byte[] brevdata) {
		Optional<Journalpost> journalpostOpt = journalpostRepository.findById(journalpostId);
		if (journalpostOpt.isPresent()) {
			Journalpost journalpost = journalpostOpt.get();
			validerJournalpost(journalpost);
			return opprettEllerOppdaterDokumentFil(variantFormat, brevdata, journalpost);
		} else {
			throw new JournalpostIkkeFunnetException("Fant ikke journalpostId=" + journalpostId);
		}
	}

	@NotNull
	private SettBrevdata opprettEllerOppdaterDokumentFil(VariantFormatCode variantFormat, byte[] brevdata, Journalpost journalpost) {
		FilDetaljer filDetaljer = finnFilDetaljer(journalpost, variantFormat);
		boolean dokumentFilEksisterer = dokumentFilEksisterer(filDetaljer);

		if (arkivDokumentFilEksisterer(variantFormat, dokumentFilEksisterer)) {
			return SettBrevdata.from(filDetaljer, INGEN);
		}

		filDetaljer.setEndretKildeNavn(MDC.get(MDC_CONSUMER_ID));
		filDetaljer.setFileContent(brevdata);
		sporingPopulator.populateSporingInfo(journalpost, MDC.get(MDC_USER_NAME));
		dokumentFilerDelegate.saveUpdateDokumentFiler(journalpost);
		if (dokumentFilEksisterer) {
			return SettBrevdata.from(filDetaljer, OPPDATERT_DOKUMENT);
		} else {
			return SettBrevdata.from(filDetaljer, OPPRETTET_DOKUMENT);
		}
	}

	private boolean dokumentFilEksisterer(FilDetaljer filDetaljer) {
		return dokumentFilRepository.existsByFilUuid(filDetaljer.getFilUuid());
	}

	private static boolean arkivDokumentFilEksisterer(VariantFormatCode variantFormat, boolean dokumentFilEksisterer) {
		return requireNonNull(variantFormat) == VariantFormatCode.ARKIV && dokumentFilEksisterer;
	}

	private static void validerJournalpost(Journalpost journalpost) {
		if (!GYLDIGE_JOURNALSTATUSER.contains(journalpost.getJournalstatus())) {
			throw new InputValideringBadMetadataException("Journalstatus må være en av " + GYLDIGE_JOURNALSTATUSER);
		}
	}

	private static FilDetaljer finnFilDetaljer(Journalpost journalpost, VariantFormatCode variantFormat) {
		JournalpostDokumentInfoRelasjon hoveddokumentDokumentInfoRelasjon = journalpost.findHoveddokumentDokumentInfoRelasjon();
		FilDetaljer filDetaljer = hoveddokumentDokumentInfoRelasjon.getDokumentInfo().findFilDetaljerByVariantFormat(variantFormat);
		if (filDetaljer == null) {
			throw new DokumentIkkeFunnetException("Fant ikke FilDetaljer på hoveddokumentet med variantFormat=" + variantFormat);
		}
		return filDetaljer;
	}
}
