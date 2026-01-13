package com.ubs.expensemanager.util;

import com.ubs.expensemanager.model.Expense;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utility class for currency conversion operations.
 * 
 * <p>Provides reusable methods for converting amounts between currencies
 * using exchange rates.</p>
 */
public class CurrencyConverter {
    /**
     * Converts expense amount to USD using the currency exchange rate.
     * 
     * <p>Formula: USD Amount = Original Amount / Exchange Rate</p>
     * <p>Example: BRL 500 with exchange rate 5.0 = USD 100</p>
     * 
     * @param expense the expense to convert
     * @return amount in USD
     */
    public static BigDecimal convertToUsd(Expense expense) {
        BigDecimal amount = expense.getAmount();
        BigDecimal exchangeRate = expense.getCurrency().getExchangeRate();
        
        // If already in USD (exchange rate = 1), return as is
        if (exchangeRate.compareTo(BigDecimal.ONE) == 0) {
            return amount;
        }
        
        // Convert to USD by dividing by exchange rate
        return amount.divide(exchangeRate, 2, RoundingMode.HALF_UP);
    }
}
