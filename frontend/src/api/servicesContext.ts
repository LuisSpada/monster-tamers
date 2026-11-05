import { createContext } from "react";
import { defaultServices } from "./createServices";
import type { Services } from "./contracts";

export const ServicesContext = createContext<Services>(defaultServices);
