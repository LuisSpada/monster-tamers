import { Compass } from "lucide-react";
import { Link } from "react-router-dom";
import { paths } from "../app/paths";
import { PageHeader } from "../components/common/PageHeader";
import { PageContainer } from "../components/game/PageContainer";
import { btnPrimary, btnSecondary } from "../components/game/styles";

export function NotFoundPage() {
  return (
    <main>
      <PageHeader title="Page not found" description="The requested route is outside the current map." />
      <PageContainer>
        <div className="mx-auto max-w-md rounded-xl border border-line bg-card/90 p-8 text-center shadow-lg backdrop-blur-sm">
          <Compass className="mx-auto mb-3 h-10 w-10 text-accent" aria-hidden />
          <p className="mt-2 text-sm text-muted">You wandered off the map. This page does not exist.</p>
          <div className="mt-5 flex flex-wrap justify-center gap-3">
            <Link to={paths.home} className={btnPrimary}>Return home</Link>
            <Link to={paths.encounters} className={btnSecondary}>Go to encounters</Link>
          </div>
        </div>
      </PageContainer>
    </main>
  );
}
