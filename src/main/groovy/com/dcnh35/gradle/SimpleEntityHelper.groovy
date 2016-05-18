package com.dcnh35.gradle

import com.android.build.gradle.api.AndroidSourceSet
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.ProjectConfigurationException
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.compile.JavaCompile

class SimpleEntityHelper implements Plugin<Project> {

    static final String PLUGIN_NAME = "simpleEntityHelper";

    Copy copyEntites;
    JavaCompile javaCompile

    Project project
    SimpleEntityHelperExtension extension

    String buildDir
    File mainSource;
    String classPath;
    String srcPath;

    @Override
    void apply(Project target) {
        this.project = target;
        this.extension = project.extensions.create(PLUGIN_NAME, SimpleEntityHelperExtension)

        def variants = null
        //com.android.application
        if (project.plugins.findPlugin("android") || project.plugins.findPlugin("com.android.application")) {
            variants = "applicationVariants"
        } else if (project.plugins.findPlugin("android-library") || project.plugins.findPlugin("com.android.library")) {
            variants = "libraryVariants"
        } else {
            throw new ProjectConfigurationException("The android or android-library plugin must be applied to the project", null);
        }

        //坑啊
        project.afterEvaluate {

//            //http://www.programcreek.com/java-api-examples/index.php?api=org.gradle.api.Project
//            //only work on java plugin not on android plugin
//            final JavaPluginConvention javaPluginConvention = project.getConvention().getPlugin(JavaPluginConvention.class)
//            main = javaPluginConvention.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);

            project.android[variants].any {
                variant ->
                    JavaCompile javaCompile = variant.javaCompile
//                    println(javaCompile.classpath.asPath)
                    classPath = javaCompile.classpath.asPath
            }

            def sets;
            if (Utils.is140orAbove()) {
                sets = project.android.sourceSets;
            } else {
                sets = project.android.sourceSetsContainer;
            }
            sets.all { AndroidSourceSet sourceSet ->
                if(sourceSet.name.startsWith("main"))
                    for(File file:sourceSet.java.getSrcDirs())
                        if(file.exists()) {
                            mainSource = file;
                            break;
                        }
            }


//            srcPath = src/main/java
            srcPath = mainSource.absolutePath
            buildDir = project.buildDir.absolutePath

            createGenerateOneTask()
            createCopyTask()
        }
    }

    def generatedResources = "/dcnh35/entities"

//    private void createCleanTask() {
//        clean = project.tasks.create("cleanEntities",Delete)
//        clean.targetFiles = buildDir + generatedResources
//    }

    private void createGenerateOneTask() {
//        javaCompile.dependsOn clean
        javaCompile = project.tasks.create("compileOne", JavaCompile)
        javaCompile.setDescription("compile the java file that holds json strings")
        javaCompile.source = mainSource
        javaCompile.include(classToFilename(extension.jsonStringClass))
        javaCompile.classpath = project.files(classPath)
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
        copyEntites.into(mainSource)
        copyEntites.doLast {
            setProcessorEnable(false)
        }
    }


    private void setProcessorEnable(boolean enable) {
        println("processor enable:"+enable.toString())
        enable = !enable; //skip = !enable;
        def jsonClass = srcPath + "/" + classToFilename(extension.jsonStringClass)
        def fileContent = new File(jsonClass).getText('UTF-8')
        String result = fileContent.replaceAll("(@EntitiesConfig)(?=\\s*[^(])","@EntitiesConfig(skipGenerate = "+enable.toString()+")")
        if (result.contains("skipGenerate")) {
            result = result.replaceAll("skipGenerate\\s*=\\s*" + (!enable).toString()
                    , "skipGenerate = " + enable.toString())
        } else {
            result = result.replaceAll("@EntitiesConfig\\s*\\(","@EntitiesConfig(skipGenerate = " + enable.toString()+",")
        }
        new File(jsonClass).write(result, 'UTF-8')
    }

    private String classToFilename(String className) {
        return className.replaceAll("\\.", "/") + ".java"
    }
}