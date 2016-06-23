package com.eitan.pcfutils

class BoshVM {
  String id
  int index
  String type

  BoshVM(String vmString) {
    def justTheVm = discardGuid(vmString)
    def idxText
    (id, idxText) = justTheVm.trim().split("/")
    index = idxText.toInteger()
    type = extractType(id)
  }

  private String discardGuid(vmString) {
    vmString.split()[0]
  }

  private String extractType(id) {
    id.split("partition-")[0]
  }

  boolean matchesType(String type) {
    this.type == type
  }

  String stopCommand() {
    "bosh -n stop ${id} ${index} --hard"
  }
}
