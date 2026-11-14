# Matthews's tasks: the controller

File: `src/main/java/linear/vendetta/vendetta/PrimaryController.java`

Read first:

- `INTERFACE.md`: what the interface must do and which API call goes with each event. **Section 11 has the fx:id / method / style class names you must use.**
- `README.md`: the API reference.

Rules: before each task `git pull`. After each task that works: small commit and push. Don't edit the API or the FXML; ask Ian or Adrian.

---

## Controller skeleton

This is the **structure** of `PrimaryController`: which fields and methods it has and what each one does.

### Fields bound to the FXML (annotated with @FXML)

One for each fx:id in `INTERFACE.md` section 11, same name and type.

### State fields (private, not @FXML)

| Field | Suggested type | Purpose |
|---|---|---|
| `commands` | List of String | Accepted commands, in order |
| `manualNames` | Set of String (insertion order) | Names declared with a constructor (their cells are editable) |
| `matrixCells` | Map name → 2D array of TextField | Cells of each matrix card |
| `vectorCells` | Map name → array of TextField | Cells of each vector card |
| `cards` | Map name → HBox | Each variable's card (to replace or remove it) |
| `plotted` | Set of String | Names of vectors whose radio button is selected |
| `currentLine` | TextField | The active Input line (the last one) |
| `SCALE` | integer constant | Pixels per unit on the plane (suggested: 15, one grid square) |

### Methods

| Method | Visibility | Takes | Returns | Responsibility |
|---|---|---|---|---|
| `initialize` | public, @FXML | — | — | Create the first Input line, hook the plane redraw to size changes, draw the empty plane |
| `onClear` | @FXML | — | — | Full reset |
| `onCompile` | @FXML | — | — | Full compile flow |
| `addInputLine` | private | — | — | Create a new line in `commandsBox`, set it as `currentLine` and focus it |
| `onEnter` | private | the line (TextField) | boolean (true if accepted) | Classify and process that line's command |
| `handleConstructor` | private | command text | boolean | Call `prepareVariables` and create the zero-filled card |
| `handleDirectAssignment` | private | command text | boolean | Evaluate `X = [...]` and create the card with its values |
| `isDirectAssignment` | private | text | boolean | Does it have `=` and does the right side start with `[`? |
| `extractName` | private | text | String or null | Whatever is before the first `=`, trimmed, if it's a valid name |
| `showCard` | private | name, value (Matrix/Vector), editable (boolean) | — | Create or replace the card in its section |
| `buildMatrixCard` | private | name, Matrix, editable | HBox | Label + bracket + GridPane of TextFields + bracket |
| `buildVectorCard` | private | name, Vector, editable | HBox | Same but as a column, plus a radio button if it's 2D |
| `readManualValues` | private | — | Map name → Matrix/Vector | Read every editable cell with `evaluateEntry` |
| `toDisplay` | private | Object (result) | Node | **Turn any result into something displayable** |
| `buildBracketed` | private | Matrix | Node | Grid of Labels with brackets (used by `toDisplay`) |
| `addOutput` | private | command, Node | — | Add the command to Output with the node below it |
| `addError` | private | command, message | — | Add the command to Output with the error in red below it |
| `errorMessage` | private | Exception | String | The message, or the class name if it is null |
| `drawPlane` | private | — | — | Clear and redraw the grid, axes and selected vectors |
| `drawVector` | private | name, x, y | — | Draw one arrow and its label |

---

## Task 0: Set things up

1. Pull.
2. Read `INTERFACE.md` and `README.md`. Run the console version and try: `A = [[1,2],[3,4]]`, `det(A)`, `Adj(A)` (to see an error), `memory`.
3. Open `App.java`. That file isn't yours: **tell Ian** you're going to re-enable JavaFX. Uncomment the JavaFX code (imports, `extends Application`, `start`, `setRoot`, `loadFXML`) and in `main` replace the `ConsoleApp.main` call with `launch()`. Leave `checkCoconut()` where it is.
4. In `start`, change the scene size to 1030 × 580 (or whatever you agree on with Adrian).
5. Delete all the old content of `PrimaryController` (the spinner, combo box, etc. fields no longer exist in the new design). Note: the old method was named `intialize` (misspelled), which is why it never ran. The new one must be named exactly `initialize`.
6. Import `java.util` classes one by one, not `java.util.*` (`Vector` would clash with `java.util.Vector`).

## Task 1: Fields

1. Declare one @FXML field for each row of the fx:id table (`INTERFACE.md` section 11), same name and type.
2. Declare the state fields from the skeleton, initialized empty.

## Task 2: Empty methods so it starts

1. Create empty `initialize`, `onClear` and `onCompile` (all three with @FXML).
2. When Adrian gives you the FXML, run the app. It must open without errors even though it does nothing. If it crashes with "Error resolving onAction" or a field is null, check the names against `INTERFACE.md` section 11.

