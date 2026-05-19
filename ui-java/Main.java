import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.sql.*;
import java.util.*;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import java.util.Base64;

import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.geometry.Pos;

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
    private static final String KEY = "1234567890123456"; // 16 chars = AES key
    private String currentKey;

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

            stmt.execute(
                "CREATE TABLE IF NOT EXISTS master (" +
                "password_hash TEXT)"
            );

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private String hash(String input) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hashed = md.digest(input.getBytes());

            StringBuilder sb = new StringBuilder();
            for (byte b : hashed) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    boolean hasMasterPassword() {
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM master");
            return rs.next();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    void setMasterPassword(String password) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO master(password_hash) VALUES (?)"
            );
            ps.setString(1, hash(password));
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    boolean verifyMasterPassword(String input) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT password_hash FROM master"
            );
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String stored = rs.getString("password_hash");
                return stored.equals(hash(input));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    void setKey(String password) {
        this.currentKey = password;
    }

    private String fixKey(String key) {
        if (key.length() >= 16) return key.substring(0, 16);

        StringBuilder sb = new StringBuilder(key);
        while (sb.length() < 16) {
            sb.append("0");
        }
        return sb.toString();
    }
    
    private String encrypt(String data) {
        try {
            SecretKeySpec key = new SecretKeySpec(fixKey(currentKey).getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);

            byte[] encrypted = cipher.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String decrypt(String data) {
        try {
            SecretKeySpec key = new SecretKeySpec(fixKey(currentKey).getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);

            byte[] decoded = Base64.getDecoder().decode(data);
            return new String(cipher.doFinal(decoded));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    void add(String service, String username, String password) {
        try {
            System.out.println("INSERTING: " + service);

            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO passwords(service, username, password) VALUES (?, ?, ?)"
            );
            ps.setString(1, service);
            ps.setString(2, username);
            ps.setString(3, encrypt(password));
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
            ps.setString(3, encrypt(password));
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
                        decrypt(rs.getString("password")),
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
    private TextField searchField;

    @Override
    public void start(Stage stage) {

        vault = new VaultManager();

        // sample data
        // vault.add("Gmail", "user1@gmail.com", "pass1");
        // vault.add("GitHub", "devUser", "pass2");

        // ================= LOGIN =================
        VBox loginRoot = new VBox();
        loginRoot.setAlignment(javafx.geometry.Pos.CENTER);
        loginRoot.setStyle("-fx-background-color: #30444a;"); 

        VBox loginCard = new VBox(20);
        loginCard.setAlignment(javafx.geometry.Pos.CENTER);
        loginCard.setPadding(new Insets(40));
        loginCard.setPrefWidth(380);

        loginCard.setStyle(
            "-fx-background-color: #3a4f55;" +
            "-fx-background-radius: 18;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.35), 18, 0.2, 0, 4);"
        );

        // ===== LOGO =====
        Circle outer = new Circle(32);
        outer.setStyle(
            "-fx-fill: transparent;" +
            "-fx-stroke: #67d5ff;" +
            "-fx-stroke-width: 3;"
        );

        Circle inner = new Circle(6);
        inner.setStyle(
            "-fx-fill: #67d5ff;"
        );

        Line divider = new Line(0, -18, 0, 18);
        divider.setStyle(
            "-fx-stroke: #67d5ff;" +
            "-fx-stroke-width: 3;"
        );

        StackPane logo = new StackPane(
            outer,
            divider,
            inner
        );

        logo.setPrefSize(80, 80);

        // ===== TITLE =====
        Label title = new Label("Password Manager");
        title.setStyle(
            "-fx-text-fill: white;" +
            "-fx-font-size: 26px;" +
            "-fx-font-weight: bold;"
        );

        // ===== PASSWORD FIELD =====
        PasswordField passwordField = new PasswordField();
        TextField visiblePasswordField = new TextField();

        passwordField.setPromptText("Master Password");
        visiblePasswordField.setPromptText("Master Password");

        passwordField.setPrefHeight(42);
        visiblePasswordField.setPrefHeight(42);

        passwordField.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #1e1e1e;" +
            "-fx-prompt-text-fill: #7d8b8f;"
        );

        visiblePasswordField.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #1e1e1e;" +
            "-fx-prompt-text-fill: #7d8b8f;"
        );

        visiblePasswordField.setVisible(false);
        visiblePasswordField.setManaged(false);

        passwordField.textProperty().bindBidirectional(
            visiblePasswordField.textProperty()
        );

        // ===== SHOW BUTTON =====
        Button toggle = new Button("👁");

        toggle.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #9fb0b5;" +
            "-fx-font-size: 14px;"
        );

        final boolean[] show = {false};

        toggle.setOnAction(e -> {

            show[0] = !show[0];

            passwordField.setVisible(!show[0]);
            passwordField.setManaged(!show[0]);

            visiblePasswordField.setVisible(show[0]);
            visiblePasswordField.setManaged(show[0]);

            toggle.setText(show[0] ? "✖" : "👁");
        });

        // ===== PASSWORD CONTAINER =====
        StackPane fieldStack = new StackPane(passwordField, visiblePasswordField);

        BorderPane passwordContainer = new BorderPane();
        passwordContainer.setCenter(fieldStack);
        passwordContainer.setRight(toggle);

        passwordContainer.setMaxWidth(260);
        passwordContainer.setStyle(
            "-fx-background-color: #f2f4f5;" +
            "-fx-background-radius: 12;" +
            "-fx-padding: 0 10 0 0;"
        );

        // ===== ERROR =====
        Label error = new Label();

        error.setStyle(
            "-fx-text-fill: #ff7b7b;"
        );

        // ===== UNLOCK BUTTON =====
        Button unlock = new Button("Unlock");

        unlock.setPrefWidth(180);
        unlock.setPrefHeight(42);

        unlock.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-border-color: #67d5ff;" +
            "-fx-border-radius: 12;" +
            "-fx-background-radius: 12;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;"
        );

        unlock.setOnMouseEntered(e ->
            unlock.setStyle(
                "-fx-background-color: #67d5ff;" +
                "-fx-border-color: #67d5ff;" +
                "-fx-border-radius: 12;" +
                "-fx-background-radius: 12;" +
                "-fx-text-fill: #20343a;" +
                "-fx-font-size: 14px;"
            )
        );

        unlock.setOnMouseExited(e ->
            unlock.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-border-color: #67d5ff;" +
                "-fx-border-radius: 12;" +
                "-fx-background-radius: 12;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 14px;"
            )
        );

        // ===== UNLOCK ACTION =====
        unlock.setOnAction(e -> {

            String input = passwordField.getText();

            if (!vault.hasMasterPassword()) {

                vault.setMasterPassword(input);
                vault.setKey(input);

                renderCards("");

                stage.setScene(vaultScene);

            } else if (vault.verifyMasterPassword(input)) {

                vault.setKey(input);

                renderCards("");

                stage.setScene(vaultScene);

            } else {
                error.setText("Wrong password");
            }
        });

        // ===== FORGOT PASSWORD =====
        Label forgot = new Label("Forgot Password?");

        forgot.setStyle(
            "-fx-text-fill: #9fb0b5;" +
            "-fx-font-size: 12px;"
        );

        // ================= VAULT UI =================

        VBox vaultRoot = new VBox(10);
        vaultRoot.setPadding(new Insets(15));
        vaultRoot.setStyle("-fx-background-color: #30444a;");

        // ===== TITLE =====
        Label vaultTitle = new Label("Your Vault");

        vaultTitle.setStyle(
            "-fx-text-fill: white;" +
            "-fx-font-size: 22px;" +
            "-fx-font-weight: bold;"
        );

        // ===== SEARCH =====
        searchField = new TextField();
        searchField.setPromptText("Search passwords...");

        searchField.setStyle(
            "-fx-background-radius: 12;" +
            "-fx-background-color: #f2f4f5;" +
            "-fx-prompt-text-fill: #7d8b8f;" +
            "-fx-text-fill: #1e1e1e;"
        );

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            renderCards(newVal);
        });

        // ===== ADD BUTTON =====
        Button addBtn = new Button("+ Add Password");

        addBtn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-border-color: #67d5ff;" +
            "-fx-border-radius: 12;" +
            "-fx-background-radius: 12;" +
            "-fx-text-fill: white;"
        );

        // ===== PASSWORD LIST =====
        ScrollPane scrollPane = new ScrollPane(cardsContainer);

        scrollPane.setFitToWidth(true);

        scrollPane.setStyle(
            "-fx-background: transparent;" +
            "-fx-background-color: transparent;"
        );

        // ===== ADD PASSWORD ACTION =====
        addBtn.setOnAction(e -> {

            Stage popup = new Stage();

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

                vault.add(
                    serviceField.getText(),
                    userField.getText(),
                    passField.getText()
                );

                renderCards(searchField.getText());

                popup.close();
            });

            box.getChildren().addAll(
                serviceField,
                userField,
                passField,
                save
            );

            popup.setScene(new Scene(box, 250, 200));
            popup.show();
        });

        // ===== BUILD VAULT =====
        vaultRoot.getChildren().addAll(
            vaultTitle,
            searchField,
            addBtn,
            scrollPane
        );

        vaultScene = new Scene(vaultRoot, 700, 500);

        // ===== LOGIN ACTION =====
        unlock.setOnAction(e -> {

            String input = passwordField.getText();

            if (!vault.hasMasterPassword()) {

                vault.setMasterPassword(input);
                vault.setKey(input);

                renderCards("");

                stage.setScene(vaultScene);

            } else if (vault.verifyMasterPassword(input)) {

                vault.setKey(input);

                renderCards("");

                stage.setScene(vaultScene);

            } else {
                error.setText("Wrong password");
            }
        });

        // ===== ADD TO CARD =====
        HBox bottomRow = new HBox();
        bottomRow.setAlignment(Pos.CENTER);
        bottomRow.setPrefWidth(260);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        bottomRow.getChildren().addAll(
            forgot,
            spacer,
            unlock
        );

        loginCard.getChildren().addAll(
            logo,
            title,
            passwordContainer,
            bottomRow,
            error
        );

        loginRoot.getChildren().add(loginCard);
        Scene loginScene = new Scene(loginRoot, 500, 600);

        stage.setScene(loginScene);
        stage.setTitle("Password Manager");
        stage.show();
    }

    void renderCards(String filter) {
        cardsContainer.getChildren().clear();

        for (PasswordItem item : vault.getAll()) {

            if (!item.service.toLowerCase().contains(filter.toLowerCase())
                    && !item.username.toLowerCase().contains(filter.toLowerCase())) {
                continue;
            }

            VBox card = new VBox(8);
            card.setPadding(new Insets(10));
            card.setStyle("-fx-background-color: #3c4f4f;");

            Label service = new Label(item.service);
            Label username = new Label(item.username);
            Label password = new Label(item.password);

            Button edit = new Button("Edit");
            Button del = new Button("Delete");
            Button copy = new Button("Copy");

            del.setOnAction(e -> {
                vault.delete(item.id);
                renderCards(searchField.getText());
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
                    renderCards(searchField.getText());
                    popup.close();
                });

                box.getChildren().addAll(s, u, p, save);

                popup.setScene(new Scene(box, 250, 200));
                popup.show();
            });

            copy.setOnAction(e -> {

                Clipboard clipboard = Clipboard.getSystemClipboard();
                ClipboardContent content = new ClipboardContent();
                
                content.putString(item.password);

                clipboard.setContent(content);

                System.out.println("Password copied");
            });

            HBox actions = new HBox(10, edit, del, copy);

            card.getChildren().addAll(service, username, password, actions);
            cardsContainer.getChildren().add(card);
        }
    }
}