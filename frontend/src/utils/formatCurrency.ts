/** Indian Rupee (₹) — amounts stored as plain numbers in the API. */
export function formatRupee(amount: number | string | undefined | null): string {
  const n = Number(amount)
  if (!Number.isFinite(n)) {
    return '₹0.00'
  }
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(n)
}
