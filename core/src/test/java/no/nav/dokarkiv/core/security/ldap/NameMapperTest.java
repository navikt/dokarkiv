package no.nav.dokarkiv.core.security.ldap;


import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Ignore;
import org.junit.Test;

import javax.naming.directory.BasicAttributes;

/**
 * Unit test for NameMapper
 *
 * @author Andreas Skomedal, Visma Consulting
 */
@Ignore
public class NameMapperTest {

	public static final String NAME = "name";
	private NameMapper nameMapper = new NameMapper();

	@Test
	public void shouldMapDescription() throws Exception {
		String name = nameMapper.mapFromAttributes(new BasicAttributes(NameMapper.DESCRIPTION, NAME));
		assertThat(name, is(NAME));
	}

	@Test
	public void shouldMapDisplayname() throws Exception {
		String name = nameMapper.mapFromAttributes(new BasicAttributes(NameMapper.DISPLAYNAME, NAME));
		assertThat(name, is(NAME));
	}

	@Test
	public void shouldMapNotFoundToNull() throws Exception {
		String name = nameMapper.mapFromAttributes(new BasicAttributes());
		assertThat(name, nullValue());
	}
}
