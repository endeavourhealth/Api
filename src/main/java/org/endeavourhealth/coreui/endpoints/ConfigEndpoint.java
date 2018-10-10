package org.endeavourhealth.coreui.endpoints;

import io.swagger.annotations.Api;
import io.swagger.annotations.Authorization;
import org.endeavourhealth.common.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@Api(value = "Config", authorizations = {
		@Authorization(value="oauth", scopes = {})
})
@Path("/config")
public final class ConfigEndpoint extends AbstractEndpoint {
	private static final Logger LOG = LoggerFactory.getLogger(ConfigEndpoint.class);

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.TEXT_PLAIN)
	@Path("/api")
	public Response getAudit(@Context SecurityContext sc, @QueryParam("api") String api) throws Exception {
	    LOG.debug("Getting address for API [" + api + "]");

	    String result = ConfigManager.getConfiguration("api", api);

		return Response
				.ok()
				.entity(result)
				.build();
	}
}
