package com.roomate.app.entities.grocery;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.roomate.app.entities.room.RoomEntity;
import com.roomate.app.entities.room.RoomMemberEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "grocery_list", indexes = {
        @Index(name = "idx_grocery_list_room_id", columnList = "room_id")
})
public class GroceryListEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    @JsonIgnore
    private RoomEntity room;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroceryListStatus status = GroceryListStatus.ACTIVE;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime completedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_member_id")
    private RoomMemberEntity createdBy;

    @OneToMany(mappedBy = "groceryList", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<GroceryItemEntity> items = new ArrayList<>();

    public GroceryListEntity(RoomEntity room, String name, RoomMemberEntity createdBy) {
        this.room = room;
        this.name = name;
        this.createdBy = createdBy;
        this.status = GroceryListStatus.ACTIVE;
    }

    public long getPurchasedCount() {
        return items.stream().filter(GroceryItemEntity::isPurchased).count();
    }

    public long getRemainingCount() {
        return items.stream().filter(item -> !item.isPurchased()).count();
    }

    public BigDecimal getTotalSpent() {
        return items.stream()
                .filter(GroceryItemEntity::isPurchased)
                .map(item -> item.getActualPrice() != null ? item.getActualPrice() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void complete() {
        this.status = GroceryListStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void archive() {
        this.status = GroceryListStatus.ARCHIVED;
    }
}
