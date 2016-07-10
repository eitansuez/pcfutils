package com.eitan.pcfutils

class PCFStartStop {

  static void main(String... args) {
    boolean testMode = (args.length == 1 && args[0] == 'testing')
    if (testMode) {
      println "testMode is ON"
    }
    new PCFStartStop(testing: testMode).run()
  }

  boolean testing = true

  private def pcfutils = new PCFUtils()

  def run() {
    def output = execReturn "bosh vms"
    def deployments = pcfutils.parseVMsByDeployment(output)

    println "---"
    makeStopScript(deployments)
    println "---"
    makeStartScript(deployments)
    println "---"
  }

  def makeStopScript(deployments) {
    println "#!/bin/sh"
    println "bosh -n vm resurrection off"

    // first stop non-elastic runtime vms
    def nonElasticRuntimeDeployments = deployments.values().findAll { deployment -> deployment.type != 'cf' }
    nonElasticRuntimeDeployments.each { Deployment deployment ->
      println deployment.setCommand()
      deployment.vms.each { vm ->
        println vm.stopCommand()
      }
    }

    // then stop elastic runtime vms in specific stop order
    Deployment cfDeployment = deployments['cf']
    def elasticRuntimeVms = cfDeployment.vms
    def sortedVms = pcfutils.sortStopOrder(elasticRuntimeVms)
    println cfDeployment.setCommand()
    sortedVms.each { vm ->
      println vm.stopCommand()
    }
  }

  def makeStartScript(deployments) {
    println "#!/bin/sh"

    // first, start elastic runtime vms in specific start order
    Deployment cfDeployment = deployments['cf']
    def elasticRuntimeVms = cfDeployment.vms
    def sortedVms = pcfutils.sortStartOrder(elasticRuntimeVms)
    println cfDeployment.setCommand()
    sortedVms.each { vm ->
      println vm.startCommand()
    }

    // then, start non-elastic runtime vms
    def nonElasticRuntimeDeployments = deployments.values().findAll { deployment -> deployment.type != 'cf' }
    nonElasticRuntimeDeployments.each { Deployment deployment ->
      println deployment.setCommand()
      deployment.vms.each { vm ->
        println vm.startCommand()
      }
    }

    println "bosh -n vm resurrection on"
  }

  private def execIt(cmd) {
    println cmd
    if (testing) {
      return 0
    }

    def p = startBundleExecProcess(cmd)
    p.inputStream.eachLine { line -> println line }
    p.waitFor()
    p.exitValue()
  }

  private def execReturn(cmd) {
    println cmd
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
