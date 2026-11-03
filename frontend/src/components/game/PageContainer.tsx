import type { ReactNode } from "react";

/** Consistent width and spacing for every game page (leaves room for the scenery at the bottom). */
export function PageContainer({ children }: { children: ReactNode }) {
  return <div className="mx-auto w-full max-w-[1400px] space-y-6 px-4 pb-[130px] pt-4 sm:px-6 lg:px-8 xl:space-y-7">{children}</div>;
}
