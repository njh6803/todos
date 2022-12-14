package todoapp.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ObjectToStringHttpMessageConverter;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.ContentNegotiatingViewResolver;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import com.fasterxml.jackson.databind.ser.std.StdKeySerializers.Default;

import todoapp.commons.web.servlet.ExecutionTimeHandlerInterceptor;
import todoapp.commons.web.servlet.LoggingHandlerInterceptor;
import todoapp.commons.web.view.CommaSeparatedValuesView;
import todoapp.core.todos.domain.Todo;
import todoapp.core.user.domain.ProfilePictureStorage;
import todoapp.security.UserSessionRepository;
import todoapp.security.web.servlet.RolesVerifyHandlerInterceptor;
import todoapp.security.web.servlet.UserSessionFilter;
import todoapp.security.web.servlet.UserSessionHandlerMethodArgumentResolver;
import todoapp.web.TodoController.TodoCsvView;

/**
 * Spring Web MVC ??????
 *
 * @author springrunner.kr@gmail.com
 */

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private UserSessionRepository userSessionRepository;
	
	@Autowired
	private ProfilePictureStorage profilePictureStorage;
	
	
	
	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		resolvers.add(new UserSessionHandlerMethodArgumentResolver(userSessionRepository));
	}



	public WebMvcConfiguration() {
		logger.debug("????????? MVC ???????????? ???????????????.");
	}
	
	

    @Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		// ????????? ???????????? ??????, ?????? ????????? ????????? ??? ??????
		
    	// http://localhost:8080/assets/css/todos.css
    	
    	// ????????? ???????????? ???????????? ?????? ?????? ??????
    	// registry.addResourceHandler("assets/**").addResourceLocations("assets/");
    	
    	// ?????? ???????????? ?????? ?????? ??????
    	// registry
    	// .addResourceHandler("/assets/**")
    	// .addResourceLocations("file:/Users/User/Desktop/STS-workspace/todos/files/assets/");
    	
    	// ??????????????? ???????????? ?????? ?????? ??????
    	// registry.addResourceHandler("assets/**").addResourceLocations("classpath:assets/");
    	
		/*
		 * registry.addResourceHandler("/assets/**") .addResourceLocations( "assets/",
		 * "file:/Users/User/Desktop/STS-workspace/todos/files/assets/",
		 * "classpath:assets/");
		 */
	}

	@Override
	public void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> handlers) {
		handlers.add(new UserController.ProfilePictureReturnValueHandler(profilePictureStorage));
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		// ???????????? ???????????? ????????????.
		registry.addInterceptor(new LoggingHandlerInterceptor());
		registry.addInterceptor(new ExecutionTimeHandlerInterceptor());
		registry.addInterceptor(new RolesVerifyHandlerInterceptor());
	}



	@Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
		// registry.viewResolver(new TodoController.TodoCsvViewResolver());
		
        // registry.enableContentNegotiation(new CommaSeparatedValuesView());
        // ?????? ?????? ?????? ????????????, ?????????????????? ????????? ContentNegotiatingViewResolver ????????? ????????????.
    }
	
	
	
	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		DefaultConversionService conversionService = new DefaultConversionService();
		conversionService.addConverter(new Converter<Todo, String>() {

			@Override
			public String convert(Todo source) {
				return source.toString();
			}
			
		});
		converters.add(new ObjectToStringHttpMessageConverter(conversionService));
	}



//	@Bean(name = "todos")
//	public TodoCsvView todoCsvView() {
//		return new TodoCsvView();
//	}

//	@Bean(name = "todos")
//	public CommaSeparatedValuesView todoCsvView() {
//		return new CommaSeparatedValuesView();
//	}
	
	@Bean
	public FilterRegistrationBean<CommonsRequestLoggingFilter> commonRequestLogginFilter() {
		CommonsRequestLoggingFilter commonsRequestLoggingFilter = new CommonsRequestLoggingFilter();
		commonsRequestLoggingFilter.setIncludeHeaders(true);
		commonsRequestLoggingFilter.setIncludePayload(true);
		commonsRequestLoggingFilter.setIncludeClientInfo(true);
		FilterRegistrationBean<CommonsRequestLoggingFilter> filter = new FilterRegistrationBean<>();
		filter.setFilter(commonsRequestLoggingFilter);
		filter.setUrlPatterns(Collections.singletonList("/*"));
		
		return filter;
	}

	@Bean
	public FilterRegistrationBean<UserSessionFilter> userSeesionFilter(){
		UserSessionFilter userSessionFilter = new UserSessionFilter(userSessionRepository);
		
		FilterRegistrationBean<UserSessionFilter> filter = new FilterRegistrationBean<>();
		filter.setFilter(userSessionFilter);
		filter.setUrlPatterns(Collections.singletonList("/*"));
		
		return filter;
	}
	
    /**
     * ?????????????????? ????????? ContentNegotiatingViewResolver??? ????????? ???????????? ????????? ????????????
     */
	@Configuration
    public static class ContentNegotiationCustomizer {

		@Autowired
        public void configurer(ContentNegotiatingViewResolver viewResolver) {
			List<View> defaultViews = new ArrayList<>(viewResolver.getDefaultViews());
			defaultViews.add(new CommaSeparatedValuesView());
			defaultViews.add(new MappingJackson2JsonView());
            // viewResolver.getDefaultViews().add(new CommaSeparatedValuesView());
			
			viewResolver.setDefaultViews(defaultViews);
        }

    }

}
