import { PackageSearch, ShoppingCart } from 'lucide-react';

export function Tabs({ activeTab, isAdmin, onChange }) {
  return (
    <nav className="tabs" aria-label="Store workflows">
      {isAdmin && (
        <button className={activeTab === 'catalog' ? 'tab active' : 'tab'} type="button" onClick={() => onChange('catalog')}>
          <PackageSearch size={18} />
          Catalog
        </button>
      )}
      <button className={activeTab === 'purchase' ? 'tab active' : 'tab'} type="button" onClick={() => onChange('purchase')}>
        <ShoppingCart size={18} />
        Purchase
      </button>
    </nav>
  );
}
