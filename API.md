# Vendetta API

> ## IMPORTANT: DO NOT DELETE `coconut.jpg` ⚠
>
> `src/main/resources/linear/vendetta/vendetta/coconut.jpg` is the API's emotional support coconut.
> If it's gone, `App.checkCoconut()` can't find it, calls `jarvisPullTheTrigger()`, and the program recurses forever until it dies with a `StackOverflowError`.
>
> **Don't delete it, don't rename it, don't move it. Don't add it to `.gitignore`.**

The API is the "brain" of Vendetta: it reads commands like `D = adj(A) * A` and computes them.
It's maintained by Ian. If you need something it doesn't do, ask him instead of editing it (rules 2 and 4 in `daRules.txt`).
For how the window uses it, see `INTERFACE.md`.

This file has two parts:

- **Part 1: What you Matthews and Adrian need to know.** Short. Read this.
- **Part 2: How the machine works.** Every class and every detail. Read it only when you need it.

---

# Part 1: What you Matthews and Adrian need to know because I know you guys can barely read

## 1.1 What the API does, in one minute

You give it **text**. It gives you back **a value**.

```
"det([[1, 2], [3, 4]])"   ->  -2.0
"[1, 2] + [3, 4]"         ->  [4, 6]
"A = [[1, 0], [0, 1]]"    ->  the matrix, and A is now saved
```

Every value it returns is one of these:

| Java type | What it is | Example command |
|---|---|---|
| `Double` | a number | `det(A)`, `2 + 2` |
| `Vector` | a vector | `[1, 2]`, `solveSystem(A, b)` |
| `Matrix` | a matrix | `[[1, 2], [3, 4]]`, `inv(A)` |
| `SolutionVector` | a vector **plus the steps** used to get it | `gaussJordan(A, b)` |
| `PolarForm` | a vector written as length + angles (just show its `toString()`) | `polarize(v)` |

The API remembers variables (`A`, `B`, `M2`...) between calls. That memory is shared by the whole program.

## 1.2 The only methods you need

### `Interpreter` (package `api.dsl`), all static

| Method | What it does | When the UI uses it |
|---|---|---|
| `prepareVariables(List<String> commands)` | Finds the `NAME = Matrix(r, c)` / `NAME = Vector(n)` lines and returns `name → zero-filled Matrix/Vector` of that size | Enter: "is this line a constructor? how big is its card?" |
| `interprete(List<String> commands)` | Runs commands, returns one `CompiledResult` per command | Compile: run each normal command |
| `interprete(List<String> commands, Map<String, Object> manualValues)` | Same, but first loads the values typed in the cards | Compile: load the hand-typed values once |
| `evaluateEntry(String text)` | Turns one cell's text (`3`, `sqrt(2)/2`, `pi/4`) into a `double`, or throws | Reading each card cell |
| `getMemory()` | The map `name → value` of every variable | Drawing vectors on the plane |
| `resetMemory()` | Deletes every variable; only `pi` and `e` are left | Clear button |

### `CompiledResult` (package `api.dsl`)

| Method | Returns |
|---|---|
| `getCommand()` | The command text you passed |
| `getResult()` | The value (`Double`, `Vector`, `Matrix`, `SolutionVector` or `PolarForm`) as `Object` |

### `helpers` (package `api`)

| Method | What it does |
|---|---|
| `formatNumber(double x)` | The official way to show a number: `2.0 → "2"`, `1/3 → "0.3333"`, `-0 → "0"`. Always uses `.` |

### `Matrix` and `Vector` (package `api.linear_algebra`)

Indexes start at **0**.

| Method | What it does |
|---|---|
| `matrix.getNumRows()`, `matrix.getNumCols()` | Size |
| `matrix.get(row, col)` | One value |
| `Matrix.build(List<Vector> rows)` | Builds a matrix from its rows (one `Vector` per row) |
| `Matrix.convertToMatrix(vector)` | Vector → column matrix (handy to draw a vector vertically) |
| `new Vector(List<Double> values)` | Builds a vector |
| `vector.getDimension()` | Number of components |
| `vector.get(i)` | One component |

