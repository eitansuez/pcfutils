package com.eitan.pcfutils

class PCFUtils {

  private List<String> stopOrder

  PCFUtils() {
    stopOrder = getClass().getResource("/stop-order.txt").readLines()
  }

  List<String> filterVMs(List<String> output) {
    output.grep { it.contains '-partition-' }
  }

  BoshVM filterVM(String line) {
    def parts = line.split("\\|")
    def vmString = parts[1]
    new BoshVM(vmString)
  }

  List<BoshVM> sortStopOrder(List<BoshVM> boshVMs) {
    boshVMs.sort { BoshVM vm1, BoshVM vm2 ->
      stopOrder.indexOf(vm1.type) <=> stopOrder.indexOf(vm2.type)
    }
  }
}
