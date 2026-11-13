export interface ProblemDetails {
  type?: string;
  title?: string;
  status?: number;
  detail?: string;
  code?: string;
  message?: string;
  errors?: unknown[];
}

export class ApiError extends Error {
  readonly status: number;
  readonly code: string;

  constructor(status: number, code: string, message: string) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.code = code;
  }
}

export async function createApiError(
  response: Response,
): Promise<ApiError> {
  let code = "HTTP_ERROR";
  let message = `Request failed with status ${response.status}.`;

  try {
    const problem = (await response.json()) as ProblemDetails;

    if (problem.code) {
      code = problem.code;
    }

    if (problem.detail) {
      message = problem.detail;
    } else if (problem.message) {
      message = problem.message;
    } else if (problem.title) {
      message = problem.title;
    }
  } catch {
    // The server did not return a JSON Problem Details response.
  }

  return new ApiError(response.status, code, message);
}

export function getErrorMessage(error: unknown): string {
  if (error instanceof Error && error.message) {
    return error.message;
  }

  return "Something went wrong. Please try again.";
}
