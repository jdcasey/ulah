package org.commonjava.ulah.rest;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.commonjava.ulah.db.TransactionTagDataManager;
import org.commonjava.ulah.dto.TagListDTO;

@Path("/tags")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TagResources implements RestResources {

    @Inject
    private TransactionTagDataManager tags;

    protected TagResources() {
    }

    public TagResources(TransactionTagDataManager tags) {
        this.tags = tags;
    }

    @Path("/all")
    @GET
    public TagListDTO allListing() {
        return listing();
    }

    @GET
    public TagListDTO rootListing() {
        return listing();
    }

    private TagListDTO listing() {
        return new TagListDTO(tags.getAllTags());
    }

}
