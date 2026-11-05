/**
 * Central API configuration.
 *
 * VITE_API_MODE            "mock" (default) | "http"
 * VITE_GATEWAY_BASE_URL    Base URL of the future API gateway
 * VITE_MOCK_LATENCY_MS     Simulated latency of the mock services
 */
export type ApiMode = "mock" | "http";

function readMode(value: unknown): ApiMode {
  return value === "http" ? "http" : "mock";
}

export const apiConfig = {
  mode: readMode(import.meta.env.VITE_API_MODE),
  gatewayBaseUrl: String(import.meta.env.VITE_GATEWAY_BASE_URL ?? "http://localhost:8080"),
  mockLatencyMs: Number(import.meta.env.VITE_MOCK_LATENCY_MS ?? 350),
};
