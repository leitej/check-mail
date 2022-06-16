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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLSocket;

import leitej.exception.IllegalStateLtRtException;
import leitej.log.Logger;

/**
 * @author julio
 *
 */
final class MailConnection {

	private static final Logger LOG = Logger.getInstance();

	private final SSLSocket socket;
	private final BufferedWriter output;
	private final BufferedReader input;
	private volatile boolean isInactive;

	MailConnection(final SSLSocket socket, final String username, final String password) throws IOException {
		this.socket = socket;
		this.socket.addHandshakeCompletedListener(new HandshakeCompletedListener() {
			@Override
			public void handshakeCompleted(final HandshakeCompletedEvent event) {
				LOG.debug("handshakeCompleted");
				LOG.trace("CipherSuite: #0", event.getCipherSuite());
				LOG.trace("SessionId: #0", event.getSession());
				LOG.trace("PeerHost: #0", event.getSession().getPeerHost());
			}
		});
		this.socket.setKeepAlive(true);
		this.socket.setSoTimeout(10000);
		LOG.debug("startHandshake");
		this.socket.startHandshake();
		LOG.debug("access streams");
		this.output = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
		this.input = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
		login(username, password);
	}

	private void login(final String username, final String password) throws IOException {
		LOG.debug("login");
		this.output.write("tag LOGIN ");
		this.output.write(username);
		this.output.write(" ");
		this.output.write(password);
		this.output.write("\r\n");
		this.output.flush();
		readSocket();
	}

	private String readSocket() throws IOException {
		String result = null;
		String inputLine;
		while ((inputLine = this.input.readLine()) != null && !inputLine.contains("Success")) {
			LOG.trace("#0", inputLine);
			if (inputLine.startsWith("*")) {
				result = inputLine;
			}
		}
		LOG.trace("#0", inputLine);
		if (result == null || !inputLine.contains("Success")) {
			throw new IllegalStateLtRtException();
		}
		LOG.debug("result: #0", result);
		return result;
	}

	final boolean hasUnread() throws IOException {
		try {
			LOG.debug("hasUnread");
			this.output.write("tag STATUS INBOX (UNSEEN)\r\n");
			this.output.flush();
			final int unseen = Integer.valueOf(readSocket().split("UNSEEN ")[1].split("\\)")[0]);
			return unseen != 0;
		} catch (final Exception e) {
			this.isInactive = true;
			throw e;
		}
	}

	final boolean isInactive() {
		return this.socket.isClosed() || this.isInactive;
	}

	final void close() throws IOException {
		try {
			this.isInactive = true;
			LOG.debug("logout");
			this.output.write("tag LOGOUT\r\n");
			this.output.flush();
			readSocket();
			LOG.debug("close");
			this.input.close();
			this.output.close();
		} finally {
			this.socket.close();
		}
	}

}
