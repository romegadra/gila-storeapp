import { ShoppingCart, Trash2, X } from 'lucide-react';
import { money } from '../utils/formatters.js';

export function CartPanel({ products, cart, cartTotal, busy, onRemoveItem, onClearCart, onCheckout }) {
  const selectedProducts = products.filter((product) => Number(cart[product.id] || 0) > 0);

  return (
    <aside className="panel cart-panel">
      <div className="panel-heading">
        <h2>Purchase</h2>
        <ShoppingCart size={18} />
      </div>
      <div className="cart-lines">
        {selectedProducts.map((product) => (
          <div className="cart-line" key={product.id}>
            <div>
              <span>{product.name}</span>
              <strong>{cart[product.id]} x ${money(product.price)}</strong>
            </div>
            <button className="icon-button subtle-danger" type="button" onClick={() => onRemoveItem(product.id)} title="Remove from cart">
              <X size={16} />
            </button>
          </div>
        ))}
        {!selectedProducts.length && <p className="muted">Select quantities from the product list.</p>}
      </div>
      <div className="cart-total">
        <span>Total</span>
        <strong>${money(cartTotal)}</strong>
      </div>
      <div className="cart-actions">
        <button className="ghost danger" type="button" onClick={onClearCart} disabled={busy || !selectedProducts.length}>
          <Trash2 size={18} />
          Clear Cart
        </button>
        <button className="primary" type="button" onClick={onCheckout} disabled={busy}>
          Complete payment
        </button>
      </div>
    </aside>
  );
}
