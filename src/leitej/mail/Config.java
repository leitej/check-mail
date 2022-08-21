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

import leitej.util.data.Obfuscate;
import leitej.xml.om.XmlObjectModelling;

/**
 * @author julio
 *
 */
public interface Config extends XmlObjectModelling {

	String getHostname();

	void setHostname(String hostname);

	int getPort();

	void setPort(int port);

	Boolean getStartTLS();

	void setStartTLS(Boolean startTLS);

	String getUsername();

	void setUsername(String username);

	@Obfuscate(hostnameSalt = true, usernameSalt = true)
	String getPassword();

	void setPassword(String password);

	long getRefreshIntervalMS();

	void setRefreshIntervalMS(long refreshIntervalMS);

	int getMaxConnections();

	void setMaxConnections(int maxConnections);

	String getStoppedIcon();

	void setStoppedIcon(String stoppedIcon);

	String getNoMailIcon();

	void setNoMailIcon(String noMailIcon);

	String getHasMailIcon();

	void setHasMailIcon(String hasMailIcon);

	boolean isDisableTrayIcon();

	void setDisableTrayIcon(boolean disableTrayIcon);

}
