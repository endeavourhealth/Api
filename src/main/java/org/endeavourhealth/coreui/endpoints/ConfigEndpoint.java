package org.endeavourhealth.coreui.endpoints;

import org.endeavourhealth.common.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@Path("/config")
public final class ConfigEndpoint extends AbstractEndpoint {
	private static final Logger LOG = LoggerFactory.getLogger(ConfigEndpoint.class);

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.TEXT_PLAIN)
	@Path("/api")
	public Response getApi(@Context SecurityContext sc, @QueryParam("api") String api) throws Exception {
	    LOG.debug("Getting address for API [" + api + "]");

	    String result = ConfigManager.getConfiguration("api", api);

		return Response
				.ok()
				.entity(result)
				.build();
	}

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/appMenu")
    public Response getAppMenu(@Context SecurityContext sc) throws Exception {
        LOG.debug("Getting app menu config");

        String result = ConfigManager.getConfiguration("appMenu");

        return Response
            .ok()
            .entity(result)
            .build();
    }
}
