# Vendetta API reference

> ## ⚠️🥥 SUPER IMPORTANT: DO NOT DELETE `coconut.jpg` 🥥⚠️
>
> `src/main/resources/linear/vendetta/vendetta/coconut.jpg` is the API's emotional support coconut.
> If it's gone, the API falls into a deep depression and takes itself out: `App.checkCoconut()` can't find it and calls `jarvisPullTheTrigger()`, which recurses forever until the program dies with a `StackOverflowError`.
>
> **Don't delete it, don't rename it, don't move it. Don't add it to `.gitignore`.**

Documentation of the linear algebra interpreter API (`api/dsl` and `api/linear_algebra`).
This file only covers the API. For how the UI should use it, see `INTERFACE.md`.

The API is maintained by Ian. If you need something it doesn't do, ask him instead of editing it (rules 2 and 4 in `daRules.txt`).

Contents:

1. [Overview](#1-overview)
2. [Packages and classes](#2-packages-and-classes)
3. [Interpreter](#3-interpreter)
4. [CompiledResult](#4-compiledresult)
5. [Result types: Double, Vector, Matrix](#5-result-types-double-vector-matrix)
6. [Vector](#6-vector)
7. [Matrix](#7-matrix)
8. [SystemMatrix](#8-systemmatrix)
9. [Errors](#9-errors)
10. [Memory, variables and constants](#10-memory-variables-and-constants)
11. [Language reference](#11-language-reference)
12. [Behavior you must know about](#12-behavior-you-must-know-about)
13. [Console version](#13-console-version)
14. [Known limitations and planned changes](#14-known-limitations-and-planned-changes)

---

## 1. Overview

The API takes commands written as text (for example `D = adj(A) * A`) and evaluates them.

A command goes through three stages:

| Stage | Class | Output |
|---|---|---|
| Tokenize | `Tokenizer` | List of `Token` (numbers, names, functions, constructors, operators) |
| Parse | `Parser` | A syntax tree of `Node` |
| Evaluate | `Evaluator` (+ helpers in `evaluators/`) | A `Double`, `Vector` or `Matrix` |

Code outside the API only needs **`Interpreter`**, **`CompiledResult`**, the exceptions, and the value classes **`Vector`** and **`Matrix`**. Tokenizer, Parser, Node and Evaluator are internal.

Everything in `Interpreter` is **static**: no instances are created, and there is one shared memory for the whole program.

---

## 2. Packages and classes

### `linear.vendetta.vendetta.api.dsl`

| Class | Public? | Purpose |
|---|---|---|
| `Interpreter` | **Use it** | Entry point: prepares declarations, runs commands, evaluates single cell values, holds memory |
| `CompiledResult` | **Use it** | One command plus its result |
| `CommandException` | **Use it** | Error caused by a user command; its message is meant to be shown to the user |
| `Tokenizer`, `Token` | internal | Splits text into tokens |
| `Parser`, `Node` | internal | Builds the syntax tree |
| `Evaluator`, `evaluators/*` | internal | Computes the result of a tree |

### `linear.vendetta.vendetta.api.linear_algebra`

| Class | Public? | Purpose |
|---|---|---|
| `Vector` | **Use it** | A vector of doubles |
| `Matrix` | **Use it** | A matrix of doubles, stored as a list of row Vectors |
| `SystemMatrix` | rarely | Matrix + right-hand-side vector, solves `Ax = b` |
| `MultipleOrNoSolutionException` | **Catch it** | A system has no solution or infinitely many |
| `TransformableVector`, `operable`, `elementalOperations` | internal | Row operations and shared interfaces |

> Java trap: `Vector` has the same name as `java.util.Vector`. If you import `java.util.*` in the same file, the name is ambiguous and the code won't compile. Import `java.util` classes one by one (`List`, `Map`, `ArrayList`, ...).

---

## 3. Interpreter

Package `linear.vendetta.vendetta.api.dsl`. All methods are static.

### 3.1 `prepareVariables(List<String> commands) → Map<String, Object>`

Looks only at **constructor** commands (`NAME = Matrix(rows, cols)` and `NAME = Vector(n)`) and ignores everything else. Nothing is evaluated.

- **Returns** a map `name → Matrix/Vector filled with zeros`, with the declared size, in the same order as the commands. If no command is a constructor, the map is empty.
- **Side effect:** each declared name that doesn't exist in memory yet is added with its zero value. Names that already exist keep their current value.
- **Throws** `CommandException` if:
  - a constructor is badly written (`Matrix(2)`: "matrix needs 2 argument(s)"; `Vector(2, 2)`: "vector needs 1 argument(s)");
  - a size is not a whole positive number (`Matrix(0, 2)`, `Matrix(1.5, 2)`: "Sizes must be whole positive numbers");
  - the name is a constant (`pi = Vector(2)`);
  - the same name is declared twice **inside the list it received**.
- Validation happens before memory is touched: if it throws, memory is unchanged.

Example: for `["A = Matrix(2, 2)", "B = Vector(2)", "C = A + B"]` it returns `{A = 2×2 zeros, B = [0, 0]}`.

### 3.2 `interprete(List<String> commands) → List<CompiledResult>`

Runs the commands in order and returns one `CompiledResult` per command.

- All commands are **parsed first**; only if every one parses are they executed. One syntax error means nothing runs.
- **Throws** `CommandException` if any command is a constructor ("... its values must be filled in by hand before compiling").
- Errors during execution stop the run at the failing command (results of earlier commands are not returned, but their assignments stay in memory).

### 3.3 `interprete(List<String> commands, Map<String, Object> manualValues) → List<CompiledResult>`

Same as 3.2, but first loads hand-typed values for the constructors.

- `manualValues`: map `name → Matrix/Vector` with the real values for each declared variable.
- Before anything else, it checks that **every** constructor in `commands` has a value in the map with **exactly** the declared size. If not, it throws (`"Missing the values of A"`, `"A must be a Matrix(2, 2)"`) and memory is not touched.
- Then it copies every entry of the map into memory (deep copies; your objects are not modified later), and runs the commands.
- For a constructor command, the `CompiledResult` contains the value now in memory.
- If `manualValues` is `null`, it behaves exactly like 3.2.

### 3.4 `evaluateEntry(String entry) → double`

Evaluates one expression that **must** produce a finite number. Designed for a single matrix/vector cell.

- Accepts any expression: `3`, `-1.5`, `sqrt(2)/2`, `pi/4`, `det(M1)`, `dot(C, D)`.
- **Throws** `CommandException` if:
  - it is an assignment or a constructor ("is a declaration, only values are allowed here");
  - the result is a vector or matrix ("is not a number");
  - the result is `NaN` or infinite, e.g. `sqrt(-1)`, `1/0` ("does not give a valid number");
  - it has a syntax error or an unknown variable.
- It does not modify memory.

### 3.5 `getMemory() → HashMap<String, Object>`

Returns the **live** memory map (not a copy): `name → Double/Vector/Matrix`. Read from it; don't write to it directly (use `assignToMemory` so constants are protected).

### 3.6 `assignToMemory(String name, Object value)`

Stores a value under a name. Throws `CommandException` if the name is a constant. Normally only the interpreter calls this.

### 3.7 `resetMemory()` *(planned, not implemented yet)*

Will clear memory and leave only the constants `pi`, `e`, `i`.

---

## 4. CompiledResult

| Method | Returns |
|---|---|
| `getCommand()` | The original command text, exactly as it was passed |
| `getResult()` | The value: a `Double`, a `Vector` or a `Matrix` (as `Object`) |

For an assignment (`X = ...`) the result is the assigned value. The assigned name is not stored; it's only in the command text.

---

## 5. Result types: Double, Vector, Matrix

Every evaluated value is one of three Java types:

| Type | Comes from |
|---|---|
| `Double` | numbers, `det`, `dot`, `cross` of 2D vectors, number functions, `u * v` with two vectors |
| `Vector` | `[1, 2]`, vector arithmetic, `cross` of 3D vectors, `project`, `M * v`, `solveSystem`, `gaussJordan` |
| `Matrix` | `[[1, 2], [3, 4]]`, matrix arithmetic, `inv`, `adj`, `trans`, `triangulize` |

To branch on the type, check with `instanceof` in the order `Matrix`, `Vector`, `Double`.

### Number formatting

`Vector.formatNumber(double x) → String` is the official way to display a number:

- up to **4 decimals**, trailing zeros removed: `2.0 → "2"`, `1/3 → "0.3333"`, `2.5 → "2.5"`;
- always uses `.` as the decimal point (even on computers set to French/Spanish locale);
- `-0` is shown as `0`;
- `NaN` and `Infinity` are printed as is.

`Vector.toString()` and `Matrix.toString()` already use it.

---

## 6. Vector

Package `linear.vendetta.vendetta.api.linear_algebra`. Indexes start at **0**.

### Constructors

| Constructor | Description |
|---|---|
| `Vector(List<? extends Number> values)` | Vector with those components. Throws `IllegalArgumentException` if the list is empty or has nulls |
| `Vector(Vector v)` | Copy |
| `Vector(int dimension)` | Zero vector of that size |

### Reading and writing

| Method | Description |
|---|---|
| `getDimension()` | Number of components |
| `get(int i)` | Component `i` |
| `getValues()` | The components as a `List<Double>` |
| `set(int i, double value)` | Changes one component |
| `setValues(List<Double> values)` | Replaces all components |

### Operations (return a new value, don't modify `this`)

| Method | Returns | Notes |
|---|---|---|
| `add(Vector v)` | Vector | Same dimension required |
| `subtract(Vector v)` | Vector | Same dimension required |
| `scalarMult(double k)` | Vector | |
| `dotProduct(Vector v)` | double | Same dimension required |
| `cross2D(Vector v)` | double | Both must be 2D |
| `cross3D(Vector v)` | Vector | Both must be 3D |
| `projectOn(Vector v)` | Vector | Projection of `this` onto `v` |

### Other

| Method | Description |
|---|---|
| `static convertToVector(Matrix m)` | n×1 matrix → Vector. Throws if the matrix has more than one column |
| `static formatNumber(double x)` | See section 5 |
| `toString()` | `[1, 2.5, -3]` |
| `equals` / `hashCode` | Compare by components (`-0` equals `0`) |

---

## 7. Matrix

Package `linear.vendetta.vendetta.api.linear_algebra`. Stored as a list of row Vectors. Indexes start at **0**.

### Creating

| Method | Description |
|---|---|
| `Matrix(int rows, int cols)` | Zero matrix. Throws if a size is less than 1 |
| `Matrix(Matrix m)` | Deep copy |
| `static build(List<Vector> rows)` | Matrix from row vectors. Throws if the list is empty or rows have different sizes |
| `static identity(int n)` | n×n identity |
| `static convertToMatrix(Vector v)` | Vector → n×1 column matrix |

### Reading

| Method | Description |
|---|---|
| `getNumRows()`, `getNumCols()` | Size |
| `getSize()` | `{rows, cols}` |
| `get(int row, int col)` | One entry |
| `getRow(int row)` / `getColumn(int col)` | Copy of a row / column as a Vector |
| `getRows()` | Deep copy of all rows |
| `isSquare()` | rows == cols |
| `getDeterminant()` | Determinant (cached). Throws if not square |
| `isSingular()` | det == 0. Throws if not square |
| `getGaussSteps()` | Text of the row operations of the last `toTriangle()` / `gaussJordan()` |

### Writing (in place)

| Method | Description |
|---|---|
| `set(int row, int col, double value)` | Change one entry |
| `setRow(int row, Vector values)` | Replace a row (must be the right size) |
| `rowMultiply(double a, int i)` | Rᵢ = a·Rᵢ. Returns the step as text |
| `rowSwap(int i, int j)` | Swap two rows. Returns the step as text |
| `rowComp(int i, int j, double k)` | Rᵢ = Rᵢ + k·Rⱼ. Returns the step as text |

### Operations (return a new value)

| Method | Returns | Notes |
|---|---|---|
| `add(Matrix m)`, `subtract(Matrix m)` | Matrix | Same size required |
| `scalarMult(double k)` | Matrix | |
| `transpose()` | Matrix | n×m → m×n |
| `toTriangle()` | Matrix | Row echelon form (Gaussian elimination with partial pivoting). Fills `getGaussSteps()` |
| `static multiply(Matrix a, Matrix b)` | Matrix | a.cols must equal b.rows |
| `static multiply(Matrix a, Vector v)` | Vector | `v` is used as a column |
| `static inverse(Matrix m)` | Matrix | Throws `IllegalArgumentException` if not square, `ArithmeticException` if singular |
| `static adj(Matrix m)` | Matrix | Adjugate. Square only |
| `static pow(Matrix m, double k)` | Matrix | `k` whole; negative uses the inverse. Square only |

### Display

`toString()` gives one row per line, columns right-aligned:

```
[1  -2]
[3   4]
```

---

## 8. SystemMatrix

Extends `Matrix`. Represents the system `A x = b`.

| Method | Description |
|---|---|
| `SystemMatrix(Matrix A, Vector b)` | Builds the system. `b` must have as many components as `A` has rows |
| `solve()` | Solves with the inverse. Throws `MultipleOrNoSolutionException` if `A` is singular (can't tell "no solution" from "infinitely many") |
| `gaussJordan()` | Solves by reducing to RREF. Fills `getGaussSteps()`. Throws `MultipleOrNoSolutionException` saying which case it is (e.g. "infinitely many solutions (x_1 is free)") |
| `getEqVector()` | The `b` vector |

In the language these are the functions `solveSystem(A, b)` and `gaussJordan(A, b)`.

---

## 9. Errors

| Exception | When | Message shown to user? |
|---|---|---|
| `CommandException` | Syntax errors, wrong types (`1 + [1, 2]`), unknown variables, wrong number of arguments, constants reassigned, bad declarations | Yes, written for the user |
| `IllegalArgumentException` | Incompatible sizes (adding 2D + 3D, `det` of a non-square matrix, ...) | Yes, readable |
| `ArithmeticException` | Inverse of a singular matrix | Yes, readable |
| `MultipleOrNoSolutionException` | `solveSystem` / `gaussJordan` without a unique solution | Yes, readable |
| `IllegalStateException` | Internal bug | Report it to Ian |

Rules for callers:

- Catch `Exception`, not only `CommandException`.
- Some errors have a `null` message (for example `1e3` or `.5`, which aren't valid numbers). In that case show the exception's class name, or a generic message.

---

## 10. Memory, variables and constants

- Memory is a single static map `name → Double/Vector/Matrix`, shared by every call.
- Values stay in memory until they are overwritten (or until `resetMemory()` exists and is called).
- Variable names: a letter followed by letters or digits (`A`, `M2`, `res`). **Case-sensitive**: `a` and `A` are different.
- Constants (cannot be reassigned): `pi`, `e`, and `i`.
  - `i` is the 2×2 rotation matrix `[[0, -1], [1, 0]]` (so `i * i` = `-I`), **not** a complex number.
- Right now memory also starts with demo variables: vectors `A = [1, 2, 3]`, `B = [2, -1, 3]`, `C = [-1, 2]`, `D = [0, 1]` and matrices `M1`, `M2` (3×3), `M3` (3×4), `I` (3×3 identity). They will go away (section 14).

---

## 11. Language reference

The same information is printed by `help` in the console version (`ConsoleApp.printHelp()`).

### Values

| Syntax | Meaning |
|---|---|
| `3`, `2.5` | Numbers. Dot for decimals. `1e3` and `.5` are **not** valid |
| `A`, `M2` | Variables |
| `pi`, `e`, `i` | Constants |
| `[1, 2, 3]` | Vector |
| `[[1, 2], [3, 4]]` | Matrix; each inner list is a row. All rows must be the same size |
| `[sqrt(2), pi/2]` | Elements can be any expression that gives a number |
| `[C, D]`, `[2*C, C + D]` | Rows can be any expression that gives a vector (here: a 2×2 matrix) |

Mixing numbers and vectors in one list (`[1, [1, 2]]`) is an error.

### Operators

| Expression | Meaning |
|---|---|
| `A + B`, `A - B` | Numbers, vectors or matrices of the same size. Number + vector is an error |
| `2 * A`, `A * 2` | Scalar times vector or matrix |
| `u * v` | Two vectors: dot product (a number) |
| `M * N` | Matrix product |
| `M * v` | Matrix times vector → vector. `v * M` is **not** allowed |
| `A / 2` | Vector or matrix divided by a number |
| `M / N` | `M * inv(N)` |
| `x ^ y` | Power of a number. Right-associative: `2^3^2` = `2^9` = 512 |
| `M ^ k` | Power of a square matrix, `k` whole; negative `k` uses the inverse |
| `-A` | Negative. `-2^2` = `-4` |
| `( )` | Grouping |

### Vector functions

| Function | Result |
|---|---|
| `dot(u, v)` | Dot product (number) |
| `cross(u, v)` | 2D → number; 3D → vector |
| `project(u, v)` | Projection of `u` onto `v` |

### Matrix functions

| Function | Result |
|---|---|
| `det(M)` | Determinant (square only) |
| `inv(M)` | Inverse (square, det ≠ 0) |
| `adj(M)` | Adjugate (square only) |
| `trans(M)` | Transpose |
| `triangulize(M)` | Row echelon form (Gauss) |
| `solveSystem(M, v)` | Solves `M x = v` with the inverse → vector |
| `gaussJordan(M, v)` | Solves `M x = v` by RREF → vector (steps are printed to the console, see 14) |

### Number functions

| Function | Result |
|---|---|
| `sin cos tan sec csc cot` | Trigonometry, in radians |
| `arcsin arccos arctan arcsec arccsc arccot` | Inverse trigonometry |
| `sqrt(x)` | Square root |
| `ln(x)`, `log10(x)` | Natural / base-10 logarithm |
| `log(x, b)` | Logarithm of `x` in base `b`: `log(8, 2)` = 3 |
| `mod(a, b)` | Always-positive remainder: `mod(-1, 3)` = 2 |

All function names are **case-sensitive** and **lowercase**, except `solveSystem` and `gaussJordan`. Arguments always need parentheses: `sqrt(2)`, not `sqrt2`. Functions can be nested: `det(trans(M))`, `dot(A + B, A)`, `det([[1, 2], [3, 4]])`.

### Assignment

`NAME = expression` stores the result. The assignment must be at the very start of the command. Its result is the assigned value.

### Constructors (GUI only)

| Syntax | Meaning |
|---|---|
| `M = Matrix(rows, cols)` | Declares a matrix whose values will be typed by hand |
| `v = Vector(n)` | Declares a vector whose values will be typed by hand |

- The whole command must be exactly `NAME = Matrix(...)` / `NAME = Vector(...)`; nothing else on the line.
- `Matrix` and `Vector` must be **capitalized**. `matrix(2, 2)` is not recognized as a constructor.
- Sizes must be whole positive numbers.
- They can't be run with `interprete(commands)`; they need `prepareVariables` + `interprete(commands, manualValues)`.

### Spaces

Optional: `A+B` and `A + B` are the same.

---

## 12. Behavior you must know about

1. **One syntax error kills the whole list.** `interprete` parses every command before running any. To get "run everything, report errors per command", call `interprete` once per command with a one-element list.
2. **`manualValues` are reloaded on every call** to `interprete(commands, manualValues)`. If you call it again later, any reassignment of those names done in between is overwritten.
3. **Assignments stay in memory even if a later command fails.**
4. **Case matters** everywhere: variables, functions, constructors. `Adj(A)` → error "Unexpected '('" (because `Adj` is read as a variable).
5. **`NaN` and `Infinity` are valid results** of normal commands (`sqrt(-1)`, `1/0`). Only `evaluateEntry` rejects them.
6. **`prepareVariables` only detects duplicates inside the list you give it.** Pass the full list of commands to detect a redeclaration.
7. **Memory is global.** Two windows or two tests share it.

---

## 13. Console version

`ConsoleApp` is a text interface for testing the API.

- Run: `mvn clean javafx:run` (while `App.main` calls `ConsoleApp.main`), or run `ConsoleApp` directly from the IDE.
- Type one command per line. Special commands: `help`, `memory` (lists every variable), `exit`.
- Commands can also be passed as program arguments; each one is run and printed.

---

## 14. Known limitations and planned changes

| Item | Status |
|---|---|
| `solveSystem` has no steps (it uses the inverse) | By design |
| `CompiledResult` doesn't include the assigned variable name | Possible improvement |
| Invalid numbers like `1e3` or `.5` give an error with a `null` message | Possible improvement |
