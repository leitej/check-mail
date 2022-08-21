/*******************************************************************************
 * Copyright Julio Leite
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
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

import leitej.log.Logger;

/**
 * @author Julio Leite
 *
 */
public final class StartTLSSocketFactory extends SocketFactory {

	// https://stackoverflow.com/questions/68317182/how-to-create-a-tls-connection-with-bouncycastle-in-c
	// https://stackoverflow.com/questions/32967750/how-can-i-implement-server-side-smtp-starttls

	private static final Logger LOG = Logger.getInstance();

	private static final SocketFactory FACTORY = new StartTLSSocketFactory();

	private static final SSLSocketFactory SSL_FACTORY = (SSLSocketFactory) SSLSocketFactory.getDefault();

	public static synchronized SocketFactory getDefault() {
		return FACTORY;
	}

	private static Socket startTLS(final Socket socket, final String host, final int port) throws IOException {
		final BufferedWriter output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		final BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		String inputLine;
		while ((inputLine = input.readLine()) != null && !inputLine.contains("* OK [CAPABILITY IMAP4")) {
			LOG.trace("#0", inputLine);
		}
		LOG.trace("#0", inputLine);
		output.write(". STARTTLS\r\n");
		output.flush();
		while ((inputLine = input.readLine()) != null && !inputLine.contains("OK Begin TLS")) {
			LOG.trace("#0", inputLine);
		}
		LOG.trace("#0", inputLine);
		// TODO
		return SSL_FACTORY.createSocket(socket, host, port, true);
	}

	private StartTLSSocketFactory() {
	}

	@Override
	public Socket createSocket(final String host, final int port) throws IOException, UnknownHostException {
		final Socket socket = new Socket(host, port);
		return startTLS(socket, host, port);
	}

	@Override
	public Socket createSocket(final String host, final int port, final InetAddress localHost, final int localPort)
			throws IOException, UnknownHostException {
		final Socket socket = new Socket(host, port, localHost, localPort);
		return startTLS(socket, host, port);
	}

	@Override
	public Socket createSocket(final InetAddress host, final int port) throws IOException {
		final Socket socket = new Socket(host, port);
		return startTLS(socket, host.getHostName(), port);
	}

	@Override
	public Socket createSocket(final InetAddress address, final int port, final InetAddress localAddress,
			final int localPort) throws IOException {
		final Socket socket = new Socket(address, port, localAddress, localPort);
		return startTLS(socket, address.getHostName(), port);
	}

}
