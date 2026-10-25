import { Hammer } from "lucide-react";

export default function WorkInProgress({ title, items = [] }: { title: string; items?: string[] }) {
  return (
    <div className="flex min-h-[60vh] items-center justify-center p-6 pb-[130px]">
      <div className="w-full max-w-md rounded-xl border border-line bg-card/90 px-8 py-10 text-center">
        <Hammer className="mx-auto mb-4 h-10 w-10 text-gold" />
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
  );
}
