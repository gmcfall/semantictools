This project contains a custom template that is used to generate RDF ontologies
from a StarUML model.

To install this template, copy the contents of the "RDF Schema" directory to 
the following location within your StarUML installation...

    StarUML/modules/staruml-generator/templates/RDF Schema
    
Tips for modeling in StarUML
    
    1.  Use "attributes" only to model simple types; do
        not use "attributes" on a class to model relationships
        between classes.  (See #4 below for information about
        modeling relationships.)
    
    2.  Use XSD names for simple types (string, dateTime,
        normalizedString, token, integer, etc.)
        
    3.  See the LTI model for examples that show how to define
        custom simple types.  For example, the Name type in LTI
        defines maxLength and pattern restrictions.
    
    4.  Use Composition or Aggregation arcs to define relationships
        between classes. Be sure to label the arcs with names and
        cardinality constraints.
        
    5.  Organize classes into packages.  Each package corresponds
        to a different RDF Ontology.  You must add to "attachments"
        to the package to define the URI and preferred prefix for
        the RDF Ontology.
        
        For example, the LTI model contains the following attachments
        on the LTI Core package...
        
            uri=http://www.imsglobal.org/imspurl/lti/v2/vocab/lti#
            prefix=lti
            
        The attachments tab should appear next to the "Documentation" and
        "Properties" tabs.  If you don't see it, open the View menu and
        make sure that "attachments" is checked.
        
    6.  Label your classes with one of the following stereotypes...
    
           addressable:  Designates a resource that is identified by a
                         URI.  In other words, this is a resource that
                         can be accessed through a REST endpoint.
           
           embeddable:   A resource that can exist only as a property
                         of some other resource.  This kind of resource
                         does not have its own REST endpoint.  
           
           enumerable:   This class represents an extensible enumeration.
                         Individual instances of this class correspond to
                         the possible values of the enumeration. 
                         
           simpleType:   Used to model a custom simple type.
           
    7.  Add an 'Object' to the StarUML model to represent each element within
        an extensible enumeration.
        
        For example, the LTI model contains the following objects in the
        "LTI Variables" package...
        
            User.id:Variable
            User.image:Variable
            
    8.  For enumerations that are not extensible (i.e. closed to new members of 
        the enumeration), use the built-in Enumeration element in StarUML.
        
    9.  To generate RDF ontologies from your UML model, choose
    
           Tools > StarUML Generator...
           
        Then select "RDF Schema" and use the wizard to kick-off the generator.