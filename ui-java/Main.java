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

        // -------- LOGIN UI --------
        VBox loginRoot = new VBox(15);
        loginRoot.setPadding(new Insets(30));
        loginRoot.setStyle("-fx-background-color: #0f2f2f;");

        Label title = new Label("Vault Login");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 20px;");

        PasswordField passwordField = new PasswordField();
        TextField visiblePasswordField = new TextField();

        passwordField.setPromptText("Master password");
        visiblePasswordField.setPromptText("Master password");

        visiblePasswordField.setManaged(false);
        visiblePasswordField.setVisible(false);

        passwordField.textProperty().bindBidirectional(visiblePasswordField.textProperty());

        // Toggle button
        Button toggle = new Button("Show");
        toggle.setStyle("-fx-background-color: #00bcd4; -fx-text-fill: white;");

        toggle.setOnAction(e -> {
            showPassword = !showPassword;

            if (showPassword) {
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
        unlock.setStyle("-fx-background-color: #00bcd4; -fx-text-fill: white; -fx-background-radius: 8;");

        Label forgot = new Label("Forgot password?");
        forgot.setStyle("-fx-text-fill: #80d8ff;");
        forgot.setOnMouseClicked(e -> {
            System.out.println("Forgot password clicked");
        });

        // -------- VAULT UI --------
        VBox vaultRoot = new VBox(15);
        vaultRoot.setPadding(new Insets(20));
        vaultRoot.setStyle("-fx-background-color: #0f2f2f;");

        Label vaultTitle = new Label("Vault");
        vaultTitle.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");

        Label placeholder = new Label("Passwords will show here");
        placeholder.setStyle("-fx-text-fill: white;");

        vaultRoot.getChildren().addAll(vaultTitle, placeholder);

        Scene vaultScene = new Scene(vaultRoot, 400, 300);

        // -------- LOGIN ACTION --------
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