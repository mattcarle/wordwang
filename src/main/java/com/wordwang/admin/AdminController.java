package com.wordwang.admin;

import com.wordwang.highscore.HighScoreService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final List<GrantedAuthority> ADMIN_AUTHORITIES = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));

    private final AdminService adminService;
    private final HighScoreService highScoreService;
    private final SecurityContextRepository securityContextRepository;

    public AdminController(AdminService adminService, HighScoreService highScoreService,
                            SecurityContextRepository securityContextRepository) {
        this.adminService = adminService;
        this.highScoreService = highScoreService;
        this.securityContextRepository = securityContextRepository;
    }

    @GetMapping("/setup-status")
    public SetupStatusResponse setupStatus() {
        return new SetupStatusResponse(adminService.isSetupRequired());
    }

    @PostMapping("/setup")
    public ResponseEntity<Void> setup(@RequestBody PasswordRequest request,
                                       HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        adminService.setupPassword(request.password());
        establishSession(httpRequest, httpResponse);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody PasswordRequest request,
                                    HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        if (!adminService.verifyPassword(request.password())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Incorrect password"));
        }
        establishSession(httpRequest, httpResponse);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        new SecurityContextLogoutHandler().logout(request, response, SecurityContextHolder.getContext().getAuthentication());
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }

    /** Reachable only once authenticated (see SecurityConfig) - the frontend uses this to check session state on load. */
    @GetMapping("/me")
    public ResponseEntity<Void> me() {
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/highscores")
    public ResponseEntity<Void> clearHighScores() {
        highScoreService.clearAll();
        return ResponseEntity.noContent().build();
    }

    private void establishSession(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        var authentication = new UsernamePasswordAuthenticationToken("admin", null, ADMIN_AUTHORITIES);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);
    }
}
