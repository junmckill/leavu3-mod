# Leavu3

Leavu is a software for building glass cockpits for DCS Flaming Cliffs. It uses [Dcs Remote 2](https://github.com/GiGurra/dcs-remote2) to communicate with DCS. Leavu currently includes one instrument - a generic MFD with 3 pages:
 * HSD
 * RWR
 * INFO


### INSTALLATION

* Make sure you have java 8 or greater installed
* [Download dcs-remote2.zip and leavu3.zip](http://build.culvertsoft.se/dcs/)
* Unzip both where you like
* Copy the files inside the dcs-remote2/lua/ folder into your Dcs/Scripts/ folder
  * e.g C:\Users\<username>\Saved Games\DCS\Scripts
* Launch dcs-remote2.jar
  * Verify a tray icon appears (black square with the text **oo DCS** inside it)
* Launch leavu3.jar
  * Click INF to get to the INFO page and verify Dcs Remote is connected


### CONFIGURATION

To use it solo, no configuration is needed - however - Leavu can be configured to connect to a datalink. The datalink server is just a separate instance of dcs-remote2. To configure datalink parameters check your dcs-remote2 folder for the file **static-data.json** and fill in your parameters for:
 * data link host
 * data link port
 * data link callsign
 * data link team


### COMPATIBILITY

Built with LibGDX for cross platform compatibility. Works out of the box on:
* Windows
* Linux
* Mac (untested)

If you know someone with android experience that wants to do an android build, send a pull request!
