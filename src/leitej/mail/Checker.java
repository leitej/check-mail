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

import leitej.exception.AgnosticThreadLtException;
import leitej.exception.GuiLtException;
import leitej.exception.IllegalArgumentLtRtException;
import leitej.exception.ObjectPoolLtException;
import leitej.exception.SeppukuLtRtException;
import leitej.exception.XmlInvalidLtException;
import leitej.gui.LtTrayIcon;
import leitej.log.Logger;
import leitej.thread.ThreadPriorityEnum;
import leitej.thread.XAgnosticThread;
import leitej.thread.XThreadData;
import leitej.util.AgnosticUtil;
import leitej.util.data.DateFieldEnum;
import leitej.util.data.Invoke;
import leitej.util.data.InvokeSignature;
import leitej.util.data.TimeTriggerImpl;
import leitej.xml.om.Xmlom;

/**
 * @author julio
 *
 */
public final class Checker {

	private static final Logger LOG = Logger.getInstance();

	private static final Config CONFIG;
	private static MailConnectionPool MAIL_CONN_POOL;
	private static final XAgnosticThread XA_THREAD;
	private static final LtTrayIcon TRAY_ICON;
	private static final String METHOD_STOP = "stop";
	private static final String METHOD_START = "start";
	private static final String METHOD_REFRESH = "refresh";
	private static final String METHOD_QUIT = "quit";
	private static final String ICON_STOPPED;
	private static final String ICON_NO_MAIL;
	private static final String ICON_HAS_MAIL;
	private static volatile boolean STOPPED;
	private static volatile boolean HAS_MAIL;

	static {
		XA_THREAD = new XAgnosticThread(true);
		STOPPED = true;
		HAS_MAIL = false;
		try {
			// load configuration
			try {
				CONFIG = Xmlom.getConfig(Config.class, new Config[] { configSample() }).get(0);
			} catch (SecurityException | NullPointerException | XmlInvalidLtException | NegativeArraySizeException
					| IOException e) {
				throw new SeppukuLtRtException(e);
			}
			// set icons locations
			ICON_STOPPED = CONFIG.getStoppedIcon();
			ICON_NO_MAIL = CONFIG.getNoMailIcon();
			ICON_HAS_MAIL = CONFIG.getHasMailIcon();
			// system tray icon
			TRAY_ICON = new LtTrayIcon("check-mail");
			TRAY_ICON.registerImage(ICON_STOPPED, ICON_STOPPED);
			TRAY_ICON.registerImage(ICON_NO_MAIL, ICON_NO_MAIL);
			TRAY_ICON.registerImage(ICON_HAS_MAIL, ICON_HAS_MAIL);
			TRAY_ICON.activeImage(ICON_STOPPED);
			TRAY_ICON.addMenuItem("stop",
					new InvokeSignature(Checker.class, AgnosticUtil.getMethod(Checker.class, METHOD_STOP)));
			TRAY_ICON.addMenuItem("quit",
					new InvokeSignature(Checker.class, AgnosticUtil.getMethod(Checker.class, METHOD_QUIT)));
			TRAY_ICON
					.setActionListener(new InvokeSignature(Checker.class, AgnosticUtil.getMethod(Checker.class, METHOD_START)));
			TRAY_ICON.unhide();
			// load scheduled refresh method
			XA_THREAD.workOn(new XThreadData(new Invoke(Checker.class, AgnosticUtil.getMethod(Checker.class, METHOD_REFRESH)),
					new TimeTriggerImpl(DateFieldEnum.SECOND, Long.valueOf(CONFIG.getRefreshIntervalMS() / 1000).intValue()),
					METHOD_REFRESH, ThreadPriorityEnum.NORMAL));
		} catch (final GuiLtException | InterruptedException | IllegalArgumentLtRtException | SecurityException
				| NoSuchMethodException | AgnosticThreadLtException e) {
			throw new SeppukuLtRtException(e);
		}
	}

	private static final Config configSample() {
		final Config config = Xmlom.newInstance(Config.class);
		config.setHostname("imap.hostname.com");
		config.setPort(993);
		config.setUsername("user@hostname.com");
		config.setPassword("12341234");
		config.setRefreshIntervalMS(120000);
		config.setMaxConnections(4);
		config.setStoppedIcon("./stopped.png");
		config.setNoMailIcon("./no_mail.png");
		config.setHasMailIcon("./has_mail.png");
		return config;
	}

	public static final void stop() throws InterruptedException, ObjectPoolLtException {
		LOG.debug("init");
		synchronized (Checker.class) {
			if (!STOPPED) {
				STOPPED = true;
				MAIL_CONN_POOL.close();
				try {
					TRAY_ICON.activeImage(ICON_STOPPED);
				} catch (final GuiLtException e) {
					LOG.error("#0", e);
				}
				LOG.info("stop");
			}
		}
	}

	public static final void start() throws InterruptedException {
		LOG.debug("init");
		synchronized (Checker.class) {
			if (STOPPED) {
				STOPPED = false;
				MAIL_CONN_POOL = new MailConnectionPool(CONFIG);
				try {
					TRAY_ICON.activeImage(ICON_NO_MAIL);
				} catch (final GuiLtException e) {
					LOG.error("#0", e);
				}
				LOG.info("start");
				HAS_MAIL = false;
			}
			refresh();
		}
	}

	public static final void refresh() throws InterruptedException {
		LOG.debug("init");
		if (!STOPPED) {
			MailConnection conn = null;
			try {
				conn = MAIL_CONN_POOL.poll();
				synchronized (Checker.class) {
					final boolean hasMail = conn.hasUnread();
					LOG.debug("hasUnread: #0", hasMail);
					if (HAS_MAIL != hasMail) {
						HAS_MAIL = hasMail;
						if (HAS_MAIL) {
							TRAY_ICON.activeImage(ICON_HAS_MAIL);
							LOG.info("has_mail");
						} else {
							TRAY_ICON.activeImage(ICON_NO_MAIL);
							LOG.info("no_mail");
						}
					}
				}
			} catch (final Exception e) {
				LOG.error("#0", e);
			} finally {
				LOG.debug("finally");
				if (conn != null) {
					MAIL_CONN_POOL.offer(conn);
				}
			}
		}
	}

	public static final void quit() {
		LOG.debug("init");
		XA_THREAD.closeAsync();
	}

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		try {
			LOG.info("join");
			XA_THREAD.join();
		} catch (final Exception e) {
			throw new SeppukuLtRtException(e);
		} finally {
			LOG.debug("finally");
			try {
				LOG.debug("close");
				TRAY_ICON.close();
				stop();
			} catch (final GuiLtException | InterruptedException | ObjectPoolLtException e) {
				LOG.error("#0", e);
			}
		}
		System.exit(0);
	}

}
