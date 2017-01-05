ASGresyn
--------

ASGresyn is a resynthesis tool, which implements a Breeze netlist in an optimised manner using STG based logic synthesis.

### Installation ###

Due to the operating system limitation of Balsa, ASGresyn is only available for UNIX-based systems (on UNIX with 64-bit architecture, 32-bit support is required). Balsa may need the following additional packages:  

* The multiprecision arithmetic library libgmp3c2 (32bit version). For Debian-based Linux distributions [this](http://www.ubuntuupdates.org/package/core/precise/universe/base/libgmp3c2) version should work.
* The Scheme interpreter Guile 1.x (2.x won't work). For Debian-based Linux distributions execute `sudo apt-get install guile-1.8`

Download and unpack the ASGresyn package. All external tools needed for operation are included in the package (except for the optional tool petreset, because licensing is unclear). You don't have to install anything or make changes to environment variables (except for the libraries needed by Balsa mentioned above). To run it you will need a Java runtime environment (JRE) v1.7 (or later).

### Configuration ###

##### Main configuration file #####

The default configuration file is `ASGresyn_DIR/config/resynconfig.xml`. You can specify another config file with the `-cfg <file>` option of ASGresyn.

The `<workdir>` tag specifies a path where ASGresyn stores all temporary files during operation. The default value is empty (and therefore a default operating system directory is used). You can override these settings with `-w <dir>` option of ASGresyn.

With the `<tools>` tag (and subtags) you can specify the command lines for calling external tools. Defaults are the included versions of the tools. For data path optimisation, the Synopsys Design Compiler is assumed to located on an external server. Thus, an additonal tag is required:
```xml
<tools>
	...
	<designCompilerCmd>
		<hostname></hostname>
		<username></username>
		<password></password>
		<workingdir></workingdir>
	</designCompilerCmd>
</tools>
```
If your Design Compiler executable differs from `dc_shell`, please adjust this in `ASGresyn_DIR/templates/resyn_dpopt.sh`.

With the `<components>` tag you can specify the location of the components configuration file (see next section). By default the version included in the ASGresyn jar file is used.

##### Components configuration file #####

The components configuration file contains handshake component definitions and specifies how to handle the data path implementations (if present) of these components.

You can find the XML schema [here](src/main/resources/schema/components_config.xsd).

### Usage ###

For the following example commands it is assumed that your current working directory is the ASGresyn main directory. If you want run ASGresyn from another directory you have to add the path to the ASGresyn main directory in front of the following commands (or you could add the `bin/` directory to your `PATH` variable).

##### List of supported arguments #####

To see a list of supported command line arguments execute

    bin/ASGresyn

##### Resynthesis #####

To to implement a Breeze netlist with default configuration execute

    bin/ASGresyn -lib library.xml example.breeze

The `-lib` option expects a technology library xml file in the following format:

```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<technology>
	<balsa>
		<style>resyn</style>
		<tech>example</tech>
	</balsa>
	<genlib>
		<libfile>gen.lib</libfile>
	</genlib>
</technology>
```

The `<balsa>` tag defines the technology which will be used by balsa-netlist. The `<style>` tag defines the implementation style to be used. Balsa-defaults are e.g. four_b_rb or dual_b. ASGresyn comes with a new style `resyn` which implements a appropriate data path for our STG based control. If you want to implement another one, it has to placed in `ASGresyn_DIR/tools/balsa/share/style`. The `<tech>` tag defines the implementation technology to be used. By default ASGresyn comes with no technology. You have to use your own. It must be located in `ASGresyn_DIR/tools/balsa/share/tech`. The `<genlib>` tag defines the technology file used by e.g. Petrify or ASGlogic. The file path is relative to the file path of the technology library xml file. The file must be in [genlib format](https://www.ece.cmu.edu/~ee760/760docs/genlib.pdf).

The command will create the files `resyn.v`, `resyn.log` and `resyn.zip`. `resyn.v` contains the Verilog implementation of the Breeze file. `resyn.log` is the log file of the operation. `resyn.zip` contains all temporary files created during operation. You can change these default filenames with the following parameters:

* `-out` specifies the filename of the Verilog implementation
* `-log` specifies the filename of the log file
* `-zip` specifies the filename of the zipped temporary files

##### Tackle complexity strategy #####

With the `-tc` option of ASGresyn you can specify how complexity should be tackled. There are currently two options available:

* S: Straight (or direct) synthesis, meaning no complexity reduction is applied
* D: (STG) Decomposition based synthesis: After generating the Balsa STG (specifying the control of all HS components), it is decomposed and all decomposition components are synthesised on their own.

If you combine these options, ASGresyn will try to implement the circuit with the strategies from left to right. Default is `SD`, meaning ASGresyn will at first try a direct implementation of the Breeze file and if it fails, the decomposition based approach is applied. 

You can further configure which decomposition algorithm (`-d`) and which partition heuristic (`-p`) is applied by the decomposition based approach. Defaults are `-d breeze -p common-cause`. Have a look at [DesiJ](https://github.com/hpiasg/desij)s command line options for more implemented algorithms/partitions.

##### Logic synthesis parameter #####

With the `-ls` option of ASGresyn you can configure logic synthesis. It requires 4 letters with the following meaning:

* 1st: CSC solver
  * P: Petrify
  * M: PUNF/MPSAT
* 2nd: Logic synthesis
  * P: Petrify
  * A: ASGlogic
* 3rd: Technology mapping
  * P: Petrify
  * N: No technology mapping
  * A: ASGlogic
* 4th: Reset insertion
  * P: Petrify
  * I: Petreset (This tool is not included in the bundle)
  * A: ASGlogic

Allowed combinations for 2nd-4th are [PPP, PNP, PPI, AAA]. Default is `-ls PAAA`.

##### Data path optimisation #####

With the `-odp` option, data path optimisation is enabled.

### Build instructions ###

To build ASGresyn, Apache Maven v3 (or later) and the Java Development Kit (JDK) v1.7 (or later) are required.

1. Build [ASGcommon](https://github.com/hpiasg/asgcommon)
2. Build [ASGlogic](https://github.com/hpiasg/asglogic)
3. Build [DesiJ](https://github.com/hpiasg/desij)
4. Execute `mvn clean install -DskipTests`
