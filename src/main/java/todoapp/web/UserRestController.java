package todoapp.web;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpSession;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.multipart.MultipartFile;

import todoapp.core.user.application.ProfilePictureChanger;
import todoapp.core.user.domain.ProfilePicture;
import todoapp.core.user.domain.ProfilePictureStorage;
import todoapp.core.user.domain.User;
import todoapp.security.UserSession;
import todoapp.security.UserSessionRepository;
import todoapp.web.model.UserProfile;

@RestController
@RolesAllowed("ROLE_USER")
public class UserRestController {
	
	private final UserSessionRepository userSessionRepository;
	private final ProfilePictureChanger profilePictureChanger;
	private final ProfilePictureStorage profilePictureStorage;
	
	public UserRestController(UserSessionRepository userSessionRepository, ProfilePictureChanger profilePictureChanger, ProfilePictureStorage profilePictureStorage) {
		this.userSessionRepository = userSessionRepository;
		this.profilePictureChanger = profilePictureChanger;
		this.profilePictureStorage = profilePictureStorage;
	}

	@GetMapping("/api/user/profile")
	public /*ResponseEntity<UserProfile>*/ UserProfile userProfile(/* HttpSession session */ /* @SessionAttribute("user") User user */ UserSession userSession) {
		
		// User user = (User) session.getAttribute("user");
		// UserSession userSession = userSessionRepository.get();
		/*
		 * if (Objects.nonNull(userSession)) { return ResponseEntity.ok(new
		 * UserProfile(userSession.getUser())); } else { return
		 * ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); }
		 */
		
		return new UserProfile(userSession.getUser());
	}
	
	@PostMapping("/api/user/profile-picture")
	public UserProfile changeProfilePicture(MultipartFile profilePicture, UserSession userSession) throws IOException {
		
		// 업로드된 프로필 이미지 파일 저장하기
		/*
		 * Path basePath = Paths.get("./files/user-profile-picture");
		 * 
		 * if (!basePath.toFile().exists()) { basePath.toFile().mkdirs(); }
		 * 
		 * Path profilePicturePath =
		 * basePath.resolve(profilePicture.getOriginalFilename());
		 * profilePicture.transferTo(profilePicturePath);
		 */
		
		URI profilePictureUri = profilePictureStorage.save(profilePicture.getResource());
		
		
		// 프로필 이미지 변경 후 세션을 갱신하기
		User updatedUser = profilePictureChanger.change(userSession.getName(), new ProfilePicture(profilePictureUri));
		userSessionRepository.set(new UserSession(updatedUser));
		return new UserProfile(updatedUser);
	}
}
