# Vendetta interface specification

How the graphical interface must behave and how it talks to the API.
For the API itself see `README.md`. For the work split see `TASKS_MATTHEWS.md` (controller) and `TASKS_ADRIAN.md` (FXML / Scene Builder).

Contents:

1. [Layout](#1-layout)
2. [State the interface keeps](#2-state-the-interface-keeps)
3. [Pressing Enter on an Input line](#3-pressing-enter-on-an-input-line)
4. [Card cells](#4-card-cells)
5. [Pressing Compile](#5-pressing-compile)
6. [Displaying results](#6-displaying-results)
7. [Pressing Clear](#7-pressing-clear)
8. [Vector radio buttons and the Cartesian plane](#8-vector-radio-buttons-and-the-cartesian-plane)
9. [Error handling](#9-error-handling)
10. [API calls summary](#10-api-calls-summary)
11. [FXML ↔ controller contract](#11-fxml--controller-contract)
12. [Decisions not in the original spec](#12-decisions-not-in-the-original-spec)
13. [Waiting on the API](#13-waiting-on-the-api)

---

## 1. Layout

The window has 6 areas:

| Area | Where | What it does |
|---|---|---|
| **Matrices** | top left | One "card" per matrix: `A = [ cells ]` |
| **Vectors** | top, second column | One card per vector: `B = [ cells ]` + a radio button to plot it |
| **Cartesian plane** | top right | Grid with x and y axes; draws the selected 2D vectors |
| **Clear / Compile buttons** | bottom left, top | |
| **Output** | bottom left, below the buttons | For each command: the command and, below it, its result (or the error) |
| **Input** | bottom right | Command lines, one per row |

A card is: the label `NAME =`, a left bracket, a grid of cells (TextFields), a right bracket. Vectors are shown as a single column.

---

## 2. State the interface keeps

| State | Meaning |
|---|---|
| Command list | Every accepted command, in the order the user entered them |
| Manual names | Variables declared with a constructor. Their cells are editable and their values are sent to the API as `manualValues` |
| Cards | One card per variable shown in Matrices / Vectors, with its cells |
| Plotted vectors | Names of the vectors whose radio button is selected |
| Current line | The last (active) Input line |

---

## 3. Pressing Enter on an Input line

1. Take the text of the current line, trimmed. If it is empty, do nothing.
2. Decide what kind of command it is:

   **a) Constructor** — `A = Matrix(2, 2)` or `B = Vector(2)`
   - Call `Interpreter.prepareVariables(commandList + this line)`. Passing the whole list lets the API detect a name declared twice.
   - If the returned map contains this line's name, it is a constructor: add a card to Matrices or Vectors (depending on whether the value is a `Matrix` or a `Vector`) with all cells at **0** and the requested size. The cells are editable. Remember the name as a *manual name*.
   - If the map doesn't contain it, it's not a constructor: continue with b) / c).

   **b) Direct assignment** — `A = [1, 2, 3]` or `A = [[1, 0], [0, 1]]` (there is an `=` and the right side starts with `[`)
   - Evaluate it like any other command: `Interpreter.interprete([this line])`.
   - Add the card to its section with the resulting values.

   **c) Any other command** — `D = adj(A) * A`, `det(A)`, ...
   - Not evaluated yet. It runs on Compile.

3. Add the text to the **command list**.
4. Lock the line (not editable), create a new empty line below it and give it focus.
5. If step 2 threw an error: show the error in Output, mark the line red, **do not** add it to the list and **do not** create a new line. The user fixes the same line and presses Enter again.

Locking accepted lines keeps the command list and what the user sees in sync.

---

## 4. Card cells

- Each cell accepts **any expression that evaluates to a number**: `3`, `-1.5`, `sqrt(2)/2`, `pi/4`, `det(M1)`.
- Cells are read with `Interpreter.evaluateEntry(text)`. It already rejects everything that isn't a valid number: vectors, matrices, assignments, syntax errors, `NaN`, infinity.
- When a cell is invalid it is marked red; the mark disappears as soon as the user edits it.
- Initial cell text is the number formatted with `Vector.formatNumber` (`0`, `2`, `0.3333`).

---

## 5. Pressing Compile

1. **Pending last line.** If the current line has text that was never entered, process it exactly like Enter (section 3), including creating a zero-filled card if it's a constructor. If that fails, stop.
2. **Clear the Output.**
3. **Read the manual values.** For each manual name, read every cell with `evaluateEntry` and build a `Vector` (list of doubles) or a `Matrix` (`Matrix.build` with one Vector per row). This gives the map `name → Matrix/Vector` called *manualValues*.
   If any cell is invalid: mark it red, show the error in Output (saying which variable, row and column) and **stop**. Everything else depends on these values.
4. **Load the manual values.** Call `Interpreter.interprete(declarations, manualValues)` once, where *declarations* are only the constructor commands from the list. This puts the hand-typed values into memory. If it fails, show the error and stop.
5. **Run the commands one at a time**, in the list's order:
   - Constructor command: show the command and its value from *manualValues*.
   - Any other command: call `Interpreter.interprete([command])`, take the first result, show the command and the result. If it throws, show the command and the error, then **continue with the next command**.
   - If the command is `NAME = ...`, the result is a `Matrix` or `Vector`, and `NAME` is not a manual name: create or update the card for `NAME` in its section, with read-only cells. (That's why `D` appears in Matrices and `S` in Vectors in the mock-up.)
6. **Redraw the Cartesian plane.**

### Why steps 4 and 5 are separate

- `interprete` parses the whole list before running anything, so one syntax error would stop every command. Running commands one by one is the only way for one error not to block the rest.
- `interprete(commands, manualValues)` reloads the manual values every time it's called. If it were called for every command, a later reassignment such as `A = A * 2` would be undone by the next call. Loading them once, first, avoids that.

### Example (the mock-up)

| Input line | Cells | Output |
|---|---|---|
| `A = Matrix(2, 2)` | 1 2 / 3 4 | the matrix A |
| `B = Vector(2)` | 1 / 1 | [1, 1] |
| `C = Vector(2)` | 2 / -1 | [2, -1] |
| `D = adj(A) * (1 / det(A)) * A` | — | identity 2×2, card D appears in Matrices |
| `S = solveSystem(A, B)` | — | [-1, 1], card S appears in Vectors |

---

## 6. Displaying results

The controller needs one method that turns any result into something the UI can show (`toDisplay`):

| Result | Shown as |
|---|---|
| `Double` | A label with `Vector.formatNumber(value)` |
| `Vector` | A column between brackets (one number per row), like the vector cards |
| `Matrix` | A grid between brackets |
| anything else | A label with `toString()` |
| error | A red label: `Error: message` |

Each Output entry is: the command (bold) and below it the displayed result or error. The Output scrolls to the bottom when something is added.

---

## 7. Pressing Clear

Leaves everything as if the app had just been opened:

- empty command list, no manual names, no plotted vectors;
- no cards in Matrices or Vectors;
- empty Output;
- Input with a single empty line;
- empty Cartesian plane (grid and axes only);
- memory reset with `Interpreter.resetMemory()`, so only the constants `pi`, `e`, `i` survive (**this method doesn't exist yet**, see section 13).

---

## 8. Vector radio buttons and the Cartesian plane

- Only vector cards of **dimension 2** have a radio button. Vectors of 3 or more dimensions are not plotted and get no radio button.
- Several radio buttons can be selected at the same time, so they **don't** use a ToggleGroup.
- Selecting or deselecting one redraws the plane.
- The plane shows a square grid, the x and y axes through the center, and each selected vector as a red arrow from the origin to `(x, y)`, with its name next to the tip.
- Vector values come from the API memory (`Interpreter.getMemory()`), which is updated on Compile. Editing a cell moves the arrow on the next Compile.
- The plane is redrawn when the window is resized, so the origin stays centered.
- Screen y grows downward, so a point `(x, y)` is drawn at `center + x·scale` horizontally and `center − y·scale` vertically.

---

## 9. Error handling

- Catch `Exception` around every API call, not only `CommandException` (the API also throws `IllegalArgumentException`, `ArithmeticException` and `MultipleOrNoSolutionException`).
- Show the exception's message. If it is `null`, show the exception's class name.
- Error on Enter: red line, error in Output, line not accepted.
- Invalid cell on Compile: red cell, error in Output, compile stops.
- Error in a command on Compile: error in Output under that command, compile continues.
- Case-sensitivity is a common user mistake: `matrix(2, 2)`, `Adj(A)` and `Det(A)` are errors. Optionally, detect `matrix(` / `vector(` in lowercase on Enter and show a friendlier message.

---

## 10. API calls summary

| UI event | API call | Why |
|---|---|---|
| Enter (any line) | `prepareVariables(allCommands + line)` | Is it a constructor? What size? Duplicates? |
| Enter (direct assignment) | `interprete([line])` | Get the values to fill the card |
| Compile, each cell | `evaluateEntry(cellText)` | Turn cell text into a number |
| Compile, once | `interprete(declarations, manualValues)` | Load hand-typed values into memory |
| Compile, each other command | `interprete([command])` | Run it, independently of the others |
| Plane redraw | `getMemory().get(name)` | Current value of each plotted vector |
| Clear | `resetMemory()` *(pending)* | Back to constants only |

Display helpers: `Vector.formatNumber`, `Matrix.convertToMatrix(Vector)` (vector → column matrix, handy for displaying a vector vertically), `getNumRows / getNumCols / get(r, c)`, `getDimension / get(i)`.

---

## 11. FXML ↔ controller contract

Matthews and Adrian must use **exactly** these names. If one of you changes a name, tell the other. If the FXML references an `onAction` method the controller doesn't have, the app crashes on startup.

**Controller:** `linear.vendetta.vendetta.PrimaryController`
**FXML:** `src/main/resources/linear/vendetta/vendetta/primary.fxml`
**Stylesheet:** `vendetta.css` (same folder)

### fx:id

| fx:id | Type | What it is |
|---|---|---|
| `rootPane` | VBox | Window root |
| `matricesBox` | VBox | Container for matrix cards (inside a ScrollPane) |
| `vectorsBox` | VBox | Container for vector cards (inside a ScrollPane) |
| `graphPane` | Pane | Cartesian plane area (empty in the FXML; the controller draws) |
| `clearButton` | Button | Clear button |
| `compileButton` | Button | Compile button |
| `outputBox` | VBox | Output container (inside `outputScroll`) |
| `outputScroll` | ScrollPane | Output scroll |
| `commandsBox` | VBox | Container for the Input lines (empty in the FXML; the controller creates the lines) |
| `inputScroll` | ScrollPane | Input scroll |

### onAction methods

| Button | On Action |
|---|---|
| `clearButton` | `onClear` |
| `compileButton` | `onCompile` |

### Style classes

Adrian defines them in `vendetta.css`; Matthews assigns them to the nodes he creates in code.

| Class | Applied to | Look |
|---|---|---|
| `panel` | Background of each area | Dark green, white text |
| `panel-title` | Titles "Matrices", "Vectors", "Input:" | White, centered |
| `tool-button` | Clear and Compile | Light gray, dark text, no rounded corners |
| `graph-pane` | `graphPane` | White background |
| `card` | HBox of each card | Small horizontal spacing, vertically centered |
| `card-name` | Label `A =` | White, bold |
| `cell-field` | TextField of each cell | Transparent background, no border, white centered text, ~35 px wide |
| `cell-readonly` | Cells of computed results | Same as `cell-field` but slightly dimmer |
| `field-error` | Cell or line with an error | Red border or background |
| `bracket-left` / `bracket-right` | Region on each side of the grid | 2 px white line on one side plus top and bottom (bracket shape), ~8 px wide |
| `command-line` | TextField of each Input line | Transparent background, bold white text, dotted bottom border |
| `output-command` | Label of the command in Output | White, bold |
| `output-value` | Result node | White, indented |
| `output-error` | Error label | Light red |
| `vector-radio` | RadioButton of each vector | White |

---

## 12. Decisions not in the original spec

These were chosen to fill gaps; change them here if the team wants something else.

| Decision | Reason |
|---|---|
| Accepted Input lines are locked (not editable) | Keeps the command list in sync with what's on screen |
| Cards of computed results (`D`, `S`, direct assignments) are read-only | Their values come from commands; editing them would be overwritten on Compile |
| An invalid cell stops the whole Compile | Every later command depends on the manual values |
| Radio buttons without ToggleGroup | Several vectors can be plotted at once |
| A manual name that is later directly assigned (`A = [...]`) stops being manual | The new value replaces the hand-typed one |

---

## 13. Waiting on the API

| Item | Needed for |
|---|---|
| `Interpreter.resetMemory()` | Clear button |
| Removing the demo variables (`A`, `B`, `C`, `D`, `M1`, ...) from memory at startup | So variables only exist if the user creates them |
| Returning the Gauss-Jordan steps in the result | Showing "R2 → R2 − 3(R1) ..." in Output, like the mock-up. Until then only the final vector is shown |
