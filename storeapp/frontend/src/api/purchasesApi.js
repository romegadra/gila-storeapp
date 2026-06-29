import { apiRequest } from './client.js';

export function createPurchase(items) {
  return apiRequest('/api/purchases', {
    method: 'POST',
    body: JSON.stringify({ items })
  });
}
