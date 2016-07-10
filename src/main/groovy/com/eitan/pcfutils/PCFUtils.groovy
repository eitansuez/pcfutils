package com.eitan.pcfutils

class PCFUtils {

  List<String> stopOrder, startOrder

  PCFUtils() {
    stopOrder = getClass().getResource("/stop-order.txt").readLines()
    startOrder = getClass().getResource("/start-order.txt").readLines()
  }

  List<String> filterVMs(List<String> output) {
    output.grep { isVm(it) }
  }

  BoshVM filterVM(String line) {
    def parts = line.split("\\|")
    def vmString = parts[1]
    new BoshVM(vmString)
  }

  private boolean isVm(line) {
    line.contains('-partition-')
  }

  def parseVMsByDeployment(List<String> output) {
    def deployments = [:]
    Deployment deployment = null
    output.each { line ->
      def matcher = (line =~ /^Deployment '([a-z0-9\-]+)'$/)
      if (matcher.matches()) {
        String id = matcher[0][1]
        deployment = Deployment.isCf(id) ? new CfDeployment(id, stopOrder, startOrder) : new Deployment(id)
        deployments.put(deployment.type, deployment)
      }
      else if (isVm(line)) {
        def vm = filterVM(line)
        deployment.vms << vm
      }
    }
    deployments
  }

}
