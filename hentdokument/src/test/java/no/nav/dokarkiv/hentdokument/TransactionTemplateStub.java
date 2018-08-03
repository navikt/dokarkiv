package no.nav.dokarkiv.hentdokument;


import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * @author Ole Hjalmar Herje, BEKK
 *
 */
public class TransactionTemplateStub extends TransactionTemplate {
	
	private static final long serialVersionUID = 1L;

	/** {@inheritDoc} */
	@Override
	public <T> T execute(TransactionCallback<T> action) throws TransactionException {
		return action.doInTransaction(new SimpleTransactionStatus());
	}
}
