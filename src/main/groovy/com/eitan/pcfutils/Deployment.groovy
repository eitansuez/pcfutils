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

