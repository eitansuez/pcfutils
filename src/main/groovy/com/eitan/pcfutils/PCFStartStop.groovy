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

  def start() {
    def pcfutils = new PCFUtils()

    def output = execReturn "bosh vms"

    def deployments = pcfutils.parseVMsByDeployment(output)

    // first, start elastic runtime vms in specific start order
    def elasticRuntimeVms = deployments['cf'].vms
    def sortedVms = pcfutils.sortStartOrder(elasticRuntimeVms)
    sortedVms.each { vm ->
      execIt vm.startCommand()
    }

    // then, start non-elastic runtime vms
    def nonElasticRuntimeDeployments = deployments.values().findAll { deployment -> deployment.type != 'cf' }
    def nonElasticRuntimeVms = nonElasticRuntimeDeployments.inject([], { List vms, Deployment deployment ->
      vms.addAll(deployment.vms)
      vms
    })

    nonElasticRuntimeVms.each { vm ->
      execIt vm.startCommand()
    }

    execIt "bosh -n vm resurrection on"
  }

  def stop() {
    def pcfutils = new PCFUtils()

    execIt "bosh -n vm resurrection off"
    def output = execReturn "bosh vms"

    def deployments = pcfutils.parseVMsByDeployment(output)
    def nonElasticRuntimeDeployments = deployments.values().findAll { deployment -> deployment.type != 'cf' }
    def nonElasticRuntimeVms = nonElasticRuntimeDeployments.inject([], { List vms, Deployment deployment ->
      vms.addAll(deployment.vms)
      vms
    })

    // first stop non-elastic runtime vms
    nonElasticRuntimeVms.each { vm ->
      execIt vm.stopCommand()
    }

    // then stop elastic runtime vms in specific stop order
    def elasticRuntimeVms = deployments['cf'].vms
    def sortedVms = pcfutils.sortStopOrder(elasticRuntimeVms)
    sortedVms.each { vm ->
      execIt vm.stopCommand()
    }
  }

  def execIt(cmd) {
    if (testing) {
      println "`$cmd`"
      return 0
    }

    def pb = new ProcessBuilder()
    pb.environment().put("BUNDLE_GEMFILE", "/home/tempest-web/tempest/web/vendor/bosh/Gemfile")
    pb.command('sh', '-c', "/usr/local/bin/bundle exec ${cmd}")
    pb.redirectErrorStream(true)
    def p = pb.start()

    p.inputStream.eachLine { line -> println line }
    p.waitFor()
    p.exitValue()
  }

  def execReturn(cmd) {
    if (testing) {
      println "`$cmd`"
      return getClass().getResource("/bosh-vms.txt").readLines()
    }

    def pb = new ProcessBuilder()
    pb.environment().put("BUNDLE_GEMFILE", "/home/tempest-web/tempest/web/vendor/bosh/Gemfile")
    pb.command('sh', '-c', "/usr/local/bin/bundle exec ${cmd}")
    pb.redirectErrorStream(true)
    def p = pb.start()

    def output = p.inputStream.readLines()
    p.waitFor()
    output
  }
}
