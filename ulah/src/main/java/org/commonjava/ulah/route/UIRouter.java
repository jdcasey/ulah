package org.commonjava.ulah.route;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;

import org.commonjava.vertx.vabr.ApplicationRouter;
import org.commonjava.vertx.vabr.bind.filter.FilterCollection;
import org.commonjava.vertx.vabr.bind.route.RouteCollection;
import org.commonjava.vertx.vabr.helper.RequestHandler;

@ApplicationScoped
@Named("ui")
public class UIRouter extends ApplicationRouter implements RequestHandler {

    @Inject
    @UIApp
    private Instance<RequestHandler> handlers;

    @Inject
    @UIApp
    private Instance<RouteCollection> routes;

    @Inject
    @UIApp
    private Instance<FilterCollection> filters;

    @PostConstruct
    public void initialize() {
        super.bindHandlers(handlers);
        super.bindRouteCollections(routes);
        super.bindFilterCollections(filters);
    }

}
