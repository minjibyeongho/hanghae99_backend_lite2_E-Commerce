package kr.hhplus.be.server.layered.product.model;

import jakarta.persistence.*;
import kr.hhplus.be.server.common.status.InventoryHistoryStatus;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InventoryHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long inventoryHisId;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private InventoryHistoryStatus status;

    @Column(nullable = false)
    private Integer changedQuantity;

    @Column(nullable = false)
    private Integer beforeQuantity;

    @Column(nullable = false)
    private Integer afterQuantity;

    private String memo;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private String createdBy;

    @Builder
    public InventoryHistory(Long productId, InventoryHistoryStatus status, Integer changedQuantity,
                            Integer beforeQuantity, Integer afterQuantity, String memo, String createdBy) {
        this.productId = productId;
        this.status = status;
        this.changedQuantity = changedQuantity;
        this.beforeQuantity = beforeQuantity;
        this.afterQuantity = afterQuantity;
        this.memo = memo;
        this.createdBy = createdBy;
    }
}
