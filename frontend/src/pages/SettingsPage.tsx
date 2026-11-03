import { Moon, Sun } from "lucide-react";
import { useServices } from "../api/useServices";
import { PageHeader } from "../components/common/PageHeader";
import { PageContainer } from "../components/game/PageContainer";
import { Panel } from "../components/game/Panel";
import { useThemeSetting, type ThemeChoice } from "../hooks/useThemeSetting";
import { cn } from "../lib/utils";

const OPTIONS: { value: ThemeChoice; label: string; Icon: typeof Sun }[] = [
  { value: "dark", label: "Dark", Icon: Moon },
  { value: "light", label: "Light", Icon: Sun },
];

/** Only the settings the requirements need: the display theme. Account settings come with authentication. */
export function SettingsPage() {
  const { theme, setTheme } = useThemeSetting();
  useServices(); // keeps the page on the same service wiring as the other pages
  return (
    <>
      <PageHeader title="Settings" description="Adjust how Monster Tamers looks." />
      <PageContainer>
        <Panel title="Appearance" className="bg-[radial-gradient(circle_at_100%_0%,color-mix(in_srgb,var(--accent)_14%,transparent),transparent_45%)]">
          <fieldset>
            <legend className="mb-3 text-sm text-muted">Theme</legend>
            <div className="grid gap-3 sm:grid-cols-2">
              {OPTIONS.map(({ value, label, Icon }) => (
                <label
                  key={value}
                  className={cn(
                    "flex cursor-pointer items-center gap-2 rounded-lg border px-4 py-3 text-sm font-medium focus-within:outline focus-within:outline-2 focus-within:outline-accent",
                    theme === value ? "border-accent bg-accent/15 text-accent" : "border-line bg-inset/60",
                  )}
                >
                  <input type="radio" name="theme" value={value} checked={theme === value} onChange={() => setTheme(value)} className="sr-only" />
                  <Icon className="h-4 w-4" aria-hidden /> {label}
                </label>
              ))}
            </div>
          </fieldset>
        </Panel>
      </PageContainer>
    </>
  );
}
