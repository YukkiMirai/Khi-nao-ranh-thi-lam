package com.wibuneverdie.core.init;

import com.wibuneverdie.core.dto.UserCreateRequest;

import com.wibuneverdie.core.repository.UaUserRepository;
import com.wibuneverdie.core.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserService userService;
    private final UaUserRepository uaUserRepository;

    @Override
    public void run(String... args) throws Exception {
        // 1. Kiểm tra xem DB đã có user nào chưa (để tránh tạo trùng mỗi lần restart)
        if (uaUserRepository.count() == 0) {
            log.info(">>> Hệ thống chưa có người dùng. Bắt đầu khởi tạo tài khoản ADMIN mặc định...");

            // 2. Định nghĩa data Admin mặc định
            UserCreateRequest adminReq = new UserCreateRequest(
                    "admin",              // userId
                    "admin",        // password (Service sẽ tự encode)
                    "System Admin",       // fullName
                    "0000000000",         // phone
                    "admin@wibuneverdie.com", // email
                    "ADMIN",              // type
                    "LOCAL"               // authProvider
            );

            // 3. Gọi trực tiếp Service để lưu vào DB
            try {
                userService.createUser(adminReq, "SYSTEM_INITIALIZER");
                log.info(">>> Khởi tạo tài khoản ADMIN thành công: admin / admin123456");
            } catch (Exception e) {
                log.error(">>> Lỗi khi khởi tạo Admin: {}", e.getMessage(), e);
            }
        } else {
            log.info(">>> Dữ liệu người dùng đã tồn tại. Bỏ qua bước khởi tạo.");
        }
    }
}