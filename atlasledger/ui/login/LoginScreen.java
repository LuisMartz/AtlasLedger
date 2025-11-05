package atlasledger.ui.login;

import atlasledger.app.StartupProfile;
import atlasledger.model.Worker;
import atlasledger.service.AuthService;
import atlasledger.utils.DBHelper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;

public class LoginScreen extends BorderPane {

    private static final Path CENTRAL_DB_PATH = Paths.get(System.getProperty("user.home"), ".atlasledger", "atlasledger.db");

    private final AuthService authService;
    private Consumer<StartupProfile> authenticatedListener;
    private Runnable cancelListener;

    private final TextField usernameField = new TextField();
    private final PasswordField passwordField = new PasswordField();
    private final ToggleGroup modeGroup = new ToggleGroup();
    private final RadioButton localModeButton = new RadioButton("Modo local / temporal");
    private final RadioButton centralModeButton = new RadioButton("Servidor central");
    private final TextField databasePathField = new TextField();
    private final Button browseDbButton = new Button("Elegir archivo");
    private final TextField documentsPathField = new TextField();
    private final Button browseDocsButton = new Button("Elegir carpeta");
    private final Label messageLabel = new Label();

    public LoginScreen(AuthService authService) {
        this.authService = authService;
        getStyleClass().add("login-root");
        setCenter(buildCard());
        configureDefaults();
    }

    private VBox buildCard() {
        VBox card = new VBox();
        card.getStyleClass().add("login-card");
        card.setSpacing(18);

        Label title = new Label("Atlas Ledger");
        title.getStyleClass().add("login-title");

        Label subtitle = new Label("Acceso de personal autorizado");
        subtitle.getStyleClass().add("login-subtitle");

        VBox heading = new VBox(6, title, subtitle);
        heading.setAlignment(Pos.CENTER_LEFT);

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(10);

        usernameField.setPromptText("Usuario");
        passwordField.setPromptText("Contrasena");

        form.add(new Label("Usuario"), 0, 0);
        form.add(usernameField, 1, 0);
        form.add(new Label("Contrasena"), 0, 1);
        form.add(passwordField, 1, 1);

        localModeButton.setToggleGroup(modeGroup);
        centralModeButton.setToggleGroup(modeGroup);
        localModeButton.setSelected(true);

        VBox modeBox = new VBox(6, localModeButton, centralModeButton);
        modeBox.getStyleClass().add("login-mode-box");
        form.add(new Label("Modo de trabajo"), 0, 2);
        form.add(modeBox, 1, 2);

        HBox dbChooser = new HBox(8);
        browseDbButton.setOnAction(event -> chooseDatabaseFile());
        HBox.setHgrow(databasePathField, Priority.ALWAYS);
        dbChooser.getChildren().addAll(databasePathField, browseDbButton);

        form.add(new Label("Archivo de base de datos"), 0, 3);
        form.add(dbChooser, 1, 3);

        HBox docsChooser = new HBox(8);
        browseDocsButton.setOnAction(event -> chooseDocumentsDirectory());
        HBox.setHgrow(documentsPathField, Priority.ALWAYS);
        docsChooser.getChildren().addAll(documentsPathField, browseDocsButton);

        form.add(new Label("Carpeta de documentos"), 0, 4);
        form.add(docsChooser, 1, 4);

        Button loginButton = new Button("Iniciar sesion");
        loginButton.getStyleClass().add("cta");
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setOnAction(event -> attemptLogin());

        Button cancelButton = new Button("Salir");
        cancelButton.setOnAction(event -> {
            if (cancelListener != null) {
                cancelListener.run();
            } else {
                getScene().getWindow().hide();
            }
        });

        HBox buttonRow = new HBox(10, loginButton, cancelButton);
        HBox.setHgrow(loginButton, Priority.ALWAYS);

        messageLabel.getStyleClass().add("login-message");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        card.getChildren().addAll(heading, form, buttonRow, messageLabel, spacer);
        card.setPadding(new Insets(24));

        localModeButton.setOnAction(event -> updateModeState());
        centralModeButton.setOnAction(event -> updateModeState());
        updateModeState();

        return card;
    }

    private void configureDefaults() {
        databasePathField.setText(CENTRAL_DB_PATH.toString());
        Path defaultDocs = Paths.get(System.getProperty("user.home"), ".atlasledger", "docs");
        documentsPathField.setText(defaultDocs.toString());
    }

    private void updateModeState() {
        boolean local = localModeButton.isSelected();
        databasePathField.setDisable(!local);
        browseDbButton.setDisable(!local);
        if (!local) {
            databasePathField.setText(CENTRAL_DB_PATH.toString());
        }
    }

    private void chooseDatabaseFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Seleccionar archivo de base de datos");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("SQLite (*.db)", "*.db"));
        Path current = Paths.get(databasePathField.getText());
        if (Files.exists(current)) {
            chooser.setInitialDirectory(current.toFile().getParentFile());
            chooser.setInitialFileName(current.getFileName().toString());
        }
        java.io.File file = chooser.showSaveDialog(getWindow());
        if (file != null) {
            databasePathField.setText(file.toPath().toString());
        }
    }

    private void chooseDocumentsDirectory() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Seleccionar carpeta de documentos");
        Path current = Paths.get(documentsPathField.getText());
        if (Files.exists(current)) {
            chooser.setInitialDirectory(current.toFile());
        }
        java.io.File dir = chooser.showDialog(getWindow());
        if (dir != null) {
            documentsPathField.setText(dir.toPath().toString());
        }
    }

    private void attemptLogin() {
        messageLabel.setText("");
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Ingresa usuario y contrasena.");
            return;
        }

        boolean localMode = localModeButton.isSelected();
        Path dbPath = resolveDatabasePath(localMode);
        Path docsPath = Paths.get(documentsPathField.getText().trim());
        String baseUrl = localMode ? "http://128.0.0.1" : "https://api.atlas-ledger.example";

        DBHelper.overrideDatabasePath(dbPath, true);
        authService.ensureDefaultAdmin();

        Optional<Worker> worker = authService.authenticate(username, password);
        if (worker.isEmpty()) {
            messageLabel.setText("Credenciales incorrectas.");
            return;
        }

        StartupProfile profile = new StartupProfile(dbPath, docsPath, localMode, baseUrl, worker.get());
        if (authenticatedListener != null) {
            authenticatedListener.accept(profile);
        }
    }

    private Path resolveDatabasePath(boolean localMode) {
        if (localMode) {
            String text = databasePathField.getText().trim();
            if (text.isEmpty()) {
                Path localDefault = Paths.get(System.getProperty("user.home"), ".atlasledger", "temporal.db");
                databasePathField.setText(localDefault.toString());
                return localDefault;
            }
            Path chosen = Paths.get(text);
            if (chosen.getParent() != null && !Files.exists(chosen.getParent())) {
                try {
                    Files.createDirectories(chosen.getParent());
                } catch (Exception e) {
                    messageLabel.setText("No se pudo crear el directorio de la base de datos.");
                }
            }
            return chosen;
        }
        return CENTRAL_DB_PATH;
    }

    private Window getWindow() {
        return getScene() != null ? getScene().getWindow() : null;
    }

    public void setOnAuthenticated(Consumer<StartupProfile> listener) {
        this.authenticatedListener = listener;
    }

    public void setOnCancel(Runnable listener) {
        this.cancelListener = listener;
    }
}
