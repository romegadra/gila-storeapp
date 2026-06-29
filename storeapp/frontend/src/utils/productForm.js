export const emptyProductForm = {
  name: '',
  sku: '',
  description: '',
  category: '',
  price: '',
  stock: '',
  weightKg: ''
};

export function productToForm(product) {
  return {
    name: product.name,
    sku: product.sku,
    description: product.description || '',
    category: product.category,
    price: product.price,
    stock: product.stock,
    weightKg: product.weightKg
  };
}

export function formToProduct(form) {
  return {
    ...form,
    price: Number(form.price),
    stock: Number(form.stock),
    weightKg: Number(form.weightKg)
  };
}
