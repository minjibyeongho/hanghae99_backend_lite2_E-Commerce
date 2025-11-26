package kr.hhplus.be.server.layered.sale.service;

import kr.hhplus.be.server.layered.sale.repository.SaleRepository;
import org.springframework.stereotype.Service;

@Service
public class SaleService {

    private final SaleRepository saleRepository;

    public SaleService(SaleRepository saleRepository) {
        this.saleRepository = saleRepository;
    }

}
