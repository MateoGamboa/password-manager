import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
// import java.lang.classfile.Label;
import java.sql.*;
import java.util.*;

class PasswordItem {
    String service;
    String username;
    String password;
    int id;

    PasswordItem(String service, String username, String password, int id) {
        this.service = service;
        this.username = username;
        this.password = password;
        this.id = id;
    }
}

class VaultManager {

    private Connection conn;

    VaultManager() {
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:vault.db");

            Statement stmt = conn.createStatement();
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS passwords (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "service TEXT," +
                "username TEXT," +
                "password TEXT)"
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void add(String service, String username, String password) {
        try {
            System.out.println("INSERTING: " + service);

            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO passwords(service, username, password) VALUES (?, ?, ?)"
            );
            ps.setString(1, service);
            ps.setString(2, username);
            ps.setString(3, password);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    void delete(int id) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM passwords WHERE id=?"
            );
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void update(int id, String service, String username, String password) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE passwords SET service=?, username=?, password=? WHERE id=?"
            );
            ps.setString(1, service);
            ps.setString(2, username);
            ps.setString(3, password);
            ps.setInt(4, id);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    List<PasswordItem> getAll() {
        List<PasswordItem> list = new ArrayList<>();

        try {
            System.out.println("LOADING DATA...");

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM passwords");

            while (rs.next()) {
                System.out.println("FOUND: " + rs.getString("service"));

                list.add(new PasswordItem(
                        rs.getString("service"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getInt("id")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
        
    }
}

public class Main extends Application {

    private VaultManager vault;

    private VBox cardsContainer = new VBox(10);
    private Scene vaultScene;

    @Override
    public void start(Stage stage) {

        vault = new VaultManager();

        // sample data
        // vault.add("Gmail", "user1@gmail.com", "pass1");
        // vault.add("GitHub", "devUser", "pass2");

        // ================= LOGIN =================
        VBox loginRoot = new VBox(15);
        loginRoot.setPadding(new Insets(30));
        loginRoot.setStyle("-fx-background-color: #2b3a3a;");

        Label title = new Label("Vault Login");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 20px;");

        PasswordField passwordField = new PasswordField();
        TextField visiblePasswordField = new TextField();

        passwordField.setPromptText("Master password");
        visiblePasswordField.setPromptText("Master password");

        visiblePasswordField.setVisible(false);
        visiblePasswordField.setManaged(false);

        passwordField.textProperty().bindBidirectional(visiblePasswordField.textProperty());

        Button toggle = new Button("Show");

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

        Label error = new Label();
        error.setStyle("-fx-text-fill: red;");

        Label forgot = new Label("Forgot password?");
        forgot.setStyle("-fx-text-fill: #80d8ff;");

        Button unlock = new Button("Unlock");

        // ================= VAULT =================
        VBox vaultRoot = new VBox(10);
        vaultRoot.setPadding(new Insets(15));
        vaultRoot.setStyle("-fx-background-color: #2b3a3a;");

        Label vaultTitle = new Label("Your Vault");
        vaultTitle.setStyle("-fx-text-fill: white;");

        Button addBtn = new Button("+ Add Password");

        ScrollPane scrollPane = new ScrollPane(cardsContainer);
        scrollPane.setFitToWidth(true);

        vaultRoot.getChildren().addAll(vaultTitle, addBtn, scrollPane);

        vaultScene = new Scene(vaultRoot, 400, 400);

        renderCards();

        // ================= ADD =================
        addBtn.setOnAction(e -> {

            Stage popup = new Stage();

            VBox box = new VBox(10);
            box.setPadding(new Insets(15));

            TextField s = new TextField();
            TextField u = new TextField();
            PasswordField p = new PasswordField();

            s.setPromptText("Service");
            u.setPromptText("Username");
            p.setPromptText("Password");

            Button save = new Button("Save");

            save.setOnAction(ev -> {
                vault.add(s.getText(), u.getText(), p.getText());
                renderCards();
                popup.close();
            });

            box.getChildren().addAll(s, u, p, save);

            popup.setScene(new Scene(box, 250, 200));
            popup.show();
        });

        // ================= LOGIN =================
        unlock.setOnAction(e -> {
            if (passwordField.getText().equals("test")) {
                renderCards();
                stage.setScene(vaultScene);
            } else {
                error.setText("Wrong password");
            }
        });

        loginRoot.getChildren().addAll(title, passwordBox, unlock, error, forgot);

        Scene loginScene = new Scene(loginRoot, 350, 250);

        stage.setScene(loginScene);
        stage.show();
    }

    void renderCards() {
        cardsContainer.getChildren().clear();

        for (PasswordItem item : vault.getAll()) {

            VBox card = new VBox(8);
            card.setPadding(new Insets(10));
            card.setStyle("-fx-background-color: #3c4f4f;");

            Label service = new Label(item.service);
            Label username = new Label(item.username);
            Label password = new Label(item.password);

            Button edit = new Button("Edit");
            Button del = new Button("Delete");

            del.setOnAction(e -> {
                vault.delete(item.id);
                renderCards();
            });

            edit.setOnAction(e -> {

                Stage popup = new Stage();

                VBox box = new VBox(10);
                box.setPadding(new Insets(15));

                TextField s = new TextField(item.service);
                TextField u = new TextField(item.username);
                PasswordField p = new PasswordField();
                p.setText(item.password);

                Button save = new Button("Save");

                save.setOnAction(ev -> {
                    vault.update(item.id, s.getText(), u.getText(), p.getText());
                    renderCards();
                    popup.close();
                });

                box.getChildren().addAll(s, u, p, save);

                popup.setScene(new Scene(box, 250, 200));
                popup.show();
            });

            HBox actions = new HBox(10, edit, del);

            card.getChildren().addAll(service, username, password, actions);
            cardsContainer.getChildren().add(card);
        }
    }
}