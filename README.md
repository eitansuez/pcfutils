This project is an attempt to automate the shutdown and startup of a PCF installation, per these instructions:

http://docs.pivotal.io/pivotalcf/1-7/customizing/start-stop-vms.html

Written in groovy, with a gradle build.  Two tasks are exposed via gradle:

    startPcf
    stopPcf

The way this works is that you upload a distribution of this project to your ops manager box, and invoke these tasks from there.  The gradle wrapper makes sure that you don't need to provision anything yourself.

So, locally:

    gradle distZip
    scp ./build/distributions/pcfutils.zip ubuntu@<your_ops_manager>:

Then, ssh to your ops manager, and from there:

    unzip pcfutils.zip
    cd pcfutils
    ./gradlew stopPcf -PpcfTestMode=false

and wait patiently as the script will, one by one, stop each of your PCF installation's VMs, paying attention to the order in which VMs are shutdown for the elastic runtime deployment.

Use at your own risk.

TODO:

I discovered that stopping vms actually terminates them in aws, and the subsequent output of the "bosh vms" command shows they've gone.  So, in order to bring a PCF installation back up, the stop task needs to save the list of vms that were stopped, so it can be used as input by the start script.  Once this is implemented then you will be able to..


To bring your PCF installation back:

    ./gradlew startPcf -PpcfTestMode=false

Thanks,
/ Eitan

