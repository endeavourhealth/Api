package org.endeavourhealth.coreui.endpoints;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.DAL.SecurityMasterMappingDAL;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.database.OrganisationEntity;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.database.ProjectEntity;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.database.RegionEntity;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.enums.MapType;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.json.JsonProject;
import org.endeavourhealth.common.security.usermanagermodel.models.DAL.SecurityUserProjectDAL;
import org.endeavourhealth.common.security.usermanagermodel.models.DAL.SecurityUserRegionDAL;
import org.endeavourhealth.common.security.usermanagermodel.models.caching.*;
import org.endeavourhealth.common.security.usermanagermodel.models.database.*;
import org.endeavourhealth.common.security.usermanagermodel.models.json.JsonApplicationPolicyAttribute;
import org.endeavourhealth.common.security.usermanagermodel.models.json.JsonUserOrganisationProject;
import org.endeavourhealth.common.security.usermanagermodel.models.json.JsonUserProfile;
import org.endeavourhealth.common.security.usermanagermodel.models.json.JsonUserProject;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.audit.UserAuditDalI;
import org.endeavourhealth.core.database.dal.audit.models.AuditAction;
import org.endeavourhealth.core.database.dal.audit.models.AuditModule;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import java.util.*;

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

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/getUserProfile")
    @ApiOperation(value = "Returns a representation of the access rights for a user role")
    public Response getAccessProfile(@Context SecurityContext sc,
                                     @ApiParam(value = "User id") @QueryParam("userId") String userId) throws Exception {

        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "application(s)");

        return getUserProfile(userId);

    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/flushCache")
    @ApiOperation(value = "Returns a list of applications")
    public Response flushCache(@Context SecurityContext sc) throws Exception {

        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "application(s)");

        return flushCache();

    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/getPublishersForProject")
    @ApiOperation(value = "Returns a list of publising organisations for a project")
    public Response getPublishersForProject(@Context SecurityContext sc,
                                     @ApiParam(value = "Project id") @QueryParam("projectId") String projectId) throws Exception {

        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "projects(s)");

        List<String> orgUuids = getPublishersForProject(projectId);

        return Response
                .ok()
                .entity(orgUuids)
                .build();

    }

    private Response flushCache() throws Exception {
        ApplicationCache.flushCache();
        ApplicationPolicyCache.flushCache();
        ApplicationProfileCache.flushCache();
        DelegationCache.flushCache();
        OrganisationCache.flushCache();
        ProjectCache.flushCache();
        RegionCache.flushCache();
        UserCache.flushCache();

        clearLogbackMarkers();
        return Response
                .ok()
                .build();
    }

    private Response getProjectsForUser(String userId) throws Exception {
        List<Object[]> roles = new SecurityUserProjectDAL().getUserProjects(userId);
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
            List<OrganisationEntity> orgList = OrganisationCache.getOrganisationDetails(organisations);
            for (OrganisationEntity org : orgList) {
                orgNameMap.put(org.getUuid(), org.getName());
            }

            List<ProjectEntity> projectList = ProjectCache.getProjectDetails(projects);
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

        new SecurityUserProjectDAL().changeDefaultProject(userId, defaultRoleId, userRoleId);

        return Response
                .ok()
                .build();
    }

    private Response getUserProfile(String userId) throws Exception {
        UserRepresentation userDetails = UserCache.getUserDetails(userId);
        if (userDetails == null) {
            System.out.println("user details could not be obtained");
        }
        UserApplicationPolicyEntity userApplicationPolicyEntity = UserCache.getUserApplicationPolicy(userId);

        ApplicationPolicyEntity userAppPolicyEntity = ApplicationPolicyCache.getApplicationPolicyDetails(userApplicationPolicyEntity.getApplicationPolicyId());

        List<UserProjectEntity> projectEntities = new SecurityUserProjectDAL().getUserProjectEntities(userId);

        JsonUserProfile userProfile = new JsonUserProfile();
        userProfile.setUuid(userDetails.getId());
        userProfile.setUsername(userDetails.getUsername());
        userProfile.setForename(userDetails.getFirstName());
        userProfile.setSurname(userDetails.getLastName());
        userProfile.setEmail(userDetails.getEmail());

        UserRegionEntity userRegion = UserCache.getUserRegion(userId);

        if (userRegion != null) {
            RegionEntity region = RegionCache.getRegionDetails(userRegion.getRegionId());
            if (region != null) {
                userProfile.setRegion(region);
            }
        }

        Map<String, List<String>> userAttributes = userDetails.getAttributes();
        if (userAttributes != null) {
            Iterator var3 = userAttributes.keySet().iterator();

            while(var3.hasNext()) {
                String attributeKey = (String)var3.next();
                Object obj;
                if (attributeKey.equalsIgnoreCase("mobile")) {
                    obj = userAttributes.get(attributeKey);
                    userProfile.setMobile(obj.toString().substring(1, obj.toString().length() - 1));
                } else if (attributeKey.equalsIgnoreCase("photo")) {
                    obj = userAttributes.get(attributeKey);
                    userProfile.setPhoto(obj.toString().substring(1, obj.toString().length() - 1));
                }
            }
        }

        List<JsonUserOrganisationProject> organisationProjects = new ArrayList<>();

        for (UserProjectEntity profile : projectEntities) {
            JsonUserOrganisationProject orgProject = organisationProjects.stream().filter(app -> app.getOrganisation().getUuid().equals(profile.getOrganisationId())).findFirst().orElse(new JsonUserOrganisationProject());
            if (orgProject.getOrganisation() == null) {

                OrganisationEntity organisation = OrganisationCache.getOrganisationDetails(profile.getOrganisationId());

                orgProject.setOrganisation(organisation);
                organisationProjects.add(orgProject);
            }

            JsonProject project = ProjectCache.getJsonProjectDetails(profile.getProjectId());
            ApplicationPolicyEntity projectAppPolicyEntity = ApplicationPolicyCache.getApplicationPolicyDetails(project.getApplicationPolicy());


            List<JsonApplicationPolicyAttribute> projectPolicyAttributes = processProjectPolicyAttributes(userAppPolicyEntity.getId(), projectAppPolicyEntity.getId());
            project.setApplicationPolicyAttributes(projectPolicyAttributes);

            orgProject.addProject(project);
        }

        userProfile.setOrganisationProjects(organisationProjects);

        return Response
                .ok()
                .entity(userProfile)
                .build();
    }

    private List<JsonApplicationPolicyAttribute> processProjectPolicyAttributes(String userPolicyId, String projectPolicyId) throws Exception {
        List<JsonApplicationPolicyAttribute> mergedAttributes = new ArrayList<>();

        List<JsonApplicationPolicyAttribute> projectPolicyAttributes = ApplicationPolicyCache.getApplicationPolicyAttributes(projectPolicyId);

        if (userPolicyId.equals(projectPolicyId)) {
            // both user and project have the same policy so just return the project attributes
            return projectPolicyAttributes;
        }

        List<JsonApplicationPolicyAttribute> userPolicyAttributes = ApplicationPolicyCache.getApplicationPolicyAttributes(userPolicyId);

        for (JsonApplicationPolicyAttribute attribute : projectPolicyAttributes) {
            if (userPolicyAttributes.stream().filter(a -> a.getApplicationAccessProfileId().equals(attribute.getApplicationAccessProfileId())).findFirst().isPresent()) {
                //user policy has the project attribute so add it to the list
                mergedAttributes.add(attribute);
            }
        }

        return mergedAttributes;
    }

    public List<String> getPublishersForProject(String projectId) throws Exception {
        List<String> orgUUIDs = new SecurityMasterMappingDAL().getChildMappings(projectId, MapType.PROJECT.getMapType(), MapType.PUBLISHER.getMapType());

        return orgUUIDs;
    }
}
