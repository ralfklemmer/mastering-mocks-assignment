package victor.testing.mocks.purity.domain;

import lombok.ToString;

@ToString
public class Coupon {
    private final ProductCategory category;
    private final int discountAmount;
    private boolean autoApply = true;
    private boolean isApplied;

    public Coupon(ProductCategory category, int discountAmount) {
        this.category = category;
        this.discountAmount = discountAmount;
    }

    public boolean autoApply() {
        return autoApply;
    }

    public void setAutoApply(boolean autoApply) {
        this.autoApply = autoApply;
    }

    public boolean isApplicableFor(ProductCategory category) {
        return this.category == category;
    }

    public Double apply(ProductCategory category, Double price) {
        if (!isApplicableFor(category)) {
            throw new IllegalArgumentException();
        }

        isApplied = true;
        return price - discountAmount;
    }

    public boolean isApplied() {
        return isApplied;
    }

    public Coupon setApplied(boolean applied) {
        isApplied = applied;
        return this;
    }
}
