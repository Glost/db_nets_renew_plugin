package de.renew.net;

import java.io.Serializable;

import java.util.NoSuchElementException;


// This class collects a set of doubles interpreted as
// points of time. The handling is done by an AVL tree,
// which handles all requests in O(log n) time.
public class TimeSet implements Serializable {
    // This is a special singleton object that denotes the empty set.
    public static final TimeSet EMPTY = new TimeSet(null, 0, 0, null);

    // This is a special singleton object that denotes the 
    // set with a single object 0.
    public static final TimeSet ZERO = EMPTY.including(0.0);

    // All instance variables are supposed to be final,
    // but they must not be decalred that way due to a compiler bug.
    // The left subtree handles the earlier events.
    private TimeSet left;

    // This is my local time.
    private double time;

    // The right subtree handles the later events.
    private TimeSet right;

    // This is the neumber of times that my time is contained
    // in the set. This is useful if there are time instances
    // when lots of events happen at once. This is especially
    // important for untimed situations.
    private int mult;

    // This is my height. It is used to balance the tree.
    private int height;

    // This is my size. It may be queried.
    private int size;

    // This is my size without counting duplicates. It may be queried.
    private int uniqueSize;

    // This constructor of a tree node takes care of rotating
    // other tree nodes as required. No modifications are
    // performed for other tree nodes.
    private TimeSet(TimeSet left, double time, int mult, TimeSet right) {
        if (mult == 0) {
            // This is the special empty set.
            if (left != null || right != null || time != 0) {
                throw new RuntimeException("Bad empty time set parameters.");
            }
            this.left = null;
            this.right = null;
            this.mult = 0;
            this.time = 0;
            height = 0;
            size = 0;
            uniqueSize = 0;
        } else {
            // This is an ordinary time set.
            int leftHeight = left.height;
            int rightHeight = right.height;
            int diff = leftHeight - rightHeight;
            if (diff >= 2) {
                // Left side too high.
                int leftLeftHeight = left.left.height;
                int leftRightHeight = left.right.height;
                if (leftLeftHeight > leftRightHeight) {
                    this.left = left.left;
                    this.time = left.time;
                    this.mult = left.mult;
                    this.right = new TimeSet(left.right, time, mult, right);
                } else {
                    this.left = new TimeSet(left.left, left.time, left.mult,
                                            left.right.left);
                    this.time = left.right.time;
                    this.mult = left.right.mult;
                    this.right = new TimeSet(left.right.right, time, mult, right);
                }
            } else if (diff <= -2) {
                // Right side too high.
                int rightRightHeight = right.right.height;
                int rightLeftHeight = right.left.height;
                if (rightRightHeight > rightLeftHeight) {
                    this.left = new TimeSet(left, time, mult, right.left);
                    this.time = right.time;
                    this.mult = right.mult;
                    this.right = right.right;
                } else {
                    this.left = new TimeSet(left, time, mult, right.left.left);
                    this.time = right.left.time;
                    this.mult = right.left.mult;
                    this.right = new TimeSet(right.left.right, right.time,
                                             right.mult, right.right);
                }
            } else {
                // Already well balanced.
                this.left = left;
                this.time = time;
                this.mult = mult;
                this.right = right;
            }


            // This might be optimized and included in the
            // individual cases.
            height = Math.max(left.height, right.height) + 1;
            size = left.size + right.size + mult;
            uniqueSize = left.uniqueSize + right.uniqueSize + 1;
        }
    }

    public boolean isEmpty() {
        return mult == 0;
    }

    // The factory method exposes less of the internal structure 
    // of the time set.
    public static TimeSet make(double time) {
        return new TimeSet(EMPTY, time, 1, EMPTY);
    }

    public int getSize() {
        return size;
    }

    private TimeSet findNode(double searchTime) {
        TimeSet current = this;
        while (current.mult != 0) {
            if (current.time == searchTime) {
                return current;
            } else if (current.time < searchTime) {
                current = current.right;
            } else {
                current = current.left;
            }
        }
        ;

        return EMPTY;
    }

    public int multiplicity(double searchTime) {
        return findNode(searchTime).mult;
    }

    public TimeSet including(double newTime) {
        if (mult == 0) {
            // This is an empty time set.
            return make(newTime);
        }

        TimeSet newLeft;
        TimeSet newRight;

        if (newTime == time) {
            return new TimeSet(left, time, mult + 1, right);
        }

        // One of the subtrees must be recreated.
        if (newTime < time) {
            newLeft = left.including(newTime);
            newRight = right;
        } else {
            newLeft = left;
            newRight = right.including(newTime);
        }
        return new TimeSet(newLeft, time, mult, newRight);
    }

