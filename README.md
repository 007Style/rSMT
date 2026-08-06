# rSMT

A Java-based **Reverse Simultaneous Multi-Threading (rSMT) processor simulator** written by **Daneyand Singley** in January 2009. The program models a simplified out-of-order processor pipeline with FXU (Fixed-point Execution Unit), FPU (Floating-point Execution Unit), and Branch execution units, then compares throughput with and without rSMT enabled to quantify the cycle-count improvement.

> **This code is the evidence for [IBM Patent #US8595468](https://patents.google.com/patent/US8595468).**

---

## What is Reverse SMT?

**Reverse Simultaneous Multithreading (rSMT)**, also conceptually known as **Inverse Hyper-Threading**, is a processor technology that allows multiple physical CPU cores to work together to execute a single, heavy thread.

While traditional SMT splits one physical core into multiple logical cores to run separate instruction streams simultaneously, Reverse SMT does the exact opposite: it dynamically consolidates and distributes the load of a single heavily-threaded application across multiple underlying hardware cores to maximize single-threaded performance and instruction-level parallelism (ILP).

IBM explored this concept in various microarchitecture designs and patent frameworks to balance workload throughput and latency. This simulator models the core scheduling behavior of rSMT — issuing a secondary instruction from the same thread into an available execution slot when the primary pipeline slot is stalled or idle — and measures the resulting cycle-count reduction versus a standard single-issue pipeline.

---

## Table of Contents

- [Features](#features)
- [How It Works](#how-it-works)
- [Project Structure](#project-structure)
- [Source Files](#source-files)
- [Instruction Set](#instruction-set)
- [Execution Units & Latencies](#execution-units--latencies)
- [Building](#building)
- [Running](#running)
- [Output Interpretation](#output-interpretation)
- [Authors & Credits](#authors--credits)

---

## Features

| Capability | Detail |
|---|---|
| Random instruction stream generation | Configurable mix of integer, FP, branch, and NOP instructions |
| Dual-mode simulation | Runs the same instruction stream twice: once with SMT, once without |
| Seeded randomness | Both runs use the same seed for apples-to-apples comparison |
| Configurable parameters | Instruction count, rSMT delay, %FXU, SMT availability %, data-dependency % |
| Per-unit pipelines | FXU (×2 in SMT), FPU (×1), Branch (×1), NOP (instant retire) |
| Performance summary | Prints cycle counts and `(normCycles/rCycles)*100%` efficiency gain |

---

## How It Works

```
1. genInstructions generates N instructions seeded with System.currentTimeMillis()
   - Instruction mix: add/sub/mul/div (FXU), fadd/fsub/fmul/fdiv (FPU), b (branch), nop
   - Percentage of integer instructions is user-controlled

2. rSimulate() — SMT-ON pass
   - Single issue per clock cycle
   - FXU slot [0]: primary integer instruction
   - FXU slot [1]: secondary SMT integer instruction (issued when rSMT_cycle() and !rSMT_depends())
   - Branches stall issue until retired (execBranch flag)
   - Each instruction advances curClock until doneClock; then retires into vInstFinishedBase

3. normSimulate() — SMT-OFF pass
   - Identical pipeline model but FXU slot [1] is never filled

4. finalSummary() prints: rSMT performance gain = (normCycles / rCycles) × 100%
```

---

## Project Structure

```
rSMT/
└── rSMT/
    ├── build.xml                  # Ant build (NetBeans)
    ├── manifest.mf
    ├── nbproject/                 # NetBeans IDE metadata
    ├── src/
    │   └── rsmt/
    │       ├── Main.java          # Entry point; parses CLI args
    │       ├── rSMT.java          # Simulation engine
    │       └── genInstructions.java  # Instruction generator + executor
    └── dist/
        └── rSMT.jar               # Pre-built JAR
```

---

## Source Files

### [`Main.java`](rSMT/src/rsmt/Main.java) — Entry Point

Parses the five command-line arguments and constructs an `rSMT` instance. Falls back to a 100-instruction default run if no arguments are provided.

```
Usage: java -jar rSMT <numInst> <rSMTdelay> <%intInst> <%rSMTavail> <%depends>
```

---

### [`rSMT.java`](rSMT/src/rsmt/rSMT.java) — Simulation Engine

| Member | Description |
|---|---|
| `rSMT(int inst, int delay, int perc, int rAvail, int rDepends)` | Constructor; generates instructions, runs both simulations, prints summaries |
| `rSimulate()` | SMT-ON simulation loop; fills FXU[0] + conditionally FXU[1] per clock |
| `normSimulate()` | SMT-OFF simulation loop; only FXU[0] used |
| `rReset()` | Clears pipeline state between the two simulation passes |
| `rSMT_cycle()` | Returns true with probability `rSMT_availPercent`/100 — models SMT slot availability |
| `rSMT_depends()` | Returns true with probability `rSMT_dependsPercent`/100 — models data hazards |
| `rSummary()` | Prints SMT-ON cycle count and integer instruction breakdown |
| `normSummary()` | Prints SMT-OFF cycle count |
| `finalSummary()` | Prints `(normCycles/rCycles)*100%` performance gain |
| `copyVector(Vector)` | Deep-copies a `Vector<rInst>` for baseline preservation |

**Pipeline constants:**

| Constant | Value | Meaning |
|---|---|---|
| `fpCycles` | 6 | FP instruction latency (clocks) |
| `fxCycles` | 5 | Integer instruction latency (clocks) |
| `brCycles` | 4 | Branch instruction latency (clocks) |
| `nopCycles` | 0 | NOP: retires immediately |

---

### [`genInstructions.java`](rSMT/src/rsmt/genInstructions.java) — Instruction Generator

| Member | Description |
|---|---|
| `genInst(int number, int percent)` | Generates `number` random instructions with `percent`% integer mix |
| `execute(rInst)` | Executes an instruction: performs the arithmetic and stores the result in the `rInst` |
| `instName(rInst)` | Returns a human-readable string like `add(42,-17)` for logging |
| `rInst` (inner class) | Instruction record: `inst`, `index`, `op1/op2`, `dop1/dop2`, `branch`, `curClock`, `doneClock`, `order`, `txtResult` |

**`rInst` type-detection methods:**

| Method | Returns true when |
|---|---|
| `isFXU()` | index 0–3 (add, sub, mul, div) |
| `isFPU()` | index 4–7 (fadd, fsub, fmul, fdiv) |
| `isB()` | index 8 (branch) |
| `isNOP()` | index 9 (nop) |

---

## Instruction Set

| Index | Mnemonic | Unit | Operands | Latency |
|---|---|---|---|---|
| 0 | `add` | FXU | int + int | 5 clocks |
| 1 | `sub` | FXU | int - int | 5 clocks |
| 2 | `mul` | FXU | int × int | 5 clocks |
| 3 | `div` | FXU | int / int | 5 clocks |
| 4 | `fadd` | FPU | double + double | 6 clocks |
| 5 | `fsub` | FPU | double - double | 6 clocks |
| 6 | `fmul` | FPU | double × double | 6 clocks |
| 7 | `fdiv` | FPU | double / double | 6 clocks |
| 8 | `b` | Branch | — | 4 clocks |
| 9 | `nop` | — | — | 0 clocks |

---

## Execution Units & Latencies

```
SMT-ON pipeline (per clock):
  FXU[0] ─── primary integer instruction
  FXU[1] ─── SMT integer instruction (if slot available & no data hazard)
  FPU[0] ─── FP instruction
  B[0]   ─── branch (stalls new issue until retired)
```

---

## Building

Requires **JDK 5+** and **Apache Ant**.

```bash
cd rSMT/rSMT
ant clean build
# Output: dist/rSMT.jar
```

---

## Running

```bash
# Default: 100 instructions, 0 delay, 50% int, 50% SMT avail, 20% depends
java -jar rSMT/dist/rSMT.jar

# Custom: 10000 instructions, 0 delay, 50% int, 50% SMT avail, 20% depends
java -jar rSMT/dist/rSMT.jar 10000 0 50 50 20
```

---

## Output Interpretation

```
(1) Generating Instructions... Number of Instructions:100 %FXU: 50
(2) Simulating rSMT Activated... START
CYCLE: 6 rSMT instruction: add(...)
...
(2) Simulating rSMT Activated... FINISHED
**********************************************************
Total Cycles Ran         : 412
Total        Instructions: 100
Integer      Instructions: 54
Integer rSMT Instructions: 11
**********************************************************
(3) Simulating rSMT Deactivated... START
...
(4)**********************************************************
rSMT performance gain    : 108.5%
(4)**********************************************************
```

A value above 100% means the SMT run finished in fewer cycles — i.e., `normCycles > rCycles`.

---

## Authors & Credits

| Name | Role |
|---|---|
| **Daneyand Singley** (`dsingley`) | Author; created January 2009 |
