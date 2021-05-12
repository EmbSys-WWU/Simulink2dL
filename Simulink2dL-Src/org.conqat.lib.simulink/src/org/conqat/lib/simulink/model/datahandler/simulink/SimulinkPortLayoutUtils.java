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
package org.conqat.lib.simulink.model.datahandler.simulink;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Collection;

import org.conqat.lib.commons.assertion.CCSMAssert;
import org.conqat.lib.commons.string.StringUtils;
import org.conqat.lib.simulink.model.SimulinkBlock;
import org.conqat.lib.simulink.model.SimulinkConstants;
import org.conqat.lib.simulink.model.SimulinkInPort;
import org.conqat.lib.simulink.model.SimulinkOutPort;
import org.conqat.lib.simulink.model.SimulinkPortBase;
import org.conqat.lib.simulink.model.datahandler.BlockLayoutData;
import org.conqat.lib.simulink.model.datahandler.EOrientation;
import org.conqat.lib.simulink.util.SimulinkUtils;

/**
 * Utility code for finding the position of ports.
 */
/* package */ class SimulinkPortLayoutUtils {

	/**
	 * The number of pixels to add to the size of a round sum block when
	 * calculating the pre-port bend point for a line.
	 */
	private static final int ROUND_SUM_PRE_PORT_INSET = 13;

	/**
	 * The implicit grid size used in Simulink for placing ports, i.e. the
	 * distance between ports is a multiple of this.
	 */
	public static final int PORT_SPACING_GRID = 5;

	/**
	 * The number of pixels to add to the size of a "normal" block when
	 * calculating the pre-port bend point for a line connected to an outport.
	 */
	private static final int OUTPORT_INSET = PORT_SPACING_GRID;

	/**
	 * The number of pixels to add to the size of a "normal" block when
	 * calculating the pre-port bend point for a line connected to an inport.
	 */
	private static final int INPORT_INSET = 3 * PORT_SPACING_GRID;

	/** Returns the position of a port for the given block position. */
	public static Point getPortLocation(SimulinkPortBase port, BlockLayoutData blockLayout) {
		return getPortLocation(port, blockLayout, blockLayout.getPosition(), blockLayout.getPosition());
	}

	/** Returns the position of a port for the given block position. */
	private static Point getPortLocation(SimulinkPortBase port, BlockLayoutData blockLayout, Rectangle position,
			Rectangle originalSize) {
		EOrientation orientation = blockLayout.getOrientation();

		int x = position.x;
		int y = adjustLayoutYPosition(position.y, port, orientation, originalSize);
		int width = position.width;
		int height = position.height;

		// swap for up/down
		if (orientation.isRotated()) {
			int tmp = width;
			width = height;
			height = tmp;
		}

		Point offset = determinePortOffset(port, width, height, originalSize, orientation);

		if ("1".equals(port.getIndex()) && offset.y + (y - position.y) - height / 2 > PORT_SPACING_GRID) {
			offset.y -= PORT_SPACING_GRID;
		}

		// Flip for left/up, but not for the special ones
		if (orientation.isLeftOrUp() && !port.isSpecialPort()) {
			offset.x = width - offset.x;
		}

		// swap for up/down
		if (orientation.isRotated()) {
			return new Point(x + offset.y, y + offset.x);
		}

		// adjustments for non-aligned y position of block
		if (offset.y % PORT_SPACING_GRID == 0 && y % PORT_SPACING_GRID != 0 && hasSiblings(port)) {
			y = roundDownToGrid(y);
		}
		return new Point(x + offset.x, y + offset.y);
	}

	/** Returns whether a port has sibling ports. */
	private static boolean hasSiblings(SimulinkPortBase port) {
		if (port.isSpecialPort() || SimulinkUtils.isFunctionCallSplitBlock(port.getBlock())) {
			return false;
		}

		if (port instanceof SimulinkInPort) {
			return countNormalPorts(port.getBlock().getInPorts()) > 1;
		}
		return countNormalPorts(port.getBlock().getOutPorts()) > 1;
	}

	/**
	 * In Simulink, y positions are rounded up to next number divisible by 5
	 * before port calculation is done, but only for "normal" output ports in
	 * certain situations.
	 */
	private static int adjustLayoutYPosition(int y, SimulinkPortBase port, EOrientation orientation,
			Rectangle originalSize) {
		if (port.isSpecialPort()) {
			if (orientation.isRotated()) {
				return roundUpToGrid(y);
			}
			if (y % PORT_SPACING_GRID <= 1) {
				return y - 1 - y % PORT_SPACING_GRID;
			}
			return y;
		}

		int normalInPorts = countNormalPorts(port.getBlock().getInPorts());
		if (port instanceof SimulinkInPort && SimulinkConstants.SUBSYSTEM.equals(port.getBlock().getType())
				&& normalInPorts > 10 && originalSize.height > 300 && normalInPorts <= 25
				&& !port.getBlock().getOutPorts().isEmpty() && port.getBlock().getOutPorts().size() > 8) {
			return roundUpToGrid(y);
		}

		// special handling for library blocks
		if (SimulinkConstants.TYPE_REFERENCE.equals(port.getBlock().getType())
				&& port.getBlock().getParameter(SimulinkConstants.PARAM_LIBRARY_VERSION) != null && normalInPorts == 1
				&& port.getBlock().getOutPorts().size() > 1) {
			return roundUpToGrid(y) + 1;
		}

		// no adjustment for input ports and narrow blocks
		if (!(port instanceof SimulinkOutPort) || isNonadjustedBlockType(originalSize, port.getBlock(), false)) {
			return y;
		}

		// no adjustment for rotated
		if (orientation.isRotated()) {
			return y;
		}

		// special handling for TargetLink sum blocks
		if (SimulinkConstants.TYPE_SUM.equals(port.getBlock().getType())
				&& port.getBlock().getParameter(SimulinkConstants.PARAM_LIBRARY_VERSION) != null) {
			return roundUpToGrid(y) + 2;
		}

		// adjust only for blocks with 1 out-port and even number of in-ports
		if (normalInPorts > 1 && normalInPorts % 2 == 0 && port.getBlock().getOutPorts().size() == 1) {
			return roundUpToGrid(y);
		}

		return y;
	}

	/**
	 * Returns whether this port belongs to a block whose output is not
	 * adjusted.
	 */
	private static boolean isNonadjustedBlockType(Rectangle size, SimulinkBlock block, boolean isInput) {
		if (block.getType() == null) {
			return false;
		}

		if (isInput) {
			return false;
		}

		if (SimulinkUtils.isTargetlinkBlock(block)) {
			switch (block.getType()) {
			case SimulinkConstants.TYPE_MIN_MAX:
				return true;
			case SimulinkConstants.TYPE_LOGIC:
				return size.height == 56;
			default:
				return false;
			}
		}

		switch (block.getType()) {
		case SimulinkConstants.TYPE_LOGIC:
		case SimulinkConstants.TYPE_RELATIONAL_OPERATOR:
			return (size.width + 2 < size.height && size.width + 10 > size.height)
					|| (size.width * 2 < size.height + 5);

		case SimulinkConstants.TYPE_REFERENCE:
		case SimulinkConstants.TYPE_MANUAL_SWITCH:
		case SimulinkConstants.TYPE_GOTO:
		case SimulinkConstants.TYPE_FROM:
		case SimulinkConstants.TYPE_INPORT:
		case SimulinkConstants.TYPE_SELECTOR:
			return true;

		case SimulinkConstants.TYPE_PRODUCT:
			return "Matrix(*)".equals(block.getParameter("Multiplication"));

		case SimulinkConstants.TYPE_SUM:
			return size.width < 30;

		case SimulinkConstants.SUBSYSTEM:
			return isNonAdjustedSubsystem(size, block.getInPorts().size(), block.getOutPorts().size());

		default:
			// otherwise, narrow blocks are not adjusted
			return size.width < 4 * PORT_SPACING_GRID;
		}
	}

	/** Returns whether the given subsystem is nonadjusted. */
	private static boolean isNonAdjustedSubsystem(Rectangle size, int numInPorts, int numOutPorts) {
		if (Math.abs(2 * size.width - size.height) < 5) {
			return false;
		}

		if (numInPorts == 4 && numOutPorts == 1) {
			return true;
		}

		if (Math.abs(size.width - size.height) <= 2) {
			return true;
		}

		if (Math.abs(.85 - size.height / (double) size.width) <= 0.01) {
			return true;
		}

		if (size.width == 50 && numOutPorts > 1) {
			return false;
		}

		return size.width <= size.height;
	}

	/** Rounds the given value up to be aligned to the layout grid. */
	private static int roundUpToGrid(int value) {
		return ((value + PORT_SPACING_GRID - 1) / PORT_SPACING_GRID) * PORT_SPACING_GRID;
	}

	/** Rounds the given value down to be aligned to the layout grid. */
	private static int roundDownToGrid(int value) {
		return (value / PORT_SPACING_GRID) * PORT_SPACING_GRID;
	}

	/**
	 * Calculates and returns the x/y offset of the given port relative to a
	 * block with given width/height.
	 */
	private static Point determinePortOffset(SimulinkPortBase port, int width, int height, Rectangle originalSize,
			EOrientation orientation) {
		boolean isInput = port instanceof SimulinkInPort;
		SimulinkBlock block = port.getBlock();

		int xOffset = 0;
		if (!isInput) {
			xOffset = width;
		}

		String indexParam = port.getIndex();
		if (indexParam != null && indexParam.matches("\\d+")) {
			int index = Integer.parseInt(indexParam);

			if (isInput && SimulinkUtils.isRoundSum(block)) {
				return roundSumGetNthPortPos(block, index, width, height);
			}

			if (!isInput && SimulinkUtils.isFunctionCallSplitBlock(block)) {
				return functionCallSplitPortPos(index, width, height);
			}

			int numPorts = 1;
			if (isInput && SimulinkConstants.TYPE_SUM.equals(block.getType())) {
				// for non-round sum blocks, we also have to adjust positions
				String portsDescription = getSumInputPortsDescription(block);
				numPorts = portsDescription.length();
				index = getLogicalIndexForPort(portsDescription, index) + 1;
			} else if (isInput) {
				numPorts = countNormalPorts(block.getInPorts());
			} else {
				numPorts = countNormalPorts(block.getOutPorts());
			}

			// for single ports we only adjust if other port count is even
			int otherPortCount = otherPortCount(port);
			boolean mayAdjust = ((!isInput && !SimulinkConstants.TYPE_SUM.equals(port.getBlock().getType())
					&& (numPorts > 1 || (otherPortCount % 2 == 0 && otherPortCount > 0))) || (isInput && numPorts > 1))
					&& !isNonadjustedBlockType(originalSize, port.getBlock(), isInput);
			return new Point(xOffset,
					getPortYOffset(numPorts, index, height, mayAdjust, originalSize.y % PORT_SPACING_GRID));
		}

		return determineSpecialPortOffset(port, width, height, orientation);
	}

	/**
	 * Returns the port position for function-call split blocks (only out
	 * ports).
	 */
	private static Point functionCallSplitPortPos(int index, int width, int height) {
		switch (index) {
		case 1:
			return new Point(width / 2, height + OUTPORT_INSET);
		case 2:
		default:
			return new Point(width, height / 2);
		}
	}

	/**
	 * Counts the other ports, i.e. for an inport this returns the number of
	 * outports for the parent block.
	 */
	private static int otherPortCount(SimulinkPortBase port) {
		if (port instanceof SimulinkInPort) {
			return countNormalPorts(port.getBlock().getOutPorts());
		}
		return countNormalPorts(port.getBlock().getInPorts());
	}

	/** Returns the input ports description for a sum block. */
	public static String getSumInputPortsDescription(SimulinkBlock block) {
		String inputs = block.getParameter(SimulinkConstants.PARAM_INPUTS);
		if (inputs == null) {
			return "++";
		}

		try {
			int number = Integer.parseInt(inputs);
			return StringUtils.fillString(number, '+');
		} catch (NumberFormatException e) {
			// not a number, so return directly
			return inputs;
		}
	}

	/** Returns the offset to use for special ports (trigger, enable). */
	private static Point determineSpecialPortOffset(SimulinkPortBase port, int width, int height,
			EOrientation orientation) {
		int specialPortCount = 0;
		for (SimulinkInPort inPort : port.getBlock().getInPorts()) {
			if (inPort.isSpecialPort()) {
				specialPortCount += 1;
			}
		}

		int y = 0;
		if (isSpecialPortOnBottom(port)) {
			y = height;
		}

		if (specialPortCount <= 1) {
			return new Point((width + 1) / 2, y);
		}

		// enable port is first unless down
		boolean first = port instanceof SimulinkInPort && ((SimulinkInPort) port).isEnablePort();
		if (orientation == EOrientation.DOWN) {
			first = !first;
		}

		int distance = (int) (Math.round((width - 1) / 2. / PORT_SPACING_GRID) * PORT_SPACING_GRID);
		int offset = (width - distance + 1) / 2;
		if (orientation.isRotated() && specialPortCount > 1) {
			offset = roundUpToGrid(offset);
		}

		if (first) {
			return new Point(offset, y);
		}
		return new Point(offset + distance, y);
	}

	/**
	 * Returns whether special ports should be drawn on the bottom side (instead
	 * of top).
	 */
	public static boolean isSpecialPortOnBottom(SimulinkPortBase port) {
		return SimulinkConstants.VALUE_ALTERNATE
				.equals(port.getBlock().getParameter(SimulinkConstants.PARAM_NAME_PLACEMENT));
	}

	/**
	 * Returns the y offset (from the top) for the n-th port (1 indexed).
	 * 
	 * @param negativeShift
	 *            a value that will be potentially subtracted later on.
	 */
	private static int getPortYOffset(int numPorts, int portIndex, int height, boolean mayAdjust, int negativeShift) {
		CCSMAssert.isTrue(portIndex >= 1 && portIndex <= numPorts, "Port index out of range: " + portIndex);

		// spacing between ports is rounded to a multitude of 5
		int portSpacing = (int) (Math.round((height - numPorts / 2.) / PORT_SPACING_GRID / numPorts)
				* PORT_SPACING_GRID);
		portSpacing = Math.max(PORT_SPACING_GRID, portSpacing);

		if (portSpacing * (numPorts - 1) > height + 2) {
			portSpacing -= PORT_SPACING_GRID;
		}

		int offset = (height - (numPorts - 1) * portSpacing) / 2;

		if (portSpacing > PORT_SPACING_GRID && numPorts > 8
				&& (((offset - negativeShift + 1) < 0 && offset == 0) || offset < -10)) {
			portSpacing -= PORT_SPACING_GRID;
			offset = (height - (numPorts - 1) * portSpacing) / 2;
		}

		if (offset > 2 * PORT_SPACING_GRID && mayAdjust) {
			offset = roundUpToGrid(offset);
		}

		int adjustedOffset = offset - negativeShift;
		int remainder = height - adjustedOffset - (numPorts - 1) * portSpacing;
		if (needsExtraOffset(numPorts, offset, adjustedOffset, remainder)) {
			offset += PORT_SPACING_GRID;
		}

		return offset + (portIndex - 1) * portSpacing;
	}

	/** Returns whether extra offset should be added. */
	private static boolean needsExtraOffset(int numPorts, int offset, int adjustedOffset, int remainder) {
		if (remainder <= 2 * PORT_SPACING_GRID || remainder - adjustedOffset < PORT_SPACING_GRID) {
			return false;
		}

		if (numPorts >= 8) {
			return offset <= 4 * PORT_SPACING_GRID;
		}

		if (numPorts >= 6) {
			return offset <= 6 * PORT_SPACING_GRID;
		}

		return false;
	}

	/**
	 * Returns the position of the n-th input port for a rounded sum port. This
	 * deals only with the non-rotated case.
	 */
	private static Point roundSumGetNthPortPos(SimulinkBlock block, int n, int width, int height) {
		double angle = determineRoundSumAngle(block, n);
		return new Point((int) Math.round((1. - Math.sin(angle)) * width / 2),
				(int) Math.round((1 - Math.cos(angle)) * height / 2));
	}

	/**
	 * Returns the angle to be used for the n-th input port of a round sum port.
	 * The angle is 0 for up and a half rotation (counter clock-wise)
	 * corresponds to PI.
	 */
	public static double determineRoundSumAngle(SimulinkBlock block, int n) {
		String ports = getSumInputPortsDescription(block);
		double angle = Math.PI * getLogicalIndexForPort(ports, n) / (ports.length() - 1);
		if (ports.length() <= 1) {
			angle = Math.PI / 2;
		}
		return angle;
	}

	/**
	 * Returns the position of a point just before the actual port. Simulink
	 * seems to insert an addition bend point some pixel before.
	 */
	public static Point getPrePortPoint(SimulinkPortBase port, BlockLayoutData blockLayout) {
		int inset = OUTPORT_INSET;
		SimulinkBlock block = port.getBlock();
		if (port instanceof SimulinkInPort) {
			if (SimulinkUtils.isRoundSum(block)) {
				inset = ROUND_SUM_PRE_PORT_INSET;
			} else {
				inset = INPORT_INSET;
			}
		}
		return getInsetPortPoint(port, blockLayout, inset);
	}

	/**
	 * Returns the position of a point with a relative inset to the actual port.
	 * Simulink seems to insert an additional bend point some pixel before.
	 */
	public static Point getInsetPortPoint(SimulinkPortBase port, BlockLayoutData blockLayoutData, int inset) {
		int insetX = inset;
		int insetY = inset;
		SimulinkBlock block = port.getBlock();
		if (SimulinkUtils.isRoundSum(block) || SimulinkUtils.isFunctionCallSplitBlock(block)) {
			// nothing to do, adjust both directions
		} else if (blockLayoutData.getOrientation().isRotated() ^ port.isSpecialPort()) {
			// only adjust y direction
			insetX = 0;
		} else {
			// only adjust x direction
			insetY = 0;
		}

		Rectangle position = new Rectangle(blockLayoutData.getPosition());
		position.grow(insetX, insetY);
		return getPortLocation(port, blockLayoutData, position, blockLayoutData.getPosition());
	}

	/**
	 * Calculates the logical index of the port of given index of a sum block.
	 */
	public static int getLogicalIndexForPort(String ports, int index) {
		for (int i = 0; i < ports.length(); ++i) {
			if (ports.charAt(i) != '|') {
				index--;
				if (index == 0) {
					return i;
				}
			}
		}
		return ports.length() - 1;
	}

	/**
	 * Counts the number of "normal" numeric ports. This is simply the maximal
	 * number of a port.
	 */
	private static int countNormalPorts(Collection<? extends SimulinkPortBase> ports) {
		int result = 0;
		for (SimulinkPortBase port : ports) {
			if (!port.isSpecialPort()) {
				try {
					result = Math.max(result, Integer.parseInt(port.getIndex()));
				} catch (NumberFormatException e) {
					// ignore; result not increased
				}
			}
		}
		return result;
	}

	/** Returns the direction the given port is oriented to in degree. */
	public static double determineDirection(SimulinkPortBase port, BlockLayoutData blockLayoutData) {
		double direction = blockLayoutData.getOrientation().getDirection();
		if (SimulinkUtils.isRoundSum(port.getBlock()) && port instanceof SimulinkInPort) {
			double directionOffset = determineRoundSumAngle(port.getBlock(), Integer.parseInt(port.getIndex()))
					- Math.PI / 2;
			direction += directionOffset * 180. / Math.PI;
		} else if (isBottomFunctionCallSplitPort(port)) {
			direction += 270;
			if (blockLayoutData.getOrientation().isLeftOrDown()) {
				direction += 180;
			}
			direction %= 360;
		} else if (port.isSpecialPort()) {
			direction += 270;
			if (isSpecialPortOnBottom(port) && !blockLayoutData.getOrientation().isLeftOrDown()) {
				direction += 180;
			}
			direction %= 360;
		}
		return direction;
	}

	/**
	 * Returns whether this is the bottom port of a function call split block.
	 */
	private static boolean isBottomFunctionCallSplitPort(SimulinkPortBase port) {
		return SimulinkUtils.isFunctionCallSplitBlock(port.getBlock()) && port instanceof SimulinkOutPort
				&& "1".equals(port.getIndex());
	}
}