### `SolutionVector` (package `api.linear_algebra`)

It's what `gaussJordan(A, b)` returns. It **is** a `Vector` (it extends it), so everything above works on it. It also has:

| Method | Returns |
|---|---|
| `getSteps()` | `List<String>` with every row operation, in order |

Each step looks like this (rows are numbered from **0**, and each string ends with `\n`):

```
R_1 -> R_1 + -0.5 * R_0
R_1 -> R_1 * 0.4
R_0 -> R_0 + -1 * R_1
R_0 -> R_0 * 0.5
```

### Errors

| Exception | Meaning |
|---|---|
| `CommandException` | The user wrote something wrong. The message is written for the user |
| `MultipleOrNoSolutionException` | The system has no solution or infinitely many |
| `IllegalArgumentException`, `ArithmeticException` | Wrong sizes, singular matrix, etc. Message is readable |

**Rule: always `catch (Exception e)`** and show `e.getMessage()`. If it's `null`, show the class name.

## 1.3 How the UI talks to the API (the whole flow)

```
User presses Enter on  "A = Matrix(2, 2)"
  -> prepareVariables(allCommands + line)  -> {A = 2x2 zeros}  -> draw an editable card

User presses Compile
  1. read every editable cell with evaluateEntry(...)  -> build Matrix/Vector values
  2. interprete(declarations, manualValues)            -> load them into memory ONCE
  3. for each other command: interprete(List.of(cmd))  -> show result or error, keep going
  4. redraw the plane using getMemory()

User presses Clear
  -> resetMemory()
```

Showing a result:

```java
Object r = Interpreter.interprete(List.of(command)).get(0).getResult();

if (r instanceof SolutionVector s) { /* show it like a Vector, and s.getSteps() if you want */ }
else if (r instanceof Matrix m)    { /* grid */ }
else if (r instanceof Vector v)    { /* column */ }
else if (r instanceof Double d)    { /* helpers.formatNumber(d) */ }
else                               { /* r.toString(), e.g. a PolarForm */ }
```

Check `SolutionVector` **before** `Vector` (otherwise `Vector` catches it first).

## 1.4 Five things that will bite you

1. **Never `import java.util.*`.** Our `Vector` clashes with `java.util.Vector`. Import `List`, `Map`, `ArrayList`... one by one.
2. **One syntax error in the list stops the whole list.** That's why Compile runs commands one at a time.
3. **Everything is case-sensitive.** `matrix(2, 2)`, `Adj(A)` and `Det(A)` are errors. `Matrix` and `Vector` are capitalized; functions are lowercase except `solveSystem` and `gaussJordan`.
4. **A constructor line can't be run alone.** `interprete(["A = Matrix(2, 2)"])` throws. It needs its values through `manualValues`.
5. **Normal commands can return `NaN` or `Infinity`** (`sqrt(-1)`, `1/0`). Only `evaluateEntry` rejects them.

---

# Part 2: How the machine works

## 2.1 The pipeline

A command goes through three stages:

| Stage | Class | Input → Output |
|---|---|---|
| 1. Tokenize | `Tokenizer` | `"x = det(M)*2"` → `NAME EQUALS FUNCT LPAREN NAME RPAREN STAR NUMBER END` |
| 2. Parse | `Parser` | tokens → a tree of `Node` |
| 3. Evaluate | `Evaluator` (+ `evaluators/*`) | tree → `Double` / `Vector` / `Matrix` / ... |

`Interpreter` glues the three together and owns the memory.

## 2.2 Packages and classes

### `linear.vendetta.vendetta.api`

| Class | Purpose |
|---|---|
| `helpers` | Static utilities: argument checks for language functions, `formatNumber`, the text of row operations |

