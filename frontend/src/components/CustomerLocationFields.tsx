import { Form, Select, Spin } from 'antd'
import type { FormInstance } from 'antd/es/form'
import { useEffect, useMemo, useState } from 'react'

type Csc = typeof import('country-state-city')

type Props = {
  form: FormInstance
  /** When true, country/state/city are not required (e.g. editing legacy rows). */
  locationOptional?: boolean
}

export function CustomerLocationFields({ form, locationOptional = false }: Props) {
  const [csc, setCsc] = useState<Csc | null>(null)

  useEffect(() => {
    void import('country-state-city').then(setCsc)
  }, [])

  const countryCode = Form.useWatch('countryCode', form)
  const stateCode = Form.useWatch('stateCode', form)

  const countryOptions = useMemo(() => {
    if (!csc) return []
    return csc.Country.getAllCountries()
      .map((c) => ({ value: c.isoCode, label: c.name }))
      .sort((a, b) => a.label.localeCompare(b.label))
  }, [csc])

  const stateOptions = useMemo(() => {
    if (!csc || !countryCode) return []
    return csc.State.getStatesOfCountry(countryCode)
      .map((s) => ({ value: s.isoCode, label: s.name }))
      .sort((a, b) => a.label.localeCompare(b.label))
  }, [csc, countryCode])

  const cityOptions = useMemo(() => {
    if (!csc || !countryCode || !stateCode) return []
    return csc.City.getCitiesOfState(countryCode, stateCode)
      .map((c) => ({ value: c.name, label: c.name }))
      .sort((a, b) => a.label.localeCompare(b.label))
  }, [csc, countryCode, stateCode])

  const req = locationOptional ? [] : [{ required: true as const, message: 'Required' }]

  if (!csc) {
    return (
      <div style={{ textAlign: 'center', padding: 24 }}>
        <Spin tip="Loading locations…" />
      </div>
    )
  }

  return (
    <>
      <Form.Item name="countryCode" label="Country" rules={req}>
        <Select
          allowClear={locationOptional}
          showSearch
          optionFilterProp="label"
          placeholder="Select country"
          options={countryOptions}
          onChange={() => form.setFieldsValue({ stateCode: undefined, city: undefined })}
        />
      </Form.Item>
      <Form.Item name="stateCode" label="State / province" rules={req}>
        <Select
          allowClear={locationOptional}
          showSearch
          optionFilterProp="label"
          placeholder={countryCode ? 'Select state / province' : 'Select country first'}
          options={stateOptions}
          disabled={!countryCode}
          onChange={() => form.setFieldsValue({ city: undefined })}
        />
      </Form.Item>
      <Form.Item name="city" label="City" rules={req}>
        <Select
          allowClear={locationOptional}
          showSearch
          optionFilterProp="label"
          placeholder={stateCode ? 'Select city' : 'Select state first'}
          options={cityOptions}
          disabled={!stateCode}
        />
      </Form.Item>
    </>
  )
}
