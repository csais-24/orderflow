package com.cnom.orderflow.inventory.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "inventories")
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID productId;

    @Column(nullable = false)
    private Long available;

    @Column(nullable = false)
    private Long reserved;

    @Version
    private Long version;

    protected Inventory() {
    }

    public Inventory(UUID productId, Long available, Long reserved) {
        this.productId = productId;
        this.available = available;
        this.reserved = reserved;
    }

    public UUID getId() {
        return id;
    }

    public UUID getProductId() {
        return productId;
    }

    public Long getAvailable() {
        return available;
    }

    public Long getReserved() {
        return reserved;
    }

    public Long getVersion() {
        return version;
    }

    public void setAvailable(Long available) {
        this.available = available;
    }

    public void setReserved(Long reserved) {
        this.reserved = reserved;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
