package org.commonjava.ulah.launch;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.interpolation.InterpolationException;
import org.codehaus.plexus.interpolation.PropertiesBasedValueSource;
import org.codehaus.plexus.interpolation.StringSearchInterpolator;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

public class BootOptions {

    public static final String BIND_PROP = "bind";

    public static final String PORT_PROP = "port";

    public static final String CONTEXT_PATH_PROP = "context-path";

    public static final String DEFAULT_BIND = "0.0.0.0";

    public static final int DEFAULT_PORT = 8080;

    public static final String BOOT_DEFAULTS_PROP = "boot.properties";

    public static final String ULAH_HOME_PROP = "ulah.home";

    public static final String ULAH_HOME_ENVAR = "ULAH_HOME";

    public static final int ERR_LOAD_FROM_SYSPROPS = 1;

    public static final int ERR_PARSE_ARGS = 2;

    public static final int ERR_CANT_LISTEN = 3;

    public static final int ERR_STARTING = 4;

    public static final int ERR_LOAD_CONFIG = 5;

    @Option(name = "-h", aliases = { "--help" }, usage = "Print this and exit")
    private boolean help;

    @Option(name = "-i", aliases = { "--interface", "--bind", "--listen" }, usage = "Bind to a particular IP address (default: 0.0.0.0, or all available)")
    private String bind;

    @Option(name = "-p", aliases = { "--port" }, usage = "Use different port (default: 8080)")
    private Integer port;

    @Option(name = "-c", aliases = { "--context-path" }, usage = "Specify a root context path for the app")
    private String contextPath;

    private StringSearchInterpolator interp;

    private Properties bootProps;

    private String homeDir;

    @Option(name = "-f", aliases = { "--config" }, usage = "Specify a different configuration file (defaults to ${ulah.home}/etc/main.conf or $ULAH_HOME/etc/main.conf)")
    private String config;

    public static final BootOptions loadFromSysprops() throws BootException {
        final String bootDef = System.getProperty(BOOT_DEFAULTS_PROP);
        File bootDefaults = null;
        if (bootDef != null) {
            bootDefaults = new File(bootDef);
        }

        try {
            String home = System.getProperty(ULAH_HOME_PROP);

            if (home == null) {
                home = System.getenv(ULAH_HOME_ENVAR);
            }

            if (home == null) {
                home = new File(".").getCanonicalPath();
            }

            return new BootOptions(bootDefaults, home);
        } catch (final IOException e) {
            throw new BootException(
                    "ERROR LOADING BOOT DEFAULTS: %s.\nReason: %s\n\n", e,
                    bootDefaults, e.getMessage());
        } catch (final InterpolationException e) {
            throw new BootException(
                    "ERROR RESOLVING BOOT DEFAULTS: %s.\nReason: %s\n\n", e,
                    bootDefaults, e.getMessage());
        }
    }

    public void setSystemProperties() {
        final Properties properties = System.getProperties();

        properties.setProperty(ULAH_HOME_PROP, homeDir);
        System.setProperties(properties);
    }

    public BootOptions() {

    }

    public BootOptions(final String aproxHome) throws IOException,
    InterpolationException {
        this(null, aproxHome);
    }

    public BootOptions(final File bootDefaults, final String home)
            throws IOException, InterpolationException {
        homeDir = home;
        bootProps = new Properties();

        if (bootDefaults != null && bootDefaults.exists()) {
            FileInputStream stream = null;
            try {
                stream = new FileInputStream(bootDefaults);

                bootProps.load(stream);
            } finally {
                IOUtils.closeQuietly(stream);
            }
        }

        if (bind == null) {
            bind = resolve(bootProps.getProperty(BIND_PROP, DEFAULT_BIND));
        }

        if (port == null) {
            port = Integer.parseInt(resolve(bootProps.getProperty(PORT_PROP,
                    Integer.toString(DEFAULT_PORT))));
        }

        contextPath = bootProps.getProperty(CONTEXT_PATH_PROP, contextPath);
    }

    public String resolve(final String value) throws InterpolationException {
        if (value == null || value.trim().length() < 1) {
            return null;
        }

        if (bootProps == null) {
            if (homeDir == null) {
                return value;
            } else {
                bootProps = new Properties();
            }
        }

        bootProps.setProperty("aprox.home", homeDir);

        if (interp == null) {
            interp = new StringSearchInterpolator();
            interp.addValueSource(new PropertiesBasedValueSource(bootProps));
        }

        return interp.interpolate(value);
    }

    public boolean isHelp() {
        return help;
    }

    public String getBind() {
        return bind;
    }

    public int getPort() {
        return port;
    }

    public BootOptions setHelp(final boolean help) {
        this.help = help;
        return this;
    }

    public BootOptions setBind(final String bind) {
        this.bind = bind;
        return this;
    }

    public BootOptions setPort(final int port) {
        this.port = port;
        return this;
    }

    public String getContextPath() {
        if (contextPath == null) {
            return null;
        }

        if (!contextPath.startsWith("/")) {
            contextPath = "/" + contextPath;
        }

        return contextPath;
    }

    public void setContextPath(final String contextPath) {
        this.contextPath = contextPath;
    }

    public boolean parseArgs(final String[] args) throws BootException {
        final CmdLineParser parser = new CmdLineParser(this);
        boolean canStart = true;
        try {
            parser.parseArgument(args);
        } catch (final CmdLineException e) {
            throw new BootException("Failed to parse command-line args: %s", e,
                    e.getMessage());
        }

        if (isHelp()) {
            printUsage(parser, null);
            canStart = false;
        }

        return canStart;
    }

    public static void printUsage(final CmdLineParser parser,
            final CmdLineException error) {
        if (error != null) {
            System.err.println("Invalid option(s): " + error.getMessage());
            System.err.println();
        }

        System.err.println("Usage: $0 [OPTIONS] [<target-path>]");
        System.err.println();
        System.err.println();
        // If we are running under a Linux shell COLUMNS might be available for
        // the width
        // of the terminal.
        parser.setUsageWidth(System.getenv("COLUMNS") == null ? 100 : Integer
                .valueOf(System.getenv("COLUMNS")));
        parser.printUsage(System.err);
        System.err.println();
    }

    public String getHomeDir() {
        return homeDir;
    }

    public void setHomeDir(final String home) {
        homeDir = home;
    }

    public void setPort(final Integer port) {
        this.port = port;
    }

    public String getConfig() {
        return config == null ? new File(getHomeDir(), "etc/main.conf")
        .getPath() : config;
    }

}
