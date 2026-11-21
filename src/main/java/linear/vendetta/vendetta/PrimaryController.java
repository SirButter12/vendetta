package linear.vendetta.vendetta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import linear.vendetta.vendetta.api.dsl.CommandException;
import linear.vendetta.vendetta.api.dsl.CompiledResult;
import linear.vendetta.vendetta.api.dsl.Interpreter;
import linear.vendetta.vendetta.api.linear_algebra.Matrix;
import linear.vendetta.vendetta.api.linear_algebra.Vector;

/**
 * Controller of primary.fxml.
 *
 * Behavior: INTERFACE.md. Step-by-step tasks: TASKS_MATTHEWS.md. API: README.md.
 *
 * DO NOT import java.util.* : Vector would clash with java.util.Vector.
 *
 * Note: javafx.scene.Node is used here for UI nodes. The API also has a class
 * called Node (api.dsl.Node) but the controller never needs it, so don't import it.
 *
 * @author Matthews
 */
public class PrimaryController {

    // =====================================================================
    // FXML fields. Names and types MUST match INTERFACE.md section 11.
    // =====================================================================

    /** Window root. */
    @FXML
    private VBox rootPane;

    /** Container of the matrix cards (inside a ScrollPane). */
    @FXML
    private VBox matricesBox;

    /** Container of the vector cards (inside a ScrollPane). */
    @FXML
    private VBox vectorsBox;

    /** Cartesian plane. Empty in the FXML, everything is drawn here by code. */
    @FXML
    private Pane graphPane;

    @FXML
    private Button clearButton;

    @FXML
    private Button compileButton;

    /** Output entries go here (inside outputScroll). */
    @FXML
    private VBox outputBox;

    @FXML
    private ScrollPane outputScroll;

    /** Input lines go here. Empty in the FXML, lines are created by code. */
    @FXML
    private VBox commandsBox;

    @FXML
    private ScrollPane inputScroll;

    // =====================================================================
    // State
    // =====================================================================

    /** Pixels per unit on the Cartesian plane (one grid square). */
    private static final int SCALE = 15;

    /** Accepted commands, in the order the user entered them. */
    private final List<String> commands = new ArrayList<>();

    /** Names declared with Matrix(...) / Vector(...): editable cells, sent as manualValues. */
    private final Set<String> manualNames = new LinkedHashSet<>();

    /** name -> cells of its matrix card, [row][col]. */
    private final Map<String, TextField[][]> matrixCells = new HashMap<>();

    /** name -> cells of its vector card, [component]. */
    private final Map<String, TextField[]> vectorCells = new HashMap<>();

    /** name -> its card (to replace or remove it). */
    private final Map<String, HBox> cards = new HashMap<>();

    /** Names of the vectors whose radio button is selected. */
    private final Set<String> plotted = new HashSet<>();

    /** The active (last) Input line. */
    private TextField currentLine;

    // =====================================================================
    // FXML entry points
    // =====================================================================

    /**
     * Called automatically by FXMLLoader after the @FXML fields are filled.
     * The name must be exactly "initialize".
     */
    @FXML
    public void initialize() {
        addInputLine();
        // TODO Task 10: redraw the plane when graphPane's width/height change
        // TODO Task 10: drawPlane()
    }

    /** Clear button (onAction="#onClear"). Resets everything. */
    @FXML
    private void onClear() {
        Interpreter.resetMemory();
        commands.clear();
        manualNames.clear();
        matrixCells.clear();
        vectorCells.clear();
        cards.clear();
        plotted.clear();
        matricesBox.getChildren().clear();
        vectorsBox.getChildren().clear();
        outputBox.getChildren().clear();
        commandsBox.getChildren().clear();
        addInputLine();
        drawPlane();
    }

