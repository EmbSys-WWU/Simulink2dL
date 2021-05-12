/*-----------------------------------------------------------------------+
 | com.teamscale.index
 |                                                                       |
   $Id: codetemplates.xml 18709 2009-03-06 13:31:16Z hummelb $            
 |                                                                       |
 | Copyright (c)  2009-2016 CQSE GmbH                                 |
 +-----------------------------------------------------------------------*/
package org.conqat.lib.simulink.ui;

/**
 * Base class for transfering UI data to the web client.
 * 
 * @ConQAT.Rating RED Hash: 123
 */
public abstract class SimulinkUiDataBase {

	/** The name of the corresponding block. */
	private final String name;

	/** The qualified name ("id") of the block */
	private final String qualifiedName;

	/** Constructor. */
	protected SimulinkUiDataBase(String name, String qualifiedName) {
		this.name = name;
		this.qualifiedName = qualifiedName;
	}

	/** @see #name */
	public String getName() {
		return name;
	}

	/** @see #qualifiedName */
	public String getQualifiedName() {
		return qualifiedName;
	}
}
