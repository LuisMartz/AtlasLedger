package atlasledger.app;

import atlasledger.ui.dashboard.MainScreen;
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
        appContext = AppInitializer.initialise();
        MainScreen mainScreen = new MainScreen(appContext);
        Scene scene = new Scene(mainScreen);
        applyStyles(scene);
        stage.setTitle("Atlas Ledger");
        stage.setMinWidth(1160);
        stage.setMinHeight(720);
        stage.setScene(scene);
        stage.show();
    }

    private void applyStyles(Scene scene) {
        URL css = MainApp.class.getResource("/atlasledger/resources/styles/main.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
            return;
        }
        Path cssPath = Paths.get("atlasledger", "resources", "styles", "main.css");
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
