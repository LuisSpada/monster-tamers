import { apiConfig, type ApiMode } from "./config";
import type { Services } from "./contracts";
import { createHttpServices } from "./httpServices";
import { createMockServices } from "./mock/mockServices";

/** The ONE place that decides between mock and real services. */
export function createServices(mode: ApiMode = apiConfig.mode): Services {
  if (mode === "http") return createHttpServices(apiConfig.gatewayBaseUrl);
  return createMockServices({ latencyMs: apiConfig.mockLatencyMs });
}

export const defaultServices: Services = createServices();
