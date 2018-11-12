![OpenMUC logo](https://www.openmuc.org/wp-content/uploads/2016/03/Logo.png)

**This is an unofficial fork of the OpenMUC project**, a software framework based on Java and OSGi that simplifies the development of customized *monitoring, logging and control* systems. It can be used as a basis to flexibly implement anything from simple data loggers to complex SCADA systems. The main goal of OpenMUC is to shield the application developer of monitoring and control applications from the details of the communication protocol and data logging technologies. Third parties are encouraged to create their own customized systems based on OpenMUC. OpenMUC is licensed under the GPL. We sell individual licenses on request.


# Applications

At Fraunhofer ISE the flexible OpenMUC framework is used as a basis in various smart grid projects. Among other tasks it is used in energy management gateways to readout smart meters, control CHP units, monitor PV systems and control electric vehicle charging. Therefore the OpenMUC framework includes mostly communication protocol drivers from the energy domain. But due to its open and modular architecture there is virtually no limit to the number of applications that can be realized using OpenMUC.


# Features

In summary OpenMUC features the following highlights:

- **Easy application development:** OpenMUC offers an abstract service for accessing data. Developers can focus on the application’s logic rather than the details of the communication and data logging technology.
    
- **Simple and flexible configuration:** All communication and data logging parameters can be dynamically configured through a central file, the framework’s configuration service or a web interface.

- **Communication support:** Out of the box support for several popular communication protocols. New communication protocol drivers can easily be added through a plug-in interface.

- **Data logging:** Data can be logged in two formats (ASCII & binary). New data loggers can easily be added through a plug-in interface.

- **Web interface:** Convenient user interface for configuration and visualization.

- **Data servers:** Remote applications (e.g. smart phone apps, cloud applications) or local non-Java applications can access OpenMUC through one of the available data servers (e.g. a RESTful Web Service, Modbus server).

- **Modularity:** Drivers, data loggers etc. are all individual components. By selecting only the components you need you can create a very light weight system.

- **Embedded systems:** The framework is designed to run on low-power embedded devices. It is currently being used on embedded x86 and ARM systems. Because OpenMUC is based on Java and OSGi it is platform independent.

- **Open-source:** The software is being developed at the Fraunhofer Institute for Solar Energy Systems in Freiburg, Germany and is licensed under the GPLv3. We sell individually negotiated licenses upon request.


# Contact

To get in contact with the projects developers, visit their homepage at [openmuc.org](https://www.openmuc.org/).  
This fork is maintained by:

- **Adian Minde**: adrian.minde@isc-konstanz.de
- **ISC** Konstanz (International Solar Energy Research Center)
