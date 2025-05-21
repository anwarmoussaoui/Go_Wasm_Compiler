package com.example.Go_Compiler;


import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

@org.springframework.stereotype.Controller

public class Controller {
    private final ContextPool contextPool;

    public Controller(ContextPool contextPool) {
        this.contextPool = contextPool;
    }

    @GetMapping("/")
    public String getForm() {
        return "index";
    }

    @PostMapping("/submit")
    public String handleSubmit(@RequestParam String inputText, Model model) throws IOException, InterruptedException {

        Context context = contextPool.getContext();
        File goProjectDir = new File("src/main/resources");

        if (!goProjectDir.exists()) {
            goProjectDir.mkdirs();
        }

        File goFile = new File(goProjectDir, "main.go");

        try {
            Files.writeString(goFile.toPath(), inputText);
            System.out.println("Go source code written to main.go");
        } catch (IOException e) {
            e.printStackTrace();
        }

        ProcessBuilder builder = new ProcessBuilder("go", "build", "-o", "main.wasm");
        builder.directory(goProjectDir);
        builder.environment().put("GOOS", "js");
        builder.environment().put("GOARCH", "wasm");

        String output = "";
        String errors = "";
        int exitCode = -1;

        try {
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
            status = "error"; // red
        }

        model.addAttribute("inputText", inputText);   // preserve textarea content
        model.addAttribute("outputText", displayText); // console text
        model.addAttribute("status", status);          // css class for coloring

        return "index";
    }


}
