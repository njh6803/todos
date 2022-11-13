package todoapp.web;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.AbstractView;

import todoapp.core.todos.application.TodoFinder;
import todoapp.core.todos.domain.Todo;
import todoapp.web.convert.TodoToSpreadsheetConverter;
import todoapp.web.model.SiteProperties;

@Controller
public class TodoController {
	
//	private Environment environment;
//	private String siteAuthor;
//	private final SiteProperties siteProperties;
	private final TodoFinder finder;
	
	public TodoController(/* SiteProperties siteProperties,*/ TodoFinder finder) {
		// this.siteProperties = siteProperties;
		this.finder = finder;
	}
	
	// 방법1
//	public TodoController(Environment environment) {
//		this.environment = environment;
//	}
	
	// 방법2
//	public TodoController(Environment environment, @Value("${site.author}") String siteAuthor) {
//		this.environment = environment;
//		this.siteAuthor = siteAuthor;
//	}
	
	// 방법3
//	public TodoController(Environment environment, @Value("${site.author}") String siteAuthor, SiteProperties siteProperties) {
//		this.environment = environment;
//		this.siteAuthor = siteAuthor;
//		this.siteProperties = siteProperties;
//	}
	
	// 방법4
//	public TodoController(SiteProperties siteProperties) {
//		this.siteProperties = siteProperties;
//	}
	
//	@RequestMapping("/todos")
//	public ModelAndView todos() throws Exception {
//		// SiteProperties site = new SiteProperties();
//		// site.setAuthor(environment.getProperty("site.author"));
//		// site.setAuthor(siteAuthor);
//		// site.setDescription("스프링 MVC 할 일 관리 앱");
//		
//		ModelAndView mav = new ModelAndView();
//		mav.addObject("site", siteProperties);
//		mav.setViewName("todos");
//		
//		// ViewResolver viewResolver = new InternalResourceViewResolver();
//		// View view =  viewResolver.resolveViewName("todos", null);
//		
//		// viewname = "todos"
//		// prefix => classpath:/templates/
//		// suffix => .html
//		// fullViewName = prefix + "todos" + suffix
//		// fullViewName = classpath:/templates/todos.html
//		return mav;
//	}
	
//	@ModelAttribute("site")
//	public SiteProperties siteProperties() {
//		return siteProperties;
//	}
	
	@RequestMapping("/todos")
	public void todos(Model model) throws Exception {
		// model.addAttribute("site", siteProperties);
	}
	
	@RequestMapping(path = "/todos", produces = "text/csv")
	public void downloadTodos(Model model) {
		model.addAttribute("todos", new TodoToSpreadsheetConverter().convert(finder.getAll()));
	}
	
	public static class TodoCsvViewResolver implements ViewResolver {

		@Override
		public View resolveViewName(String viewName, Locale locale) throws Exception {
			if ("todos".equals(viewName)) {
				return new TodoCsvView();
			}
			return null;
		}
		
	}
	
	public static class TodoCsvView extends AbstractView implements View {
		
		private final Logger logger = LoggerFactory.getLogger(getClass());
		
		public TodoCsvView() {
			setContentType("text/csv");
		}
		
		

		@Override
		protected boolean generatesDownloadContent() {
			return true;
		}



		@Override
		protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request,
				HttpServletResponse response) throws Exception {
			logger.info("render model as csv content");	
			
			response.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment: filename=\"todos.csv\"");
			response.getWriter().println("id, title, complated");
			
			List<Todo> todos  = (List<Todo>)model.getOrDefault("todos", Collections.emptyList());
			for(Todo todo :todos) {
				String line = String.format("%d, %s, %s", todo.getId(), todo.getTitle(), todo.isCompleted());
				response.getWriter().println(line);
			}
			
			response.flushBuffer();
		}
		
	}
}
