package com.eitan.pcfutils

import spock.lang.Specification

class PCFUtilsSpec extends Specification {

  PCFUtils pcfutils

  def setup() {
    pcfutils = new PCFUtils()
  }

  private List<String> sampleMultiDeploymentOutput() {
    this.getClass().getResource("/bosh-vms.txt").readLines()
  }

  def "should parse vms by deployment"() {
    given:
    def output = sampleMultiDeploymentOutput()

    when:
    def deployments = pcfutils.parseVMsByDeployment(output)

    then:
    deployments.size() == 5

    and:
    deployments['cf'].id == 'cf-1b2be785ec079a610bad'

    and:
    deployments['cf'].vms.size() == 17
  }


}
