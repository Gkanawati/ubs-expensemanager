package com.ubs.expensemanager.repository.specification;

import com.ubs.expensemanager.model.ExpenseCategory;
import org.springframework.data.jpa.domain.Specification;

/**
 * JPA Specifications for filtering ExpenseCategory entities.
 *
 * <p> This class provides reusable specifications that can be combined to build dynamic queries
 * for the ExpenseCategory entity using Spring Data JPA Criteria API. </p>
 */
public class ExpenseCategorySpecifications {
    /**
     * Creates a specification to filter expense categories by name.
     *
     * @param search string to filter category name by, or {@code null} to not apply this filter
     * @return a specification that matches expense categories with the search term in their name
     */
    public static Specification<ExpenseCategory> nameContains(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) {
                return null;
            }

            String pattern = "%" + search.toLowerCase() + "%";

            return cb.like(cb.lower(root.get("name")), pattern);
        };
    }
}
