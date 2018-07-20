package no.nav.dokarkiv.core.repository.ondemand;

import static org.junit.Assert.fail;

import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;

/**
 * Verifies the behaviour of Apache Commons GenericObjectPool.
 * 
 * @author Marius Thøring, Visma Consulting
 */
public class GenericObjectPoolTest {

	private GenericObjectPool<Object> objectPool;

	@Before
	public void setup() throws Exception {
		objectPool = new GenericObjectPool<Object>(new TestPortableObjectFactory());
	}

	@Test
	public void shouldAlwaysReturnNewObjectWhenMaxIdleIsZero() throws Exception {
		objectPool.setMaxActive(10);
		objectPool.setMaxIdle(0);

		HashSet<Object> objectSet = borrowAndReturnAllObjects();
		
		Object object = objectPool.borrowObject();
		if (objectSet.contains(object)) {
			fail("Expected new object!");
		}
	}

	@Test
	public void shouldCacheIdleObjects() throws Exception {
		objectPool.setMaxActive(10);
		objectPool.setMaxIdle(1);

		HashSet<Object> objectSet = borrowAndReturnAllObjects();
		
		Object object = objectPool.borrowObject();
		if (!objectSet.contains(object)) {
			fail("Expected pooled object!");
		}
	}
	
	@Test
	public void shouldEvictIdleObjectsAfterSomeTime() throws Exception {
		int waitMillis = 1000;
		objectPool.setMaxActive(10);
		objectPool.setMaxIdle(10);
		objectPool.setMinEvictableIdleTimeMillis(waitMillis/4);
		objectPool.setTimeBetweenEvictionRunsMillis(waitMillis/5);
		objectPool.setNumTestsPerEvictionRun(objectPool.getMaxIdle());

		HashSet<Object> objectSet = borrowAndReturnAllObjects();
		
		Object object = objectPool.borrowObject();
		if (!objectSet.contains(object)) {
			fail("Expected pooled object!");
		}
		
		Thread.sleep(waitMillis);
		object = objectPool.borrowObject();
		if (objectSet.contains(object)) {
			fail("Objects should be evicted due to idle time beeing exceeded");
		}
	}
	
	private HashSet<Object> borrowAndReturnAllObjects() throws Exception {
		//Borrow all objects:
		HashSet<Object> objectSet = new HashSet<Object>();
		for (int i = 0; i < objectPool.getMaxActive(); i++) {
			boolean isUnique = objectSet.add(objectPool.borrowObject());
			if (!isUnique) {
				fail("The same object was returned twice!");
			}
		}

		//Return all objects:
		for(Object object : objectSet) {
			objectPool.returnObject(object);
		}
		return objectSet;
	}

	private class TestPortableObjectFactory extends BasePoolableObjectFactory<Object> {
		@Override
		public Object makeObject() throws Exception {
			return new Object();
		}
	}
}
