package com.nara.aivleTK.controller;

import com.nara.aivleTK.domain.user.User;
import com.nara.aivleTK.dto.ApiResponse;
import com.nara.aivleTK.dto.user.*;
import com.nara.aivleTK.exception.ResourceNotFoundException;
import com.nara.aivleTK.exception.UnauthorizedException;
import com.nara.aivleTK.repository.UserRepository;
import com.nara.aivleTK.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    // 1. 유저 생성
    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@RequestBody UserCreateRequest user) {
        UserResponse saved = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(saved));
    }

    // 2. 유저 조회
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable("id") Integer id) {
        UserResponse userResponse = userService.getUserInfo(id);
        return ResponseEntity.ok(ApiResponse.success(userResponse));
    }

    // 3. 로그인 (POST)
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserResponse>> login(@RequestBody LoginRequest request, HttpSession session) {
        UserResponse loginUser = userService.login(request);

        // 세션에 회원 정보 저장 (서버가 "이 사람 로그인했음" 하고 기억표를 줌)
        session.setAttribute("loginUser", loginUser);

        return ResponseEntity.ok(ApiResponse.success("로그인 성공", loginUser));
    }

    // 4. 로그아웃 (POST)
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Object>> logout(HttpSession session) {
        session.invalidate(); // 세션 폭파 (기억표 삭제)
        return ResponseEntity.ok(ApiResponse.success("로그아웃 되었습니다."));
    }

    // 6. 비밀번호 찾기 (GET)
    @PostMapping("/reset_password/")
    public ResponseEntity<ApiResponse<Object>> resetPassword(@RequestBody ResetPasswordRequest rpr) {
        String password = userService.resetPassword(rpr.getEmail(), rpr.getName(), rpr.getAnswer(), rpr.getBirth());
        return ResponseEntity.ok(ApiResponse.success("임시 비밀번호가 발급 되었습니다."));
    }

    // 7. 회원정보 수정 (PUT)
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(@PathVariable Integer id,
            @RequestBody UserCreateRequest request) {
        UserResponse updatedUser = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success("회원정보가 수정되었습니다.", updatedUser));
    }

    // 8. 회원정보 삭제 (DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteUser(@PathVariable Integer id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("회원정보가 삭제되었습니다."));
    }

    // 9. 휴먼 계정 전환
    @PostMapping("/restUser/{id}")
    public ResponseEntity<ApiResponse<Object>> restUser(@PathVariable Integer id, @RequestParam Integer rest) { // rest가
        // 전환
        userService.restUser(id, rest);
        return ResponseEntity.ok(ApiResponse.success("계정 상태가 변경되었습니다."));
    }

    // 10. 로그인 확인
    @GetMapping("/checkLogin")
    public ResponseEntity<ApiResponse<UserResponse>> checkLogin(HttpSession session) {
        UserResponse loginUser = (UserResponse) session.getAttribute("loginUser");

        if (loginUser == null) {
            throw new UnauthorizedException("로그인이 필요합니다.");
        }

        return ResponseEntity.ok(ApiResponse.success(loginUser));
    }

    // 11. 계정(아이디) 찾기 질문 조회
    @GetMapping("/find-email/identify")
    public ResponseEntity<ApiResponse<QuestionDto>> checkQuestion(@RequestParam String name,
            @RequestParam LocalDate birth) {
        User user = userRepository.findByNameAndBirth(name, birth)
                .orElseThrow(() -> new ResourceNotFoundException("해당 계정이 없습니다."));

        // question 필드가 숫자 문자열인 경우 Integer로 파싱
        Integer questionIndex = Integer.parseInt(user.getQuestion());
        QuestionDto data = new QuestionDto(user.getId(), questionIndex);
        return ResponseEntity.ok(new ApiResponse<>("success", "확인성공", data));
    }

    // 12. 답변 검증 및 이메일 반환
    @GetMapping("/find-email/verify")
    public ResponseEntity<ApiResponse<String>> verifyAndSendEmail(@RequestParam String name,
            @RequestParam LocalDate birth) {
        User user = userRepository.findByNameAndBirth(name, birth)
                .orElseThrow(() -> new ResourceNotFoundException("해당 계정이 없습니다."));

        return ResponseEntity.ok(ApiResponse.success(user.getQuestion()));
    }

    // 5. 아이디 찾기 (GET)
    @GetMapping("/find_email")
    public ResponseEntity<ApiResponse<String>> findEmail(@RequestParam String name, @RequestParam String answer,
            @RequestParam LocalDate birth) {
        String email = userService.findEmail(name, answer, birth);
        return ResponseEntity.ok(ApiResponse.success("이메일을 찾았습니다.", email));
    }
}