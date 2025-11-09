> each page after a api call will show the type and message options returned by backend

# login:
> Redirect to redirect param or /profile if redirect param is empty or null
- Email Input
- Password Input
- Login Button
- Sign Up Instead Link
- resend verification option if the backend reutrns `"reverify" = "true"` in response json
- Reset forgotten password link (send a backend request which mails the link with a token)

# register:
> Redirect to redirect param or /profile if redirect param is empty or null
- Full Name Input
- Email Input
- Password Input
- Signup Button Button
- Login Instead Link
- resend verification option if the backend reutrns `"reverify" = "true"` in response json

# profile:
> if redirect param exist, use it as a go back link
- returned user info:
	- uuid
	- email
	- is_verified
	- acc create date
	- acc last password change date
	- last login
	- acc type
	- profile pic
	- full name
	- metadata
	- permissons
- option to link/unlink google based on acc_type (acc_type = password || google || both)
	- if aacc_type is google, changing password with old password being blank automatically links apssword to a google based account
	- to link google to a password type acc, initiate a google login exactly how a normal google login initiastes but password source=glink
	- to unlink google, the backend hasnt implemented that so show a disabled button for it
- link to sessions/login management page
- link to change password form
- link to logout page

# logout:
> Redirect to redirect param or /login if redirect param is empty or null
- logouts and redirects

# sessions:
> if redirect param exist, use it as a go back link otherwise /profile
- list all user sessions
- gives button to delete/logout indivisual sessions

# change password
> if redirect param exist, use it as a go back link otherwise /profile
- old password feild
- new password
- confirm new password
- submit

# reset forgotten password page: (not yet implemented by backend but implement here with a always disabled submit button)
> Redirect to redirect param or /profile if redirect param is empty or null
- send the token for pass change to backend and backend decides if password changes or not
- new password
- confirm new password
- submit