## Task 3: Input lines

1. In `addInputLine`: create a TextField, give it the `command-line` class, unlimited max width.
2. Set its action (what happens when Enter is pressed in a TextField) to call `onEnter` with that same TextField. If `onEnter` returns true: make it non-editable and call `addInputLine`.
3. Add it at the end of `commandsBox`, store it in `currentLine`, request focus.
4. Make `inputScroll` scroll to the bottom (vertical value 1) so the new line is always visible.
5. In `initialize`, call `addInputLine`.
6. Test: type something, press Enter, a new line must appear.

## Task 4: Basic output

1. `errorMessage`: return the exception's message, or if it is null, its simple class name.
2. `addOutput`: create a VBox; inside it, a Label with the command (class `output-command`) and below it the Node (class `output-value`). Add it to `outputBox`.
3. `addError`: same, but the second Label says "Error: " + message with class `output-error`.
4. Make `outputScroll` scroll to the bottom after adding something.

## Task 5: `toDisplay` (turn a result into something displayable)

1. If the result is a `Matrix`: return `buildBracketed(matrix)`.
2. If it is a `Vector`: convert it to a column matrix with `Matrix.convertToMatrix(vector)` and use `buildBracketed`, so it shows vertically like in the mock-up.
3. If it is a `Double`: return a Label with `Vector.formatNumber(value)`.
4. Anything else: a Label with its `toString()`.
5. `buildBracketed`: HBox with a `bracket-left` Region, a GridPane with one Label per cell (text from `formatNumber`, centered), and a `bracket-right` Region. Both Regions must be as tall as the GridPane (bind their preferred height to the GridPane's height).

## Task 6: Cards

1. `buildMatrixCard`: HBox with class `card`. Inside: Label `name + " ="` (class `card-name`), `bracket-left` Region, GridPane with one TextField per cell, `bracket-right` Region.
   - Each TextField: initial text = `formatNumber(value of that cell)`, class `cell-field`, preferred width ~35.
   - If not editable: non-editable TextField and class `cell-readonly`.
   - When the user edits a cell, remove its `field-error` class (listen to text changes).
   - Store the 2D array of TextFields in `matrixCells` under the name.
2. `buildVectorCard`: same, but the GridPane has a single column; store the array in `vectorCells`.
   - If the vector has dimension 2: add a RadioButton at the end (class `vector-radio`, no ToggleGroup). If the name is in `plotted`, it starts selected. When its selection changes: add or remove the name from `plotted` and call `drawPlane`.
3. `showCard(name, value, editable)`:
   - If there is already a card with that name in `cards`, remove it from its container and delete its cells from `matrixCells`/`vectorCells` (a variable can change from matrix to vector).
   - Build the card depending on whether it is a Matrix or Vector, add it to `matricesBox` or `vectorsBox`, store it in `cards`.
   - Optional: if the old card was at some position, insert the new one at the same position so it doesn't jump.

## Task 7: Enter

1. `onEnter(line)`:
   1. Take the text trimmed. If it is empty, return false.
   2. Remove the `field-error` class from the line.
   3. Try, in this order: `handleConstructor`; if it wasn't a constructor, `isDirectAssignment` → `handleDirectAssignment`; otherwise it is a normal command (nothing to do now).
   4. If anything threw an exception: `addError(text, errorMessage(e))`, mark the line with `field-error` and return false.
   5. If all went well: add the text to `commands` and return true.
2. `handleConstructor(text)`:
   1. Build a new list = `commands` + text.
   2. Call `Interpreter.prepareVariables` with that list (it detects duplicate names).
   3. Get the name with `extractName(text)`. If the map doesn't contain that name, it wasn't a constructor: return false.
   4. If it does: add the name to `manualNames` and call `showCard(name, zeroValue, true)`. Return true.
   5. Optional: if the text contains `matrix(` or `vector(` in lowercase, throw an error saying it must be capitalized (otherwise it falls through as a normal command and fails on Compile).
3. `isDirectAssignment(text)`: true if there is an `=` and what follows, trimmed, starts with `[`.
4. `handleDirectAssignment(text)`:
   1. Call `Interpreter.interprete` with a list containing only that text.
   2. Take the result of the first `CompiledResult`. If it is a Matrix or Vector: `showCard(extractName(text), result, false)`.
   3. If the name was in `manualNames` (it was a constructor before), remove it from there.
5. `extractName(text)`: whatever is before the first `=`, trimmed; if it doesn't start with a letter or has other characters than letters and digits, return null.

## Task 8: Reading the cells

1. `readManualValues`: create an empty map. For each name in `manualNames`:
   1. If it is a matrix: for each row, a list of Double; each cell is converted with `Interpreter.evaluateEntry(text)`. Build Vectors from the rows and then `Matrix.build`.
   2. If it is a vector: list of Double with `evaluateEntry` on each cell and create the Vector.
   3. If a cell fails: mark it with `field-error` and throw a `CommandException` with a message saying where it happened, for example "A, row 2, column 1: " + original message.
   4. Put the value in the map.
2. Return the map.

## Task 9: Compile

1. If `currentLine` has text: call `onEnter(currentLine)`. If it returned true, lock it and create a new line (the same thing the Enter action does). If it returned false, stop here (the error was already shown).
2. Clear `outputBox`.
3. Call `readManualValues` inside a try. If it fails: `addError("Compile", message)` and stop.
4. Build `declarations` = the commands in `commands` whose name (`extractName`) is in `manualNames`.
5. In a try: `Interpreter.interprete(declarations, manualValues)`. If it fails, `addError` and stop.
6. Go through `commands` in order. For each one:
   - If it is a declaration: `addOutput(command, toDisplay(manualValues for that name))`.
   - If not: in a try, `Interpreter.interprete` with a list containing only that command; take the first result; `addOutput(command, toDisplay(result))`. If the command has a name (`extractName` is not null), the result is a Matrix or Vector, and the name is **not** in `manualNames`: `showCard(name, result, false)`. In the catch: `addError(command, errorMessage(e))` and continue with the next one.
7. At the end: `drawPlane`.
8. Test with the mock-up's example: `A = Matrix(2, 2)` (fill in 1 2 3 4), `B = Vector(2)` (1 1), `C = Vector(2)` (2 -1), `D = adj(A) * (1 / det(A)) * A`, `S = solveSystem(A, B)`. You should get D = identity and S = [-1, 1].

`INTERFACE.md` section 5 explains why the manual values are loaded once and the rest is run one command at a time.

## Task 10: Cartesian plane

1. `drawPlane`:
   1. Remove all children of `graphPane`.
   2. Width and height = current size of `graphPane`. Center = half of each.
   3. Grid: vertical lines every `SCALE` px from the center to both sides, and the same for horizontal ones. Gray, dashed (Line's dash pattern).
   4. Axes: one horizontal and one vertical line through the center, black and thicker. Labels "x" (right end of the x axis) and "y" (top of the y axis).
   5. For each name in `plotted`: look up the value in `Interpreter.getMemory()`. If it is a 2-dimensional Vector, call `drawVector(name, x, y)`. If it doesn't exist or isn't 2D, skip it.
2. `drawVector(name, x, y)`:
   1. End point in pixels: `centerX + x * SCALE` and `centerY - y * SCALE` (**minus**: on screen y grows downward).
   2. Red line from the center to the end point, width 2.
   3. Arrowhead: two short lines (10 px) from the end point, rotated ±25° from the direction opposite to the vector (use the angle from the arctangent of the differences).
   4. Label with the name, red, slightly offset from the tip.
3. Clip the drawing to the Pane's area (give it a clip rectangle of the same size, bound to its width and height) so nothing spills out.
4. In `initialize`: listen to `graphPane`'s width and height changes and call `drawPlane` on each change.
5. Optional: to move arrows as soon as a cell changes (instead of on the next Compile), in `drawPlane` read the cells with `evaluateEntry` for names in `manualNames`.

## Task 11: Clear

1. Call `Interpreter.resetMemory()` (once Ian pushes it; until then leave it commented out with a TODO).
2. Empty `commands`, `manualNames`, `matrixCells`, `vectorCells`, `cards`, `plotted`.
3. Remove the children of `matricesBox`, `vectorsBox`, `outputBox`, `commandsBox`.
4. `addInputLine` and `drawPlane`.

## Task 12: Final tests (checklist)

- [ ] Enter on an empty line does nothing.
- [ ] `A = Matrix(2, 3)` creates a zero-filled 2×3 card in Matrices.
- [ ] `v = Vector(3)` creates a card in Vectors **without** a radio button.
- [ ] `A = Matrix(2, 2)` twice → duplicate error, the line doesn't advance.
- [ ] `pi = Vector(2)` → constant error.
- [ ] `X = [[1, 0], [0, 1]]` creates a card with those values.
- [ ] `X = [1, 2` → error, the line doesn't advance.
- [ ] A cell with `sqrt(2)/2` works; a cell with `B` or `hello` → red cell + error, doesn't compile.
- [ ] A failing command in the middle doesn't stop the others.
- [ ] `inv([[1,2],[2,4]])` shows the singular matrix error.
- [ ] `1e3` shows an error even though its message is null.
- [ ] Compile with the last line not entered still includes it.
- [ ] Several radio buttons selected → several arrows; deselecting removes them.
- [ ] Resizing the window redraws the plane centered.
- [ ] Clear leaves everything as if just opened.
