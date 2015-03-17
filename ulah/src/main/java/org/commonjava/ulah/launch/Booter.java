package org.commonjava.ulah.launch;

import io.undertow.Undertow;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;

import javax.servlet.ServletException;

import org.commonjava.ulah.jaxrs.UlahDeployer;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Booter {

    public static void main(String[] args) {
        BootOptions options = null;
        try {
            options = BootOptions.loadFromSysprops();
        } catch (BootException e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
            System.exit(BootOptions.ERR_LOAD_FROM_SYSPROPS);
        }

        try {
            options.parseArgs(args);
        } catch (BootException e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
            System.exit(BootOptions.ERR_PARSE_ARGS);
        }

        BootStatus status = null;
        try {
            status = new Booter().runAndWait(options);
        } catch (BootException e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
            System.exit(BootOptions.ERR_STARTING);
        }

        if (status.isFailed()) {
            status.getError().printStackTrace();
            System.err.println(status.getError().getMessage());
            System.exit(status.getExitCode());
        }
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private BootOptions options;

    private Weld weld;

    private WeldContainer container;

    private Undertow server;

    private BootStatus status;

    private LifecycleManager lifecycleManager;

    private void initialize(final BootOptions options) throws BootException {
        this.options = options;

        try {
            options.setSystemProperties();

            weld = new Weld();
            container = weld.initialize();
        } catch (final RuntimeException e) {
            throw new BootException("Failed to initialize Booter: "
                    + e.getMessage(), e);
        }
    }

    public BootStatus runAndWait(final BootOptions bootOptions)
            throws BootException {
        start(bootOptions);

        logger.info("Setting up shutdown hook...");
        lifecycleManager.installShutdownHook();

        synchronized (server) {
            try {
                server.wait();
            } catch (final InterruptedException e) {
                e.printStackTrace();
                logger.info("AProx exiting");
            }
        }

        return status;
    }

    public WeldContainer getContainer() {
        return container;
    }

    public BootOptions getBootOptions() {
        return options;
    }

    public boolean deploy() {
        boolean started;
        final UlahDeployer deployer = container.instance()
                .select(UlahDeployer.class).get();

        final DeploymentInfo di = deployer.getDeployment(
                options.getContextPath()).setContextPath("/");

        final DeploymentManager dm = Servlets.defaultContainer().addDeployment(
                di);
        dm.deploy();

        status = new BootStatus();
        try {
            server = Undertow.builder().setHandler(dm.start())
                    .addHttpListener(options.getPort(), options.getBind())
                    .build();

            server.start();
            status.markSuccess();
            started = true;

            System.out.printf("uLah server listening on %s:%s\n\n",
                    options.getBind(), options.getPort());

        } catch (ServletException | RuntimeException e) {
            status.markFailed(BootOptions.ERR_CANT_LISTEN, e);
            started = false;
        }

        return started;
    }

    public BootStatus start(final BootOptions bootOptions) throws BootException {
        initialize(bootOptions);
        logger.info("Booter running: " + this);

        startLifecycle();

        deploy();
        return status;
    }

    public void startLifecycle() {
        lifecycleManager = container.instance().select(LifecycleManager.class)
                .get();
    }

    public void stop() {
        if (container != null) {
            server.stop();
            lifecycleManager.stop();
            weld.shutdown();
        }
    }
}
