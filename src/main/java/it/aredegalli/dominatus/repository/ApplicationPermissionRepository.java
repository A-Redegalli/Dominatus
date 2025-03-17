package it.aredegalli.dominatus.repository;

import it.aredegalli.dominatus.model.ApplicationPermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ApplicationPermissionRepository extends JpaRepository<ApplicationPermission, UUID> {
    List<ApplicationPermission> findByApplicationId(UUID applicationId);
}
