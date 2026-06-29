import { useState } from 'react';
import { CartPanel } from './components/CartPanel.jsx';
import { Header } from './components/Header.jsx';
import { ImportIssues } from './components/ImportIssues.jsx';
import { LoginPage } from './components/LoginPage.jsx';
import { OrderHistory } from './components/OrderHistory.jsx';
import { PurchaseToast } from './components/PurchaseToast.jsx';
import { PurchaseProductList } from './components/PurchaseProductList.jsx';
import { ProductForm } from './components/ProductForm.jsx';
import { ProductTable } from './components/ProductTable.jsx';
import { Tabs } from './components/Tabs.jsx';
import { Toolbar } from './components/Toolbar.jsx';
import { useProducts } from './hooks/useProducts.js';

export function App() {
  const [currentUser, setCurrentUser] = useState(null);
  const [loginCredentials, setLoginCredentials] = useState({ username: 'admin', password: 'admin123' });
  const [loginError, setLoginError] = useState('');
  const [activeTab, setActiveTab] = useState('catalog');
  const [showOrderHistory, setShowOrderHistory] = useState(false);
  const {
    products,
    categories,
    filters,
    form,
    editingId,
    cart,
    notice,
    purchaseReceipt,
    orderHistory,
    historyLoading,
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
    checkout,
    dismissPurchaseReceipt,
    loadOrderHistory
  } = useProducts();
  const isAdmin = currentUser?.role === 'ADMIN';

  function updateLoginCredentials(changes) {
    setLoginCredentials((current) => ({ ...current, ...changes }));
    setLoginError('');
  }

  function login(user) {
    if (!user) {
      setLoginError('Invalid username or password');
      return;
    }
    setCurrentUser(user);
    setActiveTab(user.role === 'ADMIN' ? 'catalog' : 'purchase');
  }

  function logout() {
    setCurrentUser(null);
    setLoginCredentials({ username: 'admin', password: 'admin123' });
    setLoginError('');
    setShowOrderHistory(false);
    dismissPurchaseReceipt();
  }

  async function openOrderHistory() {
    setShowOrderHistory(true);
    await loadOrderHistory();
  }

  if (!currentUser) {
    return (
      <LoginPage
        credentials={loginCredentials}
        error={loginError}
        onChange={updateLoginCredentials}
        onLogin={login}
      />
    );
  }

  return (
    <main className="app-shell">
      <Header
        user={currentUser}
        onOrderHistory={openOrderHistory}
        onLogout={logout}
      />

      <Tabs activeTab={activeTab} isAdmin={isAdmin} onChange={setActiveTab} />

      {notice && <div className={`notice ${notice.type}`}>{notice.message}</div>}

      {showOrderHistory && (
        <OrderHistory
          orders={orderHistory}
          loading={historyLoading}
          onClose={() => setShowOrderHistory(false)}
        />
      )}

      {activeTab === 'catalog' && isAdmin && !showOrderHistory && (
        <>
          <Toolbar
            filters={filters}
            categories={categories}
            busy={busy}
            onFilterChange={updateFilters}
            onSearch={searchProducts}
            onImport={importProducts}
          />

          <section className="workspace">
            <ProductForm
              form={form}
              editingId={editingId}
              busy={busy}
              onChange={updateForm}
              onSubmit={saveProduct}
              onCancel={cancelEdit}
            />
          </section>

          <ProductTable
            products={products}
            onEdit={editProduct}
            onDelete={removeProduct}
          />

          <ImportIssues report={importReport} />
        </>
      )}

      {activeTab === 'purchase' && !showOrderHistory && (
        <>
          <Toolbar
            filters={filters}
            categories={categories}
            busy={busy}
            onFilterChange={updateFilters}
            onSearch={searchProducts}
            showImport={false}
          />

          <section className="workspace purchase-workspace">
            <PurchaseProductList
              products={products}
              cart={cart}
              onQuantityChange={updateCartQuantity}
            />
            <CartPanel
              products={products}
              cart={cart}
              cartTotal={cartTotal}
              busy={busy}
              onRemoveItem={removeCartItem}
              onClearCart={clearCart}
              onCheckout={checkout}
            />
          </section>
        </>
      )}

      <PurchaseToast receipt={purchaseReceipt} onClose={dismissPurchaseReceipt} />
    </main>
  );
}
