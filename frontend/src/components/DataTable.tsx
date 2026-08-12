import { ReactNode } from 'react';

export interface Column<T> {
  label: string;
  render: (row: T) => ReactNode;
}

interface Props<T> {
  rows: T[];
  columns: Column<T>[];
  empty?: string;
  testId?: string;
}

export function DataTable<T>({ rows, columns, empty = 'Geen gegevens', testId }: Props<T>) {
  if (rows.length === 0) {
    return <p className="empty">{empty}</p>;
  }
  return (
    <table data-testid={testId}>
      <thead>
      <tr>
        {columns.map((column) => (
          <th key={column.label}>{column.label}</th>
        ))}
      </tr>
      </thead>
      <tbody>
      {rows.map((row, index) => (
        <tr key={index}>
          {columns.map((column) => (
            <td key={column.label}>{column.render(row)}</td>
          ))}
        </tr>
      ))}
      </tbody>
    </table>
  );
}
