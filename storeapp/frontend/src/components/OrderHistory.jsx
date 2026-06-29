import { money } from '../utils/formatters.js';

export function OrderHistory({ orders, loading, onClose }) {
  return (
    <section className="history-panel">
      <div className="history-header">
        <div>
          <p className="eyebrow">Purchase history</p>
          <h2>Orders</h2>
        </div>
        <button className="ghost" type="button" onClick={onClose}>Close</button>
      </div>

      {loading && <p className="muted">Loading orders...</p>}
      {!loading && !orders.length && <p className="muted">No purchases yet.</p>}

      <div className="order-list">
        {orders.map((order) => (
          <article className="order-card" key={order.id}>
            <div className="order-summary">
              <div>
                <strong>Order #{order.id}</strong>
                <span>{new Date(order.purchasedAt).toLocaleString()}</span>
              </div>
              <div>
                <span>{order.status}</span>
                <strong>${money(order.total)}</strong>
              </div>
            </div>
            <div className="order-items">
              {order.items.map((item) => (
                <div className="order-item" key={`${order.id}-${item.productId}-${item.sku}`}>
                  <span>{item.productName}</span>
                  <span>{item.quantity} x ${money(item.unitPrice)}</span>
                  <strong>${money(item.lineTotal)}</strong>
                </div>
              ))}
            </div>
          </article>
        ))}
      </div>
    </section>
  );
}
