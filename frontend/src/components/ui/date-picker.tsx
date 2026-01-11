import * as React from "react"
import { format } from "date-fns"
import { Calendar as CalendarIcon } from "lucide-react"

import { cn } from "@/lib/utils"
import { Calendar } from "@/components/ui/calendar"
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover"

export interface DatePickerProps {
  value?: Date
  onChange?: (date: Date | undefined) => void
  placeholder?: string
  disabled?: boolean
  maxDate?: Date
  minDate?: Date
  className?: string
}

export function DatePicker({
  value,
  onChange,
  placeholder = "Select date",
  disabled = false,
  maxDate,
  minDate,
  className,
}: DatePickerProps) {
  const [open, setOpen] = React.useState(false)

  const handleSelect = (date: Date | undefined) => {
    onChange?.(date)
    setOpen(false)
  }

  return (
    <Popover open={open} onOpenChange={setOpen} modal={true}>
      <PopoverTrigger asChild>
        <button
          type="button"
          disabled={disabled}
          className={cn(
            "flex h-9 w-full items-center rounded-md border border-input bg-transparent px-3 py-1 text-sm shadow-xs",
            "transition-[color,box-shadow] outline-none",
            "focus:outline-none focus:border-ring focus:ring-ring/50 focus:ring-[3px]",
            "disabled:cursor-not-allowed disabled:opacity-50",
            "dark:bg-input/30",
            !value && "text-muted-foreground",
            className
          )}
        >
          <span className="flex-1 text-left">
            {value ? format(value, "MMM dd, yyyy") : placeholder}
          </span>
          <CalendarIcon className="ml-2 h-4 w-4 shrink-0" />
        </button>
      </PopoverTrigger>
      <PopoverContent className="w-auto p-0" align="end">
        <Calendar
          mode="single"
          selected={value}
          onSelect={handleSelect}
          disabled={(date) => {
            if (maxDate && date > maxDate) return true
            if (minDate && date < minDate) return true
            return false
          }}
          initialFocus
        />
      </PopoverContent>
    </Popover>
  )
}
