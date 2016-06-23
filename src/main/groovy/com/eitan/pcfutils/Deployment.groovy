package com.eitan.pcfutils

class Deployment {
  String type
  String id
  List<BoshVM> vms

  def Deployment(id) {
    this.id = id
    vms = []
    type = deriveType(id)
  }

  private String deriveType(id) {
    id.substring(0, id.lastIndexOf('-'))
  }
}
