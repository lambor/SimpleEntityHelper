package com.dcnh35.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.compile.JavaCompile

class SimpleEntityHelper implements Plugin<Project> {

    static final String PLUGIN_NAME = "simpleEntityHelper";

    Copy copyEntites;
    JavaCompile javaCompile
    Delete clean;

    Project project
    SimpleEntityHelperExtension extension

    String buildDir
    SourceSet main;
    String srcPath;

    @Override
    void apply(Project target) {
        this.project = target;
        this.extension = project.extensions.create(PLUGIN_NAME, SimpleEntityHelperExtension)

        //坑啊
        project.afterEvaluate {
            buildDir = project.buildDir.absolutePath

            //http://www.programcreek.com/java-api-examples/index.php?api=org.gradle.api.Project
            final JavaPluginConvention javaPluginConvention = project.getConvention().getPlugin(JavaPluginConvention.class);
            main = javaPluginConvention.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);

            //srcPath = /src/main/java
            srcPath = main.java.srcDirs[0].absolutePath

            createGenerateOneTask()
            createCopyTask()
        }
    }

//    task compileOne (type: JavaCompile) {
//        source = sourceSets.main.java.srcDirs
//        include 'com/example/MyClass.java'
//        classpath = sourceSets.main.compileClasspath
//        destinationDir = new File(generatedResources)
//    }
    def generatedResources = "/dcnh35/entities"

//    private void createCleanTask() {
//        clean = project.tasks.create("cleanEntities",Delete)
//        clean.targetFiles = buildDir + generatedResources
//    }

    private void createGenerateOneTask() {
//        javaCompile.dependsOn clean
        javaCompile = project.tasks.create("compileOne", JavaCompile)
        javaCompile.setDescription("compile the java file that holds json strings")
        javaCompile.source = main.java.srcDirs
        javaCompile.include(classToFilename(extension.jsonStringClass))
        javaCompile.classpath = main.compileClasspath
        javaCompile.destinationDir = new File(buildDir + generatedResources)
        javaCompile.doFirst {
            setProcessorEnable(true)
        }
    }

    private void createCopyTask() {
        copyEntites = project.tasks.create("compileEntities", Copy);
        copyEntites.setDescription("copy the generated entity java files to srcDir")
        copyEntites.dependsOn javaCompile
        copyEntites.from(buildDir + generatedResources)
        copyEntites.into(main.java.srcDirs[0])
        copyEntites.doLast {
            setProcessorEnable(false)
        }
    }


    private void setProcessorEnable(boolean enable) {
        def jsonClass = srcPath + "/" + classToFilename(extension.jsonStringClass)
        def fileContent = new File(jsonClass).getText('UTF-8')
        String result = fileContent.replaceAll("(@EntitiesConfig)(?=\\s*[^(])","@EntitiesConfig(switchGenerate = "+enable.toString()+")")
        if (result.contains("switchGenerate")) {
            result = result.replaceAll("switchGenerate\\s*=\\s*" + (!enable).toString()
                    , "switchGenerate = " + enable.toString())
        } else {
            result = result.replaceAll("@EntitiesConfig\\s*\\(","@EntitiesConfig(switchGenerate = " + enable.toString()+",")
        }
        new File(jsonClass).write(result, 'UTF-8')
        println("processor enable:"+enable.toString())
    }

    private String classToFilename(String className) {
        return className.replaceAll("\\.", "/") + ".java"
    }
}