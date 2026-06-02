import hudson.security.HudsonPrivateSecurityRealm
import jenkins.model.Jenkins

def username = 'ismail0133'
def password = 'admin123'
def instance = Jenkins.get()
def realm = instance.getSecurityRealm()

if (!(realm instanceof HudsonPrivateSecurityRealm)) {
    println("Le security realm Jenkins n'est pas HudsonPrivateSecurityRealm.")
    return
}

def user = realm.getUser(username)

if (user == null) {
    realm.createAccount(username, password)
    println("Utilisateur Jenkins cree : ${username}")
} else {
    user.addProperty(HudsonPrivateSecurityRealm.Details.fromPlainPassword(password))
    println("Mot de passe Jenkins reinitialise pour : ${username}")
}

instance.save()
