package com.demo.ticket.service;

import com.demo.ticket.dto.TenantResponse;
import com.demo.ticket.model.Tenant;
import com.demo.ticket.repository.TenantRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TenantService {

    private final TenantRepository tenantRepository;

    public TenantService(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    public List<TenantResponse> listTenants() {
        return tenantRepository.findAllByOrderByTenantIdAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    public TenantResponse getTenant(String tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "未知租户: " + tenantId));
        return toResponse(tenant);
    }

    private TenantResponse toResponse(Tenant tenant) {
        return new TenantResponse(
                tenant.getTenantId(),
                tenant.getName(),
                tenant.getTier(),
                tenant.getIsolationMode(),
                tenant.getMaxQps(),
                tenant.getEnabledPlugins());
    }
}
