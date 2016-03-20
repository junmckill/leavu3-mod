# Leavu3

Leavu is a software for building home cockpits for DCS Flaming Cliffs. It uses [Dcs Remote 2](https://github.com/GiGurra/dcs-remote2) to communicate with DCS. Leavu currently includes one instrument - a generic MFD with 3 pages:
 * HSD
 * RWR
 * INFO


### INSTALLATION

* Make sure you have java 8 or greater installed
* [Download dcs-remote2.zip and leavu3.zip](http://build.culvertsoft.se/dcs/)
* Unzip both where you like
* Copy the files inside the dcs-remote2/lua/ folder into your Dcs/Scripts/ folder
  * e.g C:\Users\<username>\Saved Games\DCS\Scripts
* Launch dcs-remote2.jar on your game PC
  * Verify a tray icon appears (black square with the text *oo DCS* inside it)
* Launch leavu3.jar (an MFD should appear)
  * Click INF to get to the INFO page and verify Dcs Remote is connected


### CONFIGURATION

**To use it solo on the same PC as DCS, no configuration is needed** - however - You can also run it on a separate computer. 
To run Leavu3 on a different computer than your game PC, edit the leavu3-cfg.json file and input the ip address of your game PC. NOTE: DCS Remote **must** be running on your game PC.

Leavu can be configured to connect to a datalink. The datalink server is just a separate instance of dcs-remote2. To configure datalink parameters check your dcs-remote2 folder for the file *static-data.json* and fill in your parameters for:
 * data link host
 * data link port
 * data link callsign
 * data link team

**By default your datalink host is set to build.culvertsoft.se, team: BLUE_RABBITS, callsign: JarJar**

You can also configure parameters for window position, size, AA, update rates, etc. Check the leavu3-cfg.json for a full list of configurable parameters.


### WHERE IS THE DATA COMING FROM?

Leavu3 uses [dcs-remote2](https://github.com/GiGurra/dcs-remote2) to inject a [data export script](https://github.com/GiGurra/leavu3/blob/master/src/main/resources/lua_scripts/LoDataExport.lua) into DCS, which then grabs ownship + AI wingman information from the game.

In order to display information regarding your human wingmen and their targets, you both need to be connected to the Leavu3 datalink (see above).


### COMPATIBILITY

Built with LibGDX for cross platform compatibility. Works out of the box on:
* Windows
* Linux
* Mac (untested)

If you know someone with android experience that wants to do an android build, send a pull request!
