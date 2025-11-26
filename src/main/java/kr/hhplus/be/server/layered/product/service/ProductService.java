package kr.hhplus.be.server.layered.product.service;

import kr.hhplus.be.server.layered.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;


}
