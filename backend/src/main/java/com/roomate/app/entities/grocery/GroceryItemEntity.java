package com.roomate.app.entities.grocery;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.roomate.app.entities.room.RoomMemberEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "grocery_item", indexes = {
        @Index(name = "idx_grocery_item_list_id", columnList = "grocery_list_id")
})
public class GroceryItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grocery_list_id", nullable = false)
    @JsonIgnore
    private GroceryListEntity groceryList;

    @Column(nullable = false)
    private String name;

    private String quantity;

    private String category;

    private String notes;

    @Column(nullable = false)
    private boolean isPurchased = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "added_by_member_id")
    private RoomMemberEntity addedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchased_by_member_id")
    private RoomMemberEntity purchasedBy;

    @Column(precision = 10, scale = 2)
    private BigDecimal estimatedPrice;

    @Column(precision = 10, scale = 2)
    private BigDecimal actualPrice;

    private LocalDateTime purchasedAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public GroceryItemEntity(GroceryListEntity groceryList, String name, RoomMemberEntity addedBy) {
        this.groceryList = groceryList;
        this.name = name;
        this.addedBy = addedBy;
        this.isPurchased = false;
    }

    public GroceryItemEntity(GroceryListEntity groceryList, String name, String quantity,
            String category, RoomMemberEntity addedBy) {
        this.groceryList = groceryList;
        this.name = name;
        this.quantity = quantity;
        this.category = category;
        this.addedBy = addedBy;
        this.isPurchased = false;
    }

    public void markPurchased(RoomMemberEntity purchaser, BigDecimal actualPrice) {
        this.isPurchased = true;
        this.purchasedBy = purchaser;
        this.actualPrice = actualPrice;
        this.purchasedAt = LocalDateTime.now();
    }

    public void unmarkPurchased() {
        this.isPurchased = false;
        this.purchasedBy = null;
        this.actualPrice = null;
        this.purchasedAt = null;
    }
}
