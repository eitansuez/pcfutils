package com.eitan.pcfutils

class PCFUtils {

  private List<String> stopOrder, startOrder

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

  List<BoshVM> sortStopOrder(List<BoshVM> boshVMs) {
    boshVMs.sort { BoshVM vm -> stopOrder.indexOf(vm.type) }
  }

  List<BoshVM> sortStartOrder(List<BoshVM> boshVMs) {
    boshVMs.sort { BoshVM vm -> startOrder.indexOf(vm.type) }
  }

  def parseVMsByDeployment(List<String> output) {
    def deployments = [:]
    Deployment deployment = null
    output.each { line ->
      def matcher = (line =~ /^Deployment '([a-z0-9\-]+)'$/)
      if (matcher.matches()) {
        deployment = new Deployment(matcher[0][1])
        deployments.put(deployment.type, deployment)
      }
      else if (isVm(line)) {
        def vm = filterVM(line)
        deployment.vms << vm
      }
    }
    deployments
  }

  private boolean isVm(line) {
    line.contains('-partition-')
  }
}
