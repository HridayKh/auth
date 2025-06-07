package entities;

import org.json.JSONObject;

public class User {
	private final String uuid;
	private final String email;
	private final String passwordHash;
	private final boolean isVerified;
	private final long createdAt;
	private final long updatedAt;
	private final Long lastLogin; // Nullable

	// Optional fields with defaults
	private final String profilePic;
	private final String fullName;
	private final JSONObject metadata;
	private final JSONObject permissions;

	private User(Builder builder) {
		this.uuid = builder.uuid;
		this.email = builder.email;
		this.passwordHash = builder.passwordHash;
		this.isVerified = builder.isVerified;
		this.createdAt = builder.createdAt;
		this.updatedAt = builder.updatedAt;

		this.lastLogin = builder.lastLogin;
		this.profilePic = builder.profilePic;
		this.fullName = builder.fullName;
		this.metadata = builder.metadata;
		this.permissions = builder.permissions;
	}

	// Getters
	public String uuid() {
		return uuid;
	}

	public String email() {
		return email;
	}

	public String passwordHash() {
		return passwordHash;
	}

	public boolean isVerified() {
		return isVerified;
	}

	public long createdAt() {
		return createdAt;
	}

	public long updatedAt() {
		return updatedAt;
	}

	public Long lastLogin() {
		return lastLogin;
	}

	public String profilePic() {
		return profilePic;
	}

	public String fullName() {
		return fullName;
	}

	public JSONObject metadata() {
		return metadata;
	}

	public JSONObject permissions() {
		return permissions;
	}

	public static class Builder {
		private final String uuid;
		private final String email;
		private final String passwordHash;
		private boolean isVerified = false;
		private final long createdAt;
		private final long updatedAt;

		private Long lastLogin = null;

		// ✅ Default profile picture
		private String profilePic = "https://i.pinimg.com/736x/2f/15/f2/2f15f2e8c688b3120d3d26467b06330c.jpg";

		private String fullName = null;

		// ✅ Default metadata and permissions: empty JSON objects
		private JSONObject metadata = new JSONObject();
		private JSONObject permissions = new JSONObject();

		public Builder(String uuid, String email, String passwordHash, long createdAt, long updatedAt) {
			this.uuid = uuid;
			this.email = email;
			this.passwordHash = passwordHash;
			this.createdAt = createdAt;
			this.updatedAt = updatedAt;
		}

		public Builder isVerified(boolean val) {
			this.isVerified = val;
			return this;
		}

		public Builder lastLogin(Long val) {
			this.lastLogin = val;
			return this;
		}

		public Builder profilePic(String val) {
			this.profilePic = val;
			return this;
		}

		public Builder fullName(String val) {
			this.fullName = val;
			return this;
		}

		public Builder metadata(JSONObject val) {
			this.metadata = val;
			return this;
		}

		public Builder permissions(JSONObject val) {
			this.permissions = val;
			return this;
		}

		public User build() {
			return new User(this);
		}
	}
}
