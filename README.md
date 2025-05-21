#  Text to Console — Spring Boot Demo

This is a simple Spring Boot web application that executes go code on graalWASM — like an IDE.

##  Important note!!!!

This demo is just for experimental purposes and does not support multiple users!

##  How it works?

This demo Compiles Go code into WASM and then it takes the wasm code and run it on GraalWASM.

##  The purpose of this demo?

performance evaluation.
This project was built as a technical exploration to evaluate GraalVM’s WebAssembly runtime (GraalWasm) performance and compatibility with Go-compiled WebAssembly.

The main goals of this demo are:

- Test how well GraalWasm can run WebAssembly modules generated from Go code
- Explore which Go features, libraries, and functionalities are supported
- Evaluate the execution behavior and performance of GraalWasm in running non-trivial Go code
- 
This demo serves as a starting point to understand the integration limits and capabilities of using Go and WebAssembly within the GraalVM ecosystem.


