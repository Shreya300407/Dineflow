package com.dineflow.model;

public enum OrderStatus {
    PLACED("Placed", "Your order has been received by the restaurant."),
    CONFIRMED("Confirmed", "The restaurant has accepted and confirmed your order."),
    PREPARING("Preparing", "Our chefs are freshly preparing your artisanal dishes."),
    OUT_FOR_DELIVERY("Out for Delivery", "Your order is with the delivery valet on its way to you."),
    DELIVERED("Delivered", "Your order has been successfully delivered. Bon appétit!"),
    CANCELLED("Cancelled", "This order has been cancelled.");

    private final String displayName;
    private final String description;

    OrderStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Requirement 7: Validate status transitions and prevent invalid updates.
     */
    public boolean canTransitionTo(OrderStatus next) {
        if (next == null) {
            return false;
        }
        if (this == next) {
            return true; // Idempotent update
        }

        return switch (this) {
            case PLACED -> next == CONFIRMED || next == CANCELLED;
            case CONFIRMED -> next == PREPARING || next == CANCELLED;
            case PREPARING -> next == OUT_FOR_DELIVERY || next == CANCELLED;
            case OUT_FOR_DELIVERY -> next == DELIVERED || next == CANCELLED;
            case DELIVERED, CANCELLED -> false; // Terminal states cannot transition further
        };
    }
}
