export function PageHeader({
  title,
  description,
}: {
  title: string;
  description: string;
}) {
  return (
    <header className="relative mx-auto w-full max-w-[1400px] px-4 pb-2 pt-6 sm:px-6 lg:px-8">
      <div className="relative overflow-hidden rounded-xl border border-line bg-card/70 px-5 py-5 shadow-lg shadow-black/10 backdrop-blur-sm sm:px-7 sm:py-6">
        <div className="pointer-events-none absolute -right-10 -top-16 h-40 w-40 rounded-full bg-accent/10 blur-3xl" />
        <p className="relative mb-1 font-pixel text-xs font-semibold uppercase tracking-[0.2em] text-accent">
          Monster Tamers
        </p>
        <h1 className="relative font-pixel text-3xl font-bold leading-tight text-fg sm:text-4xl">
          {title}
        </h1>
        <p className="relative mt-2 max-w-3xl text-sm leading-relaxed text-muted sm:text-base">
          {description}
        </p>
        <div className="mt-4 h-px w-24 bg-gradient-to-r from-accent to-transparent" />
      </div>
    </header>
  );
}
