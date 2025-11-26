package kr.hhplus.be.server.layered.user.controller;

import kr.hhplus.be.server.layered.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 유저 잔액 조회
    @PostMapping("/{userId}/balance")
    public ResponseEntity<Long> getBalance(@PathVariable String user_id) {
        return ResponseEntity.ok(Long.parseLong(user_id));
    }
}
