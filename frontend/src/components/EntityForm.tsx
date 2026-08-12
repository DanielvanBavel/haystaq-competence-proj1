import { FormEvent, useState } from 'react';
import { ApiError } from '../api';

export interface FieldOption {
  value: string;
  label: string;
}

export interface FieldDefinition {
  name: string;
  label: string;
  type?: 'text' | 'number' | 'date' | 'email' | 'tel' | 'select' | 'checkbox';
  options?: FieldOption[];
  step?: string;
  placeholder?: string;
  defaultValue?: string | number | boolean;
}

interface Props {
  title?: string;
  fields: FieldDefinition[];
  submitLabel: string;
  onSubmit: (values: Record<string, unknown>) => Promise<void>;
  testId?: string;
}

function initialValues(fields: FieldDefinition[]): Record<string, unknown> {
  const values: Record<string, unknown> = {};
  for (const field of fields) {
    if (field.defaultValue !== undefined) {
      values[field.name] = field.defaultValue;
    } else if (field.type === 'checkbox') {
      values[field.name] = false;
    } else if (field.type === 'select') {
      values[field.name] = field.options?.[0]?.value ?? '';
    } else {
      values[field.name] = '';
    }
  }
  return values;
}

export function EntityForm({ title, fields, submitLabel, onSubmit, testId }: Props) {
  const [values, setValues] = useState<Record<string, unknown>>(() => initialValues(fields));
  const [message, setMessage] = useState<{ text: string; kind: 'ok' | 'error' | 'busy' } | null>(null);

  function update(name: string, value: unknown) {
    setValues((current) => ({ ...current, [name]: value }));
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setMessage({ text: 'Bezig...', kind: 'busy' });
    const payload: Record<string, unknown> = {};
    for (const field of fields) {
      const raw = values[field.name];
      if (raw === '' || raw === undefined) {
        payload[field.name] = null;
      } else if (field.type === 'number') {
        payload[field.name] = Number(raw);
      } else {
        payload[field.name] = raw;
      }
    }
    try {
      await onSubmit(payload);
      setMessage({ text: 'Opgeslagen', kind: 'ok' });
    } catch (error) {
      const text = error instanceof ApiError ? error.describe() : String(error);
      setMessage({ text, kind: 'error' });
    }
  }

  return (
    <form className="entity-form" onSubmit={handleSubmit} data-testid={testId}>
      {title ? <h3>{title}</h3> : null}
      <div className="grid">
        {fields.map((field) => (
          <label key={field.name} className={field.type === 'checkbox' ? 'check' : undefined}>
            {field.type === 'checkbox' ? (
              <>
                <input
                  type="checkbox"
                  name={field.name}
                  checked={Boolean(values[field.name])}
                  onChange={(event) => update(field.name, event.target.checked)}
                />
                <span>{field.label}</span>
              </>
            ) : field.type === 'select' ? (
              <>
                <span>{field.label}</span>
                <select
                  name={field.name}
                  value={String(values[field.name] ?? '')}
                  onChange={(event) => update(field.name, event.target.value)}
                >
                  {(field.options ?? []).map((option) => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
                </select>
              </>
            ) : (
              <>
                <span>{field.label}</span>
                <input
                  type={field.type ?? 'text'}
                  name={field.name}
                  step={field.step}
                  placeholder={field.placeholder}
                  value={String(values[field.name] ?? '')}
                  onChange={(event) => update(field.name, event.target.value)}
                />
              </>
            )}
          </label>
        ))}
      </div>
      <div className="form-actions">
        <button type="submit">{submitLabel}</button>
        {message ? <span className={`msg ${message.kind}`}>{message.text}</span> : null}
      </div>
    </form>
  );
}
