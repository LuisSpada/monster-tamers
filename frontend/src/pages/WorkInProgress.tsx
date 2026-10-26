function HammerIcon() {
  return (
    <svg
      aria-hidden="true"
      className="mx-auto mb-4 h-10 w-10 text-gold"
      fill="none"
      viewBox="0 0 24 24"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <path d="m15 5 4 4" />
      <path d="M13 7 8.7 2.7a1 1 0 0 0-1.4 0L2.7 7.3a1 1 0 0 0 0 1.4L7 13" />
      <path d="m8 6 10 10" />
      <path d="m16 8 4.3 4.3a1 1 0 0 1 0 1.4l-4.6 4.6a1 1 0 0 1-1.4 0L10 14" />
      <path d="m2 22 6-6" />
    </svg>
  );
}

export default function WorkInProgress({ title, items = [] }: { title: string; items?: string[] }) {
  return (
    <div className="relative isolate min-h-full overflow-hidden">
      <div className="flex min-h-[68vh] w-full items-center justify-center p-4 pb-[130px] lg:px-6">
        <div className="w-full max-w-6xl rounded-xl border border-line bg-card/90 px-8 py-16 text-center shadow-lg shadow-black/10 backdrop-blur-sm">
          <HammerIcon />
          <h1 className="font-pixel text-3xl font-bold">{title}</h1>
          <p className="mt-2 text-muted">Work in progress – this page is coming soon.</p>
          {items.length > 0 && (
            <div className="mt-6 border-t border-line pt-4 text-left">
              <div className="mb-2 text-sm font-semibold">Planned</div>
              <ul className="list-disc space-y-1 pl-5 text-sm text-muted">
                {items.map((i) => <li key={i}>{i}</li>)}
              </ul>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
