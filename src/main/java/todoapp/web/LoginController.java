package todoapp.web;

import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import javax.validation.constraints.Size;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import todoapp.core.user.application.UserPasswordVerifier;
import todoapp.core.user.application.UserRegistration;
import todoapp.core.user.domain.User;
import todoapp.core.user.domain.UserEntityNotFoundException;
import todoapp.core.user.domain.UserPasswordNotMatchedException;
import todoapp.security.UserSession;
import todoapp.security.UserSessionRepository;
import todoapp.web.model.SiteProperties;

@Controller
// @SessionAttributes("user")
public class LoginController {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final UserPasswordVerifier userPasswordVerifier;
	private final UserRegistration userRegistration;
	private final UserSessionRepository userSessionRepository;
	
	public LoginController(UserPasswordVerifier userPasswordVerifier, UserRegistration userRegistration, UserSessionRepository userSessionRepository) {
		this.userPasswordVerifier = userPasswordVerifier;
		this.userRegistration = userRegistration;
		this.userSessionRepository = userSessionRepository;
	}
	
//	private final SiteProperties siteProperties;
//	
//	public LoginController(SiteProperties siteProperties) {
//		this.siteProperties = siteProperties;
//	}
//	
//	@ModelAttribute("site")
//	public SiteProperties siteProperties() {
//		return siteProperties;
//	}
	
	@GetMapping("/login")
	public String loginForm() {
		if (Objects.nonNull(userSessionRepository.get())) {
			return "redirect:/todos";
		}
		return "login";
	}
	
	/*
	 * @PostMapping("/login") public void loginProcess(String username, String
	 * password) { // ??????????????? ?????? @RequestParam ???????????? // String username =
	 * request.getParameter("username"); // String password =
	 * request.getParameter("password");
	 * 
	 * logger.debug("login command: {}, {}", username, password); }
	 */
	
	@PostMapping("/login")
	public String loginProcess(@Valid LoginCommand command, BindingResult bindingResult, Model model/*, HttpSession session*/) { // form?????? ?????? ??? @RequestBody ????????????
		logger.debug("login command: {}", command);
		
		// 0. ?????? ??? ????????? ????????? ??????: ????????? ???????????? ???????????????
		if (bindingResult.hasErrors()) {
			model.addAttribute("bindingResult", bindingResult);
			model.addAttribute("message", "?????? ?????? ????????? ???????????? ?????????.");
			return "login";
		}
		
//		if (command.getUsername().length() < 4) {
//			// ???????????? ???????????? ????????????.
//		}
		
		User user;
		try {
			// 1. ????????? ???????????? ???????????? ?????? ??????: ???????????? ?????? ??? ????????? ??????
			user = userPasswordVerifier.verify(command.getUsername(), command.getPassword());
		} catch (UserEntityNotFoundException e) {
			// 2. ???????????? ?????? ??????: ???????????? ?????? ??? ????????? ??????
			user = userRegistration.join(command.getUsername(), command.getPassword());
		} /*
			 * catch (UserPasswordNotMatchedException e) { // 3. ??????????????? ?????? ??????: ????????? ???????????? ???????????????
			 * model.addAttribute("message", e.getMessage()); return "login"; }
			 */

		// session.setAttribute("user", user);
		// model.addAttribute("user", user);
		userSessionRepository.set(new UserSession(user));
		return "redirect:/todos";
	}
	
	@ExceptionHandler(BindException.class)
	public String handleBindException(BindException error, Model model) {
		model.addAttribute("bindingResult", error.getBindingResult());
		model.addAttribute("message", "?????? ?????? ????????? ???????????? ?????????.");
		return "login";
	}
	
	@ExceptionHandler(UserPasswordNotMatchedException.class)
	public String handleUserPasswordNotMatchedException(UserPasswordNotMatchedException error, Model model) {
		model.addAttribute("message", error.getMessage());
		return "login";
	}
	
	@RequestMapping("/logout")
	public View logout() {
		userSessionRepository.clear();
		return new RedirectView("/todos");
	}
	
	static class LoginCommand {
		
		@Size(min = 4, max = 20)
		String username;
		String password;
		public String getUsername() {
			return username;
		}
		public void setUsername(String username) {
			this.username = username;
		}
		public String getPassword() {
			return password;
		}
		public void setPassword(String password) {
			this.password = password;
		}
		@Override
		public String toString() {
			return "LoginCommand [username=" + username + ", password=" + password + "]";
		}
		
		
	}
	
}
