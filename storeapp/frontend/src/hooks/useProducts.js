import { useEffect, useMemo, useState } from 'react';
import {
  createImportJob,
  createProduct,
  deleteProduct,
  fetchCategories,
  fetchImportJob,
  fetchProducts,
  updateProduct
} from '../api/productsApi.js';
import { createPurchase } from '../api/purchasesApi.js';
import { emptyProductForm, formToProduct, productToForm } from '../utils/productForm.js';
import { money } from '../utils/formatters.js';

export function useProducts() {
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [filters, setFilters] = useState({ query: '', category: '' });
  const [form, setForm] = useState(emptyProductForm);
  const [editingId, setEditingId] = useState(null);
  const [cart, setCart] = useState({});
  const [notice, setNotice] = useState(null);
  const [busy, setBusy] = useState(false);
  const [importReport, setImportReport] = useState(null);

  useEffect(() => {
    refreshProducts();
    loadCategories();
  }, []);

  const cartTotal = useMemo(() => {
    return products.reduce((total, product) => total + Number(product.price) * Number(cart[product.id] || 0), 0);
  }, [cart, products]);

  function updateFilters(changes) {
    setFilters((current) => ({ ...current, ...changes }));
  }

  function updateForm(changes) {
    setForm((current) => ({ ...current, ...changes }));
  }

  function updateCartQuantity(productId, quantity) {
    const product = products.find((item) => item.id === productId);
    const maxStock = Number(product?.stock || 0);
    const requested = Number(quantity || 0);
    const nextQuantity = Math.max(0, Math.min(requested, maxStock));

    setCart((current) => ({ ...current, [productId]: nextQuantity ? String(nextQuantity) : '' }));

    if (requested > maxStock) {
      setNotice({
        type: 'warning',
        message: `${product.name} only has ${maxStock} in stock. Quantity was adjusted.`
      });
    }
  }

  function removeCartItem(productId) {
    setCart((current) => ({ ...current, [productId]: '' }));
  }

  function clearCart() {
    setCart({});
    setNotice({ type: 'success', message: 'Cart cleared' });
  }

  async function refreshProducts(nextFilters = filters) {
    await run(async () => {
      setProducts(await fetchProducts(nextFilters));
    });
  }

  async function loadCategories() {
    setCategories(await fetchCategories());
  }

  async function searchProducts(event) {
    event.preventDefault();
    await refreshProducts();
  }

  async function saveProduct(event) {
    event.preventDefault();
    await run(async () => {
      const payload = formToProduct(form);
      if (editingId) {
        await updateProduct(editingId, payload);
        setNotice({ type: 'success', message: 'Product updated' });
      } else {
        await createProduct(payload);
        setNotice({ type: 'success', message: 'Product created' });
      }
      resetForm();
      await reloadCatalog();
    });
  }

  function editProduct(product) {
    setEditingId(product.id);
    setForm(productToForm(product));
  }

  function cancelEdit() {
    resetForm();
  }

  async function removeProduct(product) {
    await run(async () => {
      await deleteProduct(product.id);
      setNotice({ type: 'success', message: `${product.name} deleted` });
      await reloadCatalog();
    });
  }

  async function importProducts(event) {
    const file = event.target.files?.[0];
    if (!file) return;

    await run(async () => {
      const job = await createImportJob(file);
      setNotice({ type: 'success', message: `Import job #${job.id} queued` });
      const completed = await waitForImport(job.id);
      setImportReport(completed);
      setNotice({
        type: completed.skipped > 0 ? 'warning' : 'success',
        message: `Import completed: ${completed.created} created, ${completed.updated} updated, ${completed.skipped} skipped`
      });
      await reloadCatalog();
    });

    event.target.value = '';
  }

  async function checkout() {
    const items = Object.entries(cart)
      .map(([productId, quantity]) => ({ productId: Number(productId), quantity: Number(quantity) }))
      .filter((item) => item.quantity > 0);

    if (!items.length) {
      setNotice({ type: 'warning', message: 'Add at least one product to checkout' });
      return;
    }

    await run(async () => {
      const purchase = await createPurchase(items);
      setCart({});
      setNotice({ type: 'success', message: `Purchase #${purchase.id} paid for $${money(purchase.total)}` });
      setProducts(await fetchProducts(filters));
    });
  }

  async function waitForImport(jobId) {
    for (let attempt = 0; attempt < 20; attempt += 1) {
      const job = await fetchImportJob(jobId);
      if (job.status === 'COMPLETED') {
        return {
          created: job.created,
          updated: job.updated,
          skipped: job.skipped,
          errors: job.errorSummary ? [{ row: 'summary', sku: '', message: job.errorSummary }] : []
        };
      }
      if (job.status === 'FAILED') {
        throw new Error(job.errorSummary || 'Import failed');
      }
      await new Promise((resolve) => setTimeout(resolve, 500));
    }
    throw new Error('Import is still running. Check import job status later.');
  }

  async function reloadCatalog() {
    setProducts(await fetchProducts(filters));
    setCategories(await fetchCategories());
  }

  async function run(action) {
    try {
      setBusy(true);
      setNotice(null);
      await action();
    } catch (error) {
      setNotice({ type: 'error', message: error.message });
    } finally {
      setBusy(false);
    }
  }

  function resetForm() {
    setForm(emptyProductForm);
    setEditingId(null);
  }

  return {
    products,
    categories,
    filters,
    form,
    editingId,
    cart,
    notice,
    busy,
    importReport,
    cartTotal,
    updateFilters,
    updateForm,
    updateCartQuantity,
    removeCartItem,
    clearCart,
    searchProducts,
    saveProduct,
    editProduct,
    cancelEdit,
    removeProduct,
    importProducts,
    checkout
  };
}
