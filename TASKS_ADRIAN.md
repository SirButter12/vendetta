# Adrian's tasks: the FXML in Scene Builder

Files: `primary.fxml` and `vendetta.css` in `src/main/resources/linear/vendetta/vendetta/`.

Read first: `INTERFACE.md`, especially **section 1 (layout)** and **section 11 (fx:id, onAction and style class names you must use)**.

Cards, Input lines, Output entries and the plane drawing are created by the controller in code. You build **the empty containers** where all of that lives, plus the CSS for how it looks.

Rules: before each task `git pull`. After each task: small commit and push. Don't edit the controller; ask Matthews.

---

## Task 0: Set up

1. Pull. Make a backup copy of the current `primary.fxml` outside the repo (the old spinner design is no longer used).
2. Open Scene Builder (a version compatible with JavaFX 21).
3. File → New. Save it over `primary.fxml` (replace).
4. Keep `INTERFACE.md` section 11 at hand: fx:ids and method names must be **identical**.

## Task 1: Root

1. Drag a **VBox** from the Library panel (Containers section) onto the canvas.
2. Code panel (right): fx:id = `rootPane`.
3. Layout panel: Pref Width 1030, Pref Height 580. Min Width 900, Min Height 520.
4. Properties panel → Stylesheets: add `vendetta.css`.
5. Bottom-left panel, **Controller** section: Controller class = `linear.vendetta.vendetta.PrimaryController`.
6. VBox Spacing: 4. The light gray gap between areas in the mock-up comes from the root's background: give the root a light gray background and each area the `panel` class (green).

## Task 2: Top row

1. Drag an **HBox** inside `rootPane`. Layout: Vgrow = ALWAYS, Pref Height 320, Spacing 10.
2. **Matrices column:** drag a VBox inside the HBox. Pref Width 150, Style Class `panel`, Alignment TOP_CENTER, top Padding 10.
   1. Inside: a Label with text "Matrices", Style Class `panel-title`.
   2. Below it: a **ScrollPane**. Layout: Vgrow ALWAYS. Properties: Fit To Width checked, Hbar Policy NEVER.
   3. Inside the ScrollPane: a VBox with fx:id `matricesBox`, Spacing 20, Alignment TOP_CENTER, Padding 10.
3. **Vectors column:** same as the previous one, but the Label says "Vectors" and the inner VBox is named `vectorsBox`. Pref Width 140.
4. **Plane:** drag a **Pane** into the HBox after the columns. fx:id `graphPane`. Layout: Hgrow ALWAYS, Min Width 300, Min Height 250. Style Class `graph-pane`. **Leave it empty**.

## Task 3: Bottom row

1. Drag another **HBox** inside `rootPane`, below the first one. Vgrow ALWAYS, Pref Height 256, Spacing 10.
2. **Left panel:** VBox, Pref Width 290, Style Class `panel`.
   1. Inside, an HBox (Spacing 2) with two **Buttons**:
      - Text "Clear", fx:id `clearButton`, On Action `onClear`, Style Class `tool-button`, Max Width MAX_VALUE, Hgrow ALWAYS, Pref Height 38.
      - Text "Compile", fx:id `compileButton`, On Action `onCompile`, everything else the same.
   2. Below: a **ScrollPane** with fx:id `outputScroll`, Vgrow ALWAYS, Fit To Width checked.
   3. Inside the ScrollPane: VBox with fx:id `outputBox`, Spacing 10, Padding 8. **Empty**.
3. **Input panel:** a **ScrollPane** with fx:id `inputScroll`, Hgrow ALWAYS, Fit To Width checked, Style Class `panel`.
   1. Inside: a VBox, Padding 8.
   2. Inside that VBox: Label "Input:", Style Class `panel-title` (left-aligned).
   3. Below the Label: VBox with fx:id `commandsBox`, Spacing 0. **Empty** (the controller creates the lines).

## Task 4: Check

1. Open View → Show Document and check the tree looks like this:
   - rootPane (VBox)
     - Top HBox: VBox(Label Matrices, ScrollPane → matricesBox) · VBox(Label Vectors, ScrollPane → vectorsBox) · graphPane
     - Bottom HBox: VBox(HBox[clearButton, compileButton], outputScroll → outputBox) · inputScroll → VBox(Label Input:, commandsBox)
2. View → Show Sample Controller Skeleton: confirm the 10 fx:ids and the `onClear` and `onCompile` methods appear. Compare with `INTERFACE.md` section 11. (Don't copy that code; it's only for checking names.)
3. Preview → Show Preview in Window. Resize the window: the plane and Input should grow, the Matrices/Vectors columns and the output panel should not get squashed.
4. Save.

## Task 5: CSS (`vendetta.css`)

Define every class from the "Style classes" table in `INTERFACE.md` section 11. Color reference (from the mock-up):

| Element | Color |
|---|---|
| Area background (`panel`) | dark green, approx. #1F3A14 |
| Gap between areas (root background) | light gray, approx. #C9CCD1 |
| Text | white |
| Buttons | light gray #D9DCE0 with dark gray text |
| Plane | white |
| Drawn vectors / errors | red, approx. #E53935 (error text: light red #FF8A80) |

Common pitfalls:

1. ScrollPanes paint their inside (viewport) white by default: give the viewport a green or transparent background too, not just the ScrollPane, or they'll look white.
2. TextFields come with a white border and background by default: for `cell-field` and `command-line` remove the background, border and blue focus highlight.
3. `command-line` only has a dotted bottom border (like the Input rows in the mock-up).
4. The brackets (`bracket-left` / `bracket-right`) are Regions with a 2 px white border on three sides only: left+top+bottom for the left one; right+top+bottom for the right one. ~8 px wide.
5. `field-error` must win over `cell-field` and `command-line` (put it at the end of the file).

## Task 6: Hand off

1. Commit `primary.fxml` and `vendetta.css` (make sure you don't commit `target/` or `.idea/`).
2. Tell Matthews. He should first run it with empty methods (his Task 2) to confirm the names match.
3. Once Matthews has cards and output working, review the CSS together with real data and adjust sizes.
