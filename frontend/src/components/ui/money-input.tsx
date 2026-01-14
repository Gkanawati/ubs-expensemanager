import * as React from 'react';
import { Input } from './input';
import { cn } from '@/lib/utils';

const MAX_AMOUNT = 9999999999999.99;

// Parse a formatted string to a number
const parseMoneyValue = (value: string): number | null => {
  if (!value || value === '') return null;
  const cleanValue = value.replace(/,/g, '');
  const numberValue = parseFloat(cleanValue);
  if (isNaN(numberValue)) return null;
  return numberValue;
};

// Format a number with thousand separators and 2 decimal places
const formatMoneyValue = (amount: number | null): string => {
  if (amount === null || amount === 0) return '0.00';

  return new Intl.NumberFormat('en-US', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(amount);
};

// Convert cents (integer) to formatted currency string
const centsToFormatted = (cents: number): string => {
  const value = cents / 100;
  return formatMoneyValue(value);
};

export interface MoneyInputProps
  extends Omit<React.ComponentProps<'input'>, 'value' | 'onChange'> {
  value: number | null;
  currency: string;
  onChange: (value: number | null) => void;
  maxValue?: number;
}

const MoneyInput = React.forwardRef<HTMLInputElement, MoneyInputProps>(
  (
    {
      className,
      value,
      currency,
      onChange,
      disabled,
      maxValue = MAX_AMOUNT,
      ...props
    },
    ref
  ) => {
    const [displayValue, setDisplayValue] = React.useState('0.00');
    const inputRef = React.useRef<HTMLInputElement>(null);

    // Combine refs
    React.useImperativeHandle(ref, () => inputRef.current!);

    // Update display value when value changes from outside
    React.useEffect(() => {
      setDisplayValue(formatMoneyValue(value));
    }, [value]);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
      const rawInput = e.target.value;

      // Extract only digits from input
      const digits = rawInput.replace(/\D/g, '');

      // Convert to cents (integer)
      const cents = parseInt(digits, 10) || 0;

      // Convert cents to actual value
      const newValue = cents / 100;

      // Block values that exceed the maximum
      if (newValue > maxValue) {
        return;
      }

      // Format and update
      const formatted = centsToFormatted(cents);
      setDisplayValue(formatted);
      onChange(newValue === 0 ? null : newValue);
    };

    const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
      // Allow: backspace, delete, tab, escape, enter
      if (
        e.key === 'Backspace' ||
        e.key === 'Delete' ||
        e.key === 'Tab' ||
        e.key === 'Escape' ||
        e.key === 'Enter'
      ) {
        return;
      }

      // Allow: Ctrl+A, Ctrl+C, Ctrl+V, Ctrl+X
      if (e.ctrlKey || e.metaKey) {
        return;
      }

      // Allow: arrow keys
      if (e.key.startsWith('Arrow')) {
        return;
      }

      // Block non-numeric keys
      if (!/^\d$/.test(e.key)) {
        e.preventDefault();
      }
    };

    // Always move cursor to end
    const moveCursorToEnd = () => {
      requestAnimationFrame(() => {
        if (inputRef.current) {
          const len = inputRef.current.value.length;
          inputRef.current.setSelectionRange(len, len);
        }
      });
    };

    const handleFocus = () => {
      moveCursorToEnd();
    };

    const handleClick = () => {
      moveCursorToEnd();
    };

    const handleSelect = () => {
      moveCursorToEnd();
    };

    return (
      <div className='relative flex w-full'>
        <div
          className={cn(
            'flex h-9 items-center rounded-l-md border border-r-0 border-input bg-transparent px-3 text-sm shadow-xs',
            disabled && 'opacity-50 cursor-not-allowed bg-muted'
          )}
        >
          <span className='font-medium text-muted-foreground whitespace-nowrap'>
            {currency || '---'}
          </span>
        </div>
        <Input
          ref={inputRef}
          type='text'
          inputMode='numeric'
          placeholder='0.00'
          value={displayValue}
          onChange={handleChange}
          onKeyDown={handleKeyDown}
          onFocus={handleFocus}
          onClick={handleClick}
          onSelect={handleSelect}
          disabled={disabled}
          className={cn(
            'rounded-l-none focus-visible:ring-offset-0',
            className
          )}
          {...props}
        />
      </div>
    );
  }
);

MoneyInput.displayName = 'MoneyInput';

export { MoneyInput, parseMoneyValue, formatMoneyValue, MAX_AMOUNT };
