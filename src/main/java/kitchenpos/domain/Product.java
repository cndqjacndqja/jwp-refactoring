package kitchenpos.domain;

import java.math.BigDecimal;
import java.util.Objects;

public class Product {
    private Long id;
    private String name;
    private BigDecimal price;

    private Product(final Long id, final String name, final BigDecimal price) {
        validatePrice(price);
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public static Product of(final String name, final BigDecimal price) {
        return new Product(null, name, price);
    }

    public static Product of(final Long id, final String name, final BigDecimal price) {
        return new Product(id, name, price);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    private void validatePrice(final BigDecimal price) {
        if (Objects.isNull(price) || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("product의 가격은 0원 이상이어야 합니다.");
        }
    }

    public BigDecimal getCalculatedPrice(final long quantity) {
        return price.multiply(BigDecimal.valueOf(quantity));
    }
}
