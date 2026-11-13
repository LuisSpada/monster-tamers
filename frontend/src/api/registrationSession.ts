const REGISTRATION_SESSION_KEY =
  "monster-tamers.pending-registration";

export interface PendingRegistration {
  registrationId: string;
  email: string;
}

export function getPendingRegistration():
  | PendingRegistration
  | null {
  const value = sessionStorage.getItem(
    REGISTRATION_SESSION_KEY,
  );

  if (!value) {
    return null;
  }

  try {
    const parsed = JSON.parse(
      value,
    ) as Partial<PendingRegistration>;

    if (
      typeof parsed.registrationId !== "string" ||
      parsed.registrationId.length === 0 ||
      typeof parsed.email !== "string" ||
      parsed.email.length === 0
    ) {
      clearPendingRegistration();
      return null;
    }

    return {
      registrationId: parsed.registrationId,
      email: parsed.email,
    };
  } catch {
    clearPendingRegistration();
    return null;
  }
}

export function setPendingRegistration(
  registration: PendingRegistration,
): void {
  sessionStorage.setItem(
    REGISTRATION_SESSION_KEY,
    JSON.stringify(registration),
  );
}

export function clearPendingRegistration(): void {
  sessionStorage.removeItem(
    REGISTRATION_SESSION_KEY,
  );
}