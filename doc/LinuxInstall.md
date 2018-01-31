![emonmuc header](https://github.com/isc-konstanz/emonmuc/blob/master/doc/img/emonmuc-logo.png)

This document describes how to install emonmuc (**e**nergy **mon**itoring **m**ulty **u**tility **c**ommunication), an open-source protocoll driver project to enable the communication with a variety of metering or other devices, developed based on the [OpenMUC](https://www.openmuc.org/) project.


---------------

# 1 Install emoncms

First, the emoncms webserver needs to be installed and running.  
The project provides detailed installation guides for several platforms. Recommended for this guide are Linux oriented instructions:

- [Ubuntu / Debian Linux via git](https://github.com/emoncms/emoncms/blob/master/docs/LinuxInstall.md)

- [Raspbian Jessie](https://github.com/emoncms/emoncms/blob/master/docs/RaspberryPi/readme.md)


---------------

# 2 Install emonmuc

First, create some necessary directories for the installation and data and set the right permissions
~~~
sudo mkdir /var/{lib,log,run}/emonmuc
sudo chown ctrl:root /var/{lib,log,run}/emonmuc
~~~

Now, the emonmuc application can be installed either via git or simply copied in a subdirection */opt/emonmuc*.

Git is a source code management and revision control system but at this stage it is just used to download and update the emoncms application. After downloading, the right permissions need to be set:
~~~
sudo git clone -b stable https://github.com/isc-konstanz/emonmuc.git /opt/emonmuc
sudo chown ctrl:root -R /opt/emonmuc
~~~


## 2.1 Emonmuc settings

For some configurations, the settings may be necessary to be adjusted. All settings can be found in *emoncms.conf*.  
This file needs to be copied from the provided default config first:  
~~~
cp /opt/emonmuc/conf/emoncms.default.conf /opt/emonmuc/conf/emoncms.conf
nano /opt/emonmuc/conf/emoncms.conf
~~~

- The web servers location may be updated. By default, it is commented and points to an emoncms sever at localhost, e.g. VPN addresses or the remote emoncms.org server can be a valid selection though.  
   >     # URL of emoncms web server, used to post data
   >     address = https://emoncms.org/

- A default authentication for emoncms may be configured. While each data channel can be configured to have its own credentials, it may be preferable to group them with the same authentication, as this improves bulk posting and hence reduced traffic.  
To do this, **uncomment the lines** related to authorization and authentication, and enter the users Write Api Key  
   >     # API Key credentials to authorize communication with the emoncms webserver
   >     authorization = WRITE
   >     authentication = <apiKey>

- The maximum allowed threads for the emoncms logger to post values simultaniously to the specified webserver may be configured, if the configured server and the platform supports or needs higher traffic  
   >     # Set the maximum amount of IPC threads running synchronously. Default is 1
   >     maxThreads = 10

## 2.2 Emoncms module

Inside the projects direcotry is the designated emoncms module, needed to be linked to the emoncms dir
~~~
sudo chown www-data:root -R /opt/emonmuc/projects/emoncms/Modules
sudo ln -s /opt/emonmuc/projects/emoncms/Modules/muc /var/www/emoncms/Modules/muc
~~~

Then, check for Database upates in the Administration pane for the necessary tables to be created.


## 2.3 System service

To provide the comfortable starting, stopping or automatic execution at boot, a systemd service is provided to install:
~~~
sudo chmod ugo+x /opt/emonmuc/bin/emonmuc
sudo cp /opt/emonmuc/bin/emonmuc.service /lib/systemd/system/emonmuc.service
sudo systemctl enable emonmuc.service
~~~

With `/var/run/emonmuc` being located in a tmpfs and not created automatically at boot, this needs to be taken care of, for the service to work properly.
This will be handled by systemds' service **tmpfiles**, which can be configured in `/usr/lib/tmpfiles.d/`:

Create the configuration file *emonmuc.conf*
~~~
sudo nano /usr/lib/tmpfiles.d/emonmuc.conf
~~~
and add the line
>     d /var/run/emonmuc 0755 ctrl root -

The application will now start at boot and can be started with
~~~
sudo systemctl start emonmuc
~~~
as well as other systemctl commands *[start|restart|stop|status]*


## 2.4 System script

Emonmuc provides a run-script, allowing the framework to be configured, started and stopped comfortably via bash shell commands.

Configure this script to be part of the the users environment variable *path* in `~/.bashrc`:
~~~
export PATH=$PATH:/opt/emonmuc/bin
~~~

**After a reboot or logout**, several basic commands are available:

 - Start the framework: `emonmuc start`
 - Stop the framework: `emonmuc stop`
 - Restart the framework: `emonmuc restart`
 - Reload configuration: `emonmuc reload`

If desired, the framework may be started in the foreground, by passing the option **-fg**
~~~
emonmuc start -fg
~~~

Further, the script allows the configuration of apps, drivers, or other bundles, registered to the framework.


### 2.4.1 Protocol drivers

By default, no drivers are enabled. As a first step, a set of protocol drivers ought to be used should be selected.  
This can be done with their unique ID, e.g. to enable the **CSV** driver:
~~~
emonmuc enable driver csv
~~~

To disable the driver, use
~~~
emonmuc disable driver csv
~~~

Several drivers can be enabled at once, while each needs to be selected individually. A list of possible drivers are:

 - **csv**: Read CSV files
 - **dlms**: DLMS/COSEM
 - **ehz**: eHz meters
 - **iec60870**: IEC 60870-5-104
 - **iec61850**: IEC 61850
 - **iec62056p21**: IEC 62056-21
 - **knx**: KNX
 - **mbus**: M-Bus (wired)
 - **wmbus**: M-Bus (wireless)
 - **modbus**: Modbus TCP
 - **rpi-gpio**: GPIO (Raspberry Pi)
 - **rpi-s0**: S0 (Raspberry Pi)
 - **rpi-w1**: 1-Wire (Raspberry Pi)
 - **snmp**: SNMP
 
Details about most drivers and specific information about their usage and configuration may be found in the [OpenMUC User Guide](https://www.openmuc.org/openmuc/user-guide/).


---------------

# 3 Setup

With both components installed and running, the OpenMUC framework needs to be registered to the emoncms user. This can be done in the Controllers page, accessible at **Setup->Device Connections** from the menu.

![emonmuc controllers](https://github.com/isc-konstanz/emonmuc/blob/master/doc/img/emonmuc-controllers.JPG)

Click **Controllers->New controller** and confirm the default settings, if the framework is running on the same machine.  
Energy meters and other utility devices connected to the platform can now be configured in the **Device Connections** site.

To learn about the features of EmonMUC, a [First Steps guide](https://github.com/isc-konstanz/emonmuc/blob/master/doc/FirstSteps.md) was documented and may be followed.
