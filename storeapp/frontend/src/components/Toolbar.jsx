import { Search, Upload } from 'lucide-react';

export function Toolbar({ filters, categories, busy, onFilterChange, onSearch, onImport, showImport = true }) {
  return (
    <section className="toolbar">
      <form className="search-form" onSubmit={onSearch}>
        <Search size={18} />
        <input
          value={filters.query}
          onChange={(event) => onFilterChange({ query: event.target.value })}
          placeholder="Search by name, SKU, description"
        />
        <select value={filters.category} onChange={(event) => onFilterChange({ category: event.target.value })}>
          <option value="">All categories</option>
          {categories.map((item) => <option key={item} value={item}>{item}</option>)}
        </select>
        <button type="submit" disabled={busy}>Search</button>
      </form>
      {showImport && (
        <label className="upload-button">
          <Upload size={18} />
          <span>Import CSV</span>
          <input type="file" accept=".csv,text/csv" onChange={onImport} />
        </label>
      )}
    </section>
  );
}
