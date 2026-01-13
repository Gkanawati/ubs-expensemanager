package com.ubs.expensemanager.model.audit;

import com.ubs.expensemanager.model.User;
import org.hibernate.envers.RevisionListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Custom revision listener that captures the authenticated user
 * when an audited entity is modified.
 */
public class CustomRevisionListener implements RevisionListener {

    @Override
    public void newRevision(Object revisionEntity) {
        CustomRevisionEntity customRevisionEntity = (CustomRevisionEntity) revisionEntity;
        
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null && authentication.isAuthenticated() 
                    && authentication.getPrincipal() instanceof User) {
                User user = (User) authentication.getPrincipal();
                customRevisionEntity.setModifiedBy(user.getEmail());
            }
        } catch (Exception e) {
            // If we can't get the user, just leave it null
            // This can happen during automated processes or migrations
        }
    }
}
