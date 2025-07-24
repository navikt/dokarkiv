package no.nav.dokarkiv.core.consumer.ereg;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record EregResponse(
		String organisasjonsnummer,
		Navn navn) {

	public record Navn(
			String sammensattnavn,
			Bruksperiode bruksperiode,
			Gyldighetsperiode gyldighetsperiode) {

		public boolean erGyldig() {
			if (bruksperiode == null || gyldighetsperiode == null) {
				return false;
			}

			return bruksperiode.erGyldig() && gyldighetsperiode.erGyldig();
		}

		public record Bruksperiode(
				LocalDateTime fom,
				LocalDateTime tom) {

			public boolean erGyldig() {
				if (fom == null) {
					return false;
				} else if (tom == null) {
					return !fom.isAfter(LocalDateTime.now());
				}

				return !fom.isAfter(LocalDateTime.now()) && !tom.isBefore(LocalDateTime.now());
			}
		}

		public record Gyldighetsperiode(
				LocalDate fom,
				LocalDate tom) {

			public boolean erGyldig() {
				if (fom == null) {
					return false;
				} else if (tom == null) {
					return !fom.isAfter(LocalDate.now());
				}

				return !fom.isAfter(LocalDate.now()) && !tom.isBefore(LocalDate.now());
			}
		}
	}
}
