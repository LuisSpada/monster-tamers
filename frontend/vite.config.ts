import { fileURLToPath } from "node:url";
import react from "@vitejs/plugin-react";
import path from "path";
import { defineConfig as defineVitestConfig } from "vitest/config";

const srcPath = fileURLToPath(new URL("./src", import.meta.url));

export default defineVitestConfig({
  plugins: [react()],
  resolve: { alias: { "@": path.resolve(srcPath) } },
  test: {
    environment: "jsdom",
    setupFiles: "./src/test/setup.ts",
  },
});
