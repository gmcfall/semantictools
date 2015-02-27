This project is an example that illustrates the usage of semantictools.  
This example shows how the IMS Learning Tools Interoperability (LTI) REST Service 
specifications are generated.  We'll use this example as a cookbook for
creating your own REST service specification.

# Understanding the Methodology
 
Our approach to defining REST services is based on the principles of
[Linked Data] (http://www.w3.org/DesignIssues/LinkedData.html)
which Tim Berners-Lee summarized with four basic ideas :

1.  Use URIs as names for things
2.  Use HTTP URIs so that people can look up those names
3.  When someone looks up a URI, provide useful information using RDF standards.
4.  Include links to other URIs so that they can discover more things.

JSON-LD is a W3C standard that expresses linked data in JSON syntax.
The big idea behind JSON-LD is that one can represent resources in a very simple
JSON syntax, and the terms that appear in a JSON document are mapped to classes
and properties in an RDF ontology.  As a result, the semantics of the JSON document
are unambiguous.

The mapping from terms in a JSON document to terms in an RDF ontology is defined
within an artifact known as a JSON-LD context, which is itself a JSON document. 
Thus, a JSON-LD implementation requires that you produce an RDF ontology and a
JSON-LD context that effectively defines the representations for your resources. We
go one step further and associate each JSON-LD context with a vendor-specific media type.
 
The key artifact that drives the entire methodology is an RDF ontology.
Unfortunately, most of the tools for constructing an RDF ontology require highly
specialized knowledge.  To simplify the process, we geenerally start with UML since it is
familiar to most software engineers.  In particular, we use StarUML to create a
logical model of the resources, and then we generate the RDF ontologies using a
custom template. See  [semantictools-staruml/README] (http://github.com/gmcfall/semantictools/blob/master/semantictools-staruml/README.md) 


# Cookbook

The simplest way to create your own REST service specification is to start by copying the 
top-level files from `example\lti`...

> .classpath  
  .project  
  .settings  
  pom.xml  
     
All you really need is the `pom.xml` file, which shows how to configure
a pom to generate documentation via the semantictools-maven-plugin.
     
The other assets (`.classpath`, `.project`, `.settings`) are useful if you are using
eclipse with the m2eclipse plugin.


To produce the documentation for the LTI project, do the following...

    cd examples/lti  
    mvn generate-sources  
    
The output can be found at

    target/generated-sources/rdf

But we are getting ahead of ourself.  We need to backup and describe the other 
assets within the `examples/lti` project.

    
### src/main/resources/rdf/uml
> LTI_v2.uml

You start by creating a UML model in StarUML.  In this example, `LTI_v2.uml` contains
the StarUML model that was created by the IMS LTI working group. Obviously, you'll be 
creating your own UML model.

See  [semantictools-staruml/README] (http://github.com/gmcfall/semantictools/blob/master/semantictools-staruml/README.md) 
for tips on how to build an effective UML model with StarUML. The RDF Schema generator assumes that
you are following these modeling guidelines, and you may get surprising results if you deviate
from the guidelines.

     
### src/main/resources/rdf  
>    liso.ttl  
     liso_binding.ttl  
     lti.ttl  
     lti_binding.ttl  
     ltic.ttl  
     ltic_binding.ttl  
     lti-type.xsd  
     ltiv.ttl  
     ltiv_binding.ttl  
     xmlDatatypes.ttl  
     
These are the files output from StarUML via the "RDF Schema" generator.
It is important that the output files go into the
`src/main/resources/rdf` directory within your maven project.

The next step is to create a directory for each RESTful resource that
you wish to define.  Here's an example:
     
### src/main/resources/rdf/ToolProxy  
>    context.properties  
     sample.json  
     service.properties  
     
These files drive the documentation for one RESTful resource --
in this case, the ToolProxy.
     
A given resource can have multiple representations.  At the present time, 
semantictools only supports JSON representations.  More precisely, it
produces documentation for JSON-LD representations.
     
Each representation is associated with a media type and a JSON-LD
context.  Information about the media type and corresponding JSON-LD
context are defined in the context.properties file.
     
The `context.properties` file listed above has been annotated with comments 
that explain the various properties.
     
The `service.properties` file drives the documentation for a REST
service that provides access to the JSON-LD representation.
     
As illustrated in the example above, the documentation for a REST 
service can be produced mostly from boiler plate.  All you really need
to specify is the mediaType for representations accessible through the
REST service, plus the "status" of the service specification.
     
However, it is possible to customize the REST Service documentation.
(For an example, see the service.properties file for the ToolConsumerProfile.)
     
The sample.json file contains an example of a resource representation 
expressed in JSON-LD.
     
If this file is not supplied, the `semantictools-maven-plugin` will automatically 
generate a sample JSON file.  Typically, you will start by generating the documentation 
once without a sample.json file. Then, you customize the the auto-generated sample
and drop it here so that your tailored sample will appear in the documentation.
     
### src/main/resources/rdf/Result  
>    context.properties  
     sample.json  
     service.properties

### src/main/resources/rdf/ToolConsumerProfile  
>    context.properties  
     sample.json  
     service.properties  
     
### src/main/resources/rdf/ToolProxy.id  
>    context.properties  
     sample.json  
     service.properties  
     
These are examples of property files (and JSON samples) used to drive the
generation for other resource representations.
     
The ToolProxy.id folder is interesting because it defines an alternative
representation of the ToolProxy resource that contains nothing but
the identifier for a ToolProxy.  This representation is returned when
one submits a POST request to the ToolProxy REST service.
     


