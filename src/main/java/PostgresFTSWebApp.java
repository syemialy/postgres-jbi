import com.jetbi.postgresftsapp.spring.config.PostgresFtsSpringMVCConfig;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

/**
 * Web application provides REST services to perform FTS searches on a PostgreSQL database.
 * The application is Spring based and this class basically replaces standard web.xml file.
 * <p/>
 * <p/>
 * Created by Sergei.Emelianov on 19.03.2016.
 */
public class PostgresFTSWebApp implements WebApplicationInitializer {

    /**
     * Injects Spring standard DispatcherServlet into servlet context of the application. Registers
     * standard RequestCOntext listener
     *
     * @param servletContext
     * @throws ServletException
     */
    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        WebApplicationContext wctx = getContext();

        servletContext.addListener(new ContextLoaderListener(wctx));
        ServletRegistration.Dynamic dispatcher = servletContext.addServlet("DispatcherServlet", new DispatcherServlet(wctx));
        dispatcher.setLoadOnStartup(1);
        dispatcher.addMapping("/*");

        //support bean scopes
        servletContext.addListener(RequestContextListener.class);
    }

    /**
     * App context initialization
     *
     * @return
     */
    private WebApplicationContext getContext() {
        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.setConfigLocation(PostgresFtsSpringMVCConfig.class.getCanonicalName());
        return context;
    }

}
