import { Check, Moon, Sun } from "lucide-react";
import { useServices } from "../api/useServices";
import { PageHeader } from "../components/common/PageHeader";
import { PageContainer } from "../components/game/PageContainer";
import { Panel } from "../components/game/Panel";
import { PageBanner } from "../components/game/fx/PageBanner";
import { BANNER } from "../components/game/fx/fx";
import { useThemeSetting, type ThemeChoice } from "../hooks/useThemeSetting";
import { cn } from "../lib/utils";

const OPTIONS: { value: ThemeChoice; label: string; Icon: typeof Sun; swatch: string; card: string; text: string }[] = [
  { value: "dark", label: "Dark", Icon: Moon, swatch: "bg-[#081a20]", card: "bg-[#0e242c] border-[#1e3a44]", text: "bg-[#2dd4c4]" },
  { value: "light", label: "Light", Icon: Sun, swatch: "bg-[#eaf2ef]", card: "bg-white border-[#cfdfd9]", text: "bg-[#0fb5a6]" },
];

/** Only the settings the requirements need: the display theme. Account settings come with authentication. */
export function SettingsPage() {
  const { theme, setTheme } = useThemeSetting();
  useServices(); // keeps the page on the same service wiring as the other pages
  return (
    <>
      <PageHeader title="Settings" description="Adjust how Monster Tamers looks." />
      <PageContainer>
        <PageBanner image={BANNER.hero} tone="violet" eyebrow="Make it yours" tagline="Day or night, the forest is waiting." />
        <Panel title="Appearance" className="animate-rise [animation-delay:100ms]">
          <fieldset>
            <legend className="mb-3 text-sm text-muted">Theme</legend>
            <div className="grid gap-4 sm:grid-cols-2">
              {OPTIONS.map(({ value, label, Icon, swatch, card, text }) => {
                const selected = theme === value;
                return (
                  <label
                    key={value}
                    className={cn(
                      "relative cursor-pointer overflow-hidden rounded-xl border-2 p-3 text-sm font-medium transition-all focus-within:outline focus-within:outline-2 focus-within:outline-accent hover:-translate-y-0.5",
                      selected ? "border-accent bg-accent/10 text-accent" : "border-line bg-inset/60",
                    )}
                  >
                    <input type="radio" name="theme" value={value} checked={selected} onChange={() => setTheme(value)} className="sr-only" />
                    <div aria-hidden className={cn("mb-3 flex h-24 gap-2 rounded-lg p-2", swatch)}>
                      <div className="w-1/4 rounded bg-black/10" />
                      <div className="flex flex-1 flex-col gap-2">
                        <div className={cn("h-3 w-2/3 rounded", text)} />
                        <div className={cn("flex-1 rounded border", card)} />
                      </div>
                    </div>
                    <span className="flex items-center gap-2">
                      <Icon className="h-4 w-4" aria-hidden /> {label}
                    </span>
                    {selected && (
                      <span aria-hidden className="absolute right-3 top-3 grid h-6 w-6 animate-pop place-items-center rounded-full bg-accent text-accent-fg">
                        <Check className="h-4 w-4" />
                      </span>
                    )}
                  </label>
                );
              })}
            </div>
          </fieldset>
        </Panel>
      </PageContainer>
    </>
  );
}
