package todoapp.web;

import java.util.Objects;

import javax.servlet.http.HttpSession;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;

import todoapp.core.user.domain.User;
import todoapp.security.UserSession;
import todoapp.security.UserSessionRepository;
import todoapp.web.model.UserProfile;

@RestController
public class UserRestController {
	
	private final UserSessionRepository userSessionRepository;
	
	public UserRestController(UserSessionRepository userSessionRepository) {
		this.userSessionRepository = userSessionRepository;
	}

	@GetMapping("/api/user/profile")
	public ResponseEntity<UserProfile> userProfile(/* HttpSession session */ /* @SessionAttribute("user") User user */ UserSession userSession) {
		
		// User user = (User) session.getAttribute("user");
		// UserSession userSession = userSessionRepository.get();
		if (Objects.nonNull(userSession)) {
			return ResponseEntity.ok(new UserProfile(userSession.getUser()));
		} else {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
	}
}
