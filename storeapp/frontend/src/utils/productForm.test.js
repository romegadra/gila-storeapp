import assert from 'node:assert/strict';
import { describe, it } from 'node:test';
import { emptyProductForm, formToProduct, productToForm } from './productForm.js';

describe('product form helpers', () => {
  it('keeps the empty form shape stable', () => {
    assert.deepEqual(emptyProductForm, {
      name: '',
      sku: '',
      description: '',
      category: '',
      price: '',
      stock: '',
      weightKg: ''
    });
  });

  it('maps API products into editable form values', () => {
    assert.deepEqual(
      productToForm({
        name: 'Trail Backpack',
        sku: 'PACK-1',
        description: null,
        category: 'Outdoors',
        price: 79.99,
        stock: 12,
        weightKg: 0.9
      }),
      {
        name: 'Trail Backpack',
        sku: 'PACK-1',
        description: '',
        category: 'Outdoors',
        price: 79.99,
        stock: 12,
        weightKg: 0.9
      }
    );
  });

  it('converts numeric form fields before sending them to the API', () => {
    assert.deepEqual(
      formToProduct({
        name: 'Trail Backpack',
        sku: 'PACK-1',
        description: 'Weather resistant',
        category: 'Outdoors',
        price: '79.99',
        stock: '12',
        weightKg: '0.9'
      }),
      {
        name: 'Trail Backpack',
        sku: 'PACK-1',
        description: 'Weather resistant',
        category: 'Outdoors',
        price: 79.99,
        stock: 12,
        weightKg: 0.9
      }
    );
  });
});
