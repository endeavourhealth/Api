package org.endeavourhealth.coreui.endpoints;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.audit.UserAuditDalI;
import org.endeavourhealth.core.database.dal.audit.models.AuditAction;
import org.endeavourhealth.core.database.dal.audit.models.AuditModule;
import org.endeavourhealth.datasharingmanagermodel.models.database.OrganisationEntity;
import org.endeavourhealth.datasharingmanagermodel.models.database.ProjectEntity;
import org.endeavourhealth.usermanagermodel.models.database.UserProjectEntity;
import org.endeavourhealth.usermanagermodel.models.json.JsonUserProject;
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
    @Path("/getProjects")
    @ApiOperation(value = "Returns a list of all roles for the user")
    public Response getProjects(@Context SecurityContext sc,
                             @ApiParam(value = "User Id") @QueryParam("userId") String userId) throws Exception {
        super.setLogbackMarkers(sc);

        userAudit.save(getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Roles", "userId", userId);

        LOG.trace("getRole");

        return getProjectsForUser(userId);

    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/setDefaultProject")
    @ApiOperation(value = "Returns a list of all roles for the user")
    public Response setDefaultProject(@Context SecurityContext sc,
                             @ApiParam(value = "User Id the role is being changed for") @QueryParam("userId") String userId,
                             @ApiParam(value = "Id of the new default project") @QueryParam("defaultProjectId") String defaultProjectId,
                             @ApiParam(value = "User project id of the user making the change") @QueryParam("userProjectId") String userProjectId) throws Exception {
        super.setLogbackMarkers(sc);

        userAudit.save(getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Roles", "userId", userId);

        LOG.trace("getRole");

        return changeDefaultProject(userId, defaultProjectId, userProjectId);

    }

    private Response getProjectsForUser(String userId) throws Exception {
        List<Object[]> roles = UserProjectEntity.getUserProjects(userId);
        List<JsonUserProject> jsonUserProjects = new ArrayList<>();
        List<String> organisations = new ArrayList<>();
        List<String> projects = new ArrayList<>();

        for (Object[] obj : roles){
            JsonUserProject userProject = new JsonUserProject();
            userProject.setId(obj[0].toString());
            userProject.setUserId(obj[1].toString());
            userProject.setProjectId(obj[2].toString());
            userProject.setOrganisationId(obj[3].toString());
            userProject.setDeleted(obj[4].toString().equals("1"));
            userProject.setDefault(obj[5].toString().equals("1"));

            jsonUserProjects.add(userProject);

            organisations.add(obj[3].toString());
            projects.add(obj[2].toString());
        }

        if (roles.size() > 0) {

            Map<String, String> orgNameMap = new HashMap<>();
            Map<String, String> projectNameMap = new HashMap<>();
            List<OrganisationEntity> orgList = OrganisationEntity.getOrganisationsFromList(organisations);
            for (OrganisationEntity org : orgList) {
                orgNameMap.put(org.getUuid(), org.getName());
            }

            List<ProjectEntity> projectList = ProjectEntity.getProjectsFromList(projects);
            for (ProjectEntity proj : projectList) {
                projectNameMap.put(proj.getUuid(), proj.getName());
            }


            for (JsonUserProject re : jsonUserProjects) {
                re.setOrganisationName(orgNameMap.get(re.getOrganisationId()));
                re.setProjectName(projectNameMap.get(re.getProjectId()));
            }
        }

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(jsonUserProjects)
                .build();
    }

    private Response changeDefaultProject(String userId, String defaultRoleId, String userRoleId) throws Exception {

        UserProjectEntity.changeDefaultProject(userId, defaultRoleId, userRoleId);

        return Response
                .ok()
                .build();
    }
}