    /** Compile button (onAction="#onCompile"). */
    @FXML
    private void onCompile() {
        // 1. pending line
        if (!currentLine.getText().trim().isEmpty()) {
            if (!onEnter(currentLine)) {
                return;
            }
            currentLine.setEditable(false);
            addInputLine();
        }

        // 2. clear output
        outputBox.getChildren().clear();

        // 3. read the hand-typed values
        Map<String, Object> manualValues;
        try {
            manualValues = readManualValues();
        } catch (Exception e) {
            addError("Compile", errorMessage(e));
            return;
        }

        // 4. load them into memory (declarations only)
        List<String> declarations = new ArrayList<>();
        for (String command : commands) {
            if (isConstructor(command) && manualNames.contains(extractName(command))) {
                declarations.add(command);
            }
        }
        try {
            Interpreter.interprete(declarations, manualValues);
        } catch (Exception e) {
            addError("Compile", errorMessage(e));
            return;
        }

        // 5. run the commands one at a time
        for (String command : commands) {
            String name = extractName(command);

            if (isConstructor(command)) {
                if (manualNames.contains(name)) {
                    addOutput(command, toDisplay(manualValues.get(name)));
                }
                continue;   // a declaration that was later overwritten by "NAME = [...]"
            }

            try {
                Object result = Interpreter.interprete(List.of(command)).get(0).getResult();
                addOutput(command, toDisplay(result));
                if (name != null && !manualNames.contains(name)
                        && (result instanceof Matrix || result instanceof Vector)) {
                    showCard(name, result, false);
                }
            } catch (Exception e) {
                addError(command, errorMessage(e));
            }
        }

        // 6. plane
        drawPlane();
    }

    // =====================================================================
    // Input lines
    // =====================================================================

    /** Creates a new empty Input line at the end of commandsBox and focuses it. */
    private void addInputLine() {
        TextField field = new TextField();
        field.getStyleClass().add("command-line");
        field.setMaxWidth(Double.MAX_VALUE);
        field.setOnAction(ev -> {
            if (onEnter(field)) {
                field.setEditable(false);
                addInputLine();
            }
        });
        commandsBox.getChildren().add(field);
        currentLine = field;
        Platform.runLater(() -> {
            field.requestFocus();
            inputScroll.setVvalue(1.0);
        });
    }

    /**
     * Processes the command typed in a line (Enter, or Compile with a pending line).
     *
     * @return true if the command was accepted and added to commands
     */
    private boolean onEnter(TextField line) {
        String text = line.getText().trim();
        if (text.isEmpty()) {
            return false;
        }
        line.getStyleClass().remove("field-error");

        try {
            if (!handleConstructor(text) && isDirectAssignment(text)) {
                handleDirectAssignment(text);
            }
        } catch (Exception e) {
            addError(text, errorMessage(e));
            line.getStyleClass().add("field-error");
            return false;
        }

        commands.add(text);
        return true;
    }

    /**
     * NAME = Matrix(r, c) / NAME = Vector(n): calls prepareVariables with
     * (commands + text) and shows a zero-filled editable card.
     *
     * @return false if the command is not a constructor
     * @throws CommandException bad declaration, constant or duplicated name
     */
    private boolean handleConstructor(String text) {
        if (!isConstructor(text)) {
            return false;
        }
        String name = extractName(text);
        if (name == null) {
            return false;
        }

        List<String> all = new ArrayList<>(commands);
        all.add(text);
        Object template = Interpreter.prepareVariables(all).get(name);
        if (template == null) {
            return false;
        }

        manualNames.add(name);
        showCard(name, template, true);
        return true;
    }

    /**
     * NAME = [ ... ]: evaluates it and shows a read-only card with its values.
     *
     * @return true if a card was shown
     * @throws CommandException (or other exceptions from the API) if it can't be evaluated
     */
    private boolean handleDirectAssignment(String text) {
        String name = extractName(text);
        Object result = Interpreter.interprete(List.of(text)).get(0).getResult();
        if (name == null || !(result instanceof Matrix || result instanceof Vector)) {
            return false;
        }
        manualNames.remove(name);
        showCard(name, result, false);
        return true;
    }

    /** True if text has '=' and what follows (trimmed) starts with '['. */
    private boolean isDirectAssignment(String text) {
        int eq = text.indexOf('=');
        return eq >= 0 && text.substring(eq + 1).trim().startsWith("[");
    }

