export class ApiError extends Error {
  status: number;
  reference?: string;

  constructor(status: number, message: string, reference?: string) {
    super(message);
    this.status = status;
    this.reference = reference;
  }

  /** Precies zoveel als de server kwijt wil. Niet meer. */
  describe(): string {
    const parts = [`HTTP ${this.status}`, this.message];
    if (this.reference) {
      parts.push(`ref ${this.reference}`);
    }
    return parts.join(' - ');
  }
}

async function call<T>(method: string, path: string, body?: unknown): Promise<T> {
  const response = await fetch(`/api${path}`, {
    method,
    headers: { 'content-type': 'application/json' },
    body: body === undefined ? undefined : JSON.stringify(body)
  });

  const text = await response.text();
  const payload = text ? JSON.parse(text) : null;

  if (!response.ok) {
    throw new ApiError(response.status, payload?.error ?? 'onbekende fout', payload?.ref);
  }
  return payload as T;
}

export const api = {
  get: <T>(path: string) => call<T>('GET', path),
  post: <T>(path: string, body?: unknown) => call<T>('POST', path, body ?? {}),
  patch: <T>(path: string, body: unknown) => call<T>('PATCH', path, body),
  del: <T>(path: string) => call<T>('DELETE', path)
};
