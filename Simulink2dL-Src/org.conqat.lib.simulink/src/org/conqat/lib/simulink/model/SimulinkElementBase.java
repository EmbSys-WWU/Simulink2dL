/*-------------------------------------------------------------------------+
|                                                                          |
| Copyright 2005-2011 The ConQAT Project                                   |
|                                                                          |
| Licensed under the Apache License, Version 2.0 (the "License");          |
| you may not use this file except in compliance with the License.         |
| You may obtain a copy of the License at                                  |
|                                                                          |
|    http://www.apache.org/licenses/LICENSE-2.0                            |
|                                                                          |
| Unless required by applicable law or agreed to in writing, software      |
| distributed under the License is distributed on an "AS IS" BASIS,        |
| WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. |
| See the License for the specific language governing permissions and      |
| limitations under the License.                                           |
+-------------------------------------------------------------------------*/
package org.conqat.lib.simulink.model;

import java.util.ArrayList;
import java.util.Set;

import org.conqat.lib.commons.assertion.CCSMAssert;
import org.conqat.lib.commons.clone.IDeepCloneable;
import org.conqat.lib.commons.collections.CollectionUtils;
import org.conqat.lib.commons.collections.IdentityHashSet;
import org.conqat.lib.commons.collections.UnmodifiableSet;
import org.conqat.lib.simulink.util.SimulinkUtils;

/**
 * Base class for Simulink elements. This is either a {@link SimulinkAnnotation}
 * or a {@link SimulinkBlock}. The common aspect is that they have a name and a
 * parent and can store meta-data in objects.
 */
public abstract class SimulinkElementBase extends ParameterizedElement implements IDeepCloneable {

	/** The parent of this element. */
	private SimulinkElementBase parent;

	/** Objects of this element. */
	private final Set<SimulinkObject> objects = new IdentityHashSet<>();

	/** Create element. */
	protected SimulinkElementBase() {
		// required to also have a default constructor
	}

	/** Create element from other element (for deep cloning). */
	protected SimulinkElementBase(SimulinkElementBase other) {
		super(other);

		for (SimulinkObject object : other.objects) {
			addObject(object.deepClone());
		}
	}

	/** Get id of this element. */
	public String getId() {
		return SimulinkUtils.buildId(parent, getName());
	}

	/**
	 * Get resolved id of this element. This is different from {@link #getId()}
	 * for library blocks included in another model, as then the id of the
	 * surrounding block is prefixed.
	 */
	public String getResolvedId() {
		String discardedPrefix = getModel().getModelDataHandler().getDiscardedPrefix();
		if (discardedPrefix != null && getId().equals(discardedPrefix)) {
			return getModel().getModelDataHandler().getParentBlockId();
		}

		if (parent != null) {
			return parent.getResolvedId() + SimulinkUtils.SIMULINK_ID_SEPARATOR
					+ SimulinkUtils.escapeSlashes(getName());
		}
		return SimulinkUtils.escapeSlashes(getName());
	}

	/** Get the model this element belongs to or null. */
	public SimulinkModel getModel() {
		if (parent == null) {
			return null;
		}
		return parent.getModel();
	}

	/** Returns the name. May return null if no name parameter is set. */
	public String getName() {
		return getParameter(SimulinkConstants.PARAM_NAME);
	}

	/** Returns the parent element (may be null). */
	public SimulinkElementBase getParent() {
		return parent;
	}

	/** Remove this element from the model. */
	public void remove() {
		if (parent != null) {
			parent.removeElement(this);
			parent = null;
		}

		for (SimulinkObject object : new ArrayList<>(objects)) {
			object.remove();
		}
	}

	/**
	 * Removes the given element. The default implementation only handles
	 * {@link SimulinkObject}s, but subclasses must override this method to
	 * support all types of children they support.
	 */
	protected void removeElement(SimulinkElementBase element) {
		if (element instanceof SimulinkObject) {
			objects.remove(element);
		} else {
			CCSMAssert.fail(element.getClass().getName() + " is an unknown sub class of "
					+ SimulinkElementBase.class.getName());
		}
	}

	/** Get string representation of this block. */
	@Override
	public String toString() {
		return getName();
	}

	/**
	 * Sets the parent for this block.
	 * 
	 * @throws AssertionError
	 *             if element already has parent or the new parent is
	 *             <code>null</code>.
	 */
	protected void setParent(SimulinkElementBase parent) {
		CCSMAssert.isTrue(this.parent == null, "Element already has a parent!");
		CCSMAssert.isFalse(parent == null, "Parent cannot be null!");
		this.parent = parent;
	}

	/** Add a Simulink object. */
	public void addObject(SimulinkObject object) {
		objects.add(object);
		object.setParent(this);
	}

	/** Get objects. */
	public UnmodifiableSet<SimulinkObject> getObjects() {
		return CollectionUtils.asUnmodifiable(objects);
	}
}