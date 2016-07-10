package com.eitan.pcfutils

import spock.lang.Specification

class PCFUtilsCFOnlySpec extends Specification {

  PCFUtils pcfutils

  def setup() {
    pcfutils = new PCFUtils()
  }

  private List<String> sampleCFOnlyOutput() {
    this.getClass().getResource("/bosh-vms-cf-deployment.txt").readLines()
  }

  def "should filter non-vm lines from bosh vms output"() {
    given:
    def output = sampleCFOnlyOutput()

    when:
    def filtered = pcfutils.filterVMs(output)

    then:
    filtered.size() == 17
  }

  def "should parse vm from bosh output line"() {
    given:
    def line = """
| cloud_controller_worker-partition-31e36c66f26d6a07fd24/0 (a5f81bae-4fd5-4ce1-aef0-54ce4b70adf6)       | running | n/a | cloud_controller_worker-partition-31e36c66f26d6a07fd24       | 10.0.16.20 |
""".trim()

    when:
    def vm = pcfutils.filterVM(line)

    then:
    vm.index == 0
    vm.id == "cloud_controller_worker-partition-31e36c66f26d6a07fd24"
    vm.matchesType "cloud_controller_worker-"
  }

}
