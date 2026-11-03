import { useCallback } from "react";
import { Link, useParams } from "react-router-dom";
import { ArrowLeft } from "lucide-react";
import { paths } from "../app/paths";
import { useServices } from "../api/useServices";
import { PageHeader } from "../components/common/PageHeader";
import { BattleReport } from "../components/game/BattleReport";
import { ErrorState, LoadingState } from "../components/game/AsyncState";
import { PageContainer } from "../components/game/PageContainer";
import { linkCls } from "../components/game/styles";
import { useAsync } from "../hooks/useAsync";

export function HistoryDetailPage() {
  const { battleId = "" } = useParams();
  const { history } = useServices();
  const load = useCallback(() => history.getBattle(battleId), [history, battleId]);
  const { state, reload } = useAsync(load);

  return (
    <>
      <PageHeader title="Battle details" description="Participants, rewards and the full battle log." />
      <PageContainer>
        <Link to={paths.history} className={`${linkCls} inline-flex items-center gap-1 text-sm`}>
          <ArrowLeft className="h-4 w-4" aria-hidden /> Back to history
        </Link>
        {state.status === "loading" && <LoadingState label="Loading battle…" />}
        {state.status === "error" && <ErrorState message={state.message} onRetry={reload} />}
        {state.status === "success" && <BattleReport result={state.data} variant="history" />}
      </PageContainer>
    </>
  );
}
