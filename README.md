# University project on intelligent agents
Expert systems & Intelligent agents project.

## First part
The creation of a GUI desktop Java/JavaFX application powered by an *expert system*.

**Dependencies :**
- JavaFX
- Simple-Json

**Note** : the custum simple-json module is compiled from the simple-json package using :
```
jdeps --generate-module-info . <jar_path>
javac --patch-module <module_name>=<jar_path> <module_name>/module-info.java
jar uf <jar_path> -C <module_name> module-info.class
```
And then this new module is installed to the maven local repository, using :
```
mvn install:install-file -Dfile=<path-to-file> -DgroupId=<group-id> -DartifactId=<artifact-id> -Dversion=<version> -Dpackaging=<packaging>
```

The generation of the custom JRE running image is done using maven :
```
mvn clean javafx:jlink
```
And the generated image launcher is in `target/expertsystem/bin/launcher`.
