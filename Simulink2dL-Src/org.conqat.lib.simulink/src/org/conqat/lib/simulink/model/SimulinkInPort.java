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

import org.conqat.lib.commons.assertion.CCSMAssert;

/**
 * A Simulink inport. An inport can be connected to only one
 * {@link SimulinkLine}.
 */
public class SimulinkInPort extends SimulinkPortBase {

	/** The line connected to this port (or null). */
	private SimulinkLine line;

	/**
	 * Create simulink inport.
	 * 
	 * @param block
	 *            The block this port belongs to.
	 * @param index
	 *            The port index. This may be a number or a string like 'enable'
	 */
	public SimulinkInPort(SimulinkBlock block, String index) {
		super(block, index);
		block.addInPort(this);
	}

	/**
	 * Get line connected to this port.
	 * 
	 * @return the line or <code>null</code> if no line is connected.
	 */
	public SimulinkLine getLine() {
		return line;
	}

	/**
	 * Set line connected to this port. This is only called from the
	 * {@link SimulinkLine}.
	 * 
	 * @throws AssertionError
	 *             if this port already has a line or the line's destination
	 *             port does not match this port.
	 */
	/* package */void setLine(SimulinkLine line) throws IllegalArgumentException {
		CCSMAssert.isTrue(this.line == null, "Port already has a line");
		CCSMAssert.isTrue(line.getDstPort() == this, "Line's port does not match.");
		this.line = line;
	}

	/**
	 * Remove line. This is only called from the {@link SimulinkLine}.
	 * 
	 * @throws AssertionError
	 *             if the provided line is not connected to this port
	 */
	/* package */void removeLine(SimulinkLine line) throws IllegalArgumentException {
		CCSMAssert.isTrue(line != null, "Can not remove null line.");
		CCSMAssert.isTrue(line == this.line, "Line does not belong to this port.");
		this.line = null;
	}

	/** {@inheritDoc} */
	@Override
	public void remove() {
		getBlock().removeInPort(this);
		if (line != null) {
			line.remove();
		}
		super.remove();
	}

	/** {@inheritDoc} */
	@Override
	public boolean isConnected() {
		return line != null;
	}

	/** Returns whether this is a trigger port. */
	public boolean isTriggerPort() {
		return SimulinkConstants.TYPE_TRIGGER.equals(getIndex());
	}

	/** Returns whether this is an enable port. */
	public boolean isEnablePort() {
		return SimulinkConstants.TYPE_ENABLE.equals(getIndex());
	}

	/** Returns whether this is a reset port. */
	public boolean isResetPort() {
		// Need to ignore casing, as reset is written differently in different
		// places in the model
		return SimulinkConstants.TYPE_RESET.equals(getIndex());
	}

	/** Returns whether this is an action port. */
	public boolean isActionPort() {
		return SimulinkConstants.TYPE_IFACTION.equals(getIndex());
	}

	/** Returns whether this is a state port */
	public boolean isStatePort() {
		return getIndex().equals("state");
	}

	/** {@inheritDoc} */
	@Override
	public boolean isSpecialPort() {
		return isEnablePort() || isTriggerPort() || isResetPort() || isActionPort() || isStatePort();
	}
}
