package com.eitan.pcfutils

/* Models the Elastic Runtime deployment */
class CfDeployment extends Deployment {

  List<String> stopOrder, startOrder

  CfDeployment(id, List<String> stopOrder, List<String> startOrder) {
    super(id)
    this.stopOrder = stopOrder
    this.startOrder = startOrder
  }

  @Override
  List<BoshVM> vmsInStopOrder() {
    vms.sort { BoshVM vm -> stopOrder.indexOf(vm.type) }
  }

  @Override
  List<BoshVM> vmsInStartOrder() {
    vms.sort { BoshVM vm -> startOrder.indexOf(vm.type) }
  }

}
