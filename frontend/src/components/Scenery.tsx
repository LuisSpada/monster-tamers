export function SceneBackground({
  overlay = "",
  className,
}: { overlay?: string; className?: string }) {
  return (
    <div aria-hidden className={`pointer-events-none absolute inset-0 -z-20 ${className ?? ""}`}>
      <div className="absolute inset-0 bg-[radial-gradient(circle_at_15%_10%,color-mix(in_srgb,var(--accent)_18%,transparent),transparent_35%),radial-gradient(circle_at_85%_85%,rgba(99,102,241,0.12),transparent_35%)]" />
      <div className="atmosphere-grid absolute inset-0 opacity-40" />
      <div className={`absolute inset-0 ${overlay}`} />
    </div>
  );
}

export default function Scenery() {
  return <SceneBackground className="-z-10" />;
}