### `linear.vendetta.vendetta.api.dsl`

| Class | Public use? | Purpose |
|---|---|---|
| `Interpreter` | **yes** | Entry point and memory |
| `CompiledResult` | **yes** | One command + its result |
| `CommandException` | **yes** | User error (extends `RuntimeException`) |
| `Tokenizer`, `Token` | internal | Text → tokens |
| `Parser`, `Node` | internal | Tokens → tree |
| `Evaluator` | internal | Walks the tree; dispatches each function name |
| `evaluators/ArithmeticEvaluator` | internal | `+ - * / ^` and `dot` |
| `evaluators/MatrixEvaluator` | internal | `adj inv trans det triangulize` |
| `evaluators/VectorEvaluator` | internal | `cross project length direction polarV polarize` |
| `evaluators/MathEvaluator` | internal | Trig, logs, `sqrt`, `mod` |

### `linear.vendetta.vendetta.api.linear_algebra`

| Class | Purpose |
|---|---|
| `Vector` | Vector of doubles |
| `Matrix` | Matrix of doubles, stored as a list of row `Vector`s |
| `SystemMatrix` | `Matrix` + right-hand side `b`; solves `Ax = b` |
| `SolutionVector` | `Vector` + the list of steps that produced it |
| `PolarForm` | Record: length + angles. Display only |
| `TransformableVector` | A `Vector` that accepts row operations (used as the `b` of a system during elimination) |
| `MultipleOrNoSolutionException` | No solution / infinitely many |
| `operable`, `elementalOperations` | Interfaces: arithmetic (`add`, `subtract`, `scalarMult`) and row operations (`rowMultiply`, `rowSwap`, `rowComp`) |

## 2.3 Tokenizer

Walks the text character by character. Symbols `+ - * / ^ ( ) [ ] , =` and spaces end the current word. Each word becomes:

| Word | Token |
|---|---|
| A function name (list in `Tokenizer.functions`) | `FUNCT` |
| `Matrix` or `Vector` | `CONSTRUCTOR` |
| Letter followed by letters/digits (`A`, `M2`) | `NAME` |
| `2`, `2.5`, `1e3`, `2.5E-4` | `NUMBER` |
| Anything else | `CommandException` with an explanation |

Details:

- Scientific notation works: `1e3`, `1.5e-4`. The `-` in `1e-4` belongs to the number; `2e - 3` with spaces is an error ("needs a whole exponent right after the e").
- `.5` and `5.` are rejected with a hint ("Write 0.5").
- `2x` is rejected with "To multiply, write 2 * x".
- Accented letters, `_`, `@`, `#`... are rejected.
- The token list always ends with `END`.

## 2.4 Parser

Recursive descent, one method per rule, lowest to highest precedence:

```
statement   = NAME '=' expression | expression
expression  = term (('+' | '-') term)*            left-assoc
term        = unary (('*' | '/') unary)*          left-assoc
unary       = '-' unary | exp                     -2^2 = -(2^2) = -4
exp         = factor ('^' unary)?                 right-assoc: 2^3^2 = 2^9
factor      = NUMBER | NAME | '(' expression ')'
            | FUNCT '(' [expression (',' expression)*] ')'
            | '[' expression (',' expression)* ']'
constructor = NAME '=' (Matrix | Vector) '(' NUMBER (',' NUMBER)* ')'   whole command only
```

- If `Matrix`/`Vector` appears anywhere, the **whole** command must be `NAME = Matrix(n, m)` / `NAME = Vector(n)`, with plain numbers as sizes. Otherwise: error.
- `-5` is folded into the number `-5`; `-A` becomes `-1 * A`.
- Leftover tokens are an error: `det(M) det(M)` → "Unexpected 'det'".

### Node kinds

`Node` is an immutable record `(kind, text, children)`:

