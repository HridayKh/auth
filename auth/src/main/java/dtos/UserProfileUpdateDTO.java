package dtos;

/**
 * Data Transfer Object for updating a user's profile information. This includes
 * fields like email, profile picture, and full name. It's designed to map
 * directly from an incoming JSON request body.
 */
public class UserProfileUpdateDTO {
	private String email;
	private String profilePic;
	private String fullName;

	// Getters and Setters
	public String getEmail() {
		return email.toLowerCase();
	}

	public void setEmail(String email) {
		this.email = email.toLowerCase();
	}

	public String getProfilePic() {
		return profilePic;
	}

	public void setProfilePic(String profilePic) {
		this.profilePic = profilePic;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
}