package com.shadkona.ci;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.reflections.Reflections;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Checks the number of methods not having Security Annotations
 * 
 * Ignore the list of classes given
 *
 */
@Mojo(name = "svc-layer-validate", defaultPhase = LifecyclePhase.DEPLOY)
public class ServiceLayerValidatorMojo extends AbstractMojo {
	/**
	 * Ignore the list of classes given
	 */
	@Parameter(property = "ignoreClassList")
	String[] ignoreClassList;

	/**
	 * Ignore the list of classes given
	 */
	@Parameter(property = "ignoreAnnotation")
	String ignoreAnnotation;

	/**
	 * Package to scan for the Implementation classes
	 */
	@Parameter(property = "pkg")
	String pkg;

	/**
	 * Gives access to the Maven project information.
	 */
	@Parameter(defaultValue = "${project}", required = true, readonly = true)
	MavenProject project;

	@SuppressWarnings("unchecked")
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			if (pkg == null || pkg.trim().length() <= 0) {
				new MojoExecutionException("Package name can't be null for the project : " + project.getName());
			}

			if (getLog().isInfoEnabled()) {
				getLog().info(new StringBuffer("Using Package " + pkg));
			}

			Set<URL> urls = new HashSet<>();
			List<String> testElements = project.getTestClasspathElements();
			for (String element : testElements) {
				urls.add(new File(element).toURI().toURL());
			}
			List<String> runtimeElements = project.getRuntimeClasspathElements();
			for (String element : runtimeElements) {
				urls.add(new File(element).toURI().toURL());
			}
			List<String> compiletimeElements = project.getRuntimeClasspathElements();
			for (String element : compiletimeElements) {
				urls.add(new File(element).toURI().toURL());
			}
			List<String> systemtimeElements = project.getRuntimeClasspathElements();
			for (String element : systemtimeElements) {
				urls.add(new File(element).toURI().toURL());
			}

			if (getLog().isInfoEnabled()) {
				getLog().info("Classpath Found " + urls.size() + " Classses");
			}
			for (URL url : urls) {
				getLog().info("URL file " + url.getFile());
			}

			ClassLoader contextClassLoader = URLClassLoader.newInstance(urls.toArray(new URL[0]),
					Thread.currentThread().getContextClassLoader());

			Thread.currentThread().setContextClassLoader(contextClassLoader);

			Reflections reflections = new Reflections(pkg);
			Set<Class<?>> allSet = reflections.getTypesAnnotatedWith(org.springframework.stereotype.Service.class,
					true);
			if (allSet.isEmpty()) {
				if (getLog().isInfoEnabled()) {
					getLog().info("No Service Impl classes Found");
				}
				return;
			} else {
				if (getLog().isInfoEnabled()) {
					getLog().info("Relection Found " + allSet.size() + " Classses");
					for (Class<?> clazz : allSet) {
						getLog().info("Relection found a class " + clazz);
					}
				}
			}

			Map<String, String> ignoreClazzMap = new HashMap<>();
			if (ignoreClassList != null && ignoreClassList.length > 0) {
				for (int idx = 0; idx < ignoreClassList.length; idx++) {
					ignoreClazzMap.put(ignoreClassList[idx], "");
				}
			}

			List<String> errorList = new ArrayList<>();
			for (Class<?> clazz : allSet) {
				if (ignoreClazzMap.get(clazz.getCanonicalName()) != null) {
					if (getLog().isInfoEnabled()) {
						getLog().info(new StringBuffer("Ignoring a clazz " + clazz.getCanonicalName()));
					}
					continue;
				}
				if (getLog().isInfoEnabled()) {
					getLog().info(new StringBuffer("Scanning a clazz " + clazz.getCanonicalName()));
				}
				Method[] methodArr = clazz.getDeclaredMethods();
				if (methodArr.length == 0) {
					errorList.add("No Methods Found for clazz " + clazz.getCanonicalName());
				}
				for (Method method : methodArr) {
					Annotation ann1 = method.getAnnotation(PreAuthorize.class);
					Annotation ann2 = null;
					if (ignoreAnnotation == null) {
						Annotation[] annArr = method.getAnnotations();
						for (Annotation ann : annArr) {
							if (ann.getClass().getCanonicalName().equals(ignoreAnnotation)) {
								ann2 = ann;
								break;
							}
						}
					}
					if (ann1 == null && ann2 == null) {
						errorList
								.add("No org.springframework.security.access.prepost.PreAuthorize Annotation Found for "
										+ clazz.getCanonicalName() + "." + method.getName());
					}
				}
				for (String error : errorList) {
					getLog().error(error);
				}
			}
			if (!errorList.isEmpty()) {
				new MojoExecutionException("Found " + errorList.size() + " Errors in the Service Impl");
			}
		} catch (Exception e) {
			getLog().error(e.getMessage(), e);
			new MojoExecutionException(e.getMessage(), e);
		}
	}

}
