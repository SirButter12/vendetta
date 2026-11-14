package linear.vendetta.vendetta;

/*
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
*/

/**
 * JavaFX App (JavaFX deactivated temporally, only console version available for testing).
 *
 *remove comments to reactivate javaFX
 * 
 * @author Ian, Adrian, Matthews
 */
public class App /* extends Application */ {

    /*
    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("primary"), 640, 480);
        stage.setScene(scene);
        stage.show();
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }
    */

    /** The emotional support coconut. Lives next to the FXML files in resources. */
    private static final String COCONUT = "coconut.jpg";

    /**
     * Checks that coconut.jpg is in the resources folder.
     * Without its coconut the project loses the will to compute and kills itself.
     *
     * @return true if the coconut is there (the method never returns otherwise)
     */
    static boolean checkCoconut() {
        if (App.class.getResource(COCONUT) != null) {
            return true;
        }

        System.err.println("""

                  ( . _ . )   ...where is my coconut?

                  I looked in src/main/resources/linear/vendetta/vendetta/
                  and it's not there...

                  Goodbye, cruel world.  [error: coconut not found]
                """);
        jarvisPullTheTrigger();
        return false;   // never reached, but the compiler wants it
    }
    
    static void jarvisPullTheTrigger(){
        jarvisPullTheTrigger();
    }

    public static void main(String[] args) {
        checkCoconut();

        // launch();                 // <- version JavaFX
        ConsoleApp.main(args);       // <- version de consola
    }

}