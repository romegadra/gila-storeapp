import { apiRequest } from './client.js';

export function fetchPurchases() {
  return apiRequest('/api/purchases');
}

export function createPurchase(items, idempotencyKey) {
  return apiRequest('/api/purchases', {
    method: 'POST',
    body: JSON.stringify({ idempotencyKey, items })
  });
}
