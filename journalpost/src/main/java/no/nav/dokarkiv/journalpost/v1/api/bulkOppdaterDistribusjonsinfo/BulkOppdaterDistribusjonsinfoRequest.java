package no.nav.dokarkiv.journalpost.v1.api.bulkOppdaterDistribusjonsinfo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;


@Builder
@Getter
@AllArgsConstructor
public class BulkOppdaterDistribusjonsinfoRequest {

	private ArrayList<JournalpostWithDistribusjonsinfo> journalposter;
}
