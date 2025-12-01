package atlasledger.app;

import atlasledger.app.StartupProfile;
import atlasledger.service.AuthService;
import atlasledger.ui.dashboard.MainScreen;
import atlasledger.ui.login.LoginScreen;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    private AppContext appContext;

    @Override
    public void start(Stage stage) {
        showLogin(stage);
    }

    private void showLogin(Stage stage) {
        if (appContext != null) {
            appContext.getSyncService().close();
            appContext = null;
        }

        AuthService authService = new AuthService();
        authService.ensureDefaultAdmin();

        LoginScreen loginScreen = new LoginScreen(authService);
        loginScreen.setOnAuthenticated(profile -> openMainWindow(stage, profile, authService));
        loginScreen.setOnCancel(stage::close);

        Scene loginScene = new Scene(loginScreen);
        applyStyles(loginScene);
        stage.setTitle("Atlas Ledger - Acceso");
        stage.setScene(loginScene);
        stage.setMinWidth(720);
        stage.setMinHeight(520);
        stage.centerOnScreen();
        stage.show();
    }

    private void openMainWindow(Stage stage, StartupProfile profile, AuthService authService) {
        if (appContext != null) {
            appContext.getSyncService().close();
        }
        appContext = AppInitializer.initialise(profile, authService);
        Runnable logoutHandler = () -> showLogin(stage);
        MainScreen mainScreen = new MainScreen(appContext, logoutHandler);
        Scene scene = new Scene(mainScreen);
        applyStyles(scene);
        stage.setTitle("Atlas Ledger - " + profile.getWorker().getNombre());
        stage.setMinWidth(1160);
        stage.setMinHeight(720);
        stage.setWidth(1280);
        stage.setHeight(820);
        stage.setScene(scene);
        stage.centerOnScreen();
    }

    private void applyStyles(Scene scene) {
        URL css = MainApp.class.getResource("/styles/main.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
            return;
        }
        Path cssPath = Paths.get("src", "main", "resources", "styles", "main.css");
        if (Files.exists(cssPath)) {
            scene.getStylesheets().add(cssPath.toUri().toString());
        }
    }

    @Override
    public void stop() {
        if (appContext != null) {
            appContext.getSyncService().close();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
