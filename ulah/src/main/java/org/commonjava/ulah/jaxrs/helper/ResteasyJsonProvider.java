package org.commonjava.ulah.jaxrs.helper;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;

@Provider
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class ResteasyJsonProvider implements ContextResolver<ObjectMapper> {
    @Inject
    private ObjectMapper mapper;

    @Override
    public ObjectMapper getContext(final Class<?> type) {
        return mapper;
    }

}
