package org.endeavourhealth.coreui.endpoints;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.endeavourhealth.common.cache.CacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@Path("/cache")
@Api(value = "/cache", description = "Manage cache")
public final class CacheEndpoint extends AbstractEndpoint {
	private static final Logger LOG = LoggerFactory.getLogger(CacheEndpoint.class);

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/list")
	@PermitAll
	@ApiOperation(
			value = "List all caches",
			notes = "List all caches and their current size"
	)
	public Response listCache(@Context SecurityContext sc) throws Exception {

		super.setLogbackMarkers(sc);

		String result = CacheManager.listCaches();

		clearLogbackMarkers();

		return Response
				.ok(result)
				.build();
	}

	@DELETE
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/clear")
	@PermitAll
	@ApiOperation(
			value = "Clear all caches"
	)
	public Response clearCache(@Context SecurityContext sc) throws Exception {

		super.setLogbackMarkers(sc);

		CacheManager.clearCaches();
		LOG.info("Cache manager caches cleared");

		clearLogbackMarkers();

		return Response
				.ok("OK")
				.build();
	}

}