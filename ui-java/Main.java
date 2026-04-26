import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class Main extends Application {

    private boolean showPassword = false;

   @Override
    public void start(Stage stage) {

        // ================= LOGIN UI =================
        VBox loginRoot = new VBox(15);
        loginRoot.setPadding(new Insets(30));
        loginRoot.setStyle("-fx-background-color: #2b3a3a;"); // bluish gray teal

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

        final boolean[] showPassword = {false};

        toggle.setOnAction(e -> {
            showPassword[0] = !showPassword[0];

            if (showPassword[0]) {
                passwordField.setVisible(false);
                passwordField.setManaged(false);

                visiblePasswordField.setVisible(true);
                visiblePasswordField.setManaged(true);

                toggle.setText("Hide");
            } else {
                passwordField.setVisible(true);
                passwordField.setManaged(true);

                visiblePasswordField.setVisible(false);
                visiblePasswordField.setManaged(false);

                toggle.setText("Show");
            }
        });

        HBox passwordBox = new HBox(10, passwordField, visiblePasswordField, toggle);

        Label error = new Label("");
        error.setStyle("-fx-text-fill: red;");

        Button unlock = new Button("Unlock");
        unlock.setStyle("-fx-background-color: #00bcd4; -fx-text-fill: white;");

        Label forgot = new Label("Forgot password?");
        forgot.setStyle("-fx-text-fill: #80d8ff;");

        // ================= VAULT UI =================

        VBox vaultRoot = new VBox(10);
        vaultRoot.setPadding(new Insets(15));
        vaultRoot.setStyle("-fx-background-color: #2b3a3a;");

        Label vaultTitle = new Label("Your Vault");
        vaultTitle.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");

        VBox cardsContainer = new VBox(10);

        // fake data (for now)
        for (int i = 0; i < 5; i++) {
            VBox card = new VBox(5);
            card.setPadding(new Insets(10));
            card.setStyle("-fx-background-color: #3c4f4f; -fx-background-radius: 10;");

            Label service = new Label("Service " + (i + 1));
            service.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

            Label username = new Label("user" + i + "@mail.com");
            username.setStyle("-fx-text-fill: #cccccc;");

            Label password = new Label("••••••••");
            password.setStyle("-fx-text-fill: #00bcd4;");

            card.getChildren().addAll(service, username, password);

            cardsContainer.getChildren().add(card);
        }

        ScrollPane scrollPane = new ScrollPane(cardsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        vaultRoot.getChildren().addAll(vaultTitle, scrollPane);

        Scene vaultScene = new Scene(vaultRoot, 400, 400);

        // ================= LOGIN ACTION =================
        unlock.setOnAction(e -> {
            String input = passwordField.getText();

            if (input.equals("test")) {
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
}