| Kind | Example | `text` | `children` |
|---|---|---|---|
| `NUMBER` | `2.5` | the number | — |
| `NAME` | `M1` | variable name | — |
| `CALL` | `det(M1)` | function name | arguments |
| `BINARY` | `A + B` | operator | 2 operands |
| `ASSIGN` | `x = A + B` | variable name | 1: the value |
| `DECLARE` | `M = Matrix(2, 3)` | variable name | 1: a `CALL` to `Matrix`/`Vector` with the sizes |
| `LIST` | `[1, 2]` | `"[]"` | the elements |

## 2.5 Evaluator

`Evaluator.evaluateNode(node)` recurses through the tree:

| Node | What happens |
|---|---|
| `NUMBER` | `Double.parseDouble` |
| `NAME` | Looks it up in memory; missing → "X is used but never initialized!" |
| `ASSIGN` | Evaluates the value, saves it with `Interpreter.assignToMemory`, returns it |
| `BINARY` | Evaluates both sides, sends them to `ArithmeticEvaluator` |
| `CALL` | Evaluates the arguments, then a `switch` on the name sends them to the right evaluator |
| `LIST` | All numbers → `Vector` (NaN/∞ rejected). All vectors → `Matrix` (one row each, same sizes). Mixed → error |
| `DECLARE` | Error: constructors can't be evaluated |

To add a function: add its name to `Tokenizer.functions`, a `case` in `Evaluator.call`, and the method in the right evaluator.

## 2.6 Interpreter in detail

### Memory

- One static `HashMap<String, Object>`: `name → value`. Shared by every call (and by `Evaluator`, which holds the same map).
- Starts with the constants `pi` and `e`. Constants can't be reassigned (`CommandException`).
- Variables stay until overwritten or until `resetMemory()`.
- `getMemory()` returns the **live** map, not a copy. Read it; write with `assignToMemory` so constants stay protected.

### `prepareVariables(commands)`

1. For each command that contains `Matrix`/`Vector`, parses it as a declaration (everything else is ignored, not even parsed).
2. Validates: sizes are whole positive numbers, the name isn't a constant, no name is declared twice **inside the given list**.
3. Only if everything is valid: adds each **new** name to memory with its zero value. Names already in memory keep their value.
4. Returns `name → zero Matrix/Vector`, in command order (`LinkedHashMap`).

Example: `["A = Matrix(2, 2)", "B = Vector(3)", "C = A"]` → `{A = 2×2 zeros, B = [0, 0, 0]}`.

### `interprete(commands)`

1. Parses **every** command first. One syntax error → exception, nothing runs.
2. If any command is a declaration → exception ("... its values must be filled in by hand before compiling").
3. Runs them in order. If one fails at runtime, the run stops there; earlier assignments stay in memory.

### `interprete(commands, manualValues)`

1. If `manualValues` is `null`, it's the same as `interprete(commands)`.
2. Parses everything.
3. Checks that every declaration has a value in the map with **exactly** the declared shape. Errors: "Missing the values of A", "A must be a Matrix(2, 2)". Memory is untouched if this fails.
4. Copies **every** entry of the map into memory (deep copies).
5. Runs the commands. For a declaration, the result is the value now in memory.

Because step 4 happens on every call, calling it again would overwrite any later reassignment of those names. That's why the UI calls it **once** per Compile.

### `evaluateEntry(text)`

Parses and evaluates one expression that must give a finite number. Throws for:

| Input | Message |
|---|---|
| `A = 3`, `A = Matrix(2, 2)` | "'...' is a declaration, only values are allowed here" |
| `B` (a vector) | "'B' is not a number" |
| `sqrt(-1)`, `1/0` | "'...' does not give a valid number" |
| syntax error, unknown variable | the usual message |

It doesn't modify memory.

### `resetMemory()`

Clears the map and puts back `pi` and `e`.

## 2.7 Vector

Indexes from 0. Arithmetic returns **new** vectors; `set`/`setValues` modify this one.

