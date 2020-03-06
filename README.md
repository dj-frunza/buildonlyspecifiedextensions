1. In Order to be able to get rid of Intellij errors, the classes that are used in this project should be searched in the associated local Hybris project and then the jars containing them need
	to be added in the classpath of this project as well. Example of such a class: de.hybris.bootstrap.codegenerator.CodeGenerator --> If you just need to use this project without doing development,
    you can do that directly by using the jar that can be found in the 'artifact' folder.
2. Take the jar directly from the 'artifact' directory or build the jar using Intellij: https://www.jetbrains.com/help/idea/packaging-a-module-into-a-jar-file.html 
3. Put the jar into your local hybris\bin\platform\bootstrap\bin\ so that ant will know where to get it from. The name of the jar should be BuildOnlySpecifiedExtensions.jar.
4. Modify ant in the following way:
5. Add the full content of the following file into your local platform/build.xml

		resources/ant/generateAndCompileNewTypeAntTarget.xml (This file can be found in this repo)
							
6. Add the full content of the following file into your local platform\resources\ant\antmacros.xml

		 resources/ant/declareNewAntTasks.xml (This file can be found in this repo)
	
7. Create "djfrunzatasks.properties" file in "hybris\bin\platform\bootstrap\resources\" and put in there the following:

		codegeneratorforspecifiedextensions=com.dj.frunza.hybris.ant.utils.CodeGeneratorForSpecifiedExtensionsTask


5. Example of command: 

		ant generateAndCompileNewType -Dextensions.to.generate.models.for="projectnamecore" -Dtype.to.compile="com.training.hybris.core.jalo.cms.CustomCmsComponent"


		
This project is intended to be used only during development. Hybris standard ant targets are not touched and not affected. 

Description Video: https://www.youtube.com/watch?v=NHtMg8vZfdI&feature=youtu.be
		

        
