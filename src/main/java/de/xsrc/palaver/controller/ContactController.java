package de.xsrc.palaver.controller;

import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import org.datafx.controller.FXMLController;
import org.datafx.controller.FxmlLoadException;
import org.datafx.controller.context.ApplicationContext;
import org.datafx.controller.flow.Flow;
import org.datafx.controller.flow.FlowException;
import org.datafx.controller.flow.action.BackAction;
import org.datafx.controller.flow.context.FXMLViewFlowContext;
import org.datafx.controller.flow.context.ViewFlowContext;
import org.datafx.controller.util.VetoException;
import org.jivesoftware.smack.util.StringUtils;

import de.jensd.fx.fontawesome.AwesomeDude;
import de.jensd.fx.fontawesome.AwesomeIcon;
import de.xsrc.palaver.model.Palaver;
import de.xsrc.palaver.provider.ContactProvider;
import de.xsrc.palaver.provider.PalaverProvider;
import de.xsrc.palaver.utils.Utils;
import de.xsrc.palaver.xmpp.ChatUtils;
import de.xsrc.palaver.xmpp.UiUtils;
import de.xsrc.palaver.xmpp.model.Contact;

@FXMLController("/fxml/ContactView.fxml")
public class ContactController {

	@FXML
	@BackAction
	private Button back;

	@FXML
	private Button addBuddy;

	@FXMLViewFlowContext
	private ViewFlowContext context;

	@FXML
	private Button startPalaverButton;

	@FXML
	private TextField searchInput;

	@FXML
	private ListView<Contact> list;

	private static final Logger logger = Logger.getLogger(ContactController.class
			.getName());

	@FXML
	private void initialize() {
		AwesomeDude.setIcon(back, AwesomeIcon.CHEVRON_LEFT, "20");

		HBox hbox = new HBox();
		hbox.setAlignment(Pos.CENTER);

		hbox.getChildren().add(AwesomeDude.createIconLabel(AwesomeIcon.PLUS, "24"));
		hbox.getChildren().add(AwesomeDude.createIconLabel(AwesomeIcon.USER, "24"));
		addBuddy.setGraphic(hbox);

		ContactProvider provider = ApplicationContext.getInstance()
				.getRegisteredObject(ContactProvider.class);
		list.setItems(provider.getData());
		list.setManaged(true);
		list.setCellFactory(new Callback<ListView<Contact>, ListCell<Contact>>() {
			@Override
			public ListCell<Contact> call(ListView<Contact> listView) {
				return new BuddyCell();
			}
		});

		searchInput.textProperty().addListener(new ChangeListener<String>() {
			public void changed(ObservableValue<? extends String> observable,
					String oldVal, String newVal) {
				handleSearchByKey(oldVal, newVal);
			}

		});

		AwesomeDude.setIcon(startPalaverButton, AwesomeIcon.SEARCH, "20");
		Platform.runLater(() -> searchInput.requestFocus());
	}

	ObservableList<String> entries = FXCollections.observableArrayList();

	public void handleSearchByKey(String oldVal, String newVal) {
		// If the number of characters in the text box is less than last time
		// it must be because the user pressed delete
		if (oldVal != null && (newVal.length() < oldVal.length())) {
			// Restore the lists original set of entries
			// and start from the beginning
			list.setItems(ChatUtils.getContacts());
		}

		// Change to upper case so that case is not an issue
		newVal = newVal.toUpperCase();

		// Filter out the entries that don't contain the entered text
		ObservableList<Contact> subentries = FXCollections.observableArrayList();
		for (Contact entry : list.getItems()) {
			Contact entryText = entry;
			if (entryText.toString().toUpperCase().contains(newVal)) {
				subentries.add(entryText);
			}
		}
		list.setItems(subentries);
		list.getSelectionModel().select(0);

	}

	@FXML
	private void startPalaverAction() throws VetoException, FlowException {
		Contact buddy = list.getSelectionModel().getSelectedItems().get(0);
		PalaverProvider provider = ApplicationContext.getInstance()
				.getRegisteredObject(PalaverProvider.class);
		if (buddy != null) {
			logger.fine("Starting palaver with " + buddy.getJid());
			String recipient = StringUtils.parseBareAddress(buddy.getJid());
			Palaver p = provider.getById(buddy.getAccount(), recipient);
			if (p == null) {
				logger.finer("Palaver does not exists");
				p = new Palaver(buddy.getAccount(), recipient);
				provider.getData().add(p);
			} else {
				p.setClosed(false);
			}
			logger
					.finer(p.getAccount() + " started palaver with " + p.getRecipient());
		}
		UiUtils.getFlowHandler(context).navigateBack();
	}

	@FXML
	private void addContactAction() throws FxmlLoadException {
		Flow f = new Flow(AddContactController.class);
		try {
			Utils.getDialog(f, null).show();
		} catch (FlowException e) {
			e.printStackTrace();
		}
	}

}