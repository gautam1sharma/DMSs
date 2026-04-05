export type IsoLocationFields = {
  countryCode?: string
  stateCode?: string
  city?: string
}

/** Compact location line without pulling in the full country-state-city dataset. */
export function formatIsoLocation(c: IsoLocationFields): string {
  if (!c.countryCode && !c.stateCode && !c.city) return '—'
  return [c.countryCode, c.stateCode, c.city].filter(Boolean).join(' · ')
}

/** Alias for customer tables; same as {@link formatIsoLocation}. */
export const formatCustomerLocation = formatIsoLocation