    public TimeSet excluding(double oldTime) {
        if (mult == 0) {
            throw new NoSuchElementException();
        }

        TimeSet newLeft;
        TimeSet newRight;
        double newTime;
        int newMult;

        if (oldTime == time) {
            if (mult > 1) {
                newLeft = left;
                newRight = right;
                newTime = time;
                newMult = mult - 1;
            } else if (right.mult == 0) {
                return left;
            } else if (left.mult == 0) {
                return right;
            } else {
                TimeSetResult reordered = right.extractLeftmost();
                newLeft = left;
                newRight = reordered.tree;
                newTime = reordered.time;
                newMult = reordered.mult;
            }
        } else {
            if (oldTime < time) {
                newLeft = left.excluding(oldTime);
                newRight = right;
            } else {
                newLeft = left;
                newRight = right.excluding(oldTime);
            }
            newTime = time;
            newMult = mult;
        }

        return new TimeSet(newLeft, newTime, newMult, newRight);
    }

    // Must not be called on the empty set.
    private TimeSetResult extractLeftmost() {
        if (mult == 0) {
            throw new RuntimeException("Illegal invocation of extractLeftmost().");
        } else if (left.mult == 0) {
            return new TimeSetResult(right, time, mult);
        } else {
            TimeSetResult lifted = left.extractLeftmost();
            return new TimeSetResult(new TimeSet(lifted.tree, time, mult, right),
                                     lifted.time, lifted.mult);
        }
    }

    // Fetch the earliest point of time contained in this collection.
    public double earliestTime() {
        TimeSet tree = this;
        do {
            if (tree.left.mult == 0) {
                return tree.time;
            }
            tree = tree.left;
        } while (true);
    }

    // Fetch the earliest point of time contained in this collection.
    public double latestTime() throws NoSuchElementException {
        TimeSet tree = this;
        if (mult == 0) {
            throw new NoSuchElementException();
        }

        do {
            if (tree.right.mult == 0) {
                return tree.time;
            }
            tree = tree.right;
        } while (true);
    }

    // Fetch the last point of time that allows a certain delay
    // without missing a given deadline. It is not the same to
    // use deadline-delay as the new deadline, as round-off
    // errors might bias the possible values.
    public double latestWithDelay(double delay, double deadline) {
        TimeSet current = this;
        TimeSet best = null;
        while (current.mult != 0) {
            if (current.time + delay <= deadline) {
                // The current node is a possible candidate
                // because it can meet the deadline.
                best = current;


                // Proceed down the right subtree to find better estimates.
                // The left subtree has only worse solutions.
                current = current.right;
            } else {
                // Only the left subtree might have solutions.
                current = current.left;
            }
        }

        if (best != null) {
            return best.time;
        } else {
            throw new NoSuchElementException();
        }
    }

    // Return the contents of this set as an array.
    public double[] asUniqueArray() {
        double[] result = new double[uniqueSize];
        fillIn(result, 0, true);
        return result;
    }

    // Return the contents of this set as an array.
    public double[] asArray() {
        double[] result = new double[size];
        fillIn(result, 0, false);
        return result;
    }

    // Copy the contents of this set into an array.
    private void fillIn(double[] result, int start, boolean unique) {
        if (mult == 0) {
            return;
        }
        left.fillIn(result, start, unique);
        if (unique) {
            start = start + left.uniqueSize;
            result[start++] = time;
        } else {
            start = start + left.size;
            for (int i = 0; i < mult; i++) {
                result[start++] = time;
            }
        }
        right.fillIn(result, start, unique);
    }

    // Requires O(min(size,that.size)).
    public double computeEarliestTime(TimeSet that) {
        return computeEarliestTime(that.asArray());
    }

    // Requires O(min(size,times.length)).
    public double computeEarliestTime(double[] delays) {
        if (delays.length > size) {
            // The other set contains more elements than I do.
            // I cannot match it in number, no matter how long 
            // we wait.
            return Double.POSITIVE_INFINITY;
        }
        return computeEarliestTime(delays, delays.length - 1);
    }

    // Check part of the necessary comparisons.
    private double computeEarliestTime(double[] delays, int offset) {
        double earliest = Double.NEGATIVE_INFINITY;
        if (mult > 0) {
            earliest = left.computeEarliestTime(delays, offset);
            offset -= left.size;
            if (offset >= 0) {
                // Regardless of the multiplier, we only need to check against
                // the last entry in the array, because the array is ordered.
                earliest = Math.max(earliest, time + delays[offset]);
                offset -= mult;
                if (offset >= 0) {
                    earliest = Math.max(earliest,
                                        right.computeEarliestTime(delays, offset));
                }
            }
        }
        return earliest;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("TimeSet(");
        toString(buffer);
        buffer.append(')');
        return buffer.toString();
    }

    private void toString(StringBuffer buffer) {
        if (mult == 0) {
            return;
        } else {
            left.toString(buffer);
            for (int i = 0; i < mult; i++) {
                buffer.append(' ');
                buffer.append(time);
            }
            right.toString(buffer);
        }
    }
}

class TimeSetResult {
    final TimeSet tree;
    final double time;
    final int mult;

    TimeSetResult(TimeSet tree, double time, int mult) {
        this.tree = tree;
        this.time = time;
        this.mult = mult;
    }
}