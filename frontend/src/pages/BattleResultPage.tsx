import { useCallback } from "react";
import { Link, useParams } from "react-router-dom";
import { RotateCcw } from "lucide-react";
import { paths } from "../app/paths";
import { useServices } from "../api/useServices";
import { PageHeader } from "../components/common/PageHeader";
import { BattleReport } from "../components/game/BattleReport";
import { ErrorState, LoadingState } from "../components/game/AsyncState";
import { PageContainer } from "../components/game/PageContainer";
import { BattleSplash } from "../components/game/fx/BattleSplash";
import { Confetti } from "../components/game/fx/Confetti";
import { btnPrimary } from "../components/game/styles";
import { useAsync } from "../hooks/useAsync";

export function BattleResultPage() {
  const { battleId = "" } = useParams();
  const { history } = useServices();
  const load = useCallback(() => history.getBattle(battleId), [history, battleId]);
  const { state, reload } = useAsync(load);

  return (
    <>
      <PageHeader title="Battle result" description="The outcome of your battle and the rewards you earned." />
      <PageContainer>
        {state.status === "loading" && <LoadingState label="Loading battle result…" />}
        {state.status === "error" && <ErrorState message={state.message} onRetry={reload} />}
        {state.status === "success" && (
          <>
            {state.data.outcome === "victory" && <Confetti />}
            <BattleSplash outcome={state.data.outcome} />
            <BattleReport result={state.data} variant="result" />
            <Link to={paths.prepare(state.data.encounterId)} className={btnPrimary}>
              <RotateCcw className="h-4 w-4" aria-hidden /> Fight this encounter again
            </Link>
          </>
        )}
      </PageContainer>
    </>
  );
}
