import { useContext } from "react";
import type { Services } from "./contracts";
import { ServicesContext } from "./servicesContext";

export function useServices(): Services {
  return useContext(ServicesContext);
}
