package com.cnom.orderflow;

import com.cnom.orderflow.inventory.InventoryController;
import com.cnom.orderflow.inventory.dto.InventoryResponse;
import com.cnom.orderflow.inventory.service.InventoryService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

@WebMvcTest(InventoryController.class)
public class InventoryControllerTest {

    @MockitoBean
    private InventoryService inventoryService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void shouldCreateInventorySuccessfully() throws Exception {
        UUID fixedInventoryUUID = UUID.fromString("f591ca91-4fb9-49a2-9b9e-304f83d11911");
        String jsonInventoryReq = """
                    {
                        "productId": "f353ca91-4fc5-49f2-9b9e-304f83d11914"
                    }
                """;
        InventoryResponse inventoryResponse = new InventoryResponse(
                fixedInventoryUUID,
                UUID.fromString("f353ca91-4fc5-49f2-9b9e-304f83d11914"),
                0L,
                0L
        );
        Mockito.when(inventoryService.createInventory(Mockito.any(UUID.class)))
                .thenReturn(inventoryResponse);

        mockMvc.perform(
                post("/api/v1/inventories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInventoryReq))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productId").value("f353ca91-4fc5-49f2-9b9e-304f83d11914"));
    }

    @Test
    public void shouldThrowBadRequestExceptionWhenCreatingInventory() throws Exception {
        String jsonInventoryReq = """
                    {
                        "productId": ""
                    }
                """;
        mockMvc.perform(
                post("/api/v1/inventories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInventoryReq))
                .andExpect(status().isBadRequest());

        Mockito.verify(inventoryService, Mockito.never()).createInventory(Mockito.any(UUID.class));
    }

    @Test
    public void shouldAddStockSuccessfully() throws Exception {
        ArgumentCaptor<UUID> uuidArgumentCaptor = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<Long> quantityArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        String jsonAddStockToInventoryRequest = """
                    {
                        "quantity": 10
                    }
                """;
        mockMvc.perform(
                patch("/api/v1/inventories/f353ca91-4fc5-49f2-9b9e-304f83d11914/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonAddStockToInventoryRequest)
        )
                .andExpect(status().isNoContent());
        Mockito.verify(inventoryService).addStock(uuidArgumentCaptor.capture(), quantityArgumentCaptor.capture());
        Assertions.assertEquals(10L, quantityArgumentCaptor.getValue());
        Assertions.assertEquals("f353ca91-4fc5-49f2-9b9e-304f83d11914", uuidArgumentCaptor.getValue().toString());
    }

    @Test
    public void shouldReturnBadRequestWhenAddingStock() throws Exception {
        String jsonAddStockToInventoryRequest = """
                    {
                        "quantity": 0
                    }
                """;
        mockMvc.perform(
                patch("/api/v1/inventories/f353ca91-4fc5-49f2-9b9e-304f83d11914/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonAddStockToInventoryRequest)
        )
                .andExpect(status().isBadRequest());
        Mockito.verify(inventoryService, Mockito.never()).addStock(Mockito.any(UUID.class), Mockito.anyLong());
    }
}
