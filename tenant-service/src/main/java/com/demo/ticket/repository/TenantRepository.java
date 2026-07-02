package com.demo.ticket.repository;

import com.demo.ticket.model.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TenantRepository extends JpaRepository<Tenant, String> {

    List<Tenant> findAllByOrderByTenantIdAsc();
}
