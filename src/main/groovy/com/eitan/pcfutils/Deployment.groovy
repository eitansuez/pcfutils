package com.eitan.pcfutils

class Deployment {
  String type
  String id
  List<BoshVM> vms

  def Deployment(id) {
    this.id = id
    vms = []
    type = type(id)
  }

  static String type(id) {
    id.substring(0, id.lastIndexOf('-'))
  }
  static boolean isCf(id) {
    type(id) == 'cf'
  }

  String setCommand() {
    "bosh deployment /var/tempest/workspaces/default/deployments/${id}.yml"
  }

  boolean matchesType(String type) {
    this.type == type
  }

  List<BoshVM> vmsInStopOrder() { vms }
  List<BoshVM> vmsInStartOrder() { vms }

  void stop(PrintWriter pw) {
    pw.println setCommand()
    vmsInStopOrder().each { vm ->
      pw.println vm.stopCommand()
    }
  }

  void start(PrintWriter pw) {
    pw.println setCommand()
    vmsInStartOrder().each { vm ->
      pw.println vm.startCommand()
    }
    pw.println "bosh cloudcheck"
  }

}

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
