package edu.upenn.turbo

import java.io.File
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
    def main(args: Array[String]): Unit =
    {
        loadRDFUsingNeosemantics(args(0), args(1))
    }
    
    def loadRDFUsingNeosemantics(file: String, output: String)
    {
        val graphDb: GraphDatabaseService = new GraphDatabaseFactory()
            .newEmbeddedDatabaseBuilder(new File (output))
            .loadPropertiesFromFile(s"conf//neo4j.conf").newGraphDatabase() 
            
        try
        {   
            val filePath = new File(file)
            val fileString = filePath.getCanonicalFile().getAbsolutePath().replace('\\', '/')
            println(fileString)
            val createResourceIndex: String = "CREATE INDEX ON :Resource(uri)"
            graphDb.execute(createResourceIndex)
            
            println("created index on :Resource(uri)")
            
            println("Beginning transaction")
            addNamespaces(graphDb)

            val query: String = s"""CALL semantics.importRDF("file:///$fileString","Turtle")"""
            graphDb.execute(query)
            
            println("imported RDF data")
            println("transaction successful")

            printLabelCounts(graphDb)
        }
        finally
        {
            graphDb.shutdown 
            println("shut down graph server")
            System.exit(0)
        }
    }

    def printLabelCounts(graphDb: GraphDatabaseService)
    {
        val mondoCountRes = graphDb.execute("Match (n:graphBuilder__mondoDiseaseClass) return count(n) as MondoClassCount")
        val icd9CountRes = graphDb.execute("Match (n:graphBuilder__icd9Class) return count(n) as ICD9ClassCount")
        val icd10CountRes = graphDb.execute("Match (n:graphBuilder__icd10Class) return count(n) as ICD10ClassCount")
        val snomedCountRes = graphDb.execute("Match (n:graphBuilder__snomedDisorderClass) return count(n) as snomedDisorderClass")

        val countsToQuery = Array(mondoCountRes, icd9CountRes, icd10CountRes, snomedCountRes)

        for (countQuery <- countsToQuery)
        {
            while ( countQuery.hasNext() )
            {
                val row = countQuery.next();
                for ( column <- row.entrySet().asScala )
                {
                    println(column.getKey() + ": " + column.getValue() + "; ")
                }
            }
        }
    }

    def addNamespaces(graphDb: GraphDatabaseService)
    {
        println("adding namespaces")
        val namespaceCalls = Array(
            """call semantics.addNamespacePrefix("owl","http://www.w3.org/2002/07/owl#")""",
            """call semantics.addNamespacePrefix("dct","http://purl.org/dc/terms/")""",
            """call semantics.addNamespacePrefix("rdfs","http://www.w3.org/2000/01/rdf-schema#")""",
            """call semantics.addNamespacePrefix("obo","http://purl.obolibrary.org/obo/")""",
            """call semantics.addNamespacePrefix("dc","http://purl.org/dc/elements/1.1/")""",
            """call semantics.addNamespacePrefix("rdf","http://www.w3.org/1999/02/22-rdf-syntax-ns#")""",
            """call semantics.addNamespacePrefix("bioPortal","http://purl.bioontology.org/ontology/")""",
            """call semantics.addNamespacePrefix("skos","http://www.w3.org/2004/02/skos/core#")""",
            """call semantics.addNamespacePrefix("sch","http://schema.org/")""",
            """call semantics.addNamespacePrefix("mondo","http://purl.obolibrary.org/obo/mondo#")""",
            """call semantics.addNamespacePrefix("sh","http://www.w3.org/ns/shacl#")""",
            """call semantics.addNamespacePrefix("mydata","http://example.com/resource/")""",
            """call semantics.addNamespacePrefix("umls_mapping","https://www.nlm.nih.gov/research/umls/mapping_projects/")""",
            """call semantics.addNamespacePrefix("icd10","http://purl.bioontology.org/ontology/ICD10CM/")""",
            """call semantics.addNamespacePrefix("icd9","http://purl.bioontology.org/ontology/ICD9CM/")""",
            """call semantics.addNamespacePrefix("snomed","http://purl.bioontology.org/ontology/SNOMEDCT_US/")""",
            """call semantics.addNamespacePrefix("umls","http://bioportal.bioontology.org/ontologies/umls/")""",
            """call semantics.addNamespacePrefix("cui","http://example.com/cui/")""",
            """call semantics.addNamespacePrefix("oboInOwl", "http://www.geneontology.org/formats/oboInOwl#")""",
            """call semantics.addNamespacePrefix("foaf", "http://xmlns.com/foaf/0.1/")""",
            """call semantics.addNamespacePrefix("graphBuilder", "http://graphBuilder.org/")""")

        for (a <- namespaceCalls) graphDb.execute(a)
        println("added all namespaces")

    }
}