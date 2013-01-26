What is Origami?
============
**Origami plugin** is a Java O/G mapper for the [OrientDB](https://github.com/nuvolabase/orientdb/wiki) with  [Play! framework](http://www.playframework.org/) 2. It maps annotated model classes to ODocument classes of the OrientDB with mapping functionality. 

* For Play2.0.4 repository is [here](http://goo.gl/zldem).

Features
======
* Object-Graph-Mapping of annotated model classes based on Object Oriented Inheritance([src](http://goo.gl/93Auo))
* Supports for both the embedded OrientDB and the remote OrientDB
* Enables to customize a id format of Model ([src](http://goo.gl/Ql7Fy))
* Only embedded OrienDB:
   * Enables to use the [OrientDB Studio](https://github.com/nuvolabase/orientdb/wiki/OrientDB-Studio)
   * Enables to make the database directory of the embedded OrientDB a zip file when your application has just started up.

Requirements
=========
* Only supports Play2.1-RC2 or greater.

Install
====
  1)  Install Play framework 2.1-RC2 and Java 5 or 6

  2)  Executing the command 

         % git git@github.com:sgougi/play21-origami-plugin.git

  3)  Publishing Origami to your local repository

         % cd play21-origami-plugin
         % play publish-local

Run sample application and Usage
=======================
At a command prompt, type the following commands:

         % cd play21-origami-plugin
         % play publish-local
         % cd samples
         % cd origami-simple-app
         % play run

There are basic usage in the source code of a [sample application](http://goo.gl/MhIHl). 

* [Annotated model classes](http://goo.gl/s5Vje)

* [Customizing id format](http://goo.gl/xHajA)

* [Application configuration: conf/application.conf](http://goo.gl/jUAyh)

* [Dependency settings: project/Build.scala](http://goo.gl/gTgBO)  

* [Controller with transaction](http://goo.gl/HQLhK)

## Facade Class for OrientDB
The com.wingnest.play2.origami.[GraphDB](http://goo.gl/1vS9Y) class is a Facade class.

* GraphDB.open()
* GraphDB.close()
* GraphDB.begin()
* GraphDB.commit()
* GraphDB.rollback()
* GraphDB.asynchCommand()
* GraphDB.synchCommand()
* GraphDB.synchQuery()
* GraphDB.synchQueryModel()
* GraphDB.findById()
* GraphDB.findDocumentById()
* GraphDB.documentsToModel()
* GraphDB.documentToModel()
* GraphDB.getDocumentsByFieldName()

## Abstract Model classes

* [com.wingnest.play2.origami.GraphVertexModel](http://goo.gl/MNV6d)
* [com.wingnest.play2.origami.GraphEdgeModel](http://goo.gl/W2htm)
* [com.wingnest.play2.origami.GraphModel](http://goo.gl/WIPUd)

## Annotatins for O/G mapping

### For Models
####[@Index](http://goo.gl/j1ixJ)
Defines one or more indexes.

  ex.:

         public class A extends ... {
            ...
            @Index(OClass.INDEX_TYPE.UNIQUE)
            public String email;     
    
            @Index(OClass.INDEX_TYPE.NOTUNIQUE)
            public String name;     
           ...
         }

####[@CompositeIndex](http://goo.gl/G6mkc)
Defines one or more composite indexes.

  ex.:

        public class A extends ... {
            ...
            @CompositeIndex(indexName="ci_1", OClass.INDEX_TYPE.UNIQUE)
            public String attr1;     
    
            @CompositeIndex(indexName="ci_1", OClass.INDEX_TYPE.NOTUNIQUE)
            public Integer attr2;
            ...
        }     

####[@SmartDate](http://goo.gl/ta57G)
Defines one or more attributes to set the saved time automatically.

  ex.:

        @SmartDate(type = SmartDateType.CreatedDate)
        @Index()
        public Date createdDate;

  ex.:

        @SmartDate(type = SmartDateType.UpdatedDate)
        @Index()
        public Date updatedDate;    

####[@DisupdateFlag](http://goo.gl/LsFCr)
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

### For Controllers
####[@Transactional](http://goo.gl/c6mLF)
The **@Transactional** annotation enables annotated Actions and/or Controllers to rollback and commit by **GraphDB.rollback()** and **GraphDB.commit()**. If a controller is annotated by @Transaction, its all actions is able to enabled. 

####[@WithGraphDB](http://goo.gl/goLmg)
The **@WithGraphDB** annotation enables annotated Actions and/or Controllers to use **OrientDB** implicitly.


Known Issues
=============
* Origami is influenced by the following bug of Play! 2.1-RC2
 * [Play Framework 2.1-RC2: NoSuchFieldError's created by reverse routing in templates](http://goo.gl/dKSJd)
 
Licence
========
Origami is distributed under the [Apache 2 licence](http://www.apache.org/licenses/LICENSE-2.0.html).
 