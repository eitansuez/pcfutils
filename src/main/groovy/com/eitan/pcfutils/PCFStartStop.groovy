package com.eitan.pcfutils

import groovy.util.logging.Slf4j

@Slf4j
class PCFStartStop {

  static void main(String... args) {
    boolean testMode = (args.length == 1 && args[0] == 'testing')
    if (testMode) {
      log.info "testMode is ON"
    }
    new PCFStartStop(testing: testMode).run()
  }

  boolean testing = true

  def run() {
    List<String> output = execReturn "bosh vms"
    CfInstallation installation = new PCFUtils().fromOutput(output)
    installation.makeScripts(new File('stopscript.sh'), new File('startscript.sh'))
  }

  private def execIt(cmd) {
    log.debug "execit: command is: $cmd"
    if (testing) {
      return 0
    }

    def p = startBundleExecProcess(cmd)
    p.inputStream.eachLine { line -> log.info line }
    p.waitFor()
    p.exitValue()
  }

  private List<String> execReturn(cmd) {
    log.debug "execReturn: command is: $cmd"
    if (testing) {
      return getClass().getResource("/bosh-vms.txt").readLines()
    }

    def p = startBundleExecProcess(cmd)
    List<String> output = p.inputStream.readLines()
    p.waitFor()
    output
  }

  private Process startBundleExecProcess(cmd) {
    def pb = new ProcessBuilder()
    pb.environment().put("BUNDLE_GEMFILE", "/home/tempest-web/tempest/web/vendor/bosh/Gemfile")
    pb.command('sh', '-c', "/usr/local/bin/bundle exec ${cmd}")
    pb.redirectErrorStream(true)
    pb.start()
  }

}
