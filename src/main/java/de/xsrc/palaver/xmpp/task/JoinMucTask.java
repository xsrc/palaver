package de.xsrc.palaver.xmpp.task;

import de.xsrc.palaver.beans.Palaver;
import de.xsrc.palaver.utils.Utils;
import de.xsrc.palaver.xmpp.ConnectionManager;
import de.xsrc.palaver.xmpp.listeners.MucListener;
import org.datafx.concurrent.DataFxTask;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.bookmarks.BookmarkManager;
import org.jivesoftware.smackx.muc.MultiUserChat;

import java.util.logging.Logger;


public class JoinMucTask extends DataFxTask {

	private final static Logger logger = Logger.getLogger(JoinMucTask.class.getName());

	private Palaver palaver;
	private XMPPConnection connection;

	public JoinMucTask(Palaver palaver, XMPPConnection connection) {
		this.palaver = palaver;
		this.connection = connection;
		this.updateTitle("Joining Conference " + palaver.getRecipient());

	}

	public JoinMucTask(Palaver palaver) {
		this.palaver = palaver;
		this.updateTitle("Joining Conference " + palaver.getRecipient());
		this.connection = ConnectionManager.getConnection(palaver.getAccount());
	}


	@Override
	protected MultiUserChat call() {

		MultiUserChat muc = new MultiUserChat(connection, palaver.getRecipient());
		try {
			muc.createOrJoin(StringUtils.parseName(palaver.getRecipient()));
			muc.addMessageListener(new MucListener(palaver));
			// TODO Implement Subject fetching and setting as Contact name
			Utils.getJoinedMucs().put(palaver.getId(), muc);
			logger.info(String.format("Joined %s with account %s", palaver.getRecipient(), palaver.getAccount()));
			try {
				BookmarkManager bookmarkManager = BookmarkManager.getBookmarkManager(connection);
				bookmarkManager.addBookmarkedConference(StringUtils.parseName(palaver.getRecipient()), palaver.getRecipient(), true, StringUtils.parseName(palaver.getAccount()), null);
			} catch (XMPPException e) {
				e.printStackTrace();
			}
			return muc;
		} catch (XMPPException.XMPPErrorException | SmackException e) {
			e.printStackTrace();
			logger.warning(String.format("Failed joining %s with account %s", palaver.getRecipient(), palaver.getAccount()));
			return null;
		}

	}
}