    /** True if text looks like NAME = Matrix(...) / NAME = Vector(...). */
    private boolean isConstructor(String text) {
        return text.matches("\\s*[A-Za-z][A-Za-z0-9]*\\s*=\\s*(Matrix|Vector)\\s*\\(.*");
    }

    /**
     * Variable name before the first '=', trimmed.
     *
     * @return null if there is no '=' or it's not a valid name (letter followed by letters/digits)
     */
    private String extractName(String text) {
        int eq = text.indexOf('=');
        if (eq < 0) {
            return null;
        }
        String name = text.substring(0, eq).trim();
        return name.matches("[A-Za-z][A-Za-z0-9]*") ? name : null;
    }

    // =====================================================================
    // Cards (Matrices / Vectors sections)
    // =====================================================================

    /**
     * Creates the card of a variable, or replaces it if it already exists
     * (also if it changed from matrix to vector or the other way around).
     *
     * @param value    Matrix or Vector
     * @param editable true for constructor variables (manualNames)
     */
    private void showCard(String name, Object value, boolean editable) {
        // remove any previous card (and cells) of this name
        HBox old = cards.remove(name);
        if (old != null && old.getParent() instanceof Pane parent) {
            parent.getChildren().remove(old);
        }
        matrixCells.remove(name);
        vectorCells.remove(name);

        HBox card;
        VBox target;
        if (value instanceof Matrix m) {
            card = buildMatrixCard(name, m, editable);
            target = matricesBox;
        } else if (value instanceof Vector v) {
            card = buildVectorCard(name, v, editable);
            target = vectorsBox;
        } else {
            return;
        }
        cards.put(name, card);
        target.getChildren().add(card);
    }

