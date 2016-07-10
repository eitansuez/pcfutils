package com.eitan.pcfutils

import spock.lang.Specification

class CfDeploymentSpec extends Specification {

  private List<String> sampleCFOnlyOutput() {
    this.getClass().getResource("/bosh-vms-cf-deployment.txt").readLines()
  }

  CfDeployment deployment

  def setup() {
    PCFUtils pcfutils = new PCFUtils()
    def filtered = pcfutils.filterVMs(sampleCFOnlyOutput())
    deployment = new CfDeployment('cf-1b2be785ec079a610bad', pcfutils.stopOrder, pcfutils.startOrder)
    deployment.vms = filtered.collect { line -> pcfutils.filterVM(line) }
  }

  def "should sort vms in stop order"() {
    when:
    def sortedVms = deployment.vmsInStopOrder()

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
    when:
    def sortedVms = deployment.vmsInStartOrder()

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
