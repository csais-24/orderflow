package com.cnom.orderflow;

import com.cnom.orderflow.product.controller.ProductController;
import com.cnom.orderflow.product.dto.CreateProductRequest;
import com.cnom.orderflow.product.dto.ProductResponse;
import com.cnom.orderflow.product.service.ProductService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@WebMvcTest(ProductController.class)
public class ProductControllerTest {

    @MockitoBean
    ProductService productService;

    @Autowired
    MockMvc mockMvc;

    @Test
    public void shouldCreateProductSuccessfully() throws Exception {
        // Arrange
        String newProductJsonPayload = """
                {
                    "name":"keyboard",
                    "description": "mechanical keyboard",
                    "price": 145.55
                }
                """;
        UUID fixedUUID =  UUID.fromString("12345678-1234-1234-1234-1234567890ab");
        Instant fixedInstant = Instant.parse("2026-09-29T10:00:00Z");
        BigDecimal fixedPrice = new BigDecimal("145.55");

        ProductResponse productResponse = new ProductResponse(
                fixedUUID,
                "keyboard",
                "mechanical keyboard",
                fixedPrice,
                true,
                fixedInstant,
                fixedInstant
        );

        Mockito.when(productService.createProduct(Mockito.any(CreateProductRequest.class)))
                .thenReturn(productResponse);
        // Assert
        mockMvc.perform(
                post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newProductJsonPayload)
        )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("keyboard"))
                .andExpect(jsonPath("$.description").value("mechanical keyboard"))
                .andExpect(jsonPath("$.price").value(fixedPrice))
                .andExpect(jsonPath("$.active").value(true));
        Mockito.verify(productService).createProduct(Mockito.any(CreateProductRequest.class));
    }

    @Test
    public void shouldRejectInvalidProduct() throws Exception {
        String newProductJsonPayload = """
                {
                    "name":"",
                    "description": "mechanical keyboard",
                    "price": 145.55
                }
                """;

        mockMvc.perform(
                post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newProductJsonPayload)
        )
                .andExpect(status().isBadRequest());

        Mockito.verify(productService, Mockito.never())
                .createProduct(Mockito.any(CreateProductRequest.class));
    }

}
