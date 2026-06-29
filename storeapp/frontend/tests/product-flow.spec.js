import { expect, test } from '@playwright/test';

test('searches products and completes fake purchase', async ({ page }) => {
  let products = [
    {
      id: 1,
      name: 'Running Shoes',
      sku: 'RS-001',
      description: 'Lightweight running shoes',
      category: 'Footwear',
      price: 89.99,
      stock: 10,
      weightKg: 0.35
    }
  ];

  await page.route('**/api/products/categories', async (route) => {
    await route.fulfill({ json: ['Footwear'] });
  });

  await page.route('**/api/products?**', async (route) => {
    await route.fulfill({ json: products });
  });

  await page.route('**/api/products', async (route) => {
    await route.fulfill({ json: products });
  });

  await page.route('**/api/purchases', async (route) => {
    products = [{ ...products[0], stock: 8 }];
    await route.fulfill({
      status: 201,
      json: {
        id: 77,
        status: 'PAID',
        total: 179.98,
        items: []
      }
    });
  });

  await page.goto('/');
  await expect(page.getByText('Running Shoes')).toBeVisible();
  await page.getByPlaceholder('Search by name, SKU, description').fill('shoe');
  await page.getByRole('button', { name: 'Search' }).click();
  await page.getByRole('button', { name: 'Purchase' }).click();
  await page.locator('.qty').fill('12');
  await expect(page.locator('.qty')).toHaveValue('10');
  await expect(page.getByText('only has 10 in stock')).toBeVisible();
  await page.getByTitle('Remove from cart').click();
  await expect(page.getByText('Select quantities from the product list.')).toBeVisible();
  await page.locator('.qty').fill('2');
  await page.getByRole('button', { name: 'Clear Cart' }).click();
  await expect(page.getByText('Cart cleared')).toBeVisible();
  await page.locator('.qty').fill('2');
  await page.getByRole('button', { name: 'Complete payment' }).click();
  await expect(page.getByText('Purchase #77 paid for $179.98')).toBeVisible();
});