    /**
     * "A =" + left bracket + GridPane of TextFields + right bracket.
     * Stores the cells in matrixCells.
     */
    private HBox buildMatrixCard(String name, Matrix matrix, boolean editable) {
        int rows = matrix.getNumRows();
        int cols = matrix.getNumCols();
        TextField[][] cells = new TextField[rows][cols];
        GridPane grid = new GridPane();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                cells[r][c] = newCell(matrix.get(r, c), editable);
                grid.add(cells[r][c], c, r);
            }
        }
        matrixCells.put(name, cells);
        return assembleCard(name, grid);
    }

    /**
     * Same as buildMatrixCard but one column, plus a RadioButton (no ToggleGroup)
     * if the vector is 2D. Stores the cells in vectorCells.
     */
    private HBox buildVectorCard(String name, Vector vector, boolean editable) {
        int n = vector.getDimension();
        TextField[] cells = new TextField[n];
        GridPane grid = new GridPane();
        for (int i = 0; i < n; i++) {
            cells[i] = newCell(vector.get(i), editable);
            grid.add(cells[i], 0, i);
        }
        vectorCells.put(name, cells);
        // TODO Task 10: radio button (plot) for 2D vectors
        return assembleCard(name, grid);
    }

    private TextField newCell(double value, boolean editable) {
        TextField cell = new TextField(Vector.formatNumber(value));
        cell.getStyleClass().add(editable ? "cell-field" : "cell-readonly");
        cell.setEditable(editable);
        cell.setPrefColumnCount(4);
        cell.textProperty().addListener((obs, a, b) -> cell.getStyleClass().remove("field-error"));
        return cell;
    }

    private HBox assembleCard(String name, GridPane grid) {
        Label label = new Label(name + " =");
        label.getStyleClass().add("card-name");
        HBox card = new HBox(label, bracket("bracket-left"), grid, bracket("bracket-right"));
        card.getStyleClass().add("card");
        return card;
    }

    private Region bracket(String styleClass) {
        Region r = new Region();
        r.getStyleClass().add(styleClass);
        r.setMinWidth(8);
        r.setPrefWidth(8);
        return r;
    }

    /**
     * Reads every cell of every manual variable with Interpreter.evaluateEntry.
     *
     * @return name -> Matrix/Vector with the typed values
     * @throws CommandException if a cell is invalid (mark the cell with "field-error"
     *         and say which variable, row and column in the message)
     */
    private Map<String, Object> readManualValues() {
        Map<String, Object> values = new HashMap<>();
        for (String name : manualNames) {
            TextField[][] mCells = matrixCells.get(name);
            TextField[] vCells = vectorCells.get(name);
            if (mCells != null) {
                List<Vector> rows = new ArrayList<>();
                for (int r = 0; r < mCells.length; r++) {
                    List<Double> row = new ArrayList<>();
                    for (int c = 0; c < mCells[r].length; c++) {
                        row.add(readCell(name, mCells[r][c], "row " + (r + 1) + ", column " + (c + 1)));
                    }
                    rows.add(new Vector(row));
                }
                values.put(name, Matrix.build(rows));
            } else if (vCells != null) {
                List<Double> comps = new ArrayList<>();
                for (int i = 0; i < vCells.length; i++) {
                    comps.add(readCell(name, vCells[i], "component " + (i + 1)));
                }
                values.put(name, new Vector(comps));
            }
        }
        return values;
    }

    private double readCell(String name, TextField cell, String where) {
        try {
            return Interpreter.evaluateEntry(cell.getText());
        } catch (Exception e) {
            if (!cell.getStyleClass().contains("field-error")) {
                cell.getStyleClass().add("field-error");
            }
            throw new CommandException(name + " (" + where + "): " + errorMessage(e));
        }
    }

    // =====================================================================
    // Output
    // =====================================================================

    /**
     * Turns any API result into something the UI can show.
     * Matrix -> buildBracketed, Vector -> buildBracketed(Matrix.convertToMatrix(v)),
     * Double -> Label with Vector.formatNumber, anything else -> Label with toString().
     */
    private Node toDisplay(Object result) {
        if (result instanceof Matrix m) {
            return buildBracketed(m);
        }
        if (result instanceof Vector v) {
            return buildBracketed(Matrix.convertToMatrix(v));
        }
        if (result instanceof Double d) {
            return new Label(Vector.formatNumber(d));
        }
        return new Label(String.valueOf(result));
    }

    /** Grid of Labels between two bracket Regions ("bracket-left" / "bracket-right"). */
    private Node buildBracketed(Matrix matrix) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        for (int r = 0; r < matrix.getNumRows(); r++) {
            for (int c = 0; c < matrix.getNumCols(); c++) {
                grid.add(new Label(Vector.formatNumber(matrix.get(r, c))), c, r);
            }
        }
        HBox box = new HBox(4, bracket("bracket-left"), grid, bracket("bracket-right"));
        box.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        return box;
    }

    /** Adds to outputBox: the command ("output-command") and below it the result ("output-value"). */
    private void addOutput(String command, Node result) {
        Label cmd = new Label(command);
        cmd.getStyleClass().add("output-command");
        result.getStyleClass().add("output-value");
        outputBox.getChildren().add(new VBox(2, cmd, result));
        scrollOutput();
    }

    /** Adds to outputBox: the command and below it "Error: message" ("output-error"). */
    private void addError(String command, String message) {
        Label cmd = new Label(command);
        cmd.getStyleClass().add("output-command");
        Label err = new Label("Error: " + message);
        err.setWrapText(true);
        err.getStyleClass().add("output-error");
        outputBox.getChildren().add(new VBox(2, cmd, err));
        scrollOutput();
    }

    private void scrollOutput() {
        Platform.runLater(() -> outputScroll.setVvalue(1.0));
    }

    /** The exception's message, or its simple class name if the message is null. */
    private String errorMessage(Exception e) {
        return e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
    }

    // =====================================================================
    // Cartesian plane
    // =====================================================================

    /**
     * Clears graphPane and draws: dashed grid every SCALE px, x and y axes through
     * the center, and every vector in plotted that is a 2D Vector in Interpreter.getMemory().
     */
    private void drawPlane() {
        // TODO Task 10.1 (+ clip to graphPane's size, Task 10.3)
    }

    /**
     * Red arrow from the origin to (x, y) with the name next to the tip.
     * Pixel position: (centerX + x * SCALE, centerY - y * SCALE)  <- minus: screen y goes down.
     */
    private void drawVector(String name, double x, double y) {
        // TODO Task 10.2
    }
}
