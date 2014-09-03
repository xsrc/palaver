package de.xsrc.palaver.controller;

import de.jensd.fx.fontawesome.AwesomeDude;
import de.jensd.fx.fontawesome.AwesomeIcon;
import de.xsrc.palaver.beans.Palaver;
import de.xsrc.palaver.provider.PalaverProvider;
import de.xsrc.palaver.utils.Utils;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.BorderPane;
import org.datafx.concurrent.ObservableExecutor;
import org.datafx.control.cell.ServiceListCellFactory;
import org.datafx.controller.FXMLController;
import org.datafx.controller.ViewFactory;
import org.datafx.controller.context.ApplicationContext;
import org.datafx.controller.context.ViewContext;
import org.datafx.controller.flow.action.LinkAction;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smackx.muc.MultiUserChat;

import java.util.HashMap;

@FXMLController("/fxml/MainView.fxml")
public class MainController {

	@FXML
	@LinkAction(AccountController.class)
	private Button showAccountsButton;

	@FXML
	@LinkAction(ContactController.class)
	private Button showBuddyListButton;

	@FXML
	private ListView<Palaver> palaverListView;

	private HashMap<Palaver, ViewContext<HistoryController>> historyMap = new HashMap<Palaver, ViewContext<HistoryController>>();

	@FXML
	private BorderPane borderPane;

	@FXML
	private Button hidePalaverButton;

	private Node palaverListTmp;

	@FXML
	private void initialize() {

		PalaverProvider provider = ApplicationContext.getInstance()
						.getRegisteredObject(PalaverProvider.class);
				palaverListView.setItems(provider.getData());
		palaverListView
						.setCellFactory(listView -> new PalaverCell());

		MultipleSelectionModel<Palaver> selModel = palaverListView
						.getSelectionModel();

		selModel.setSelectionMode(SelectionMode.SINGLE);

		selModel.selectedItemProperty().addListener(
						(ObservableValue<? extends Palaver> observable, Palaver oldValue,
						 Palaver newValue) -> {
							if (newValue != null) {
								newValue.setUnread(false);
								if (!historyMap.containsKey(newValue)) {
									try {
										ViewContext<HistoryController> context = ViewFactory
														.getInstance().createByController(HistoryController.class);
										context.getController().setPalaver(newValue);
										historyMap.put(newValue, context);
									} catch (Exception e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}

								borderPane.setCenter(historyMap.get(newValue).getRootNode());
								historyMap.get(newValue).getController().requestFocus();
							}
						});
		AwesomeDude.setIcon(showAccountsButton, AwesomeIcon.GEAR, "24");
		AwesomeDude.setIcon(hidePalaverButton, AwesomeIcon.CHEVRON_LEFT, "24");
		AwesomeDude.setIcon(showBuddyListButton, AwesomeIcon.USERS, "24");

	}

	@FXML
	private void hidePalaverList() {
		if (palaverListTmp == null) {
			palaverListTmp = borderPane.getLeft();
			borderPane.setLeft(null);
			hidePalaverButton.setGraphic(AwesomeDude
							.createIconLabel(AwesomeIcon.CHEVRON_RIGHT, "24"));
		} else {
			borderPane.setLeft(palaverListTmp);
			palaverListTmp = null;
			hidePalaverButton.setGraphic(AwesomeDude
							.createIconLabel(AwesomeIcon.CHEVRON_LEFT, "24"));
		}
	}

	@FXML
	private void removeAction() {
		Palaver p = palaverListView.getSelectionModel().getSelectedItem();
		if (p.getConference()) {
			MultiUserChat muc = Utils.getMuc(p);
			try {
				muc.leave();
			} catch (SmackException.NotConnectedException e) {
				e.printStackTrace();
			}
		}
		palaverListView.getItems().remove(p);
		historyMap.remove(p);
		p.setClosed(true);
	}
}
