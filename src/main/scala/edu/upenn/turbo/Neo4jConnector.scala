package edu.upenn.turbo

import java.io.File
import scala.reflect.io.Directory

import org.neo4j.graphdb._
import org.neo4j.graphdb.factory.GraphDatabaseFactory
import scala.collection.mutable.ArrayBuffer

import org.neo4j.kernel.internal.GraphDatabaseAPI
import org.neo4j.kernel.impl.proc.Procedures

import java.nio.file.Path
import java.nio.file.Paths
import java.io.PrintWriter

import scala.collection.JavaConverters._

object Neo4jConnector 
{
    var deleteOutputDir = false
    def main(args: Array[String]): Unit =
    {
        if (args.size < 2) throw new RuntimeException("Run command must be followed by 2 arguments: input TURTLE file location and output Neo4j directory location")
        loadRDFUsingNeosemantics(args(0), args(1))
    }
    
    def loadRDFUsingNeosemantics(file: String, output: String)
    {
        // delete output directory if exists
        val directory = new Directory(new File(output))
        directory.deleteRecursively()

        val graphDb: GraphDatabaseService = new GraphDatabaseFactory()
            .newEmbeddedDatabaseBuilder(new File (output))
            .loadPropertiesFromFile(s"conf//neo4j.conf").newGraphDatabase() 
            
        try
        {   
            val filePath = new File(file)
            val fileString = filePath.getCanonicalFile().getAbsolutePath().replace('\\', '/')
            println("Input file: " + fileString)
            println("Output directory: " + output)

            // this index is required for neosemantics to work properly
            val createResourceIndex: String = "CREATE INDEX ON :Resource(uri)"
            graphDb.execute(createResourceIndex)
                        
            addNamespaces(graphDb)
            val query: String = s"""CALL semantics.importRDF("file:///$fileString","Turtle")"""
            graphDb.execute(query)
            
            println("Imported RDF data")
            val checksPassed = checkLabelCounts(graphDb)
            if (checksPassed) println("All checks passed")
            else println("There were failing checks; see the output above for more information")
        }
        catch
        {
            case e: Throwable => 
            {
                println(e.printStackTrace)
                deleteOutputDir = true
            }
        }
        finally
        {
            graphDb.shutdown 
            if (deleteOutputDir)
            {
                val directory = new Directory(new File(output))
                directory.deleteRecursively()
            }
            System.exit(0)
        }
    }

    def checkLabelCounts(graphDb: GraphDatabaseService): Boolean =
    {
        var checksPassed = true
        var foundLabelsToCheck = false
        for (label <- scala.io.Source.fromFile("conf//requiredNodeLabels.conf").getLines)
        {
            foundLabelsToCheck = true
            val labelWithUnderscores = label.replaceAll("\\:","__")
            val result = graphDb.execute(s"Match (n:$labelWithUnderscores) return count(n) as count")
            while ( result.hasNext() )
            {
                val row = result.next();
                for ( column <- row.entrySet().asScala )
                {
                    println("Found " + column.getValue() + " instances of nodes with label " + label)
                    if (column.getValue().toString == "0") checksPassed = false   
                }
            }
        }
        if (!foundLabelsToCheck) println("Did not find any required labels in required labels configuration file")
        checksPassed
    }

    def addNamespaces(graphDb: GraphDatabaseService)
    {
        var foundNamespaces = false
        for (namespace <- scala.io.Source.fromFile("conf//namespaces.conf").getLines)
        {
            foundNamespaces = true
            val splitByFirstColon = namespace.split("\\:",2)
            val prefix = splitByFirstColon(0)
            val uri = splitByFirstColon(1)
            println(s"Adding prefix $prefix for namespace $uri")
            val strToExecute = s"""call semantics.addNamespacePrefix("$prefix", "$uri")"""
            graphDb.execute(strToExecute)
        }
        if (!foundNamespaces) println("Did not find any namespaces in namespaces configuration file")
    }
}