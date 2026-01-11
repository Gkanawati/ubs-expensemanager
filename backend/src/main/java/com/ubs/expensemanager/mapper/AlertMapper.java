package com.ubs.expensemanager.mapper;

import com.ubs.expensemanager.dto.request.AlertCreateRequest;
import com.ubs.expensemanager.dto.request.AlertUpdateRequest;
import com.ubs.expensemanager.dto.response.AlertResponse;
import com.ubs.expensemanager.model.Alert;
import com.ubs.expensemanager.model.Expense;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AlertMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "NEW")
    @Mapping(target = "expense", source = "expense")
    Alert toEntity(AlertCreateRequest alertCreateRequest, Expense expense);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "expense", source = "expense")
    @Mapping(target = "status", source = "alertUpdateRequest.status")
    @Mapping(target = "type", ignore = true)      // IMPORTANTE: ignore = true para preservar!
    @Mapping(target = "message", ignore = true)   // IMPORTANTE: ignore = true para preservar!
    @Mapping(target = "createdAt", ignore = true) // Adicione também
    Alert updateEntity(@MappingTarget Alert alert, AlertUpdateRequest alertUpdateRequest, Expense expense);

    @Mapping(target = "expenseId", source = "expense.id")
    @Mapping(target = "expenseDescription", source = "expense.description")
    AlertResponse toResponse(Alert alert);
}
