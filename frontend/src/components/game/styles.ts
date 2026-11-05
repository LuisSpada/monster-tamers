/** Shared class strings so buttons, fields and chips look the same on every page. */
const focus =
  "focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-accent";
const disabled = "disabled:cursor-not-allowed disabled:opacity-50 disabled:hover:brightness-100";

export const btnPrimary = `inline-flex items-center justify-center gap-2 rounded-md bg-accent px-4 py-2 text-sm font-semibold text-accent-fg transition-[filter] hover:brightness-110 ${focus} ${disabled}`;
export const btnSecondary = `inline-flex items-center justify-center gap-2 rounded-md border border-line bg-inset/70 px-4 py-2 text-sm font-medium text-fg transition-colors hover:bg-inset ${focus} ${disabled}`;
export const btnDanger = `inline-flex items-center justify-center gap-2 rounded-md border border-danger/40 bg-danger/10 px-3 py-1.5 text-sm font-medium text-danger transition-colors hover:bg-danger/20 ${focus} ${disabled}`;
export const fieldCls = `h-10 rounded-md border border-line bg-inset/60 px-3 text-sm text-fg placeholder:text-muted ${focus}`;
export const chip = `inline-flex items-center gap-1 rounded-md border border-line bg-inset px-2 py-0.5 text-xs text-muted`;
export const linkCls = `font-medium text-accent hover:underline ${focus}`;
export const surfaceCard = `rounded-xl border border-line bg-card/90 shadow-lg shadow-black/10 backdrop-blur-sm`;
export const listCard = `${surfaceCard} transition-[transform,border-color,box-shadow] duration-200 hover:-translate-y-0.5 hover:border-accent hover:shadow-xl hover:shadow-accent/5 motion-reduce:transition-none motion-reduce:hover:translate-y-0 ${focus}`;
