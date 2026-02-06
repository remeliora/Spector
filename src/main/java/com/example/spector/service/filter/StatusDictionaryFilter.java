package com.example.spector.service.filter;

import com.example.spector.domain.statusdictionary.StatusDictionary;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public record StatusDictionaryFilter(String nameStarts) {
    public Specification<StatusDictionary> toSpecification() {
        return nameStartsSpec();
    }

    private Specification<StatusDictionary> nameStartsSpec() {
        return ((root, query, cb) -> StringUtils.hasText(nameStarts)
                ? cb.like(cb.lower(root.get("name")), nameStarts.toLowerCase() + "%")
                : null);
    }
}