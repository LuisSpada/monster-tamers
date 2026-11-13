import {
  apiConfig,
  type ApiMode,
} from "./config";
import type { Services } from "./contracts";
import { createHybridServices } from "./hybridServices";
import { createMockServices } from "./mock/mockServices";

/**
 * The single place that selects the application's service adapters.
 *
 * Mock mode:
 * - Every service uses local mock data.
 *
 * HTTP mode:
 * - Integrated operations use the real backend.
 * - Operations not yet integrated remain mock-backed.
 */
export function createServices(
  mode: ApiMode = apiConfig.mode,
): Services {
  if (mode === "http") {
    return createHybridServices(
      apiConfig.gatewayBaseUrl,
      {
        latencyMs: apiConfig.mockLatencyMs,
      },
    );
  }

  return createMockServices({
    latencyMs: apiConfig.mockLatencyMs,
  });
}

export const defaultServices: Services = createServices();