import { apiRequest } from './client.js';

export async function fetchProducts(filters = {}) {
  const params = new URLSearchParams();
  if (filters.query) params.set('query', filters.query);
  if (filters.category) params.set('category', filters.category);
  params.set('page', filters.page ?? 0);
  params.set('size', filters.size ?? 25);
  const suffix = params.toString() ? `?${params}` : '';
  const page = await apiRequest(`/api/products/page${suffix}`);
  return page.content || [];
}

export function fetchCategories() {
  return apiRequest('/api/products/categories');
}

export function createProduct(product) {
  return apiRequest('/api/products', {
    method: 'POST',
    body: JSON.stringify(product)
  });
}

export function updateProduct(id, product) {
  return apiRequest(`/api/products/${id}`, {
    method: 'PUT',
    body: JSON.stringify(product)
  });
}

export function deleteProduct(id) {
  return apiRequest(`/api/products/${id}`, { method: 'DELETE' });
}

export function createImportJob(file, idempotencyKey) {
  const body = new FormData();
  body.append('file', file);
  body.append('idempotencyKey', idempotencyKey);
  return apiRequest('/api/products/import-jobs', { method: 'POST', body });
}

export function fetchImportJob(id) {
  return apiRequest(`/api/products/import-jobs/${id}`);
}
