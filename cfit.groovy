#!/usr/bin/env groovy

testing = false

def execAll(multilineCmd) {
  multilineCmd.split('\n').each { cmd ->
    if (cmd?.trim()) {
      execIt(cmd)
    }
  }
}
def execIt(cmd) {
  println "cmd: $cmd"
  if (testing) {
    return
  }

  def p = new ProcessBuilder('sh', '-c', cmd)
      .redirectErrorStream(true)
      .start()

  p.inputStream.eachLine { line -> println line }
  p.waitFor()
  if (p.exitValue() != 0) {
    throw new RuntimeException("cmd execution ($cmd) failed")
  }
  p.exitValue()
}

def makeUser(name, pass) {
  def org = "$name-org"
  execAll """
cf create-org $org
cf create-user $name $pass
cf set-org-role $name $org OrgManager
cf target -o $org
cf create-space development
cf set-space-role $name $org development SpaceManager
cf set-space-role $name $org development SpaceDeveloper
"""
}

def delUser(name, pass) {
  def org = "$name-org"
  execAll """
cf delete-org $org -f
cf delete-user $name -f
"""
}

boolean isValidCmd(cmd) {
  return cmd == "makeUser" || cmd == "delUser"
}

def promptForPass() {
  System.console().readPassword('Password: ')
}

def processUsers(username) {
  def users = [] as Set
  if (username) {
    def pass = promptForPass()
    users << [username: username, pass: pass]
  } else {
    def lines = new File('users.csv').readLines()
    def usersLines = lines.findAll { line ->
      if (line.startsWith("#")) return false
      line.split(",")[0]?.trim() // filter out blank lines
    }
    users = usersLines.collect { line ->
      def tuple = line.split(",")
      [username: tuple[0].trim().toLowerCase(), pass: tuple[1].trim()]
    } as Set

    if (users.size() != usersLines.size()) {
      throw new RuntimeException("user names list in users.csv is not unique")
    }
  }
  users
}

def cli = new CliBuilder(usage: 'cfit', width: 120, footer: """
 makeUser will create:
 - a user with specified username
 - an org named <username>-org
 - a space named development
 - and make user the manager over the org and space
 ---
 delUser will delete the org and the user
 ---
 if no username is provided,
 * the script will look for a list of usernames and passwords (comma separated)
 * in a file named users.csv
 * in the current working directory
 ---
""")
cli._(longOpt: 'cmd', required: true, args: 1, valueSeparator: '=', 'makeUser | delUser')
cli._(longOpt: 'username', required: false, args: 1, valueSeparator: '=', 'username')

def options = cli.parse(args)

boolean error = (options == null)
if (error) return

if (!isValidCmd(options.cmd)) {
  println "invalid command: ${options.cmd}"
  cli.usage()
  return
}

def users = processUsers(options.username)

users.each { user ->
  this.invokeMethod(options.cmd, [user.username, user.pass])
}
