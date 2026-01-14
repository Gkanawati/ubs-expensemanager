package com.ubs.expensemanager.model;

/**
 * Defines the type of an alert, indicating its scope.
 */
public enum AlertType {
    /**
     * Alert is related to a specific expense category.
     */
    CATEGORY,

    /**
     * Alert is related to a specific department.
     */
    DEPARTMENT,

    /**
     * Alert is applicable to the entire system.
     */
    ALL
}