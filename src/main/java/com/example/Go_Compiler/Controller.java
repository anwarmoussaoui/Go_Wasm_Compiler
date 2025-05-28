package com.example.Go_Compiler;


import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

@org.springframework.stereotype.Controller

public class Controller {
    private final ContextPool contextPool;
    private final Path tmpDir;

    public Controller(ContextPool contextPool) throws IOException {
        this.contextPool = contextPool;
        this.tmpDir = Files.createTempDirectory("rust-compiler-demo");
        tmpDir.resolve("src").toFile().mkdirs();
        Files.writeString(tmpDir.resolve("Cargo.toml"), """
                        [package]
                        name = "hello_wasm"
                        version = "0.1.0"
                        edition = "2024"
                        
                        [dependencies]
                        
    """
    );
    }

    @GetMapping("/")
    public String getForm() {
        return "index";
    }

    @PostMapping("/submit")
    public String handleSubmit(@RequestParam String inputText, Model model) throws IOException, InterruptedException {
        String output = "";
        String errors = "";
        int exitCode = -1;
        Context context = contextPool.getContext();
        File goProjectDir = new File("src/main/resources");

        if (!goProjectDir.exists()) {
            goProjectDir.mkdirs();
        }

        File goFile = new File(goProjectDir, "main.go");

        try {
            Files.writeString(goFile.toPath(), inputText);
            System.out.println("Go source code written to main.go");


        ProcessBuilder builder = new ProcessBuilder("go", "build", "-o", "main.wasm");
        builder.directory(goProjectDir);
        builder.environment().put("GOOS", "js");
        builder.environment().put("GOARCH", "wasm");




        Process process = builder.start();

        output = new BufferedReader(new InputStreamReader(process.getInputStream()))
                    .lines().collect(Collectors.joining("\n"));
        errors = new BufferedReader(new InputStreamReader(process.getErrorStream()))
                    .lines().collect(Collectors.joining("\n"));

        exitCode = process.waitFor();

        System.out.println("\n=== Compilation Output ===");
        System.out.println(output);

        System.out.println("\n=== Compilation Errors ===");
        System.out.println(errors);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            errors += "\nException: " + e.getMessage();
        }

        String displayText;
        String status;

        if (exitCode == 0) {
            // Compilation success: run WASM and capture output
            byte[] wasmfile = Files.readAllBytes(Paths.get("./src/main/resources/main.wasm"));
            context.getBindings("js").putMember("wasmbytes", wasmfile);
            context.eval(Source.newBuilder("js", Controller.class.getResource("/go.js")).build());
            Thread.sleep(100); // adjust if needed for async finish

            Value captured = context.eval("js", "capturedOutput");
            displayText = captured.asString();
            status = "success"; // green
        } else {
            // Compilation failed: show errors
            displayText = errors.isEmpty() ? "Unknown compilation error." : errors;
            status = "error";
        }

        model.addAttribute("inputText", inputText);   // preserve textarea content
        model.addAttribute("outputText", displayText); // console text
        model.addAttribute("status", status);          // css class for coloring

        return "index";
    }

    @GetMapping("/rust")
    public String getFormRust() {
        return "rust";
    }
    @PostMapping("/submit/rust")
    public String handleSubmitRust(@RequestParam String inputText, Model model) throws IOException, InterruptedException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String displayText;
        String status;

        try (Context context = Context.newBuilder("wasm")
                .option("wasm.Builtins", "wasi_snapshot_preview1")
                .out(output)
                .build()) {
            Path goFile = tmpDir.resolve("src").resolve("main.rs");
            Files.writeString(goFile, inputText);
            System.out.println("Rust source code written to main.rs");

            String target = "wasm32-wasip1";
            ProcessBuilder pb = new ProcessBuilder("cargo", "build", "--release", "--target", target);
            pb.directory(tmpDir.toFile());
            pb.redirectErrorStream(true);

            Process process = pb.start();

            StringBuilder buildOutput = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    buildOutput.append(line).append("\n");
                }


            int exitCode = process.waitFor();

            if (exitCode != 0) {
                System.err.println("Compilation failed:\n" + buildOutput);
                model.addAttribute("outputText", "Compilation failed:\n" + buildOutput.toString());
                model.addAttribute("status", "error");
                model.addAttribute("inputText", inputText);
                return "rust";
            }

            System.out.println("Rust compiled to WASM successfully.");
            Path wasmPath = tmpDir.resolve("target").resolve( "wasm32-wasip1").resolve( "release").resolve("hello_wasm.wasm");
            Source source = Source.newBuilder("wasm",wasmPath.toFile()).build();
            Value wasmBindings = context.eval( source);

            Value main = wasmBindings.getMember("_start");
            main.execute();
            displayText = output.toString();
            status = "success";
            Value bindings = context.getBindings("wasm");
            bindings.removeMember("_start");
        } catch (Exception e) {
            if (e instanceof PolyglotException pe && pe.isExit()) {
                displayText = "Nice try but you cannot crash the server ( " + e.getMessage();
                status = "error";
            }else {
                e.printStackTrace();
                displayText = "Runtime error: " + e.getMessage();
                status = "error";
            }

        }

        model.addAttribute("inputText", inputText);
        model.addAttribute("outputText", displayText);
        model.addAttribute("status", status);
        return "rust";
    }



}
