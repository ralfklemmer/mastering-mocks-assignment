package victor.testing.mocks.purity.service;

import lombok.RequiredArgsConstructor;
import victor.testing.mocks.purity.domain.Coupon;
import victor.testing.mocks.purity.domain.Product;
import victor.testing.mocks.purity.domain.ProductCategory;
import victor.testing.mocks.purity.repo.CouponRepo;
import victor.testing.mocks.purity.repo.CustomerRepo;
import victor.testing.mocks.purity.repo.ProductRepo;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class PriceService {
    private final CustomerRepo customerRepo;
    private final ThirdPartyPrices thirdPartyPrices;
    private final CouponRepo couponRepo;
    private final ProductRepo productRepo;

    // Disclaimer
    // I tried to let names speak for itself.
    // Questions for you are marked with "@Victor".
    // I'm a bit surprised how large the code grew.

    // Major decisions
    // 1. Added "isApplied" flag to Coupon. If change of Coupon is forbidden, wrapping it would have solved this.
    // 2. For training purposes: get rid of every for-loop
    // 3. I didn't care of exaggerating refactorings ;-)
    public Map<Long, Double> computePriceForProduct(long customerId, List<Long> productIds, Map<Long, Double> internalPrices) {
        final List<Coupon> unfilteredCoupons = customerRepo.findById(customerId).getCoupons();
        final List<Product> products = productRepo.findAllById(productIds);

        // @Victor: personally I'm not used to write functions in the toMap. Do you regard this as readable? Maybe it's because I'm not that used to functional stuff.
        // @Victor: A variant could have been to introduce a "discounted" price field to Product. But I haven't tried it out.
        Map<Long, Double> pricesPerProduct = products.stream()
                .collect(Collectors.toMap(Product::getId, computePriceForProduct(internalPrices, unfilteredCoupons)));

        // @Victor: it's non-transparent that coupons are applied in the upper function. Do you have a better solution for that?
        persistAppliedCoupons(customerId, unfilteredCoupons);

        return pricesPerProduct;
    }

    // pure
    private Function<Product, Double> computePriceForProduct(Map<Long, Double> internalPrices, List<Coupon> unfilteredCoupons) {
        return p -> computePrice(p, internalPrices, unfilteredCoupons);
    }

    // pure
    private List<Coupon> filterUsedCoupons(List<Coupon> unfilteredCoupons) {
        return unfilteredCoupons.stream()
                .filter(Coupon::isApplied)
                .collect(Collectors.toList());
    }

    // pure
    private Double computePrice(Product product, Map<Long, Double> internalPrices, List<Coupon> unfilteredCoupons) {
        Double regularPrice = getRegularPrice(internalPrices, product.getId());
        return calcDiscountedPrice(regularPrice, product.getCategory(), unfilteredCoupons);
    }

    // pure
    private List<Coupon> filterApplicableCoupons(List<Coupon> unfilteredCoupons, ProductCategory category) {
        return unfilteredCoupons.stream()
                .filter(Coupon::autoApply)
                .filter(c -> c.isApplicableFor(category))
                .filter(c -> !c.isApplied())
                .collect(Collectors.toList());
    }

    // imperative, because external call could return different values if I call the method twice
    @VisibleForTesting // @Victor: actually I like using this for documentation purposes (normally from Guava), to underline the subcutaneous characteristics of the test.
    Double getRegularPrice(Map<Long, Double> internalPrices, long productId) {
        // @Victor: I don't understand why I need a lambda here. Found out that despite "internalPrices.get(productId)" is set, .orElse gets called. That was surprising.
        return Optional.ofNullable(internalPrices.get(productId))
                .orElseGet(() -> thirdPartyPrices.retrievePrice(productId));
    }

    // pure ?
    // @Victor: coupon.apply sets the "isApplied" flag internally in Coupon. I would consider it still as pure (enough).
    @VisibleForTesting
    Double calcDiscountedPrice(Double regularPrice, ProductCategory category, List<Coupon> unfilteredCoupons) {
        Double discountedPrice = Double.valueOf(regularPrice);
        List<Coupon> applicableCoupons = filterApplicableCoupons(unfilteredCoupons, category);

        // @Vicor: any idea how to get rid of that for-loop without growing the code to much?
        for (Coupon coupon : applicableCoupons) {
            discountedPrice = coupon.apply(category, discountedPrice);
        }
        return discountedPrice;
    }

    // @Victor: imperative, because it's an external call? Or pure because it's as command?
    // @Victor: I removed the ArgumentCaptor because of writing a test for this method. Is that ok?
    @VisibleForTesting
    void persistAppliedCoupons(long customerId, List<Coupon> unfilteredCoupons) {
        couponRepo.markUsedCoupons(customerId, filterUsedCoupons(unfilteredCoupons));
    }
}

