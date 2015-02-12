package org.commonjava.ulah.rest;

import java.util.Collections;

import javax.inject.Inject;

import org.commonjava.ulah.db.TransactionTagDataManager;
import org.commonjava.vertx.vabr.anno.Handles;
import org.commonjava.vertx.vabr.anno.Route;
import org.commonjava.vertx.vabr.anno.Routes;
import org.commonjava.vertx.vabr.types.Method;
import org.commonjava.vertx.vabr.util.Respond;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.http.HttpServerRequest;

import com.fasterxml.jackson.databind.ObjectMapper;

@Handles("/tags")
public class TagResources {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    private TransactionTagDataManager tags;

    @Inject
    private ObjectMapper mapper;

    protected TagResources() {
    }

    public TagResources(TransactionTagDataManager tags, ObjectMapper mapper) {
        this.tags = tags;
        this.mapper = mapper;
    }

    @Routes({ @Route(path = "/all", method = Method.GET, routeKey = "all"),
            @Route(path = "/", method = Method.GET, routeKey = "base") })
    public void list(HttpServerRequest req) {
        req.endHandler(v -> {
            tags.getAllTags(tgs -> {
                try {
                    Respond.to(req)
                            .ok()
                            .jsonEntity(Collections.singletonMap("items", tgs),
                                    mapper).send();
                } catch (Exception e) {
                    logger.error(String.format(
                            "Failed to retrieve tag listing: %s",
                            e.getMessage()), e);
                    Respond.to(req).serverError(e, true).send();
                }
            });
        });
    }

}
