package org.endeavourhealth.coreui.json;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.keycloak.representations.idm.GroupRepresentation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class JsonGroup {
    private UUID uuid = null;
    private String name = null;
    private String organisationId = null;
    private List<JsonGroup> subGroups = null;
    @JsonIgnore
    private Boolean isHidden = null;

    public JsonGroup() {
    }

    public JsonGroup(GroupRepresentation keycloakGroupRepresentation) {
        this.uuid = UUID.fromString(keycloakGroupRepresentation.getId());
        this.name = keycloakGroupRepresentation.getName();
        this.subGroups = new ArrayList<>();

        //Extract attributes such as mobile and photo, remove start and end [] chars
        Map<String, List<String>> userAttributes = keycloakGroupRepresentation.getAttributes();
        if (userAttributes != null) {
            for (String attributeKey : userAttributes.keySet()) {
                if (attributeKey.equalsIgnoreCase("organisation-id")) {
                    Object obj = userAttributes.get(attributeKey);
                    this.organisationId = obj.toString().substring(1, obj.toString().length()-1);
                }
            }
        }
    }


    /**
     * gets/sets
     */
    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOrganisationId() {
        return organisationId;
    }

    public void setOrganisationId(String organisationId) {
        this.organisationId = organisationId;
    }

    public List<JsonGroup> getSubGroups() {
        return subGroups;
    }

    public void setSubGroups(List<JsonGroup> subGroups) {
        this.subGroups = subGroups;
    }

    public void setSubGroup(JsonGroup subGroup) {this.subGroups.add(subGroup); }

    public Boolean getIsHidden() {
        return isHidden;
    }

    public void setIsHidden(boolean isHidden) {
        this.isHidden = isHidden;
    }
}
