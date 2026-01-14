package com.ubs.expensemanager.dto.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString // good for logging the request at the controller
public class ExpenseCategoryFilterRequest {
    // search by category name
    private String search;
}
