package connectionpool

enum class RespondentType {
    User,
    Group
}
open class Respondent(val id:String, val type:RespondentType)

class User(userName:String, val password:String): Respondent(userName,RespondentType.User)

class Group(groupName:String, val manager:String) :Respondent(groupName,RespondentType.Group){
    val members = HashSet<String>()
    val invites = HashSet<String>()
}
