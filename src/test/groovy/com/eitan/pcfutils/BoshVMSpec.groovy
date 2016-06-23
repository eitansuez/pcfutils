package com.eitan.pcfutils

import spock.lang.Specification

class BoshVMSpec extends Specification {

  BoshVM vm
  def setup() {
    vm = new BoshVM("cloud_controller_worker-partition-31e36c66f26d6a07fd24/0 (a5f81bae-4fd5-4ce1-aef0-54ce4b70adf6)")
  }

  def "should match type cloud_controller_worker-"() {
    expect:
    vm.matchesType("cloud_controller_worker-")
  }

  def "index should be 0"() {
    expect:
    vm.index == 0
  }

  def "id should be cloud_controller_worker-partition-31e36c66f26d6a07fd24"() {
    expect:
    vm.id == "cloud_controller_worker-partition-31e36c66f26d6a07fd24"
  }

  def "check stop command"() {
    expect:
    vm.stopCommand() == "bosh -n stop cloud_controller_worker-partition-31e36c66f26d6a07fd24 0 --hard"
  }
}
