package kr.hhplus.be.server.layered.payment.repository;

import kr.hhplus.be.server.layered.payment.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentJpaRepository extends JpaRepository<Payment, Long> {
}
