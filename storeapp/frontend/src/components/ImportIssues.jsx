export function ImportIssues({ report }) {
  if (!report?.errors?.length) {
    return null;
  }

  return (
    <section className="import-errors">
      <h2>Import Issues</h2>
      {report.errors.slice(0, 8).map((error) => (
        <p key={`${error.row}-${error.sku}-${error.message}`}>Row {error.row}: {error.sku || 'unknown SKU'} - {error.message}</p>
      ))}
    </section>
  );
}
