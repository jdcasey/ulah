package org.commonjava.ulah.conf;

import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.commonjava.web.config.ConfigurationException;
import org.commonjava.web.config.DefaultConfigurationListener;
import org.commonjava.web.config.annotation.ConfigName;
import org.commonjava.web.config.io.ConfigFileUtils;
import org.commonjava.web.config.io.SingleSectionConfigReader;

public class UlahConfiguration extends DefaultConfigurationListener {

    private static final String DEFAULT_UI_DIR = "ui";

    private File uiDir;

    private File homeDir;

    private File configFile;

    public File getUIDir() {
        return uiDir == null ? new File(homeDir, DEFAULT_UI_DIR) : uiDir;
    }

    @ConfigName("ui.dir")
    public void setUIDir(File uiDir) {
        this.uiDir = uiDir;
    }

    public File getHomeDir() {
        return homeDir;
    }

    public File getConfigFile() {
        return configFile;
    }

    public void load(String config, String ulahHome)
            throws ConfigurationException {

        configFile = new File(config);
        homeDir = new File(ulahHome);

        InputStream stream = null;
        try {
            stream = ConfigFileUtils.readFileWithIncludes(config);
            new SingleSectionConfigReader(this).loadConfiguration(stream);
        } catch (final IOException e) {
            throw new ConfigurationException(
                    "Cannot open configuration file: {}. Reason: {}", e,
                    config, e.getMessage());
        } finally {
            closeQuietly(stream);
        }
    }

}
