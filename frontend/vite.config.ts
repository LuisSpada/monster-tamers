import react from "@vitejs/plugin-react";
import tailwindcss from "@tailwindcss/vite";
import { fileURLToPath } from "node:url";
import { defineConfig as defineVitestConfig } from "vitest/config";

const srcPath = fileURLToPath(new URL("./src", import.meta.url));

export default defineVitestConfig({
  plugins: [react(), tailwindcss()],
  resolve: { alias: { "@": srcPath } },
  server: { host: "0.0.0.0", port: 5173 },
  preview: { host: "0.0.0.0", port: 4173 },
  test: {
    environment: "jsdom",
    setupFiles: "./src/test/setup.ts",
    css: true,
  },
});
