package com.dj.frunza.hybris.ant.utils;

import de.hybris.ant.taskdefs.PlatformConfigAntUtils;
import de.hybris.bootstrap.codegenerator.CodeGenerator;
import de.hybris.bootstrap.codegenerator.jalo.JaloClassGenerator;
import de.hybris.bootstrap.codegenerator.model.ModelClassGenerator;
import de.hybris.bootstrap.config.ExtensionInfo;
import de.hybris.bootstrap.config.PlatformConfig;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.*;

/**
 * This class is intended to do almost exactly what {@link de.hybris.bootstrap.codegenerator.model.ModelClassGenerator} is doing, which is generating Model Classes.
 * The main two big differences are that:
 * <ul>
 *     <li>This class is not deleting models.jar in order to increase the build performance and to avoid the need for hybris restart</li>
 *     <li>The Models are generated only for the extensions that are specified via {@link #EXTENSIONS_TO_BUILD} ant property</li>
 * </ul>
 *
 * <p>
 * If the dev has a JRebel  subscription activated, he/she can reload the new classes into the JVM.
 */
public class CodeGeneratorForSpecifiedExtensionsTask extends Task {
    private static final Logger LOG = Logger.getLogger(CodeGeneratorForSpecifiedExtensionsTask.class);
    private static final String EXTENSIONS_TO_BUILD = "extensions.to.generate.models.for";


    public CodeGeneratorForSpecifiedExtensionsTask() {
    }

    @Override
    public void execute() throws BuildException {
        String extensionNamesSeparatedBySemiColon = getProject().getProperty(EXTENSIONS_TO_BUILD);
        if (StringUtils.isEmpty(extensionNamesSeparatedBySemiColon)) {
            throw new IllegalArgumentException("Please provide the extensions that need to be built using '" + EXTENSIONS_TO_BUILD + "' property");
        }
        String[] split = extensionNamesSeparatedBySemiColon.split(";");
        List<String> extensionNamesProvided = Arrays.asList(split);

        PlatformConfig cfg = PlatformConfigAntUtils.getOrLoadPlatformConfig(this);
        CodeGenerator gen = new CodeGenerator(cfg);
        ModelClassGenerator generator = new ModelClassGenerator(gen);


        List<ExtensionInfo> extensionInfosInBuildOrder = cfg.getExtensionInfosInBuildOrder();

        int counter = 0;
        List<String> successfullyCompiledExtensions = new ArrayList<>();
        for (ExtensionInfo extensionInfo : extensionInfosInBuildOrder) {
            if (extensionNamesProvided.contains(extensionInfo.getName())) {
                JaloClassGenerator jaloClassGenerator = new JaloClassGenerator(gen);
                jaloClassGenerator.generateClasses(Collections.singletonList(extensionInfo));
                generateClasses(generator, extensionInfo);
                LOG.info("Successfully generated classes for the extension:" + extensionInfo.getName());
                successfullyCompiledExtensions.add(extensionInfo.getName());
                counter++;
            }
        }

        if (counter == 0) {
            throw new IllegalArgumentException(MessageFormat.format("The provided argument:<{0}> does not contain any valid extension. The extension names should be separated using ';' character", extensionNamesSeparatedBySemiColon));
        }

        if (counter != extensionNamesProvided.size()) {
            List<String> modifiableListOfExtensionsThatCouldNotBeBuilt = new ArrayList<>(extensionNamesProvided);
            modifiableListOfExtensionsThatCouldNotBeBuilt.removeAll(successfullyCompiledExtensions);
            throw new IllegalArgumentException(MessageFormat.format("The provided argument({0}) contains the following extension names that are not valid:{1} ", EXTENSIONS_TO_BUILD, modifiableListOfExtensionsThatCouldNotBeBuilt));
        }
    }

    private void generateClasses(ModelClassGenerator generator, ExtensionInfo extensionInfo) {
        try {
            File modelsJarFile = (File) getAccessibleMethod(generator, "getModelsJar").invoke(generator);
            boolean gensrcEmpty = (boolean) getAccessibleMethod(generator, "isGensrcEmpty").invoke(generator);

            List<ExtensionInfo> extensionCfgs = Collections.singletonList(extensionInfo);
            boolean hasExtensionsWhichRequireCodeGeneration = (boolean) getAccessibleMethodWithTwoParams(generator, "hasExtensionsWhichRequireCodeGeneration", Collection.class, File.class).invoke(generator, extensionCfgs, modelsJarFile);

            if (modelsJarFile.exists() && !gensrcEmpty && !hasExtensionsWhichRequireCodeGeneration) {
                return;
            }

            File genSrcDir = (File) getAccessibleMethod(generator, "getOrCreateGensrcDir").invoke(generator);

            getAccessibleMethodWithTwoParams(generator, "generateModels", Collection.class, File.class).invoke(generator, extensionCfgs, genSrcDir);
//            generator.generateBeanClasses(extensionCfgs, genSrcDir);

        } catch (NoSuchMethodException e) {
            LOG.error("Error While trying to get the method via reflection:", e);
        } catch (IllegalAccessException e) {
            LOG.error("Error happened during method invocation:", e);
        } catch (InvocationTargetException e) {
            LOG.error("Error While trying to invoke the method via reflection:", e);
        }


    }

    private Method getAccessibleMethodWithTwoParams(ModelClassGenerator generator, String methodName, Class<?> firstParamClass, Class<?> secondParamClass) throws NoSuchMethodException {
        Method methodHasExtensionsWhichRequireCodeGeneration = generator.getClass().getDeclaredMethod(methodName, firstParamClass, secondParamClass);
        methodHasExtensionsWhichRequireCodeGeneration.setAccessible(true);
        return methodHasExtensionsWhichRequireCodeGeneration;
    }

    private Method getAccessibleMethod(ModelClassGenerator generator, String methodName) throws NoSuchMethodException {
        Method method = generator.getClass().getDeclaredMethod(methodName);
        method.setAccessible(true);
        return method;
    }
}
