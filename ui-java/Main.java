import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

class PasswordItem {
    String service;
    String username;
    String password;

    PasswordItem(String service, String username, String password) {
        this.service = service;
        this.username = username;
        this.password = password;
    }
}

public class Main extends Application {

    private List<PasswordItem> items = new ArrayList<>();

    private VBox cardsContainer = new VBox(10);
    private Scene vaultScene;

    @Override
    public void start(Stage stage) {

        // sample data
        items.add(new PasswordItem("Gmail", "user1@gmail.com", "••••••"));
        items.add(new PasswordItem("GitHub", "devUser", "••••••"));
        items.add(new PasswordItem("Netflix", "mateo@mail.com", "••••••"));

        // ================= LOGIN UI =================
        VBox loginRoot = new VBox(15);
        loginRoot.setPadding(new Insets(30));
        loginRoot.setStyle("-fx-background-color: #2b3a3a;");

        Label title = new Label("Vault Login");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 20px;");

        PasswordField passwordField = new PasswordField();
        TextField visiblePasswordField = new TextField();

        passwordField.setPromptText("Master password");
        visiblePasswordField.setPromptText("Master password");

        visiblePasswordField.setManaged(false);
        visiblePasswordField.setVisible(false);

        passwordField.textProperty().bindBidirectional(visiblePasswordField.textProperty());

        Button toggle = new Button("Show");
        toggle.setStyle("-fx-background-color: #00bcd4; -fx-text-fill: white;");

        final boolean[] show = {false};

        toggle.setOnAction(e -> {
            show[0] = !show[0];

            passwordField.setVisible(!show[0]);
            passwordField.setManaged(!show[0]);

            visiblePasswordField.setVisible(show[0]);
            visiblePasswordField.setManaged(show[0]);

            toggle.setText(show[0] ? "Hide" : "Show");
        });

        HBox passwordBox = new HBox(10, passwordField, visiblePasswordField, toggle);

        Label error = new Label("");
        error.setStyle("-fx-text-fill: red;");

        Label forgot = new Label("Forgot password?");
        forgot.setStyle("-fx-text-fill: #80d8ff;");

        forgot.setOnMouseClicked(e -> {
            System.out.println("Forgot password clicked");
        });

        Button unlock = new Button("Unlock");
        unlock.setStyle("-fx-background-color: #00bcd4; -fx-text-fill: white;");

        // ================= VAULT UI =================
        VBox vaultRoot = new VBox(10);
        vaultRoot.setPadding(new Insets(15));
        vaultRoot.setStyle("-fx-background-color: #2b3a3a;");

        Label vaultTitle = new Label("Your Vault");
        vaultTitle.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");

        Button addBtn = new Button("+ Add Password");
        addBtn.setStyle("-fx-background-color: #00bcd4; -fx-text-fill: white;");

        ScrollPane scrollPane = new ScrollPane(cardsContainer);
        scrollPane.setFitToWidth(true);

        vaultRoot.getChildren().addAll(vaultTitle, addBtn, scrollPane);

        vaultScene = new Scene(vaultRoot, 400, 400);

        renderCards();

        // ================= ADD PASSWORD =================
        addBtn.setOnAction(e -> {

            Stage popup = new Stage();
            popup.setTitle("Add Password");

            VBox box = new VBox(10);
            box.setPadding(new Insets(15));

            TextField serviceField = new TextField();
            serviceField.setPromptText("Service");

            TextField userField = new TextField();
            userField.setPromptText("Username");

            PasswordField passField = new PasswordField();
            passField.setPromptText("Password");

            Button save = new Button("Save");

            save.setOnAction(ev -> {

                items.add(new PasswordItem(
                        serviceField.getText(),
                        userField.getText(),
                        passField.getText()
                ));

                renderCards();
                popup.close();
            });

            box.getChildren().addAll(serviceField, userField, passField, save);

            popup.setScene(new Scene(box, 250, 200));
            popup.show();
        });

        // ================= LOGIN ACTION =================
        unlock.setOnAction(e -> {
            if (passwordField.getText().equals("test")) {
                stage.setScene(vaultScene);
            } else {
                error.setText("Wrong password");
            }
        });

        loginRoot.getChildren().addAll(title, passwordBox, unlock, error, forgot);

        Scene loginScene = new Scene(loginRoot, 350, 250);

        stage.setScene(loginScene);
        stage.setTitle("Password Manager");
        stage.show();
    }

    void renderCards() {
        cardsContainer.getChildren().clear();

        for (PasswordItem item : items) {

            VBox card = new VBox(5);
            card.setPadding(new Insets(10));
            card.setStyle("-fx-background-color: #3c4f4f; -fx-background-radius: 10;");

            Label service = new Label(item.service);
            service.setStyle("-fx-text-fill: white;");

            Label username = new Label(item.username);
            username.setStyle("-fx-text-fill: #cccccc;");

            Label password = new Label(item.password);
            password.setStyle("-fx-text-fill: #00bcd4;");

            card.getChildren().addAll(service, username, password);
            cardsContainer.getChildren().add(card);
        }
    }
}