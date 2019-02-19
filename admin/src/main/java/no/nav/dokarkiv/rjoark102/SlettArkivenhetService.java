package no.nav.dokarkiv.rjoark102;

import static no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode.VEDLEGG;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.repository.JoarkDeleteRepository;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@SuppressWarnings("Duplicates")
public class SlettArkivenhetService {

	private final JoarkDeleteRepository deleteRepository;
	private final JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;

	@Inject
	public SlettArkivenhetService(JoarkDeleteRepository deleteRepository, JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository) {
		this.deleteRepository = deleteRepository;
		this.journalpostDokumentInfoRelasjonRepository = journalpostDokumentInfoRelasjonRepository;
	}

	public List<ArkivElementEndringTO> slettVedleggKnyttetTilJournalpost(Long journalpostId) {
		List<ArkivElementEndringTO> arkivElementEndringTOList = new ArrayList<>();
		List<JournalpostDokumentInfoRelasjon> vedleggRelasjoner = journalpostDokumentInfoRelasjonRepository.findAllByJournalpostJournalpostIdAndTilknyttetJournalpostSom(journalpostId, VEDLEGG);
		vedleggRelasjoner
				.forEach(relasjon -> arkivElementEndringTOList.addAll(slettJournalpostDokumentInfoRelasjonVedlegg(relasjon)));

		return arkivElementEndringTOList;
	}

	public List<ArkivElementEndringTO> slettJournalpostDokumentInfoRelasjonVedlegg(JournalpostDokumentInfoRelasjon relasjonSomSkalSlettes) {
		if (relasjonSomSkalSlettes.getDokumentInfo().isRelatedToMultipleJournalposts()) {
			return slettJournalpostDokumentInfoRelasjon(relasjonSomSkalSlettes);
		} else {
			return slettVedleggKnyttetTilBareEnJournalpost(relasjonSomSkalSlettes);
		}
	}

	public List<ArkivElementEndringTO> slettVedleggKnyttetTilBareEnJournalpost(JournalpostDokumentInfoRelasjon relasjonSomSkalSlettes) {
		List<ArkivElementEndringTO> arkivElementEndringTOList = new ArrayList<>();

		arkivElementEndringTOList.addAll(slettJournalpostDokumentInfoRelasjon(relasjonSomSkalSlettes));
		slettAlleFilOgFildetaljerGittDokumentInfoId(relasjonSomSkalSlettes.getDokumentInfo().getDokumentInfoId());
		arkivElementEndringTOList.addAll(slettDokumentInfo(relasjonSomSkalSlettes.getDokumentInfo().getDokumentInfoId()));

		return arkivElementEndringTOList;
	}

	public List<ArkivElementEndringTO> slettHoveddokument(JournalpostDokumentInfoRelasjon relasjonSomSkalSlettes) {
		slettJournalpostDokumentInfoRelasjon(relasjonSomSkalSlettes);
		slettAlleFilOgFildetaljerGittDokumentInfoId(relasjonSomSkalSlettes.getDokumentInfo().getDokumentInfoId());
		return slettDokumentInfo(relasjonSomSkalSlettes.getDokumentInfo().getDokumentInfoId());
	}

	public List<ArkivElementEndringTO> slettJournalpostDokumentInfoRelasjon(JournalpostDokumentInfoRelasjon relasjon) {
		deleteRepository.deleteJournalpostDokumentInfoRelasjonByJournalpostIdAndDokumentInfoId(
				relasjon.getJournalpost().getJournalpostId(),
				relasjon.getDokumentInfo().getDokumentInfoId());

		return Arrays.asList(
				ArkivElementEndringTO.builder()
						.arkivElement("JournalpostDokumentInfoRelasjon.dokumentInfoId")
						.fraVerdi(relasjon.getDokumentInfo().getDokumentInfoId().toString())
						.tilVerdi(null)
						.build()
		);
	}

	public List<ArkivElementEndringTO> slettJournalpost(Long journalpostId) {
		deleteRepository.deleteDokUrlInfoByJournalpostId(journalpostId);
		deleteRepository.deleteKryssreferanseByJournalpostId(journalpostId);
		deleteRepository.deleteReturInfoByJournalpostId(journalpostId);
		deleteRepository.deleteJPTilleggByJournalpostId(journalpostId);
		deleteRepository.deleteSaksrelasjonByJournalpostId(journalpostId);
		deleteRepository.deleteBrukereByJournalpostId(journalpostId);
		deleteRepository.deleteJournalpostByJournalpostId(journalpostId);

		return Collections.singletonList(
				ArkivElementEndringTO.builder()
						.arkivElement("Journalpost.journalpostId")
						.fraVerdi(journalpostId.toString())
						.tilVerdi(null)
						.build()

		);
	}

	public List<ArkivElementEndringTO> slettDokumentInfo(Long dokumentInfoId) {
		deleteRepository.deleteSkannetInnholdByDokumentInfoId(dokumentInfoId);
		deleteRepository.deleteDokInfoTilleggByDokumentInfoId(dokumentInfoId);
		deleteRepository.deleteDokInfoByDokumentInfoId(dokumentInfoId);

		return Collections.singletonList(
				ArkivElementEndringTO.builder()
						.arkivElement("DokumentInfo.dokumentInfoId")
						.fraVerdi(dokumentInfoId.toString())
						.tilVerdi(null)
						.build()

		);
	}

	public List<ArkivElementEndringTO> slettFilOgFildetaljer(Long dokumentInfoId, VariantFormatCode variantFormatCode) {
		deleteRepository.deleteDokumentFilByDokumentInfoIdAndVariantFormat(dokumentInfoId, variantFormatCode.name());
		deleteRepository.deleteFilDetaljerByDokumentInfoIdAndVariantFormat(dokumentInfoId, variantFormatCode.name());

		return Collections.singletonList(
				ArkivElementEndringTO.builder()
						.arkivElement("FilDetaljer.variantFormat")
						.fraVerdi(variantFormatCode.name())
						.tilVerdi(null)
						.build()

		);
	}

	private void slettAlleFilOgFildetaljerGittDokumentInfoId(Long dokumentInfoId) {
		deleteRepository.deleteDokumentFilByDokumentInfoId(dokumentInfoId);
		deleteRepository.deleteFilDetaljerByDokumentInfoId(dokumentInfoId);
	}
}
