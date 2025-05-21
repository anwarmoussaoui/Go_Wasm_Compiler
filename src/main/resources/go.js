
async function loadAndRunWasm() {
    const go = new Go(); // defined by wasm_exec.js

    const { instance } = await WebAssembly.instantiate(new Uint8Array(wasmbytes), go.importObject);
    go.run(instance);
}

loadAndRunWasm();
