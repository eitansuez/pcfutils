package com.eitan.pcfutils

import spock.lang.Specification
import spock.lang.Unroll

class DeploymentSpec extends Specification {

  @Unroll
  def "type for deployment #id should be #type"(id, type) {
    when:
    def d = new Deployment(id)

    then:
    d.type == type

    where:
    id      | type
    'cf-1b2be785ec079a610bad'      | 'cf'
    'p-mysql-2ec933c894008afa9fb6' | 'p-mysql'
    'p-spring-cloud-services-cab77ba1709f368fea0b' | 'p-spring-cloud-services'
  }
}
