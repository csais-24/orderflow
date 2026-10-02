package com.cnom.orderflow;

import com.cnom.orderflow.exception.IllegalQuantityException;
import com.cnom.orderflow.exception.InsufficientInventoryException;
import com.cnom.orderflow.exception.InventoryAlreadyExistsException;
import com.cnom.orderflow.exception.ProductNotFoundException;
import com.cnom.orderflow.inventory.dto.InventoryResponse;
import com.cnom.orderflow.inventory.entity.Inventory;
import com.cnom.orderflow.inventory.repository.InventoryRepository;
import com.cnom.orderflow.inventory.service.InventoryServiceImpl;
import com.cnom.orderflow.product.service.ProductService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.UUID;

public class InventoryServiceImplTest {

    private InventoryRepository inventoryRepository;
    private ProductService productService;
    private InventoryServiceImpl inventoryService;

    @BeforeEach
    public void setUp(){
        inventoryRepository = Mockito.mock();
        productService = Mockito.mock();
        inventoryService = new InventoryServiceImpl(
                productService, inventoryRepository
        );
    }

    @Test
    public void shouldCreateInventorySuccessfully(){
        UUID fixedProductUUID = UUID.fromString("6ba7b814-9dad-11d1-80b4-00c04fd430d1");
        ArgumentCaptor<Inventory> inventoryArgumentCaptor = ArgumentCaptor.forClass(Inventory.class);
        Inventory fixedInventoryReq = new Inventory(fixedProductUUID, 0L, 0L);


        Mockito.when(productService.productExistsById(Mockito.any(UUID.class)))
                .thenReturn(true);
        Mockito.when(inventoryRepository.existsByProductId(Mockito.any(UUID.class)))
                .thenReturn(false);
        Mockito.when(inventoryRepository.save(Mockito.any(Inventory.class)))
                .thenReturn(fixedInventoryReq);

        InventoryResponse inventoryResponse = inventoryService.createInventory(fixedProductUUID);

        Mockito.verify(productService).productExistsById(Mockito.any(UUID.class));
        Mockito.verify(inventoryRepository).existsByProductId(Mockito.any(UUID.class));
        Mockito.verify(inventoryRepository).save(inventoryArgumentCaptor.capture());

        Assertions.assertEquals(fixedProductUUID, inventoryArgumentCaptor.getValue().getProductId());
        Assertions.assertEquals(0, inventoryArgumentCaptor.getValue().getAvailable());
        Assertions.assertEquals(0, inventoryArgumentCaptor.getValue().getReserved());
    }

    @Test
    public void shouldThrowProductNotFoundException() {
        UUID fixedProductUUID = UUID.fromString("6ba7b814-9dad-11d1-80b4-00c04fd430d1");

        Mockito.when(productService.productExistsById(Mockito.any(UUID.class)))
                .thenReturn(false);
        Assertions
                .assertThrows(ProductNotFoundException.class, () -> inventoryService.createInventory(fixedProductUUID));

        Mockito.verify(inventoryRepository, Mockito.never()).existsByProductId(Mockito.any(UUID.class));
        Mockito.verify(inventoryRepository, Mockito.never()).save(Mockito.any(Inventory.class));
    }

    @Test
    public void shouldThrowInventoryAlreadyExistsException(){
        UUID fixedProductUUID = UUID.fromString("6ba7b814-9dad-11d1-80b4-00c04fd430d1");

        Mockito.when(productService.productExistsById(Mockito.any(UUID.class)))
                .thenReturn(true);
        Mockito.when(inventoryRepository.existsByProductId(Mockito.any(UUID.class)))
                .thenReturn(true);

        Assertions
                .assertThrows(InventoryAlreadyExistsException.class, () -> inventoryService.createInventory(fixedProductUUID));
        Mockito.verify(productService).productExistsById(Mockito.any(UUID.class));
        Mockito.verify(inventoryRepository, Mockito.never()).save(Mockito.any(Inventory.class));
    }

    @Test
    public void shouldReserveInventorySuccessfully(){
        UUID fixedProductUUID = UUID.fromString("6ba7b814-9dad-11d1-80b4-00c04fd430d1");
        Inventory inventoryMock = new Inventory(fixedProductUUID, 10L, 3L);

        Mockito.when(inventoryRepository.findById(Mockito.any(UUID.class)))
                        .thenReturn(Optional.of(inventoryMock));

        inventoryService.reserve(fixedProductUUID, 5L);
        Mockito.verify(inventoryRepository).findById(Mockito.any(UUID.class));
        Assertions.assertEquals(5, inventoryMock.getAvailable());
        Assertions.assertEquals(8, inventoryMock.getReserved());
        Assertions.assertEquals(fixedProductUUID, inventoryMock.getProductId());
    }

    @Test
    public void shouldThrowInsufficientInventoryException(){
        UUID fixedProductUUID = UUID.fromString("6ba7b814-9dad-11d1-80b4-00c04fd430d1");
        Inventory inventoryMock = new Inventory(fixedProductUUID, 3L, 5L);

        Mockito.when(inventoryRepository.findById(Mockito.any(UUID.class)))
                .thenReturn(Optional.of(inventoryMock));

        Assertions
                .assertThrows(
                        InsufficientInventoryException.class,
                        () -> inventoryService.reserve(fixedProductUUID, 5L));

        Assertions.assertEquals(3L, inventoryMock.getAvailable());
        Assertions.assertEquals(5L, inventoryMock.getReserved());
    }

    @Test
    public void shouldThrowIllegalQuantityException(){
        UUID fixedProductUUID = UUID.fromString("6ba7b814-9dad-11d1-80b4-00c04fd430d1");
        Inventory inventoryMock = new Inventory(fixedProductUUID, 3L, 5L);

        Mockito.when(inventoryRepository.findById(Mockito.any(UUID.class)))
                .thenReturn(Optional.of(inventoryMock));

        Assertions
                .assertThrows(
                        IllegalQuantityException.class,
                        () -> inventoryService.reserve(fixedProductUUID, 0L));

        Assertions.assertEquals(3L, inventoryMock.getAvailable());
        Assertions.assertEquals(5L, inventoryMock.getReserved());
        Mockito.verify(inventoryRepository, Mockito.never()).findById(Mockito.any(UUID.class));
    }
}
