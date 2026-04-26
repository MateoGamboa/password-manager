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

        VBox root = new VBox(15);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #0f2f2f;");

        Label title = new Label("Vault Login");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 20px;");

        PasswordField passwordField = new PasswordField();
        TextField visiblePasswordField = new TextField();

        passwordField.setPromptText("Master password");
        visiblePasswordField.setPromptText("Master password");

        visiblePasswordField.setManaged(false);
        visiblePasswordField.setVisible(false);

        // keep both fields synced
        passwordField.textProperty().bindBidirectional(visiblePasswordField.textProperty());

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

        Button unlock = new Button("Unlock");
        unlock.setStyle("-fx-background-color: #00bcd4; -fx-text-fill: white; -fx-background-radius: 8;");

        Label error = new Label("");
        error.setStyle("-fx-text-fill: red;");

        unlock.setOnAction(e -> {
            String input = passwordField.getText();

            if (input.equals("test")) {
                error.setText("Unlocked");
                System.out.println("SUCCESS LOGIN");
            } else {
                error.setText("Wrong password");
            }
        });

        Label forgot = new Label("Forgot password?");
        forgot.setStyle("-fx-text-fill: #80d8ff;");

        forgot.setOnMouseClicked(e -> {
            System.out.println("Forgot password clicked");
        });

        HBox passwordBox = new HBox(10, passwordField, visiblePasswordField, toggle);

        root.getChildren().addAll(title, passwordBox, unlock, error, forgot);

        Scene scene = new Scene(root, 350, 250);
        stage.setTitle("Password Manager");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}