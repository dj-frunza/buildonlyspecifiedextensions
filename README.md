1. In Order to be able to get rid of Intellij errors, the classes that are used in this project should be searched in the associated Hybris project and then the jars containing them need
	to be added in the classpath of this project as well. Example of such a class: de.hybris.bootstrap.codegenerator.CodeGenerator
2. Build the jar using Intellij: https://www.jetbrains.com/help/idea/packaging-a-module-into-a-jar-file.html
3. Put the jar in hybris\bin\platform\bootstrap\bin\ so that ant will know where to get it from. The name of the jar should be BuildOnlySpecifiedExtensions.jar.
4. Modify ant in the following way:
5. Add the following code under platform/build.xml

		<macrodef name="propertycopy">
		  <attribute name="name"/>
		  <attribute name="from"/>
		  <attribute name="prefix"/>
		  <attribute name="suffix"/>
		  <sequential>
			<property name="@{name}" value="${@{prefix}@{from}@{suffix}}"/>
		  </sequential>
		</macrodef>
		
		<propertycopy name="provided.extension.path" from="${extensions.to.generate.models.for}" prefix="ext." suffix=".path" />
		<propertycopy name="provided.extension.classpath" from="${extensions.to.generate.models.for}" prefix="ext." suffix=".classpath" />

		
		<path id="new-type-compile-classpath">
		  <fileset dir="${platformhome}/bootstrap/bin">
			<include name="ybootstrap.jar"/>
		  </fileset>

		  <pathelement path="${provided.extension.classpath}"/>
		  
		  <pathelement path="${ext.validation.classpath}"/>
		  <pathelement path="${build.classpath}" />
		</path>

		<target name="generateAndCompileNewType" description="Builds the provided extensions">
			<!-- Validate that the needed properties were provided  --> 
			<fail message="Property &quot;type.to.compile&quot; needs to be set to a value. Its value should be taken from jaloClass Attribute from items.xml. EX: com.training.hybris.core.jalo.cms.CustomCmsComponent">
				<condition>
					<or>
						<equals arg1="${type.to.compile}" arg2=""/>
						<not>
							<isset property="type.to.compile"/>
						</not>
				   </or>
			   </condition>
			</fail>
			
			<fail message="Property &quot;extensions.to.generate.models.for&quot; needs to be set to a value">
				<condition>
					<or>
						<equals arg1="${extensions.to.generate.models.for}" arg2=""/>
						<not>
							<isset property="extensions.to.generate.models.for"/>
						</not>
				   </or>
			   </condition>
			</fail>
			
			<!-- Generate model and Jalo java files based on the provided extension's items.xml file --> 
			<codegeneratorforspecifiedextensions/>		 

			<propertyregex property="type.to.compile.jalo"
				   input="${type.to.compile}"
				   regexp="\."
				   replace="/"
				   global="true" />
				   
			<propertyregex property="type.to.compile.model"
				   input="${type.to.compile.jalo}"
				   regexp="jalo"
				   replace="model"
				   global="true" />
			<!-- Bellow code replaces the last occurence of "/" character with "/Generated" in order to obtain the generated class file's path.
						EX: com/training/hybris/core/jalo/cms/CustomCmsComponent -> com/training/hybris/core/jalo/cms/GeneratedCustomCmsComponent    -->	   
			<propertyregex property="type.to.compile.generated"
				   input="${type.to.compile.jalo}"
				   regexp="\/(?!.*\/)"
				   replace="/Generated"
				   global="true" />

			<!-- Compile the generated model class: (e.g CustomCmsComponentModel.java) -->
			<javac srcdir="${platformhome}/bootstrap/gensrc" destdir="${platformhome}/bootstrap/modelclasses"  compiler="modern" >
					<filename name="${type.to.compile.model}Model.java" />
					<classpath refid="new-type-compile-classpath"/>
			</javac>
			
			<!-- Compile the generated jalo class: (e.g GeneratedCustomCmsComponent.java) -->
			<javac srcdir="${provided.extension.path}/gensrc" destdir="${provided.extension.path}/classes"  compiler="modern" >
					<filename name="${type.to.compile.generated}.java" />
					<classpath refid="new-type-compile-classpath"/>
			</javac>
			
			<!-- Compile the generated jalo class: (e.g CustomCmsComponent.java) -->
			<javac srcdir="${provided.extension.path}/src" destdir="${provided.extension.path}/classes"  compiler="modern" >
					<filename name="${type.to.compile.jalo}.java" />
					<classpath refid="new-type-compile-classpath"/>
			</javac>
			
		</target>
							
6. Add the following under platform\resources\ant\antmacros.xml			

		<!-- Dj Frunza Custom -->
			<taskdef file="${platformhome}/bootstrap/resources/djfrunzatasks.properties">
				<classpath>
					<pathelement path="${platformhome}/bootstrap/bin/ybootstrap.jar" />
					<pathelement path="${platformhome}/bootstrap/bin/BuildOnlySpecifiedExtensions.jar" />
					<pathelement path="${platformhome}/bootstrap/bin/yant.jar" />
					<fileset dir="${platformhome}/lib/dbdriver">
						<include name="*.jar" />
					</fileset>
					<fileset dir="${platformhome}/ext/core/lib">
						<include name="**/*.jar" />
					</fileset>
					<fileset dir="${platformhome}/lib">
						<include name="*.jar" />
					</fileset>
					<fileset dir="${bundled.tomcat.home}/lib">
						<include name="*.jar" />
					</fileset>
				</classpath>
			</taskdef>
		<!-- Dj Frunza Custom -->
	
7. Create "djfrunzatasks.properties" file in "hybris\bin\platform\bootstrap\resources\" and put in there the following:

		codegeneratorforspecifiedextensions=com.dj.frunza.hybris.ant.utils.CodeGeneratorForSpecifiedExtensionsTask


5. Example of command: 

		ant generateAndCompileNewType -Dextensions.to.generate.models.for="projectnamecore" -Dtype.to.compile="com.training.hybris.core.jalo.cms.CustomCmsComponent"
		

        
