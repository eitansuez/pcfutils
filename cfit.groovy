#!/usr/bin/env groovy

testing = false
pwd = 'testing123'

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
    println "  not actually invoked (testing)"
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

def makeUser(name) {
  def org = "$name-org"
  execAll """
cf create-org $org
cf create-user $name $pwd
cf set-org-role $name $org OrgManager
cf target -o $org
cf create-space development
cf set-space-role $name $org development SpaceManager
cf set-space-role $name $org development SpaceDeveloper
"""
}

def delUser(name) {
  def org = "$name-org"
  execAll """
cf delete-org $org -f
cf delete-user $name -f
"""
}

boolean isValidCmd(cmd) {
  return cmd == "makeUser" || cmd == "delUser"
}

def processNames(username) {
  def names = [] as Set
  if (username) {
    names << username
  } else {
    def lines = new File('names.csv').readLines()
    def nameLines = lines.findAll { line ->
      if (line.startsWith("#")) return false
      line.split(",")[0].trim().toLowerCase() // fileter out blank lines
    }
    names = nameLines.collect { line ->
      line.split(",")[0].trim().toLowerCase()
    } as Set

    if (names.size() != nameLines.size()) {
      throw new RuntimeException("names list in names.csv is not unique")
    }
  }
}

def cli = new CliBuilder(usage: 'cfit', footer: """
 makeUser will create:
 - a user with specified username
 - an org named <username>-org
 - a space named development
 - and make user the manager over the org and space
 ---
 delUser will delete the org and the user
 ---
 if no username is provided,
 * the script will look for a list of usernames (one per line)
 * in a file named names.csv
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

def names = processNames(options.username)

names.each { name ->
  this.invokeMethod(options.cmd, name)
}

