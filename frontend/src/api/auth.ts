import { apiConfig } from "./config";
import { createHttpClient } from "./httpClient";

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
}

export interface RegistrationRequest {
  email: string;
  password: string;
  confirmPassword: string;
}

export interface RegistrationResponse {
  registrationId: string;
}

export interface ConfirmationRequest {
  code: string;
}

const authClient = createHttpClient(
  apiConfig.gatewayBaseUrl,
  false,
);

export function login(
  email: string,
  password: string,
): Promise<LoginResponse> {
  const request: LoginRequest = {
    email,
    password,
  };

  return authClient.post<LoginResponse>(
    "/api/auth/login",
    request,
  );
}

export function register(
  request: RegistrationRequest,
): Promise<RegistrationResponse> {
  return authClient.post<RegistrationResponse>(
    "/api/auth/registrations",
    request,
  );
}

export function confirmRegistration(
  registrationId: string,
  code: string,
): Promise<void> {
  const request: ConfirmationRequest = {
    code,
  };

  return authClient.post<void>(
    `/api/auth/registrations/${encodeURIComponent(
      registrationId,
    )}/confirmation`,
    request,
  );
}