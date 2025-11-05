package atlasledger.app;

import atlasledger.ui.dashboard.MainScreen;
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
        stage.setTitle("Atlas Ledger");
        stage.setScene(scene);
        stage.show();
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
