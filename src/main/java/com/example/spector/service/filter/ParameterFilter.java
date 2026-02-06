package com.example.spector.service.filter;

import com.example.spector.domain.parameter.Parameter;
import org.springframework.data.jpa.domain.Specification;

public record ParameterFilter(Long deviceTypeIdLte) {
    public Specification<Parameter> toSpecification() {
        return deviceTypeIdLteSpec();
    }

    private Specification<Parameter> deviceTypeIdLteSpec() {
        return ((root, query, cb) -> deviceTypeIdLte != null
                ? cb.lessThanOrEqualTo(root.get("deviceType").get("id"), deviceTypeIdLte)
                : null);
    }
}