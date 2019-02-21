package com.toomasr.sgf4j.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.toomasr.sgf4j.parser.GameNode;

public class ParserUtils {
  private static final Logger logger = LoggerFactory.getLogger(ParserUtils.class);

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
    GameNode branchingNode = findPreviousBranchingNode(node);

    // if we are at a branching point with multiple children
    // we need to figure out the line that is just one move up from
    // the node. Otherwise we always jump to the highest possible
    if (branchingNode.hasChildren()) {
      List<GameNode> tmpList = new ArrayList<GameNode>(branchingNode.getChildren());
      Collections.reverse(tmpList);
      for (GameNode childNode : tmpList) {
        if (childNode.getVisualDepth() < node.getVisualDepth()) {
          GameNode rtrnNode = findMoveOnBranchByNumber(childNode, node.getMoveNo());
          if (rtrnNode != null) {
            return rtrnNode;
          }
        }
      }
    }
    return findMoveOnBranchByNumber(branchingNode, node.getMoveNo());
  }

  public static GameNode findMoveOnBranchByNumber(final GameNode node, final int moveNo) {
    if (node.getMoveNo() == moveNo)
      return node;

    GameNode tmpNode = node;
    do {
      tmpNode = tmpNode.getNextNode();
      // we ran off the cliff, no such move one level up
      if (tmpNode == null) {
        return null;
      }
    }
    while (tmpNode.getMoveNo() != moveNo);
    return tmpNode;
  }

  /*
   * See the description of findCorrespondingTopMove. This method does the same
   * but just one level down
   */
  public static GameNode findCorrespondingBelowMove(final GameNode node) {
    GameNode tmpNode = node;

    GameNode branchingPoint = findPreviousBranchingNode(node);
    if (branchingPoint == null)
      return null;

    for (GameNode childNode : branchingPoint.getChildren()) {
      // ignore children that are at upper locations
      // meaning children with lower visualDepth
      if (childNode.getVisualDepth() <= node.getVisualDepth())
        continue;
      // maybe the child is the one
      if (childNode.getMoveNo() == node.getMoveNo() && childNode.getVisualDepth() != node.getVisualDepth())
        return childNode;
      tmpNode = childNode;
      // nope, let's go through the line
      do {
        tmpNode = tmpNode.getNextNode();
        // no luck for this child
        if (tmpNode == null) {
          break;
        }
      }
      while (tmpNode.getMoveNo() != node.getMoveNo());
      return tmpNode;
    }

    return null;
  }

  private static GameNode findPreviousBranchingNode(final GameNode node) {
    GameNode tmpNode = node;
    do {
      // the far left (previous branching) element will have prevNode null
      if (tmpNode.getPrevNode() == null) {
        // if there is a parent then there is a parent line of play
        // and we should continue from there
        if (tmpNode.getParentNode() != null) {
          return tmpNode.getParentNode();
        }
        // no parent, it is the main line and we cannot go further left
        else {
          return null;
        }
      }
      else {
        tmpNode = tmpNode.getPrevNode();
      }
    }
    while (!tmpNode.hasChildren());
    return tmpNode;
  }
}
