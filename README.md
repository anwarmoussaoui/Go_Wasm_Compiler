#  Go / RUST WEB IDE

This is a simple Spring Boot web application that executes go and rust code on graalWASM — like an IDE.

##  Important note!!!!

This demo is just for experimental purposes and does not support multiple users!

##  How it works?

This demo Compiles Go/rust code into WASM and then it takes the wasm code and run it on GraalWASM.

##  The purpose of this demo?

performance evaluation.

This project was built as a technical exploration to evaluate GraalVM’s WebAssembly runtime (GraalWasm) performance and compatibility with Go/rust-compiled WebAssembly.

The main goals of this demo are:

- Test how well GraalWasm can run WebAssembly modules generated from Go/RUST code
- Explore which Go/RUST features, libraries, and functionalities are supported
- Evaluate the execution behavior and performance of GraalWasm in running non-trivial Go code

This demo serves as a starting point to understand the integration limits and capabilities of using Go/RUST and WebAssembly within the GraalVM ecosystem.


