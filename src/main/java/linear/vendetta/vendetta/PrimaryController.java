package linear.vendetta.vendetta;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import linear.vendetta.vendetta.api.dsl.Node;

public class PrimaryController {

    @FXML
    private Label brandTitle;

    @FXML
    private StackPane cartesianPane;

    @FXML
    private Button clearButton;

    @FXML
    private Spinner<Integer> colsASpinner;

    @FXML
    private Spinner<Integer> colsBSpinner;

    @FXML
    private GridPane matrixAContainer;

    @FXML
    private GridPane matrixBContainer;

    @FXML
    private ComboBox<String> operationComboBox;

    @FXML
    private TextArea resultArea;

    @FXML
    private BorderPane rootPane;

    @FXML
    private Spinner<Integer> rowsASpinner;

    @FXML
    private Spinner<Integer> rowsBSpinner;

    @FXML
    private TextField scalarField;

    @FXML
    private Button solveButton;

    @FXML
    private Label subtitleLabel;
    
    @FXML
    public void intialize() {
        operationComboBox.getItems().setAll(
            "Addition",
            "Subtraction",
            "Scalar Multiplication",
            "Transpose",
            "Determinant",
            "Inverse",
            "Matrix Multiplication",
            "Dot Product",
            "Cross Product",
            "Projection"
        );

        operationComboBox.getSelectionModel().selectFirst();
    }
    
    @FXML
    public void clearButtonPressed() {
        clearTextFields(matrixAContainer);
        clearTextFields(matrixBContainer);
        
        scalarField.clear();
        resultArea.clear();
        
        resultArea.setPromptText("Enter your values and select an operation");
    }
    
    private void clearTextFields(Parent parent) {
    parent.lookupAll(".text-field").stream()
          .filter(node -> node instanceof TextField)
          .map(node -> (TextField) node)
          .forEach(TextField::clear);
    }
    
    @FXML
    private void solveButtonPressed() {
        String operation = operationComboBox.getSelectionModel().getSelectedItem();

        if (operation == null) {
            resultArea.setText("Please select an operation.");
            return;
        }

        resultArea.setText(
            "Selected operation: " + operation
            + "\n\nThe input will be validated before calculation."
        );
    }
}
