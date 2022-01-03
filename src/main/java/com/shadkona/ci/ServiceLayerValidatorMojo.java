package com.shadkona.ci;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
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
@Mojo(name = "svc-layer-validate", defaultPhase = LifecyclePhase.COMPILE)
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
			Reflections reflections = new Reflections(pkg);
			Set<Class<?>> allSet = reflections.getTypesAnnotatedWith(org.springframework.stereotype.Service.class,
					true);
			if (allSet.isEmpty()) {
				getLog().info("No Service Impl classes Found");
				return;
			}

			Map<Class<?>, String> ignoreClazzMap = new HashMap<Class<?>, String>();
			for (int idx = 0; idx < ignoreClassList.length; idx++) {
				ignoreClazzMap.put(Class.forName(ignoreClassList[idx]), "");
			}

			Class<Annotation> ignoreAnnotationClazz = (Class<Annotation>) Class.forName(ignoreAnnotation);

			List<String> errorList = new ArrayList<>();
			for (Class<?> clazz : allSet) {
				if (ignoreClazzMap.get(clazz) != null) {
					if (getLog().isDebugEnabled()) {
						getLog().debug(new StringBuffer("Ignoring a clazz " + clazz.getCanonicalName()));
					}
					continue;
				}
				if (getLog().isDebugEnabled()) {
					getLog().debug(new StringBuffer("Scanning a clazz " + clazz.getCanonicalName()));
				}
				Method[] methodArr = clazz.getDeclaredMethods();
				if (methodArr.length == 0) {
					errorList.add("No Methods Found for clazz " + clazz.getCanonicalName());
				}
				for (Method method : methodArr) {
					Annotation ann1 = method.getAnnotation(PreAuthorize.class);
					Annotation ann2 = method.getAnnotation(ignoreAnnotationClazz);
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
