package org.commonjava.ulah.route;

import java.util.Arrays;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.commonjava.vertx.vabr.MultiApplicationRouter;

@ApplicationScoped
public class MasterRouter extends MultiApplicationRouter {

    @Inject
    private RESTRouter restRouter;

    @Inject
    private UIRouter uiRouter;

    protected MasterRouter() {
    }

    public MasterRouter(RESTRouter rest, UIRouter ui) {
        restRouter = rest;
        uiRouter = ui;
        initialize();
    }

    @PostConstruct
    public void initialize() {
        super.bindRouters(Arrays.asList(restRouter, uiRouter));
    }

}
