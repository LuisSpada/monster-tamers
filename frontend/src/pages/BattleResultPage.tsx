import { useCallback } from "react";
import { useParams } from "react-router-dom";
import { useServices } from "../api/useServices";
import { PageHeader } from "../components/common/PageHeader";
import { BattleReport } from "../components/game/BattleReport";
import { ErrorState, LoadingState } from "../components/game/AsyncState";
import { PageContainer } from "../components/game/PageContainer";
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
        {state.status === "success" && <BattleReport result={state.data} variant="result" />}
      </PageContainer>
    </>
  );
}