| Method | Description |
|---|---|
| `Vector(List<? extends Number>)` | Throws if empty or has nulls |
| `Vector(Vector v)` | Copy |
| `Vector(int dimension)` | Zero vector |
| `getDimension()`, `get(i)`, `getValues()` | Read |
| `set(i, value)`, `setValues(list)` | Write |
| `add`, `subtract` | Same dimension required |
| `scalarMult(k)` | |
| `dotProduct(v)` | Number |
| `cross2D(v)` | Both 2D → number `a1·b2 − a2·b1` |
| `cross3D(v)` | Both 3D → vector |
| `projectOn(v)` | `(this·v / v·v) · v`. `ArithmeticException` if `v` is zero |
| `angleInBet(v, u)` | Angle between `v` and `u` in radians (0 to π). Any dimension `ArithmeticException` if either is zero |
| `padTo(n)` | Copy with zeros added at the end up to dimension `n`: `[1, 2].padTo(3)` = `[1, 2, 0]` |
| `getLen()` | Euclidean length |
| `getDirection()` | n−1 angles in radians (see 2.10). `ArithmeticException` for the zero vector |
| `toPolar()` | `PolarForm` |
| `static fromPolar(r, angles)` | Vector from length + angles |
| `static convertToVector(Matrix m)` | n×1 matrix → vector |
| `toString()` | `[1, 2.5, -3]` (uses `formatNumber`) |
| `equals`, `hashCode` | By components; `-0` equals `0` |

## 2.8 Matrix

Stored as a list of row `Vector`s plus `size = {rows, cols}`. Indexes from 0.

### Creating

| Method | Description |
|---|---|
| `Matrix(int rows, int cols)` | Zero matrix. Throws if a size < 1 |
| `Matrix(Matrix m)` | Deep copy |
| `static build(List<Vector> rows)` | Throws if empty or rows differ in size |
| `static identity(n)` | |
| `static convertToMatrix(Vector v)` | n×1 column matrix |

### Reading

| Method | Description |
|---|---|
| `getNumRows()`, `getNumCols()`, `getSize()` | Size |
| `get(r, c)` | One entry |
| `getRow(r)`, `getColumn(c)`, `getRows()` | Copies |
| `isSquare()` | |
| `getDeterminant()` | Cached; throws if not square |
| `isSingular()` | Use this instead of `det == 0`. Throws if not square |
| `getGaussSteps()` | Steps of the last `toTriangle()` / `gaussJordan()` **on this object** |

### Writing (in place, each one invalidates the determinant cache)

| Method | Description |
|---|---|
| `set(r, c, value)`, `setRow(r, vector)` | |
| `rowMultiply(a, i)` | Rᵢ = a·Rᵢ. Returns the step text |
| `rowSwap(i, j)` | Swap. Returns the step text |
| `rowComp(i, j, k)` | Rᵢ = Rᵢ + k·Rⱼ. Returns the step text |

### Operations (return a new value)

| Method | Notes |
|---|---|
| `add`, `subtract`, `scalarMult` | |
| `transpose()` | |
| `toTriangle()` | Row echelon form. Fills `gaussSteps` of **this** matrix |
| `static multiply(Matrix, Matrix)` | a.cols must equal b.rows |
| `static multiply(Matrix, Vector)` | Vector used as a column → `Vector` |
| `static inverse(m)` | `IllegalArgumentException` if not square, `ArithmeticException` if singular |
| `static adj(m)` | Square only. `adj` of 1×1 is `[1]` |
| `static pow(m, k)` | `k` whole, \|k\| ≤ 10¹⁵. Negative uses the inverse |

`toString()` prints one row per line, columns right-aligned:

```
[1  -2]
[3   4]
```

## 2.9 The algorithms

### Gaussian elimination: `Matrix.eliminate()`

The **only** copy of the algorithm. `toTriangle()`, the determinant and `SystemMatrix` all use it. It works on a copy, so the original matrix never changes.

