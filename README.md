What is Origami?
============

**Origami plugin** is a Java O/G mapper for the [OrientDB](https://github.com/nuvolabase/orientdb/wiki) with  [Play! framework](http://www.playframework.org/) 2. It maps annotated model classes to ODocument classes of the OrientDB with mapping functionality. 

Features
======

* Object-Graph-Mapping of annotated model classes based on Object Oriented Inheritance([src](samples/origami-simple-app/app/models))

* Supports for both **Java** and **Scala** Project (Both Scala models and Java models are supported)

* Supports for both the embedded and remote OrientDB

* Supports for auto-reloading

* Enables to customize the format of Model Id ([src](app/com/wingnest/play2/origami/IdManager.java))

* On only embedded OrienDB:

   * Enables to use the [OrientDB Studio](https://github.com/nuvolabase/orientdb/wiki/OrientDB-Studio)

   * Enables to make the database directory of the embedded OrientDB a zip file when your application just starts up.

Requirements
=========

* Play 2.2.0
* OrientDB 1.5.1

Install
====

  1)  Install Play framework 2.2 and Java 5 or 6, 7

  2)  Executing the command 

         % git clone git@github.com:sgougi/play21-origami-plugin.git

  3)  Publishing Origami to your local repository

         % cd play21-origami-plugin
         % play publish-local

Run sample application and Usage
=======================

At a command prompt, type the following commands:

         % cd play21-origami-plugin
         % cd samples
         % cd origami-simple-app
         % play run

There are basic usage in the source code of a [sample application](samples). 

* [Annotated model classes](samples/origami-simple-app/app/models)
* [Customizing id format](samples/origami-simple-app/app/Global.java)
* [Application configuration: conf/application.conf](samples/origami-simple-app/conf/application.conf)
* [Dependency settings: project/Build.scala](samples/origami-simple-app/project/Build.scala)  
* [Controller with transaction](samples/origami-simple-app/app/controllers/Application.java)

## Facade Class for OrientDB

The com.wingnest.play2.origami.[GraphDB](app/com/wingnest/play2/origami/GraphDB.java) class is a Facade class.

* GraphDB.open()
* GraphDB.close()
* GraphDB.begin()
* GraphDB.commit()
* GraphDB.rollback()
* GraphDB.asynchQuery()
* GraphDB.synchCommand()
* GraphDB.synchQuery()
* GraphDB.synchQueryModel()
* GraphDB.findById()
* GraphDB.findDocumentById()
* GraphDB.documentsToModel()
* GraphDB.documentToModel()
* GraphDB.getDocumentsByFieldName()

## Abstract Model classes

* [com.wingnest.play2.origami.GraphVertexModel](app/com/wingnest/play2/origami/GraphVertexModel.java)
* [com.wingnest.play2.origami.GraphEdgeModel](app/com/wingnest/play2/origami/GraphEdgeModel.java)

Both classes extends [com.wingnest.play2.origami.GraphModel](app/com/wingnest/play2/origami/GraphModel.java).

## Annotatins for O/G mapping

### For Models
####[@Index](app/com/wingnest/play2/origami/annotations/Index.java)
Defines one or more indexes.

  ex.:

         public class A extends ... {
            ...
            @Index(indexType = OClass.INDEX_TYPE.UNIQUE)
            public String email;     
    
            @Index(indexType = OClass.INDEX_TYPE.NOTUNIQUE)
            public String name;     
           ...
         }

####[@CompositeIndex](app/com/wingnest/play2/origami/annotations/CompositeIndex.java)
Defines one or more composite indexes.

  ex.:

        public class A extends ... {
            ...
            @CompositeIndex(indexName="ci_1", indexType = OClass.INDEX_TYPE.UNIQUE)
            public String attr1;     
    
            @CompositeIndex(indexName="ci_1", indexType = OClass.INDEX_TYPE.NOTUNIQUE)
            public Integer attr2;
            ...
        }     

####[@SmartDate](app/com/wingnest/play2/origami/annotations/SmartDate.java)
Defines one or more attributes to set the saved time automatically.

  ex.:

        @SmartDate(dateType = GraphModel.SMART_DATE_TYPE.CREATED_DATE)	
        @Index()
        public Date createdDate;

  ex.:

        @SmartDate(dateType = GraphModel.SMART_DATE_TYPE.UPDATED_DATE)
        @Index()
        public Date updatedDate;    

####[@DisupdateFlag](app/com/wingnest/play2/origami/annotations/DisupdateFlag.java)
If you don't want to update by @SmartDate, set its attribute true.

  ex.:

        @DisupdateFlag
        public boolean disupdateFlag =  false;

####@javax.persistence.Transient
Defines one or more attributes as transient attribute.

####@javax.persistence.Id
Defines one attribute as id attribute.

####@javax.persistence.Version
Defines one attribute as version attribute.

### For Controllers (Only Java Project)
####[@Transactional](app/com/wingnest/play2/origami/annotations/Transactional.java)
The **@Transactional** annotation enables annotated Actions and/or Controllers to rollback and commit by **GraphDB.rollback()** and **GraphDB.commit()**. If a controller is annotated by @Transactional, its all actions are turned into transactional. 

####[@WithGraphDB](app/com/wingnest/play2/origami/annotations/WithGraphDB.java)
The **@WithGraphDB** annotation enables annotated Actions and/or Controllers to use **OrientDB** implicitly.

Known Issues
=============
* Nothing

Licence
========
Origami is distributed under the [Apache 2 licence](http://www.apache.org/licenses/LICENSE-2.0.html).