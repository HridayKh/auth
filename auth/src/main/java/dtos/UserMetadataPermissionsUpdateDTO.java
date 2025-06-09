// dtos/UserMetadataPermissionsUpdateDTO.java (UPDATED)
package dtos;

import org.json.JSONObject;

/**
 * Data Transfer Object for updating a user's metadata and permissions. This DTO
 * now includes boolean flags to control whether the incoming JSON objects for
 * metadata and permissions should replace existing data or be merged with it.
 */
public class UserMetadataPermissionsUpdateDTO {
	private JSONObject metadata;
	private JSONObject permissions;
	private boolean metadataMerge; // New field for metadata merge control
	private boolean permissionsMerge; // New field for permissions merge control

	// Getters and Setters
	public JSONObject getMetadata() {
		return metadata;
	}

	public void setMetadata(JSONObject metadata) {
		this.metadata = metadata;
	}

	public JSONObject getPermissions() {
		return permissions;
	}

	public void setPermissions(JSONObject permissions) {
		this.permissions = permissions;
	}

	public boolean isMetadataMerge() { // Getter for boolean is standard "is" prefix
		return metadataMerge;
	}

	public void setMetadataMerge(boolean metadataMerge) {
		this.metadataMerge = metadataMerge;
	}

	public boolean isPermissionsMerge() { // Getter for boolean is standard "is" prefix
		return permissionsMerge;
	}

	public void setPermissionsMerge(boolean permissionsMerge) {
		this.permissionsMerge = permissionsMerge;
	}
}