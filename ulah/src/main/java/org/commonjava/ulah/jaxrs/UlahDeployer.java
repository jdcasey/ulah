package org.commonjava.ulah.jaxrs;

import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.ServletInfo;
import io.undertow.servlet.util.ImmediateInstanceFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.Servlet;
import javax.ws.rs.core.Application;

import org.commonjava.ulah.jaxrs.helper.CdiInjectorFactoryImpl;
import org.commonjava.ulah.jaxrs.helper.RequestScopeListener;
import org.commonjava.ulah.jaxrs.helper.ResteasyJsonProvider;
import org.commonjava.ulah.rest.RestResources;
import org.commonjava.ulah.ui.UIServlet;
import org.jboss.resteasy.plugins.server.servlet.HttpServlet30Dispatcher;
import org.jboss.resteasy.spi.ResteasyDeployment;

public class UlahDeployer extends Application {
    private static Set<Class<?>> PROVIDER_CLASSES;

    static {
        final Set<Class<?>> providers = new HashSet<>();
        providers.add(ResteasyJsonProvider.class);

        PROVIDER_CLASSES = providers;
    }

    @Inject
    private Instance<RestResources> resources;

    @Inject
    private UIServlet ui;

    private Set<Class<?>> resourceClasses;

    private Set<Class<?>> providerClasses = PROVIDER_CLASSES;

    protected UlahDeployer() {
    }

    public UlahDeployer(final Set<Class<?>> resourceClasses, final UIServlet ui) {
        this.resourceClasses = resourceClasses;
        this.ui = ui;
        providerClasses = Collections.emptySet();
    }

    @PostConstruct
    public void cdiInit() {
        providerClasses = Collections.emptySet();
        resourceClasses = new HashSet<>();
        for (final RestResources restResources : resources) {
            resourceClasses.add(restResources.getClass());
        }
    }

    public DeploymentInfo getDeployment(final String contextRoot) {
        final ResteasyDeployment deployment = new ResteasyDeployment();

        deployment.setApplication(this);
        deployment.setInjectorFactoryClass(CdiInjectorFactoryImpl.class
                .getName());

        final ServletInfo resteasyServlet = Servlets
                .servlet("REST", HttpServlet30Dispatcher.class)
                .setAsyncSupported(true).setLoadOnStartup(1)
                .addMapping("/api*").addMapping("/api/*");

        final ServletInfo uiServlet = Servlets.servlet("UI", UIServlet.class)
                .setAsyncSupported(true).setLoadOnStartup(2)
                .addMapping("/.html").addMapping("/").addMapping("/js/*")
                .addMapping("/css/*").addMapping("/partials/*")
                .addMapping("/ui-addons/*");

        uiServlet.setInstanceFactory(new ImmediateInstanceFactory<Servlet>(ui));

        final DeploymentInfo di = new DeploymentInfo()
        .addListener(Servlets.listener(RequestScopeListener.class))
        .setContextPath(contextRoot)
        .addServletContextAttribute(ResteasyDeployment.class.getName(),
                deployment).addServlet(resteasyServlet)
                .addServlet(uiServlet)
                .setClassLoader(ClassLoader.getSystemClassLoader());

        return di;
    }

    @Override
    public Set<Class<?>> getClasses() {
        final Set<Class<?>> classes = new HashSet<>(resourceClasses);
        classes.addAll(providerClasses);
        return classes;
    }

}
