package org.endeavourhealth.coreui.endpoints;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.audit.UserAuditDalI;
import org.endeavourhealth.core.database.dal.audit.models.AuditAction;
import org.endeavourhealth.core.database.dal.audit.models.AuditModule;
import org.endeavourhealth.datasharingmanagermodel.models.database.OrganisationEntity;
import org.endeavourhealth.usermanagermodel.models.database.UserRoleEntity;
import org.endeavourhealth.usermanagermodel.models.json.JsonUserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.endeavourhealth.common.security.SecurityUtils.getCurrentUserId;

@Path("/userManager")
@Api(value = "/userManager", description = "Get data related to the user manager")
public class UserManagerEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(SecurityEndpoint.class);
    private static final UserAuditDalI userAudit = DalProvider.factoryUserAuditDal(AuditModule.EdsUiModule.User);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/getRoles")
    @ApiOperation(value = "Returns a list of all roles for the user")
    public Response getRoles(@Context SecurityContext sc,
                             @ApiParam(value = "User Id") @QueryParam("userId") String userId) throws Exception {
        super.setLogbackMarkers(sc);

        userAudit.save(getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Roles", "userId", userId);

        LOG.trace("getRole");

        return getRolesForUser(userId);

    }

    private Response getRolesForUser(String userId) throws Exception {
        List<Object[]> roles = UserRoleEntity.getUserRoles(userId);
        List<JsonUserRole> jsonUserRoles = new ArrayList<>();
        List<String> organisations = new ArrayList<>();

        for (Object[] obj : roles){
            JsonUserRole userRole = new JsonUserRole();
            userRole.setId(obj[0].toString());
            userRole.setUserId(obj[1].toString());
            userRole.setRoleTypeId(obj[2].toString());
            userRole.setRoleTypeName(obj[3].toString());
            userRole.setOrganisationId(obj[4].toString());
            userRole.setUserAccessProfileId(obj[5].toString());
            userRole.setDeleted(obj[6].toString() == "1");
            userRole.setDefault(obj[7].toString() == "1");

            jsonUserRoles.add(userRole);

            organisations.add(obj[4].toString());
        }

        if (roles.size() > 0) {

            Map<String, String> orgNameMap = new HashMap<>();
            List<OrganisationEntity> orgList = OrganisationEntity.getOrganisationsFromList(organisations);
            for (OrganisationEntity org : orgList) {
                orgNameMap.put(org.getUuid(), org.getName());
            }


            for (JsonUserRole re : jsonUserRoles) {
                re.setOrganisationName(orgNameMap.get(re.getOrganisationId()));
            }
        }

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(jsonUserRoles)
                .build();
    }
}
