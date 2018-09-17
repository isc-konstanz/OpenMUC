![emonmuc header](doc/img/emonmuc-logo.png)

EmonMUC (**e**nergy **mon**itoring **m**ulty **u**tility **c**ommunication) is an *unofficial fork* of the open-source project [OpenMUC](https://www.openmuc.org/), a software framework based on Java and OSGi, that simplifies the development of customized *monitoring, logging and control* systems. It can be used as a basis to flexibly implement anything from simple data loggers to complex SCADA systems. The main goal of OpenMUC is to shield the application developer of monitoring and control applications from the details of the communication protocol and data logging technologies.

This project focuses on the easy setup and configuration of household metering devices and their visualisation. All configurations of *OpenMUC* metering devices and further handling of monitored data is therefore managed by [emoncms](https://emoncms.org/), an open-source web application for processing, logging and visualising energy, temperature and other environmental data.


----------

# OpenMUC

At Fraunhofer ISE the flexible OpenMUC framework is used as a basis in various smart grid projects. Among other tasks it is used in energy management gateways to readout smart meters, control CHP units, monitor PV systems and control electric vehicle charging. Therefore the OpenMUC framework includes mostly communication protocol drivers from the energy domain. But due to its open and modular architecture there is virtually no limit to the number of applications that can be realized using OpenMUC.


## Features

In summary, OpenMUC features the following highlights:

- **Easy application development:** OpenMUC offers an abstract service for accessing data. Developers can focus on the applications logic rather than the details of the communication and data logging technology.

- **Integrated drivers:** Out of the box support for several popular communication protocols

    - CSV
    - DLMS/COSEM
    - eHz
    - IEC 60870-5-104
    - IEC 61850
    - IEC 62056-21
    - KNX
    - M-Bus (wired)
    - M-Bus (wireless)
    - Modbus
    - 1-Wire (Raspberry Pi)
    - GPIO (Raspberry Pi)
    - SNMP
    
- **External drivers:** New communication protocol drivers can easily be added through a plug-in interface.  
Additional known external communication protocols:

    - [HomeMatic (CC1101)](https://github.com/isc-konstanz/OpenHomeMatic)
    - [P-CHARGE](https://github.com/isc-konstanz/OpenPCharge)
    - [SolarEdge API](https://github.com/isc-konstanz/OpenSolarEdge)


- **Modularity:** Drivers, data loggers etc. are all individual components. By selecting only the components you need you can create a very light weight system.

- **Embedded systems:** The framework is designed to run on low-power embedded devices. It is currently being used on embedded x86 and ARM systems. Because OpenMUC is based on Java and OSGi it is platform independent.

- **Open-source:** The software is being developed at the Fraunhofer Institute for Solar Energy Systems in Freiburg, Germany and is licensed under the GPLv3. They sell individually negotiated licenses upon request.


## Emoncms

Utilizing emoncms possibility to add standalone extensions, a designated PHP module is part of this project, to configure the device communication within the emoncms web views and to incorporate as much helpful tooltips, descriptions and templates as possible, to allow an easy configuration of metering devices.


----------

# Installation

The systems setup guide was documented for Linux based platforms, specifically *Raspbian Stretch*, but further guides may follow.

Recommended:

- [Ubuntu / Debian Linux via git](doc/LinuxInstall.md)


----------

# First steps

With the system being prepared, some first steps can be taken to learn about the features of emonmuc.
For this purpose, a [First Steps guide](doc/FirstSteps.md) was documented to be followed.


----------

# Contact

To get in contact with the developers of the OpenMUC project, visit their homepage at [openmuc.org](https://www.openmuc.org/).  
This fork is maintained by:

![ISC logo](doc/img/isc-logo.png)

- **[ISC Konstanz](http://isc-konstanz.de/)** (International Solar Energy Research Center)
- **Adrian Minde**: adrian.minde@isc-konstanz.de
