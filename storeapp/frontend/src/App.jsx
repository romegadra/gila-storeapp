import { useState } from 'react';
import { CartPanel } from './components/CartPanel.jsx';
import { Header } from './components/Header.jsx';
import { ImportIssues } from './components/ImportIssues.jsx';
import { PurchaseProductList } from './components/PurchaseProductList.jsx';
import { ProductForm } from './components/ProductForm.jsx';
import { ProductTable } from './components/ProductTable.jsx';
import { Tabs } from './components/Tabs.jsx';
import { Toolbar } from './components/Toolbar.jsx';
import { useProducts } from './hooks/useProducts.js';

export function App() {
  const [activeTab, setActiveTab] = useState('catalog');
  const [currentUser, setCurrentUser] = useState({
    name: 'Avery Stone',
    role: 'ADMIN'
  });
  const {
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
  } = useProducts();
  const isAdmin = currentUser.role === 'ADMIN';

  function switchRole() {
    setCurrentUser((user) => {
      const nextRole = user.role === 'ADMIN' ? 'USER' : 'ADMIN';
      if (nextRole === 'USER') {
        setActiveTab('purchase');
      }
      return { ...user, role: nextRole };
    });
  }

  return (
    <main className="app-shell">
      <Header
        user={currentUser}
        onSwitchRole={switchRole}
      />

      <Tabs activeTab={activeTab} isAdmin={isAdmin} onChange={setActiveTab} />

      {notice && <div className={`notice ${notice.type}`}>{notice.message}</div>}

      {activeTab === 'catalog' && isAdmin && (
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

      {activeTab === 'purchase' && (
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
    </main>
  );
}
