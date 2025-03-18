package it.aredegalli.dominatus.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "audit_event_type")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditEventType {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(name = "description", nullable = false, length = 50)
    private String description;

}
