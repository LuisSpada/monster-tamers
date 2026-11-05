import { useTheme, type Theme } from "../lib/theme";

export type ThemeChoice = Theme;

export function useThemeSetting() {
  return useTheme();
}
