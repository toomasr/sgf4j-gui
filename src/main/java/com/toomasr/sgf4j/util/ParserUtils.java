package com.toomasr.sgf4j.util;

import com.sun.media.jfxmedia.logging.Logger;
import com.toomasr.sgf4j.parser.GameNode;

public class ParserUtils {
	/*
	 * Imagine a tree like this.
	 * 
	 * - - - - - C |- - B |A - X - -
	 * 
	 * A minus is a move and | is branching. Now when you are at move 6 and actually
	 * at the branch where the move A is then the correspondingTopMove is B. And the
	 * correspondingTopMove for B is C.
	 * 
	 * The corresponding top move for X is null. This utility method helps jumping
	 * between the game tree nodes to quickly visually alter between "what-if"
	 * scenarios.
	 */
	public static GameNode findCorrespondingTopMove(final GameNode node) {
		// no correspondingTopMove for the 0th line
		if (node.getVisualDepth() < 1)
			return null;

		GameNode tmpNode = node;
		GameNode foundNode = null;
		// find the previous branching place
		do {
			foundNode = tmpNode;
			tmpNode = tmpNode.getPrevNode();
		} while (tmpNode != null);

		// now fast forward to the correct move number
		tmpNode = foundNode.getParentNode();
		do {
			tmpNode = tmpNode.getNextNode();
			// we ran off the cliff, no such move one level up
			if (tmpNode == null) {
				return null;
			}
		} while (tmpNode.getMoveNo() != node.getMoveNo());
		return tmpNode;
	}

	/*
	 * See the description of findCorrespondingTopMove. This method does the same
	 * but just one level down
	 */
	public static GameNode findCorrespondingBelowMove(final GameNode node) {
		GameNode tmpNode = node;
		GameNode foundNode = node;
		do {
			// the far left element will have prevNode null
			if (tmpNode.getPrevNode() == null) {
				// if there is a parent then there is a parent line of play
				if (tmpNode.getParentNode() != null) {
					tmpNode = tmpNode.getParentNode();
				}
				// no parent, main line and we cannot go further left
				else {
					return null;
				}
			}
			else {
				tmpNode = tmpNode.getPrevNode();
			}
		} while (!tmpNode.hasChildren());
		
		foundNode = tmpNode;
		for (GameNode childNode : foundNode.getChildren()) {
			System.out.println("YYYYYYYYYYYYYYY");
			System.out.println(childNode+" XXXXXXXXX "+node);
			// maybe the child is the one 
			if (childNode.getMoveNo() == node.getMoveNo() && childNode.getVisualDepth() != node.getVisualDepth())
				return childNode;

			// nope, let's go through the line
			do {
				tmpNode = childNode.getNextNode();
				// no luck for this child
				if (tmpNode == null) {
					break;
				}
			} while (tmpNode.getMoveNo() != node.getMoveNo());
			return tmpNode;
		}

		return null;
	}
}
