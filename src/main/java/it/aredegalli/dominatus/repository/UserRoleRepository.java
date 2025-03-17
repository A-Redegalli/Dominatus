package it.aredegalli.dominatus.repository;

import it.aredegalli.dominatus.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {
    List<UserRole> findByUserId(UUID userId);

    List<UserRole> findByUserIdAndApplicationId(UUID userId, UUID applicationId);

    boolean existsByUserIdAndRoleIdAndApplicationId(UUID userId, UUID roleId, UUID applicationId);
}
