import { useState, useEffect } from "react";
import { AlertCircle } from "lucide-react";
import { Input } from "@/components/ui/input";
import { MoneyInput, MAX_AMOUNT } from "@/components/ui/money-input";
import { DatePicker } from "@/components/ui/date-picker";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import {
  ExpenseCategory,
  Currency,
  CreateExpensePayload,
  getExpenseCategories,
  getCurrencies,
} from "@/api/expense.api";

interface CreateExpenseDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (data: CreateExpensePayload) => Promise<void>;
  error?: string;
}

interface FormErrors {
  amount?: string;
  description?: string;
  expenseDate?: string;
  expenseCategoryId?: string;
  currencyName?: string;
  receiptUrl?: string;
}

export const CreateExpenseDialog = ({
  open,
  onOpenChange,
  onSubmit,
  error,
}: CreateExpenseDialogProps) => {
  const [formData, setFormData] = useState<CreateExpensePayload>({
    amount: 0,
    description: "",
    expenseDate: new Date().toISOString().split("T")[0],
    expenseCategoryId: 0,
    currencyName: "",
    receiptUrl: "",
  });

  const [errors, setErrors] = useState<FormErrors>({});
  const [categories, setCategories] = useState<ExpenseCategory[]>([]);
  const [currencies, setCurrencies] = useState<Currency[]>([]);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    const loadData = async () => {
      try {
        const [categoriesData, currenciesData] = await Promise.all([
          getExpenseCategories(),
          getCurrencies(),
        ]);
        setCategories(categoriesData);
        setCurrencies(currenciesData);
      } catch (err) {
        console.error("Error loading form data:", err);
      }
    };
    loadData();
  }, []);

  useEffect(() => {
    if (!open) {
      handleReset();
    }
  }, [open]);

  const handleInputChange = (
    field: keyof CreateExpensePayload,
    value: string | number
  ) => {
    setFormData({ ...formData, [field]: value });

    const newErrors = { ...errors };

    if (field === "amount") {
      const numValue = typeof value === "string" ? parseFloat(value) : value;
      if (!numValue || numValue <= 0) {
        newErrors.amount = "Amount must be greater than 0";
      } else if (numValue > MAX_AMOUNT) {
        newErrors.amount = "Amount exceeds maximum allowed value";
      } else {
        delete newErrors.amount;
      }
    }

    if (field === "description") {
      const strValue = String(value);
      if (strValue.length > 500) {
        newErrors.description = "Description must not exceed 500 characters";
      } else {
        delete newErrors.description;
      }
    }

    if (field === "expenseDate") {
      const strValue = String(value);
      if (!strValue) {
        newErrors.expenseDate = "Date is required";
      } else {
        const selectedDate = new Date(strValue);
        const today = new Date();
        today.setHours(23, 59, 59, 999);
        if (selectedDate > today) {
          newErrors.expenseDate = "Date cannot be in the future";
        } else {
          delete newErrors.expenseDate;
        }
      }
    }

    if (field === "expenseCategoryId") {
      if (!value) {
        newErrors.expenseCategoryId = "Category is required";
      } else {
        delete newErrors.expenseCategoryId;
      }
    }

    if (field === "currencyName") {
      if (!value) {
        newErrors.currencyName = "Currency is required";
      } else {
        delete newErrors.currencyName;
      }
    }

    if (field === "receiptUrl") {
      const strValue = String(value);
      if (strValue.length > 1000) {
        newErrors.receiptUrl = "Receipt URL must not exceed 1000 characters";
      } else {
        delete newErrors.receiptUrl;
      }
    }

    setErrors(newErrors);
  };

  const validateForm = (): boolean => {
    const newErrors: FormErrors = {};

    if (!formData.amount || formData.amount <= 0) {
      newErrors.amount = "Amount must be greater than 0";
    } else if (formData.amount > MAX_AMOUNT) {
      newErrors.amount = "Amount exceeds maximum allowed value";
    }

    if (!formData.expenseDate) {
      newErrors.expenseDate = "Date is required";
    } else {
      const selectedDate = new Date(formData.expenseDate);
      const today = new Date();
      today.setHours(23, 59, 59, 999);
      if (selectedDate > today) {
        newErrors.expenseDate = "Date cannot be in the future";
      }
    }

    if (!formData.expenseCategoryId) {
      newErrors.expenseCategoryId = "Category is required";
    }

    if (!formData.currencyName) {
      newErrors.currencyName = "Currency is required";
    }

    if (formData.description && formData.description.length > 500) {
      newErrors.description = "Description must not exceed 500 characters";
    }

    if (formData.receiptUrl && formData.receiptUrl.length > 1000) {
      newErrors.receiptUrl = "Receipt URL must not exceed 1000 characters";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const isFormValid = (): boolean => {
    return (
      formData.amount > 0 &&
      formData.amount <= MAX_AMOUNT &&
      !!formData.expenseDate &&
      !!formData.expenseCategoryId &&
      !!formData.currencyName &&
      Object.keys(errors).length === 0
    );
  };

  const handleReset = () => {
    setFormData({
      amount: 0,
      description: "",
      expenseDate: new Date().toISOString().split("T")[0],
      expenseCategoryId: 0,
      currencyName: "",
      receiptUrl: "",
    });
    setErrors({});
    setIsLoading(false);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!validateForm()) {
      return;
    }

    setIsLoading(true);

    const payload: CreateExpensePayload = {
      amount: formData.amount,
      expenseDate: formData.expenseDate,
      expenseCategoryId: formData.expenseCategoryId,
      currencyName: formData.currencyName,
    };

    if (formData.description?.trim()) {
      payload.description = formData.description.trim();
    }

    if (formData.receiptUrl?.trim()) {
      payload.receiptUrl = formData.receiptUrl.trim();
    }

    try {
      await onSubmit(payload);
    } finally {
      setIsLoading(false);
    }
  };

  const handleOpenChange = (newOpen: boolean) => {
    if (!newOpen) {
      handleReset();
    }
    onOpenChange(newOpen);
  };

  const selectClassName = (hasError: boolean) =>
    `flex h-9 w-full rounded-md border bg-transparent px-3 py-1 text-sm shadow-xs transition-[color,box-shadow] outline-none focus:outline-none focus:border-ring focus:ring-ring/50 focus:ring-[3px] disabled:cursor-not-allowed disabled:opacity-50 dark:bg-input/30 dark:focus:ring-ring/50 ${
      hasError ? "border-destructive" : "border-input"
    }`;

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      <DialogContent
        className="sm:max-w-[500px]"
        onInteractOutside={(e) => e.preventDefault()}
        onEscapeKeyDown={(e) => e.preventDefault()}
      >
        <DialogHeader>
          <DialogTitle>Expense Submission</DialogTitle>
          <DialogDescription>
            Fill in the expense details below
          </DialogDescription>
        </DialogHeader>

        {error && (
          <div className="flex items-start gap-3 rounded-lg bg-red-50 p-4 dark:bg-red-950/50">
            <AlertCircle className="h-5 w-5 text-red-600 dark:text-red-400 flex-shrink-0 mt-0.5" />
            <div className="flex-1">
              <h3 className="text-sm font-medium text-red-800 dark:text-red-300">
                Error creating expense
              </h3>
              <p className="mt-1 text-sm text-red-700 dark:text-red-400">
                {error}
              </p>
            </div>
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
                    <div className="space-y-2">
            <Label htmlFor="expenseCategoryId" className="text-sm font-medium">
              Category <span className="text-red-600">*</span>
            </Label>
            <select
              id="expenseCategoryId"
              value={formData.expenseCategoryId || ""}
              onChange={(e) =>
                handleInputChange(
                  "expenseCategoryId",
                  parseInt(e.target.value) || 0
                )
              }
              className={selectClassName(!!errors.expenseCategoryId)}
            >
              <option value="">Select a category</option>
              {categories.map((category) => (
                <option key={category.id} value={category.id}>
                  {category.name}
                </option>
              ))}
            </select>
            {errors.expenseCategoryId && (
              <p className="text-sm text-red-600">{errors.expenseCategoryId}</p>
            )}
          </div>

          <div className="grid grid-cols-2 gap-4">
                        <div className="space-y-2">
              <Label htmlFor="currencyName" className="text-sm font-medium">
                Currency <span className="text-red-600">*</span>
              </Label>
              <select
                id="currencyName"
                value={formData.currencyName}
                onChange={(e) =>
                  handleInputChange("currencyName", e.target.value)
                }
                className={selectClassName(!!errors.currencyName)}
              >
                <option value="">Select currency</option>
                {currencies.map((currency) => (
                  <option key={currency.id} value={currency.name}>
                    {currency.name}
                  </option>
                ))}
              </select>
              {errors.currencyName && (
                <p className="text-sm text-red-600">{errors.currencyName}</p>
              )}
            </div>

                        <div className="space-y-2">
              <Label htmlFor="amount" className="text-sm font-medium">
                Amount <span className="text-red-600">*</span>
              </Label>
              <MoneyInput
                id="amount"
                placeholder="0.00"
                value={formData.amount || null}
                currency={formData.currencyName}
                onChange={(value) =>
                  handleInputChange("amount", value ?? 0)
                }
                disabled={!formData.currencyName}
              />
              {errors.amount && (
                <p className="text-sm text-red-600">{errors.amount}</p>
              )}
            </div>
          </div>

                    <div className="space-y-2">
            <Label htmlFor="expenseDate" className="text-sm font-medium">
              Date <span className="text-red-600">*</span>
            </Label>
            <DatePicker
              value={formData.expenseDate ? new Date(formData.expenseDate + "T00:00:00") : undefined}
              onChange={(date) =>
                handleInputChange("expenseDate", date ? date.toISOString().split("T")[0] : "")
              }
              maxDate={new Date()}
              placeholder="Select date"
            />
            {errors.expenseDate && (
              <p className="text-sm text-red-600">{errors.expenseDate}</p>
            )}
          </div>

                    <div className="space-y-2">
            <Label htmlFor="description" className="text-sm font-medium">
              Description
            </Label>
            <textarea
              id="description"
              placeholder="Enter a description"
              value={formData.description || ""}
              onChange={(e) =>
                handleInputChange("description", e.target.value)
              }
              rows={3}
              className={`flex w-full rounded-md border bg-transparent px-3 py-2 text-sm shadow-xs transition-[color,box-shadow] outline-none focus:outline-none focus:border-ring focus:ring-ring/50 focus:ring-[3px] disabled:cursor-not-allowed disabled:opacity-50 dark:bg-input/30 dark:focus:ring-ring/50 resize-none placeholder:text-muted-foreground ${
                errors.description ? "border-destructive" : "border-input"
              }`}
            />
            <div className="flex justify-between">
              {errors.description && (
                <p className="text-sm text-red-600">{errors.description}</p>
              )}
              <p className="text-xs text-gray-500 ml-auto">
                {formData.description?.length || 0}/500
              </p>
            </div>
          </div>

                    <div className="space-y-2">
            <Label htmlFor="receiptUrl" className="text-sm font-medium">
              Receipt URL
            </Label>
            <Input
              id="receiptUrl"
              type="url"
              placeholder="https://example.com/receipt.pdf"
              value={formData.receiptUrl || ""}
              onChange={(e) =>
                handleInputChange("receiptUrl", e.target.value)
              }
              className={errors.receiptUrl ? "border-red-500" : ""}
            />
            {errors.receiptUrl && (
              <p className="text-sm text-red-600">{errors.receiptUrl}</p>
            )}
          </div>

          <DialogFooter className="gap-2 pt-4">
            <Button
              type="button"
              variant="outline"
              size="lg"
              onClick={() => handleOpenChange(false)}
              disabled={isLoading}
            >
              Cancel
            </Button>
            <Button
              type="submit"
              variant="default"
              size="lg"
              disabled={!isFormValid() || isLoading}
            >
              {isLoading ? "Creating..." : "Submit"}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
};
