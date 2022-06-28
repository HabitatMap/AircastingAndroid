package pl.llp.aircasting.data.api.params

class CreateAccountParams(val username: String, val password: String, val email: String, val send_emails: Boolean = true)
