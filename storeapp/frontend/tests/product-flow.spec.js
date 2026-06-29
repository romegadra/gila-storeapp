import { expect, test } from '@playwright/test';

test('searches products, completes fake purchase, and shows order history', async ({ page }) => {
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
  let purchases = [];

  await page.route('**/api/products/categories', async (route) => {
    await route.fulfill({ json: ['Footwear'] });
  });

  await page.route('**/api/products/page?**', async (route) => {
    await route.fulfill({
      json: {
        content: products,
        totalElements: products.length,
        totalPages: 1,
        number: 0,
        size: 25
      }
    });
  });

  await page.route('**/api/products', async (route) => {
    await route.fulfill({ json: products });
  });

  await page.route('**/api/purchases', async (route) => {
    if (route.request().method() === 'GET') {
      await route.fulfill({ json: purchases });
      return;
    }

    products = [{ ...products[0], stock: 8 }];
    const purchase = {
      id: 77,
      status: 'PAID',
      total: 179.98,
      purchasedAt: '2026-06-29T18:00:00Z',
      items: [
        {
          productId: 1,
          productName: 'Running Shoes',
          sku: 'RS-001',
          quantity: 2,
          unitPrice: 89.99,
          lineTotal: 179.98
        }
      ]
    };
    purchases = [purchase];
    await route.fulfill({ status: 201, json: purchase });
  });

  await page.goto('/');
  await page.getByRole('button', { name: /user1/i }).click();
  await page.getByRole('button', { name: 'Sign in' }).click();
  await expect(page.getByRole('button', { name: 'Catalog' })).toHaveCount(0);
  await expect(page.getByText('Running Shoes')).toBeVisible();
  await page.getByPlaceholder('Search by name, SKU, description').fill('shoe');
  await page.getByRole('button', { name: 'Search' }).click();
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
  await expect(page.getByText('Purchase confirmed')).toBeVisible();
  await expect(page.getByText('Order #77 - $179.98 paid')).toBeVisible();
  await page.getByTitle('Account menu').click();
  await page.getByRole('menuitem', { name: 'Order history' }).click();
  await expect(page.getByText('Purchase history')).toBeVisible();
  await expect(page.getByText('Order #77')).toBeVisible();
  await expect(page.getByText('Running Shoes')).toBeVisible();
  await expect(page.getByText('2 x $89.99')).toBeVisible();
});
