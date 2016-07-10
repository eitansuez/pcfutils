package com.eitan.pcfutils

class CfInstallation {

  Map deployments

  def makeScripts(File stopScript, File startScript) {
    makeStopScript(stopScript)
    makeStartScript(startScript)
  }

  def writeShellScript(stream, closure) {
    stream.withPrintWriter { Writer pw ->
      pw.println '#!/bin/sh'
      pw.println()

      closure.call(pw)
    }
  }

  def vmResurrection(onOrOff) { "bosh -n vm resurrection ${onOrOff}" }

  Comparator cfFirst = { Deployment d1, Deployment d2 -> d1 instanceof CfDeployment ? -1 : 1 }
  Comparator cfLast = { Deployment d1, Deployment d2 -> d2 instanceof CfDeployment ? -1 : 1 }

  def makeStopScript(stream) {
    writeShellScript(stream) { pw ->
      pw.println(vmResurrection("off"))
      pw.println()

      def sortedDeployments = deployments.values().sort(false, cfLast)

      sortedDeployments.each { Deployment deployment ->
        deployment.stop(pw)
        pw.println()
      }
    }
  }

  def makeStartScript(stream) {
    writeShellScript(stream) { pw ->

      def sortedDeployments = deployments.values().sort(false, cfFirst)

      sortedDeployments.each { Deployment d ->
        d.start(pw)
        pw.println()
      }

      pw.println(vmResurrection("on"))
      pw.println()
    }

  }
}
