import { apiConfig } from "./config";
import { createHttpClient } from "./httpClient";

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
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