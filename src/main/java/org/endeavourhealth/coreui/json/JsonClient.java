package org.endeavourhealth.coreui.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class JsonClient {
    private String uuid = null;
    private String name = null;
    private String clientId = null;
    private String description = null;
    private List<JsonEndUserRole> clientRoles = null;

    public JsonClient() {
    }

    public JsonClient(ClientRepresentation keycloakClientRepresentation, List<RoleRepresentation> keycloakClientRoles) {
        this.uuid = keycloakClientRepresentation.getId();  //maybe be non UUID, i.e. eds-ui
        this.name = keycloakClientRepresentation.getName() == null ? keycloakClientRepresentation.getClientId() : keycloakClientRepresentation.getName();
        this.clientId = keycloakClientRepresentation.getClientId();

        this.description = keycloakClientRepresentation.getDescription();

        //set the linked client roles
        this.clientRoles = new ArrayList<>();
        for (RoleRepresentation userClientRole : keycloakClientRoles) {
            JsonEndUserRole endUserClientRole = new JsonEndUserRole(userClientRole);
            this.setClientRole(endUserClientRole);
        }
    }

    /**
     * gets/sets
     */
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<JsonEndUserRole> getClientRoles() {
        return clientRoles;
    }

    public void setClientRoles(List<JsonEndUserRole> clientRoles) {
        this.clientRoles = clientRoles;
    }

    public void setClientRole(JsonEndUserRole clientRole) {
        this.clientRoles.add(clientRole);
    }

}