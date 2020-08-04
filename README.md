**Convert RDF to Neo4J Graph**

This utility provides a wrapper for the [Neosemantics plugin](https://neo4j.com/labs/neosemantics-rdf/) to import a TURTLE file into a Neo4j Property Graph.

Before getting started, you will need to download a [.jar](https://github.com/neo4j-labs/neosemantics/releases) of the neosemantics Neo4j plugin. We can confirm compatibility with version 3.5.0.4, and haven't tested any preceding or following verions yet. Put the .jar in a directory called `plugins/` at the base of the cloned directory. You will also need [SBT](https://www.scala-sbt.org/) installed to run the Scala program.

To run the program, you must supply two command line arguments to the SBT console: the location of a TURTLE RDF file, and the location where the output Neo4j graph directory should be placed.

`run myTurtleFile.ttl output_dir`

The TURTLE file will be read into the Neo4j graph using the default neosemantics settings. We have not yet included functionality to alter the neosemantics settings via a configuration file. To change the neosemantics settings, it shouldn't be too much trouble to modify the code in the `Neo4jConnector` object.
