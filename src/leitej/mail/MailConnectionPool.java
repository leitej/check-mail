/*******************************************************************************
 * Copyright (C) 2019 Julio Leite
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package leitej.mail;

import java.io.IOException;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import leitej.exception.ClosedLtRtException;
import leitej.exception.ObjectPoolLtException;
import leitej.log.Logger;
import leitej.util.data.AbstractObjectPool;

/**
 * @author julio
 *
 */
final class MailConnectionPool extends AbstractObjectPool<MailConnection> {

	private static final long serialVersionUID = 4625215381637184975L;

	private static final Logger LOG = Logger.getInstance();

	private final Config config;
	private final SSLSocketFactory factory;

	protected MailConnectionPool(final Config config) throws IllegalArgumentException {
		super(config.getMaxConnections());
		this.config = config;
		this.factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
	}

	@Override
	protected MailConnection newObject() throws ObjectPoolLtException {
		try {
			return new MailConnection(
					(SSLSocket) this.factory.createSocket(this.config.getHostname(), this.config.getPort()),
					this.config.getUsername(), this.config.getPassword());
		} catch (final IOException e) {
			throw new ObjectPoolLtException("#0", e);
		}
	}

	@Override
	protected boolean isInactive(final MailConnection obj) {
		return obj.isInactive();
	}

	@Override
	protected void deactivate(final MailConnection obj) {
		try {
			obj.close();
		} catch (final IOException e) {
			LOG.error("#0", e);
		}
	}

	@Override
	protected MailConnection poll() throws ClosedLtRtException, ObjectPoolLtException, InterruptedException {
		return super.poll();
	}

	@Override
	protected void offer(final MailConnection obj) throws InterruptedException, IllegalArgumentException {
		super.offer(obj);
	}

	@Override
	protected synchronized void close() throws ObjectPoolLtException {
		super.close();
	}

}
