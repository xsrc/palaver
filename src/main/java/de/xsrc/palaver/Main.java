package de.xsrc.palaver;

import java.util.List;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.stage.Stage;

import org.datafx.controller.context.ApplicationContext;
import org.datafx.controller.flow.Flow;
import org.datafx.controller.flow.FlowException;

import de.xsrc.palaver.controller.MainController;
import de.xsrc.palaver.model.Account;
import de.xsrc.palaver.provider.AccountProvider;
import de.xsrc.palaver.utils.Storage;
import de.xsrc.palaver.xmpp.ChatUtils;
import de.xsrc.palaver.xmpp.UiUtils;

public class Main extends Application {

	private static final Logger logger = Logger.getLogger(Storage.class
			.getName());

	@Override
	public void start(Stage primaryStage) throws FlowException {
		AccountProvider accounts = new AccountProvider();
		ApplicationContext.getInstance().register(accounts);

		Platform.runLater(() -> {
			accounts.retrieve();
			handleXmpp(accounts.getData().get());
		});

		Flow flow = new Flow(MainController.class);
		Scene scene = UiUtils.prepareFlow(flow, null);

		primaryStage.setScene(scene);

		primaryStage.show();
	}

	private void handleXmpp(ObservableList<Account> accountList) {
		accountList.addListener(new ListChangeListener<Account>() {

			@Override
			public void onChanged(
					javafx.collections.ListChangeListener.Change<? extends Account> c) {
				while (c.next()) {
					if (c.getAddedSize() > 0) {
						List<? extends Account> list = c.getAddedSubList();
						for (Account account : list) {
							logger.fine("Connection account " + account);
							ChatUtils.getConnection(account);
							logger.info("Connected account: " + account);
						}
					}
				}

			}
		});

	}

	public static void main(String[] args) {

		launch(args);
	}
}
