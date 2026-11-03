import { useLocation } from "react-router-dom";

/** Shows the current path, so tests can assert where a navigation ended up. */
export function LocationProbe() {
  const location = useLocation();
  return <div data-testid="location">{location.pathname}</div>;
}
