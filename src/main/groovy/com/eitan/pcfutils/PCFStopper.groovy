package com.eitan.pcfutils

class PCFStopper {

  boolean testing = true

  static void main(String... args) {
    // TODO: provide a means to communicate testing from args
    new PCFStopper(testing: true).run()
  }

  def run() {
    execIt "bosh -n vm resurrection off"
    def output = execReturn "bosh vms"

    def pcfutils = new PCFUtils()
    def filtered = pcfutils.filterVMs(output)

    def vms = filtered.collect { line -> pcfutils.filterVM(line) }
    def sortedVms = pcfutils.sortStopOrder(vms)
    sortedVms.each { vm ->
      execIt vm.stopCommand()
    }
  }

  def execIt(cmd) {
    if (testing) {
      println "`$cmd`"
      return 0
    }

    def p = new ProcessBuilder('sh', '-c', cmd)
        .redirectErrorStream(true)
        .start()

    p.inputStream.eachLine { line -> println line }
    p.waitFor()
    p.exitValue()
  }

  def execReturn(cmd) {
    if (testing) {
      println "`$cmd`"
      return getClass().getResource("/sample-output.txt").readLines()
    }

    def p = new ProcessBuilder('sh', '-c', cmd)
        .redirectErrorStream(true)
        .start()

    def output = p.inputStream.readLines()
    p.waitFor()
    output
  }
}