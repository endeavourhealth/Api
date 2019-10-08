package org.endeavourhealth.coreui.endpoints;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.endeavourhealth.common.security.OrgRoles;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.coreui.framework.config.ConfigService;
import org.keycloak.KeycloakSecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.net.URI;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/security")
public final class SecurityEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(SecurityEndpoint.class);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/info")
    public Response userInfo(@Context SecurityContext sc) throws Exception {

        super.setLogbackMarkers(sc);

        KeycloakSecurityContext keycloakSecurityContext = SecurityUtils.getKeycloakSecurityContext(sc);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(keycloakSecurityContext.getToken()))
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/info/organisationRoles")
    public Response userInfoOrganisationRoles(@Context SecurityContext sc,
                                              @Context ContainerRequestContext containerRequestContext,
                                              @HeaderParam(value = OrgRoles.HEADER_ORGANISATION_ID) String headerOrgId) throws Exception {

        super.setLogbackMarkers(sc);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        String organisationId = SecurityUtils.getCurrentUserOrganisationId(containerRequestContext);
        Map<String,Object> response = new HashMap<>();
        response.put("organisationId", organisationId);
        List<String> orgRoles = SecurityUtils.getOrganisationRoles(sc, organisationId);
        response.put("orgRoles", orgRoles);

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response))
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/info/organisations")
    public Response userInfoOrganisations(@Context SecurityContext sc,
                                          @Context ContainerRequestContext containerRequestContext) throws Exception {

        super.setLogbackMarkers(sc);

        // TODO: complete implementation, currently only based on information in Keycloak
        Map<String, List<String>> orgRoles = SecurityUtils.getOrganisationRoles(sc);

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(orgRoles)
                .build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/logoff")
    public Response logoff(@Context SecurityContext sc) throws Exception {
        super.setLogbackMarkers(sc);
        LOG.trace("Logoff");

        String redirectUrl = URLEncoder.encode(ConfigService.instance().getAppConfig().getAppUrl() + "/api/user/details", "UTF-8");

        String url = String.format(ConfigService.instance().getAuthConfig().getAuthServerUrl() + "/realms/%s/protocol/openid-connect/logout?redirect_uri=%s",
                SecurityUtils.getKeycloakSecurityContext(sc).getRealm(), redirectUrl);

        clearLogbackMarkers();

        return Response
                .seeOther(new URI(url))
                .build();
    }
}
