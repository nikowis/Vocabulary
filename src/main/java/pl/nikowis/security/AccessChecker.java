package pl.nikowis.security;

import com.google.gwt.thirdparty.guava.common.collect.Lists;
import com.vaadin.navigator.View;
import com.vaadin.spring.access.ViewAccessControl;
import com.vaadin.spring.access.ViewInstanceAccessControl;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.ui.UI;
import org.springframework.beans.factory.annotation.Autowired;
import pl.nikowis.entities.User;
import pl.nikowis.services.SessionService;
import pl.nikowis.ui.HomeView;
import pl.nikowis.ui.LoginView;
import pl.nikowis.ui.RegisterView;

import java.util.List;

/**
 * Class for checking user access for specific views.
 * Runs on any view instantiation.
 * Created by nikowis on 2016-08-14.
 *
 * @author nikowis
 */
@SpringComponent
public class AccessChecker implements ViewInstanceAccessControl {

    @Autowired
    private SessionService sessionService;

    private final List<String> permitAllViews = Lists.newArrayList(
            LoginView.VIEW_NAME,
            RegisterView.VIEW_NAME
    );

    private final List<String> userViews = Lists.newArrayList();

    private final List<String> adminViews = Lists.newArrayList();

    private final List<String> authenticatedViews = Lists.newArrayList(
            HomeView.VIEW_NAME
    );

    @Override
    public boolean isAccessGranted(UI ui, String s, View view) {

        User user = sessionService.getUser();
        s = s.replaceFirst("View", "");

        if (permitAllViews.contains(s)) {
            return true;
        }
        if (user ==null) {
            return false;
        }
        if (authenticatedViews.contains(s)) {
            return true;
        } else if (userViews.contains(s)) {

        } else if (adminViews.contains(s)) {

        }

        return false;
    }

}