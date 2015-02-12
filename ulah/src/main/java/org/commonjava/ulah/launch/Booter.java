package org.commonjava.ulah.launch;

import org.commonjava.ulah.route.MasterRouter;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.http.HttpServer;

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

    private Vertx vertx;

    private MasterRouter router;

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

        synchronized (vertx) {
            try {
                vertx.wait();
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

    public BootStatus start(final BootOptions bootOptions) throws BootException {
        initialize(bootOptions);
        logger.info("Booter running: " + this);

        lifecycleManager = container.instance().select(LifecycleManager.class)
                .get();

        router = container.instance().select(MasterRouter.class).get();
        router.setPrefix(bootOptions.getContextPath());
        vertx = container.instance().select(Vertx.class).get();
        // for ( int i = 0; i < bootOptions.getWorkers(); i++ )
        // {
        status = new BootStatus();
        final HttpServer server = vertx.createHttpServer();
        server.requestHandler(router)
        .listen(bootOptions.getPort(),
                bootOptions.getBind(),
                event -> {
                    if (event.failed()) {
                        logger.error("HTTP server failure:\n\n",
                                event.cause());
                        status.markFailed(BootOptions.ERR_CANT_LISTEN,
                                event.cause());
                        server.close(v -> {
                            logger.info("Shutdown complete.");
                            synchronized (status) {
                                status.notifyAll();
                            }
                        });
                    } else {
                        status.markSuccess();
                        synchronized (status) {
                            status.notifyAll();
                        }
                    }
                });
        // }
        //
        // System.out.printf( "AProx: %s workers listening on %s:%s\n\n",
        // bootOptions.getWorkers(), bootOptions.getBind(),
        System.out.printf("AProx listening on %s:%s\n\n",
                bootOptions.getBind(), bootOptions.getPort());
        while (!status.isSet()) {
            synchronized (status) {
                try {
                    status.wait();
                } catch (final InterruptedException e) {
                    logger.warn("Interrupt received! Quitting.");
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        return status;
    }

}
