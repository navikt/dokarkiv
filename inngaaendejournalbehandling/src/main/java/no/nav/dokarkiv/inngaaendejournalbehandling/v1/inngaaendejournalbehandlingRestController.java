package no.nav.dokarkiv.inngaaendejournalbehandling.v1;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@RestController
@RequestMapping("rest")
@Slf4j
public class inngaaendejournalbehandlingRestController {

	@GetMapping(value = "/test")
	public @ResponseBody
	String test() {
		return "Dette er en test!";
	}
}
