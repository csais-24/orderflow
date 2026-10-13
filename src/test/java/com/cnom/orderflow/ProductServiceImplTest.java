package com.cnom.orderflow;
import com.cnom.orderflow.product.dto.CreateProductRequest;
import com.cnom.orderflow.product.dto.ProductResponse;
import com.cnom.orderflow.product.entity.Product;
import com.cnom.orderflow.product.repository.ProductRepository;
import com.cnom.orderflow.product.service.ProductServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import java.math.BigDecimal;

public class ProductServiceImplTest {

    private ProductRepository productRepository;
    private ProductServiceImpl productService;

    @BeforeEach
    public void setUp(){
        productRepository = Mockito.mock();
        productService = new ProductServiceImpl(productRepository);
    }

    @Test
    public void shouldCreateProductSuccessfully(){
        BigDecimal fixedPrice = new BigDecimal("145.55");
        ArgumentCaptor<Product> productArgumentCaptor = ArgumentCaptor.forClass(Product.class);
        CreateProductRequest createProductRequest = new CreateProductRequest(
                "keyboard",
                "Mechanical keyboard",
                fixedPrice);
        Product product = new Product(
                "keyboard",
                "Mechanical keyboard",
                fixedPrice
        );

        Mockito.when(productRepository.save(Mockito.any(Product.class)))
                .thenReturn(product);

        // Act
        ProductResponse productResponse = productService.createProduct(createProductRequest);
        // Assertions
        Mockito.verify(productRepository).save(productArgumentCaptor.capture());
        Assertions.assertEquals("keyboard", productArgumentCaptor.getValue().getName());
        Assertions.assertEquals("Mechanical keyboard", productArgumentCaptor.getValue().getDescription());
        Assertions.assertEquals(fixedPrice, productArgumentCaptor.getValue().getPrice());

    }
}
