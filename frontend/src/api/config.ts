/**
 * Central API configuration.
 *
 * VITE_API_MODE
 *   "mock" uses mock implementations for every service.
 *   "http" uses the backend for integrated operations and
 *   preserves mocks for operations that are not integrated yet.
 *
 * VITE_GATEWAY_BASE_URL
 *   Base URL of the API Gateway.
 *
 * VITE_MOCK_LATENCY_MS
 *   Simulated latency for operations that remain mock-backed.
 */
export type ApiMode = "mock" | "http";

function readMode(value: unknown): ApiMode {
  return value === "http" ? "http" : "mock";
}

export const apiConfig = {
  mode: readMode(import.meta.env.VITE_API_MODE),

  gatewayBaseUrl: String(
    import.meta.env.VITE_GATEWAY_BASE_URL ??
      "https://localhost:8080",
  ),

  mockLatencyMs: Number(
    import.meta.env.VITE_MOCK_LATENCY_MS ?? 350,
  ),
};