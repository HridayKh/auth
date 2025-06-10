package entities;

import org.json.JSONObject;

public class User {
	private final String uuid;
	private final String email;
	private final String passwordHash; // nullable
	private final boolean isVerified;
	private final long createdAt;
	private final long updatedAt;
	private final Long lastLogin; // nullable
	

	// New Fields
	private final String accType;
	private final String googleId;
	private final String refreshToken;
	private final Long refreshTokenExpiresAt;

	// Optional fields with defaults
	private final String profilePic;
	private final String fullName;
	private final JSONObject metadata;
	private final JSONObject permissions;

	private User(Builder builder) {
		this.uuid = builder.uuid;
		this.email = builder.email.toLowerCase();
		this.passwordHash = builder.passwordHash;
		this.isVerified = builder.isVerified;
		this.createdAt = builder.createdAt;
		this.updatedAt = builder.updatedAt;
		this.lastLogin = builder.lastLogin;

		// New fields
		this.accType = builder.accType;
		this.googleId = builder.googleId;
		this.refreshToken = builder.refreshToken;
		this.refreshTokenExpiresAt = builder.refreshTokenExpiresAt;

		// Optional fields
		this.profilePic = builder.profilePic;
		this.fullName = builder.fullName;
		this.metadata = builder.metadata;
		this.permissions = builder.permissions;
	}

	// --- Getters ---
	public String uuid() {
		return uuid;
	}

	public String email() {
		return email.toLowerCase();
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

	// New Getters
	public String accType() {
		return accType;
	}

	public String googleId() {
		return googleId;
	}

	public String refreshToken() {
		return refreshToken;
	}

	public Long refreshTokenExpiresAt() {
		return refreshTokenExpiresAt;
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

	// --- Builder Class ---
	public static class Builder {
		// Required fields for initial construction
		private final String uuid;
		private final String email;
		private final long createdAt;
		private final long updatedAt;

		// Fields that were previously mandatory but now nullable
		private String passwordHash = null;

		// Fields that are explicitly nullable
		private Long lastLogin = null;
		private String googleId = null;
		private String refreshToken = null;
		private Long refreshTokenExpiresAt = null;
		private String fullName = null;

		// Fields with defaults
		private boolean isVerified = false;
		private String accType = "password"; // Default value "password"
		private String profilePic = "https://i.pinimg.com/736x/2f/15/f2/2f15f2e8c688b3120d3d26467b06330c.jpg";
		private JSONObject metadata = new JSONObject();
		private JSONObject permissions = new JSONObject();

		/**
		 * Constructor for the Builder class. Sets the minimum required fields for a
		 * User.
		 *
		 * @param uuid      The unique identifier for the user.
		 * @param email     The user's email address.
		 * @param createdAt The timestamp when the user account was created.
		 * @param updatedAt The timestamp when the user account was last updated.
		 */
		public Builder(String uuid, String email, long createdAt, long updatedAt) {
			this.uuid = uuid;
			this.email = email.toLowerCase();
			this.createdAt = createdAt;
			this.updatedAt = updatedAt;
		}

		// --- Builder methods for optional fields ---
		public Builder passwordHash(String val) {
			this.passwordHash = val;
			return this;
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

		public Builder accType(String val) {
			this.accType = val;
			return this;
		}

		public Builder googleId(String val) {
			this.googleId = val;
			return this;
		}

		public Builder refreshToken(String val) {
			this.refreshToken = val;
			return this;
		}

		public Builder refreshTokenExpiresAt(Long val) {
			this.refreshTokenExpiresAt = val;
			return this;
		}

		public User build() {
			return new User(this);
		}
	}
}