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

  def "should sort vms in stop order"() {
    given:
    def output = sampleCFOnlyOutput()
    def filtered = pcfutils.filterVMs(output)
    def vms = filtered.collect { line -> pcfutils.filterVM(line) }

    when:
    def sortedVms = pcfutils.sortStopOrder(vms)

    then:
    sortedVms.size() == 17
    sortedVms[0].matchesType("loggregator_trafficcontroller-")
    sortedVms[1].matchesType("doppler-")

    sortedVms[2].matchesType("diego_cell-")
    sortedVms[2].index == 0
    sortedVms[3].matchesType("diego_cell-")
    sortedVms[3].index == 1
    sortedVms[4].matchesType("diego_cell-")
    sortedVms[4].index == 2

    sortedVms[5].matchesType("diego_brain-")
    sortedVms[6].matchesType("uaa-")
    sortedVms[7].matchesType("cloud_controller_worker-")
    sortedVms[8].matchesType("clock_global-")
    sortedVms[9].matchesType("cloud_controller-")
    sortedVms[10].matchesType("mysql-")
    sortedVms[11].matchesType("mysql_proxy-")
    sortedVms[12].matchesType("router-")
    sortedVms[13].matchesType("diego_database-")
    sortedVms[14].matchesType("etcd_server-")
    sortedVms[15].matchesType("consul_server-")
    sortedVms[16].matchesType("nats-")
  }

  def "should sort vms in start order"() {
    given:
    def output = sampleCFOnlyOutput()
    def filtered = pcfutils.filterVMs(output)
    def vms = filtered.collect { line -> pcfutils.filterVM(line) }

    when:
    def sortedVms = pcfutils.sortStartOrder(vms)

    then:
    sortedVms.size() == 17

    sortedVms[0].matchesType("nats-")
    sortedVms[1].matchesType("consul_server-")
    sortedVms[2].matchesType("etcd_server-")
    sortedVms[3].matchesType("diego_database-")
    sortedVms[4].matchesType("router-")
    sortedVms[5].matchesType("mysql_proxy-")
    sortedVms[6].matchesType("mysql-")
    sortedVms[7].matchesType("cloud_controller-")
    sortedVms[8].matchesType("clock_global-")
    sortedVms[9].matchesType("cloud_controller_worker-")
    sortedVms[10].matchesType("uaa-")
    sortedVms[11].matchesType("diego_brain-")

    sortedVms[12].matchesType("diego_cell-")
    sortedVms[12].index == 0
    sortedVms[13].matchesType("diego_cell-")
    sortedVms[13].index == 1
    sortedVms[14].matchesType("diego_cell-")
    sortedVms[14].index == 2

    sortedVms[15].matchesType("doppler-")
    sortedVms[16].matchesType("loggregator_trafficcontroller-")
  }
}
