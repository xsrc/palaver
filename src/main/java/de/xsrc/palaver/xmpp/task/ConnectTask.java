package de.xsrc.palaver.xmpp.task;

import de.xsrc.palaver.beans.Account;
import de.xsrc.palaver.beans.Contact;
import de.xsrc.palaver.models.ContactModel;
import de.xsrc.palaver.utils.Utils;
import de.xsrc.palaver.xmpp.listeners.MsgListener;
import de.xsrc.palaver.xmpp.listeners.PalaverRosterListener;
import org.datafx.concurrent.DataFxTask;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.DirectoryRosterStore;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.RosterPacket;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.carbons.CarbonManager;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.util.logging.Logger;

public class ConnectTask extends DataFxTask<XMPPConnection> {

	private static final Logger logger = Logger.getLogger(ConnectTask.class
					.getName());
	private final Account account;

	public ConnectTask(Account account) {
		super();
		this.account = account;
		this.updateTitle("Connecting to " + account.getJid());
	}

	private static ConnectionConfiguration configureConnection(String server) {
		ConnectionConfiguration config = new ConnectionConfiguration(server);
		SSLContext context;
		try {
			context = getContext();
			config.setCustomSSLContext(context);
			config.setRosterLoadedAtLogin(true);
//		config.setDebuggerEnabled(true);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return config;
	}

	protected static SSLContext getContext() throws Exception {
		try {
			SSLContext sslContext = SSLContext.getInstance("SSL");
			sslContext
							.init(null, getTrustManager(), new java.security.SecureRandom());

			return sslContext;
		} catch (Exception e) {
			throw new Exception("Error creating context for SSLSocket!", e);
		}
	}

	/**
	 * Create a trust manager that trusts all certificates It is not using a
	 * particular keyStore
	 */
	protected static TrustManager[] getTrustManager() {

		return new TrustManager[]{new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			public void checkClientTrusted(
							java.security.cert.X509Certificate[] certs, String authType) {
			}

			public void checkServerTrusted(
							java.security.cert.X509Certificate[] certs, String authType) {
			}
		}};
	}

	@Override
	protected XMPPConnection call() throws Exception {
		logger.finer("Connecting to account " + account);
		String jid = account.getJid();

		ConnectionConfiguration config = configureConnection(StringUtils
						.parseServer(jid));

		DirectoryRosterStore directoryRosterStore = Utils.getRosterStore(account);
		config.setRosterStore(directoryRosterStore);
		// TODO: move this logic some where else

		XMPPConnection connection = new XMPPTCPConnection(config);


		connection.connect();

		connection.login(StringUtils.parseName(jid), account.getPassword(), "Palaver");
		CarbonManager.getInstanceFor(connection).enableCarbons();
		connection.getRoster().addRosterListener(new PalaverRosterListener(account));
		connection.addPacketListener(new MsgListener(account), new MessageTypeFilter(Message.Type.chat));
		connection.addPacketSendingListener(new MsgListener(account), new MessageTypeFilter(Message.Type.chat));

		for (RosterPacket.Item item : directoryRosterStore.getEntries()) {
			Contact contact = Utils.createContact(account.getJid(), item.getUser(), item.getName(), false
			);
			ContactModel.getInstance().addContact(contact);
		}
//				BookmarkManager bookmarkManager = BookmarkManager.getBookmarkManager(connection);
//				for (BookmarkedConference bookmarkedConference : bookmarkManager.getBookmarkedConferences()) {
//					Contact contact = Utils.createContact(account.getJid(), bookmarkedConference.getJid(), bookmarkedConference.getName(), true);
//					ContactModel.getInstance().addContact(contact);
//				}
		return connection;
	}
}