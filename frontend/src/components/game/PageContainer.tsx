import type { ReactNode } from "react";

/** Consistent width and spacing for every game page (leaves room for the scenery at the bottom). */
export function PageContainer({ children }: { children: ReactNode }) {
  return <div className="mx-auto w-full max-w-[1600px] space-y-7 px-4 pb-[130px] pt-5 sm:px-6 lg:px-10 xl:space-y-8">{children}</div>;
}
