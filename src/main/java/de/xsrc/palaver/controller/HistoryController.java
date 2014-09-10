package de.xsrc.palaver.controller;

import de.xsrc.palaver.beans.HistoryEntry;
import de.xsrc.palaver.beans.Palaver;
import de.xsrc.palaver.controls.HistoryEntryCell;
import de.xsrc.palaver.utils.UiUtils;
import de.xsrc.palaver.xmpp.PalaverManager;
import javafx.application.Platform;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.datafx.concurrent.ObservableExecutor;
import org.datafx.controller.context.ApplicationContext;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public class HistoryController extends BorderPane {
	private static final Logger logger = Logger
					.getLogger(HistoryController.class.getName());
	private static final String HISTORY_VIEW_FXML = "/fxml/HistoryView.fxml";
	@FXML
	private TextArea chatInput;
	@FXML
	private VBox historyBox;
	@FXML
	private ScrollPane scrollPane;

	private Palaver Mpalaver;
	private ObservableList<HistoryEntry> history;
	private Palaver palaver;

	public HistoryController(Palaver palaver) {
		this.palaver = palaver;
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(HISTORY_VIEW_FXML));
		fxmlLoader.setResources(UiUtils.getRessourceBundle());
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);

		try {
			fxmlLoader.load();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}

	@FXML
	public void initialize() {
		history = palaver.history.entryListProperty();
		add(history);
		history.addListener((Change<? extends HistoryEntry> change) -> {
			while (change.next()) {
				logger.finer("New Messages were added to " + palaver);
				add(change.getAddedSubList());
			}
		});
		historyBox.heightProperty().addListener((observable, oldValue, newValue) -> {
			Platform.runLater(() -> scrollPane.setVvalue(scrollPane.getVmax()));
			palaver.setUnread(false);
		});
		requestFocus();

	}

	private void add(List<? extends HistoryEntry> list) {
		ObservableExecutor executor = ApplicationContext.getInstance().getRegisteredObject(ObservableExecutor.class);
		executor.submit(() -> {
							for (HistoryEntry historyEntry : list) {
								Label l = new Label();
								Platform.runLater(() -> historyBox.getChildren().add(new HistoryEntryCell(historyEntry, palaver.isConference())));

							}
						}
		);
	}

	@FXML
	private void sendMsgAction() throws NotConnectedException, XMPPException {
		String body = chatInput.getText();

		PalaverManager.sendMsg(this.palaver, body);
		chatInput.clear();
	}

	public void requestFocus() {
		Platform.runLater(() -> {
			chatInput.requestFocus();
			chatInput.positionCaret(chatInput.getLength());
		});

	}
}
