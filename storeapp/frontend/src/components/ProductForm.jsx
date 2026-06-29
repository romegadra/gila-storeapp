import { Check, Plus } from 'lucide-react';

export function ProductForm({ form, editingId, busy, onChange, onSubmit, onCancel }) {
  return (
    <form className="panel product-form" onSubmit={onSubmit}>
      <div className="panel-heading">
        <h2>{editingId ? 'Edit Product' : 'New Product'}</h2>
        {editingId && <button type="button" className="ghost" onClick={onCancel}>Cancel</button>}
      </div>
      <div className="form-grid">
        <label>Name<input required value={form.name} onChange={(event) => onChange({ name: event.target.value })} /></label>
        <label>SKU<input required value={form.sku} onChange={(event) => onChange({ sku: event.target.value })} /></label>
        <label>Category<input required value={form.category} onChange={(event) => onChange({ category: event.target.value })} /></label>
        <label>Price<input required type="number" min="0" step="0.01" value={form.price} onChange={(event) => onChange({ price: event.target.value })} /></label>
        <label>Stock<input required type="number" min="0" step="1" value={form.stock} onChange={(event) => onChange({ stock: event.target.value })} /></label>
        <label>Weight kg<input required type="number" min="0" step="0.01" value={form.weightKg} onChange={(event) => onChange({ weightKg: event.target.value })} /></label>
        <label className="wide">Description<textarea value={form.description} onChange={(event) => onChange({ description: event.target.value })} /></label>
      </div>
      <button className="primary" type="submit" disabled={busy}>
        {editingId ? <Check size={18} /> : <Plus size={18} />}
        {editingId ? 'Save Changes' : 'Create Product'}
      </button>
    </form>
  );
}
