const API_BASE = import.meta.env.VITE_API_BASE_URL || '';

export async function apiRequest(path, options = {}) {
  const response = await fetch(`${API_BASE}${path}`, {
    headers: options.body instanceof FormData ? undefined : { 'Content-Type': 'application/json' },
    ...options
  });

  if (!response.ok) {
    const payload = await response.json().catch(() => ({}));
    const details = payload.details?.length ? `: ${payload.details.join(', ')}` : '';
    throw new Error(`${payload.message || 'Request failed'}${details}`);
  }

  if (response.status === 204) {
    return null;
  }

  return response.json();
}
