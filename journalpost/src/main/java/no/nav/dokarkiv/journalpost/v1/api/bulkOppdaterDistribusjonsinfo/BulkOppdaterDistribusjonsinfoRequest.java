package no.nav.dokarkiv.journalpost.v1.api.bulkOppdaterDistribusjonsinfo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;


@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BulkOppdaterDistribusjonsinfoRequest {
	private List<JournalpostWithDistribusjonsinfo> journalposter;
}
