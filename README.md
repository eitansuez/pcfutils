This project is an attempt to automate the shutdown and startup of a PCF installation, per these instructions:

http://docs.pivotal.io/pivotalcf/1-7/customizing/start-stop-vms.html

Written in groovy, the functionality is exposed via a gradle task:

    stopStartPcf

The way this works is that you upload a distribution of this project to your ops manager box, and invoke this task from there.  The gradle wrapper makes sure that you don't need to provision anything yourself.

So, locally:

    gradle distZip
    scp ./build/distributions/pcfutils.zip ubuntu@<your_ops_manager>:

Then, ssh to your ops manager, and from there:

    unzip pcfutils.zip
    cd pcfutils
    ./gradlew stopStartPcf -PpcfTestMode=false

This will produce a stop script and a start script, each ensuring the order in which the elastic runtime vms are stopped and started.  The script also makes sure that the elastic runtime vms are started first and stopped last.

You can then inspect and modify these scripts to your satisfaction and invoke them to stop and retstart your specific PCF installation.

NOTE:  non-elastic runtime deployments may also have ordering considerations which are not taken into account here.  It may be that service brokers need to be stopped before other vms in that deployment.  This needs to be investigated.

Use at your own risk.


