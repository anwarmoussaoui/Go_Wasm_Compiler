package com.example.Go_Compiler;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Component

public class ContextPool {


    public ContextPool(){
    }
    Context getContext() throws IOException {
        Map<String, String> options = new HashMap<>();
        options.put("js.ecmascript-version", "2023");
        options.put("js.top-level-await", "true");
        options.put("js.webassembly", "true");
        options.put("js.commonjs-require", "true");
        options.put("js.esm-eval-returns-exports", "true");
        options.put("js.text-encoding", "true");
        options.put("js.unhandled-rejections", "throw");
        options.put("js.commonjs-require-cwd", Paths.get("./").toAbsolutePath().toString());
        Context context = Context.newBuilder("js","wasm")
                .allowAllAccess(true)
                .options(options)
                .build();
        context.eval("js", """
                globalThis.capturedOutput = "";
                const originalLog = console.log;
                console.log = function(...args) {
                    globalThis.capturedOutput += args.join(" ") + "\\n";
                    originalLog(...args);
                };
            """);
        context.eval(Source.newBuilder("js", ContextPool.class.getResource("/go_prep.js"))
                .build());
        context.eval(Source.newBuilder("js", ContextPool.class.getResource("/go_m.js"))
                .build());
        return context;
    }

}
