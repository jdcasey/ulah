/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.commonjava.ulah.ui;

import static org.commonjava.ulah.util.HttpUtils.formatDateHeader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.activation.FileTypeMap;
import javax.activation.MimetypesFileTypeMap;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.IOUtils;
import org.commonjava.ulah.conf.UlahConfiguration;
import org.commonjava.ulah.util.ApplicationHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class UIServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    private UlahConfiguration config;

    private final FileTypeMap typeMap = MimetypesFileTypeMap
            .getDefaultFileTypeMap();

    @Override
    protected void service(final HttpServletRequest request,
            final HttpServletResponse response) throws ServletException,
            IOException {
        if (config == null) {
            config = CDI.current().select(UlahConfiguration.class).get();
        }

        String path;
        try {
            path = new URI(request.getRequestURI()).getPath();
        } catch (final URISyntaxException e) {
            logger.error("Cannot parse request URI", e);
            response.setStatus(400);
            return;
        }

        final String method = request.getMethod().toUpperCase();

        logger.info("{} {}", method, path);

        switch (method) {
        case "GET":
        case "HEAD": {
            if (path == null) {
                logger.debug("null path. Using /index.html");
                path = "index.html";
            } else if (path.endsWith("/")) {
                path += "index.html";
                logger.debug("directory path. Using {}", path);
            }

            if (path.startsWith("/")) {
                logger.debug("Trimming leading '/' from path");
                path = path.substring(1);
            }

            if (path.startsWith("cp/")) {
                logger.debug("Handling request for classpath resource.");
                path = path.substring(3);
                final URL resource = Thread.currentThread()
                        .getContextClassLoader().getResource(path);

                sendURL(response, resource, method);
                return;
            }

            final File uiDir = config.getUIDir();
            logger.info("UI basedir: '{}'", uiDir);

            final File resource = new File(uiDir, path);
            logger.info("Trying to send file: " + resource);
            sendFile(response, resource, method);
            return;
        }
        default: {
            logger.error("cannot handle request for method: {}", method);
            response.setStatus(Status.BAD_REQUEST.getStatusCode());
        }
        }
    }

    private void sendURL(final HttpServletResponse response,
            final URL resource, final String method) {
        logger.debug("Checking for existence of: '{}'", resource);
        if (resource != null) {
            byte[] data = null;
            try {
                data = IOUtils.toByteArray(resource);
            } catch (final IOException e) {
                logger.error(String.format(
                        "Failed to read data from resource: %s. Reason: %s",
                        resource, e.getMessage()), e);
                try {
                    response.sendError(
                            Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                            "Failed to read resource: " + resource);
                } catch (final IOException eResp) {
                    logger.warn("Failed to send error response to client: "
                            + eResp.getMessage(), eResp);
                }
            }

            if (data == null) {
                return;
            }

            if (method == "GET") {
                logger.debug("sending file");
                OutputStream outputStream = null;
                try {
                    outputStream = response.getOutputStream();

                    outputStream.write(data);
                    outputStream.flush();
                } catch (final IOException e) {
                    logger.error(
                            String.format(
                                    "Failed to write to response output stream. Reason: %s",
                                    e.getMessage()), e);
                    try {
                        response.sendError(
                                Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                                "Failed to write response");
                    } catch (final IOException eResp) {
                        logger.warn("Failed to send error response to client: "
                                + eResp.getMessage(), eResp);
                    }
                }
            } else {
                logger.debug("sending OK");
                response.setStatus(Status.OK.getStatusCode());
                response.addHeader(ApplicationHeader.content_type.key(),
                        typeMap.getContentType(resource.toExternalForm()));
                response.addHeader(ApplicationHeader.content_length.key(),
                        Long.toString(data.length));
            }
        } else {
            logger.debug("sending 404");
            response.setStatus(Status.NOT_FOUND.getStatusCode());
        }
    }

    private void sendFile(final HttpServletResponse response,
            final File resource, final String method) {
        logger.info("Checking for existence of: '{}'", resource);
        if (resource.exists()) {
            if (method == "GET") {
                logger.debug("sending file");
                response.addHeader(ApplicationHeader.last_modified.key(),
                        formatDateHeader(resource.lastModified()));
                InputStream inputStream = null;
                OutputStream outputStream = null;
                try {
                    inputStream = new FileInputStream(resource);
                    outputStream = response.getOutputStream();

                    IOUtils.copy(inputStream, outputStream);
                    outputStream.flush();
                } catch (final IOException e) {
                    logger.error(
                            String.format(
                                    "Failed to transfer requested resource: %s. Reason: %s",
                                    resource, e.getMessage()), e);
                    try {
                        response.sendError(
                                Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                                "Failed to write response");
                    } catch (final IOException eResp) {
                        logger.warn("Failed to send error response to client: "
                                + eResp.getMessage(), eResp);
                    }
                } finally {
                    IOUtils.closeQuietly(inputStream);
                }
            } else {
                logger.debug("sending OK");
                // TODO: set headers for content info...
                response.setStatus(Status.OK.getStatusCode());
                response.addHeader(ApplicationHeader.last_modified.key(),
                        formatDateHeader(resource.lastModified()));

                response.addHeader(ApplicationHeader.content_type.key(),
                        typeMap.getContentType(resource));
                response.addHeader(ApplicationHeader.content_length.key(),
                        Long.toString(resource.length()));
            }
        } else {
            logger.debug("sending 404");
            response.setStatus(Status.NOT_FOUND.getStatusCode());
        }
    }

}
