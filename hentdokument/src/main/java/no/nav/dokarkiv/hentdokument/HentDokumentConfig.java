package no.nav.dokarkiv.hentdokument;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@ComponentScan
@EnableWebSecurity
public class HentDokumentConfig extends WebSecurityConfigurerAdapter {

	@Override
	public void configure(WebSecurity web) {
//		web.ignoring().antMatchers("/ws/**");
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
				.antMatchers("/is**").permitAll();
//		http.csrf().disable();
	}

}
