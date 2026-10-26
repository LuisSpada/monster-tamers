import { useState } from "react";
import { cn } from "@/lib/utils";

/**
 * Viser et bilde fra /public/assets hvis det finnes.
 * Ellers vises en gradient-placeholder (via className) – legg inn pikselkunsten senere.
 */
export default function SceneImage({
  src, alt = "", className, children,
}: { src: string; alt?: string; className?: string; children?: React.ReactNode }) {
  const [failed, setFailed] = useState(false);
  return (
    <div className={cn("relative overflow-hidden", className)}>
      {!failed && (
        <img src={src} alt={alt} onError={() => setFailed(true)}
          className="absolute inset-0 h-full w-full object-cover [image-rendering:pixelated]" />
      )}
      {children}
    </div>
  );
}
