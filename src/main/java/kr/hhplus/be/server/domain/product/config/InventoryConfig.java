package kr.hhplus.be.server.domain.product.config;

import kr.hhplus.be.server.domain.product.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
@RequiredArgsConstructor
public class InventoryConfig {
    @Bean
    @Lazy
    public InventoryService inventoryServiceSelf(InventoryService inventoryService) {
        return inventoryService;
    }
}