For each column:
1. **Partial pivoting:** picks the row with the biggest absolute value in that column (avoids dividing by tiny numbers).
2. If that value is "zero" (see tolerance), the column has no pivot: it's marked and skipped.
3. Swaps that row up if needed (each swap flips the determinant's sign).
4. Zeroes everything below the pivot with `Rᵣ = Rᵣ − factor·R_pivot`.

Optionally it applies the same operations to a companion vector (the `b` of `Ax = b`) and records each step as text.

### Tolerance ("is this zero?")

Not a fixed `0.00001`. The limit is **biggest |value| × max(rows, cols) × 10⁻¹²**. It scales with the numbers:

- `[[1,2,3],[4,5,6],[7,8,9]]` leaves a rounding leftover of ~1e-15 → treated as 0 → `det = 0` exactly.
- A matrix full of `0.000001` is **not** considered singular.

### Determinant

Lazy and cached. Computed the first time you ask for it: product of the triangle's diagonal × the swap sign. If some column had no pivot, it's exactly `0.0`. Any change to the rows invalidates the cache.

### Inverse and adjugate

- `adj(M)`: the transposed cofactor matrix. Each cofactor is the determinant of a submatrix.
- `inverse(M) = adj(M) / det(M)`. Fine for the small matrices we use; slow for big ones.

### Matrix power

Exponentiation by squaring: `M^13 = M^8 · M^4 · M^1`. About 2·log₂(k) multiplications, no recursion.

### Solving systems: `SystemMatrix`

`new SystemMatrix(A, b)` copies both. `b` must have one value per row of `A`.

**`solve()`** (used by `solveSystem`): `x = inverse(A) · b`. Square only. If `A` is singular it throws, but it can't tell "no solution" from "infinitely many". No steps.

**`gaussJordan()`** (used by `gaussJordan`), returns a `SolutionVector`:

1. Triangulates `A`, applying the same operations to a copy of `b`.
2. A zero row with `b ≠ 0` (reads `0 = c`) → "The system has no solution (row r reads 0 = c)."
3. Fewer equations than unknowns, or a missing pivot → "The system has infinitely many solutions (x_i is free)."
4. Back-substitution from the last column: divides each pivot row so the pivot is 1, then clears everything above it.
5. Now `A` is the identity and `b` holds the solution.

Every row operation from steps 1 and 4 is saved and returned with `getSteps()`.

### Polar form

- `getDirection()` gives n−1 angles (radians). 2D: `[atan2(y, x)]`. 3D and up: hyperspherical angles. 1D: `[0]` if positive, `[pi]` if negative.
- `fromPolar(r, angles)`: `x1 = r cos t1`, `x2 = r sin t1 cos t2`, ..., `xn = r sin t1 ⋯ sin t(n−1)`. Tiny leftovers like `cos(pi/2) = 6e-17` are snapped to 0.
- `PolarForm` prints as `{length: 5, angles: [0.9273]}`. It can only be shown or passed back to `polarV`.

## 2.10 Language reference

The same information is printed by `help` in the console version.

### Values

| Syntax | Meaning |
|---|---|
| `3`, `2.5`, `1e3`, `2.5e-4` | Numbers. Dot for decimals. `.5` is not valid (write `0.5`) |
| `A`, `M2`, `res` | Variables: a letter followed by letters or digits. Case-sensitive |
| `pi`, `e` | Constants |
| `[1, 2, 3]` | Vector |
| `[[1, 2], [3, 4]]` | Matrix; each inner list is a row |
| `[sqrt(2), pi/2]` | Elements can be any expression that gives a number |
| `[C, D]`, `[2*C, C + D]` | Rows can be any expression that gives a vector |

### Operators

| Expression | Meaning |
|---|---|
| `A + B`, `A - B` | Same type and size. Number + vector is an error |
| `2 * A`, `A * 2` | Scalar times vector or matrix |
| `u * v` | Two vectors: dot product (a number) |
| `M * N` | Matrix product |
| `M * v` | Matrix × vector → vector. `v * M` is **not** allowed |
| `A / 2` | Vector or matrix divided by a number |
| `M / N` | `M * inv(N)` |
| `x ^ y` | Power of a number. `2^3^2` = 512 |
| `M ^ k` | Power of a square matrix, `k` whole; negative uses the inverse |
| `-A` | Negative. `-2^2` = `-4` |
| `( )` | Grouping |

### Vector functions

| Function | Result |
|---|---|
| `dot(u, v)` | Dot product. Also accepts two matrices or matrix + vector (= product) |
| `cross(u, v)` | 2D → number; 3D → vector |
| `project(u, v)` | Projection of `u` onto `v` |
| `length(v)` | Length: `length([3, 4])` = 5 |
| `direction(v)` | Direction angles (radians) as a vector |
| `polarize(v)` | `PolarForm`: `polarize([3, 4])` = `{length: 5, angles: [0.9273]}` |
| `polarV(r, t1, ..., tk)` | Vector from length and angles: `polarV(5, 0.9273)` = `[3, 4]` |
| `polarV(polarize(v))` | Back to `v` |

### Matrix functions

| Function | Result |
|---|---|
| `det(M)` | Determinant (square only) |
| `inv(M)` | Inverse (square, det ≠ 0) |
| `adj(M)` | Adjugate (square only) |
| `trans(M)` | Transpose |
| `triangulize(M)` | Row echelon form |
| `solveSystem(M, v)` | Solves `M x = v` with the inverse → `Vector` |
| `gaussJordan(M, v)` | Solves `M x = v` by Gauss-Jordan → `SolutionVector` (with steps) |

### Number functions

| Function | Result |
|---|---|
| `sin cos tan sec csc cot` | Radians |
| `arcsin arccos arctan arcsec arccsc arccot` | Inverse trig. Domain checked for `arcsin`, `arccos`, `arcsec`, `arccsc` |
| `sqrt(x)`, `ln(x)`, `log10(x)` | Negative `x` gives `NaN` |
| `log(x, b)` | Log of `x` in base `b`: `log(8, 2)` = 3 |
| `mod(a, b)` | Always-positive remainder: `mod(-1, 3)` = 2 |

Functions can be nested: `det(trans(M))`, `dot(A + B, A)`.

### Assignment and constructors

| Syntax | Meaning |
|---|---|
| `NAME = expression` | Saves the result. Must be at the start of the command |
| `M = Matrix(rows, cols)` | GUI only: declares a matrix typed by hand |
| `v = Vector(n)` | GUI only: declares a vector typed by hand |

Constructors must be the whole command, capitalized, with plain whole positive numbers as sizes.

## 2.11 Errors in detail

| Exception | Thrown when |
|---|---|
| `CommandException` | Syntax, unknown variables, wrong types (`1 + [1, 2]`), wrong argument count, constants reassigned, bad declarations, bad cell values |
| `IllegalArgumentException` | Incompatible sizes (2D + 3D, `det` of a non-square matrix, bad matrix power...) |
| `ArithmeticException` | Inverse of a singular matrix, projecting onto the zero vector |
| `MultipleOrNoSolutionException` | `solveSystem` / `gaussJordan` without a unique solution |
| `IllegalStateException` | Internal bug (an unknown operator or function reached the evaluator). Tell Ian |

## 2.12 Console version

`ConsoleApp` is a text interface to test the API without the window.

- Run `App` with the argument `--console`, or run `ConsoleApp` directly from the IDE.
- One command per line. Extra commands: `help`, `memory` (lists every variable), `clear` / `reset` (calls `resetMemory()`), `exit` / `quit`.
- Commands can also be passed as program arguments: each one is run and printed.
- In the console, numbers print in Java's format (`-2.0`), not with `formatNumber`.