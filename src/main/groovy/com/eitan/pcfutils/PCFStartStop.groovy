package com.eitan.pcfutils

class PCFStartStop {

  boolean testing = true

  static void main(String... args) {
    if (args.length < 1) {
      println "usage is wrong.  need to pass start|stop [testMode]"
      return
    }
    if (! (args[0] == 'start' || args[0] == 'stop')) {
      println 'first argument is start or stop'
      return
    }

    boolean testMode = (args.length == 2 && args[1] == 'testing')
    if (testMode) {
      println "testMode is ON"
    }
    boolean start = args[0] == 'start'
    def pcfstartstop = new PCFStartStop(testing: testMode)
    if (start) {
      pcfstartstop.start()
    } else {
      pcfstartstop.stop()
    }
  }

  private def pcfutils = new PCFUtils()

  def start() {
    def output = execReturn "bosh vms"

    def deployments = pcfutils.parseVMsByDeployment(output)

    // first, start elastic runtime vms in specific start order
    Deployment cfDeployment = deployments['cf']
    def elasticRuntimeVms = cfDeployment.vms
    def sortedVms = pcfutils.sortStartOrder(elasticRuntimeVms)
    execIt cfDeployment.setCommand()
    sortedVms.each { vm ->
      execIt vm.startCommand()
    }

    // then, start non-elastic runtime vms
    def nonElasticRuntimeDeployments = deployments.values().findAll { deployment -> deployment.type != 'cf' }
    nonElasticRuntimeDeployments.each { Deployment deployment ->
      execIt deployment.setCommand()
      deployment.vms.each { vm ->
        execIt vm.startCommand()
      }
    }

    execIt "bosh -n vm resurrection on"
  }

  def stop() {
    execIt "bosh -n vm resurrection off"
    def output = execReturn "bosh vms"

    def deployments = pcfutils.parseVMsByDeployment(output)

    // first stop non-elastic runtime vms
    def nonElasticRuntimeDeployments = deployments.values().findAll { deployment -> deployment.type != 'cf' }
    nonElasticRuntimeDeployments.each { Deployment deployment ->
      execIt deployment.setCommand()
      deployment.vms.each { vm ->
        execIt vm.stopCommand()
      }
    }

    // then stop elastic runtime vms in specific stop order
    Deployment cfDeployment = deployments['cf']
    def elasticRuntimeVms = cfDeployment.vms
    def sortedVms = pcfutils.sortStopOrder(elasticRuntimeVms)
    execIt cfDeployment.setCommand()
    sortedVms.each { vm ->
      execIt vm.stopCommand()
    }
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
