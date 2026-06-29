import { money } from '../utils/formatters.js';

export function PurchaseProductList({ products, cart, onQuantityChange }) {
  return (
    <section className="product-table purchase-list">
      <div className="table-header">
        <h2>Select Products</h2>
        <span>{products.length} available</span>
      </div>
      <div className="table-scroll">
        <table>
          <thead>
            <tr>
              <th>Name</th>
              <th>SKU</th>
              <th>Category</th>
              <th>Price</th>
              <th>Stock</th>
              <th>Quantity</th>
            </tr>
          </thead>
          <tbody>
            {products.map((product) => (
              <tr key={product.id}>
                <td><strong>{product.name}</strong><span>{product.description}</span></td>
                <td>{product.sku}</td>
                <td>{product.category}</td>
                <td>${money(product.price)}</td>
                <td>{product.stock}</td>
                <td>
                  <input
                    className="qty"
                    type="number"
                    min="0"
                    max={product.stock}
                    value={cart[product.id] || ''}
                    onChange={(event) => onQuantityChange(product.id, event.target.value)}
                  />
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  );
}
