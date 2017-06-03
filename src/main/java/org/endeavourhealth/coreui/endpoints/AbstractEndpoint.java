package org.endeavourhealth.coreui.endpoints;

import org.endeavourhealth.common.security.RoleUtils;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.coreui.framework.exceptions.BadRequestException;
import org.keycloak.KeycloakPrincipal;
import org.slf4j.MDC;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import java.util.UUID;

public abstract class AbstractEndpoint {

    private static final String MDC_MARKER_UUID = "UserUuid";

    @Context
    protected SecurityContext securityContext;

    /**
     * used to set LogBack to include the user UUID in all logging
     */
    protected void setLogbackMarkers(SecurityContext sc) {
        UUID userUuid = SecurityUtils.getCurrentUserId(sc);
        if (userUuid != null) {
            MDC.put(MDC_MARKER_UUID, userUuid.toString());
        }
    }
    public static void clearLogbackMarkers() {
        MDC.remove(MDC_MARKER_UUID);
    }


    protected String getNhsNumberFromSession(SecurityContext sc) throws Exception {
        // TODO : KeyCloakPrincipal - how to get NHS Number?
        KeycloakPrincipal up = (KeycloakPrincipal)sc.getUserPrincipal();
        if (up == null) {
            return null;
        }

        return "7438549224";
    }

    protected UUID getOrganisationUuidFromToken(SecurityContext sc) throws Exception {
        //an authenticated user MUST have a EndUser UUID, but they may not have an organisation selected yet
        //TODO - need to work out ORG UUID using keycloak??
        UUID orgUuid = UUID.fromString("b6ff900d-8fcd-43d8-af37-5db3a87a6ef6");

        if (orgUuid == null) {
            throw new BadRequestException("Organisation must be selected before performing any actions");
        }
        return orgUuid;
    }

    protected boolean isAdminFromSession(SecurityContext sc) throws Exception {
        return RoleUtils.isEDSAdmin(sc);       // TODO: should this be ADMIN or SUPERUSER??
    }

}
