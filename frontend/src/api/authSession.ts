const ACCESS_TOKEN_KEY =
  "monster-tamers.access-token";

export const AUTH_SESSION_CHANGED_EVENT =
  "monster-tamers-auth-changed";

function notifyAuthSessionChanged(): void {
  window.dispatchEvent(
    new Event(AUTH_SESSION_CHANGED_EVENT),
  );
}

export function getAccessToken(): string | null {
  return sessionStorage.getItem(ACCESS_TOKEN_KEY);
}

export function setAccessToken(
  accessToken: string,
): void {
  sessionStorage.setItem(
    ACCESS_TOKEN_KEY,
    accessToken,
  );

  notifyAuthSessionChanged();
}

export function clearAccessToken(): void {
  sessionStorage.removeItem(ACCESS_TOKEN_KEY);
  notifyAuthSessionChanged();
}