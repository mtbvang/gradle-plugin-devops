package com.mtbvang.plugin

import java.util.Map
import org.gradle.api.Project
import org.apache.commons.lang3.builder.ToStringBuilder

class DevtoolPluginExtension {
	
	/*
	 * Extensions properties are added dynamically via meta object programing in DevtoolPlugin.groovy.
	 */
	@Override
	public String toString()
	{
	  return ToStringBuilder.reflectionToString(this);
	}
	
}
