import { Edit3, Trash2 } from 'lucide-react';
import { money } from '../utils/formatters.js';

export function ProductTable({ products, onEdit, onDelete }) {
  return (
    <section className="product-table">
      <div className="table-header">
        <h2>Products</h2>
        <span>{products.length} results</span>
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
              <th>Weight</th>
              <th>Actions</th>
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
                <td>{product.weightKg} kg</td>
                <td>
                  <div className="actions">
                    <button className="icon-button" type="button" onClick={() => onEdit(product)} title="Edit product"><Edit3 size={16} /></button>
                    <button className="icon-button danger" type="button" onClick={() => onDelete(product)} title="Delete product"><Trash2 size={16} /></button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  );
}
