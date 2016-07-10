package com.eitan.pcfutils

import groovy.util.logging.Slf4j

@Slf4j
class PCFStartStop {

  static void main(String... args) {
    boolean testMode = (args.length == 1 && args[0] == 'testing')
    if (testMode) {
      log.debug "testMode is ON"
    }
    new PCFStartStop(testing: testMode).run()
  }

  boolean testing = true

  private def pcfutils = new PCFUtils()

  def run() {
    def output = execReturn "bosh vms"
    def deployments = pcfutils.parseVMsByDeployment(output)

    makeStopScript(new File('stopscript.sh'), deployments)
    makeStartScript(new File('startscript.sh'), deployments)
  }

  def writeShellScript(stream, closure) {
    stream.withPrintWriter { Writer pw ->
      pw.println '#!/bin/sh'
      pw.println()

      closure.call(pw)
    }
  }

  def makeStopScript(stream, deployments) {
    writeShellScript(stream) { pw ->
      pw.println "bosh -n vm resurrection off"
      pw.println()

      def nonElasticRuntimeDeployments = deployments.values().findAll { deployment -> deployment.type != 'cf' }
      nonElasticRuntimeDeployments.each { Deployment deployment ->
        deployment.stop(pw)
        pw.println()
      }

      deployments['cf'].stop(pw)
      pw.println()
    }
  }

  def makeStartScript(stream, deployments) {
    writeShellScript(stream) { pw ->

      deployments['cf'].start(pw)
      pw.println()

      def nonElasticRuntimeDeployments = deployments.values().findAll { deployment -> deployment.type != 'cf' }
      nonElasticRuntimeDeployments.each { Deployment d ->
        d.start(pw)
        pw.println()
      }

      pw.println "bosh -n vm resurrection on"
      pw.println()
    }

  }

  private def execIt(cmd) {
    log.debug cmd
    if (testing) {
      return 0
    }

    def p = startBundleExecProcess(cmd)
    p.inputStream.eachLine { line -> log.debug line }
    p.waitFor()
    p.exitValue()
  }

  private def execReturn(cmd) {
    log.debug cmd
    if (testing) {
      return getClass().getResource("/bosh-vms.txt").readLines()
    }

    def p = startBundleExecProcess(cmd)
    def output = p.inputStream.readLines()
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
