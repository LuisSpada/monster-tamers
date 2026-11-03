import type { ReactNode } from "react";
import type { Services } from "./contracts";
import { ServicesContext } from "./servicesContext";
import { defaultServices } from "./createServices";

/** Optional: pages work without it (they fall back to the configured services). Used mainly in tests. */
export function ServicesProvider({ services = defaultServices, children }: { services?: Services; children: ReactNode }) {
  return <ServicesContext.Provider value={services}>{children}</ServicesContext.Provider>;
}
