package victor.testing.mocks.purity.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import victor.testing.mocks.purity.domain.Coupon;
import victor.testing.mocks.purity.domain.Customer;
import victor.testing.mocks.purity.domain.Product;
import victor.testing.mocks.purity.repo.CouponRepo;
import victor.testing.mocks.purity.repo.CustomerRepo;
import victor.testing.mocks.purity.repo.ProductRepo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static victor.testing.mocks.purity.domain.ProductCategory.*;

@ExtendWith(MockitoExtension.class)
class PriceServiceTest {
    public static final long CUSTOMER_ID = 13L;
    public final Coupon couponForHome = new Coupon(HOME, 2);
    public final Coupon couponForElectrics = new Coupon(ELECTRONICS, 4);
    public final Product productForHome = new Product().setId(1L).setCategory(HOME);
    public final Product productForKids = new Product().setId(2L).setCategory(KIDS);

    @Mock
    CouponRepo couponRepo;
    @Mock
    ProductRepo productRepo;

    // Fakes
    CustomerRepoFake customerRepoFake = new CustomerRepoFake();
    ThirdPartyPricesFake thirdPartyPricesFake = new ThirdPartyPricesFake(productForKids.getId());

    Customer customer = new Customer().setCoupons(List.of(couponForHome, couponForElectrics));

    PriceService underTest;

    @BeforeEach
    void setUp() {
        underTest = new PriceService(customerRepoFake, thirdPartyPricesFake, couponRepo, productRepo);
    }

    @Test
    void computePrices_shouldApplyCouponsToBothProducts() {
        // GIVEN
        List<Long> productIds = List.of(productForHome.getId(), productForKids.getId());
        Map<Long, Double> internalPrices = Map.of(productForHome.getId(), 10d);

        when(productRepo.findAllById(productIds)).thenReturn(List.of(productForHome, productForKids));

        // WHEN
        Map<Long, Double> result = underTest.computePriceForProduct(CUSTOMER_ID, productIds, internalPrices);

        // THEN
        assertThat(result)
                .containsEntry(productForHome.getId(), 8d)
                .containsEntry(productForKids.getId(), 5d);
    }

    @Test
    void getRegularPrice_shouldBeTakenFromInternalPriceList() {
        // GIVEN
        double regularPriceOfProduct1 = 12.34d;
        Map<Long, Double> internalPrices = Map.of(productForHome.getId(), regularPriceOfProduct1);

        // WHEN
        Double regularPrice = underTest.getRegularPrice(internalPrices, productForHome.getId());

        // THEN
        assertThat(regularPrice).isEqualTo(regularPriceOfProduct1);
    }

    @Test
    void getRegularPrice_shouldBeTakenFromThirdpartyService() {
        // GIVEN
        double regularPriceOfThirdparty = ThirdPartyPricesFake.PRICE;

        // WHEN
        Double regularPrice = underTest.getRegularPrice(new HashMap<>(), productForKids.getId());

        // THEN
        assertThat(regularPrice).isEqualTo(regularPriceOfThirdparty);
    }

    @Test
    void calcDiscountedPrice_shouldApplyOneCoupon() {
        //GIVEN
        Coupon applied = new Coupon(HOME, 2).setApplied(true);
        Coupon notApplied = new Coupon(HOME, 2).setApplied(false);

        // WHEN
        Double price = underTest.calcDiscountedPrice(10d, HOME, List.of(applied, notApplied));

        // THEN
        assertThat(price).isEqualTo(8d);
    }

    @Test
    void persistAppliedCoupons_shouldHandOverAppliedCoupons() {
        //GIVEN
        Coupon applied = new Coupon(HOME, 2).setApplied(true);
        Coupon notApplied = new Coupon(HOME, 2).setApplied(false);

        // WHEN
        underTest.persistAppliedCoupons(10L, List.of(applied, notApplied));

        // THEN
        verify(couponRepo).markUsedCoupons(10L, List.of(applied));
    }

    // -------------------------------

    class CustomerRepoFake implements CustomerRepo {

        @Override
        public Customer findById(long customerId) {
            return customer; // debatable to make this fake less dependent on this specific test
        }
    }

    class ThirdPartyPricesFake implements ThirdPartyPrices {

        public static final double PRICE = 5d;
        final Map<Long, Double> prices;

        public ThirdPartyPricesFake(long productId) { // give a bit of flexibility to the fake (even though it's actually not needed in this test, so just playing around here)
            prices = Map.of(productId, PRICE);
        }

        @Override
        public double retrievePrice(Long id) {
            return prices.get(id);
        }
    }
}
