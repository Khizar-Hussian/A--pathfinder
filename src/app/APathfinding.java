package app;

import java.util.ArrayList;
import java.util.Stack;

public class APathfinding {
	int size, diagonalMoveCost;
	long runTime;
	double kValue; // didnt use
	Frame frame;
	Node startNode, endNode, par;
	boolean diagonal, running, noPath, complete, trig;
	ArrayList<Node> borders, open, closed;
	Stack<Node> path;
	Sort sort = new Sort();

	public APathfinding(Frame frame, int size) {
		this.frame = frame;
		this.size = size;

		diagonalMoveCost = (int) (Math.sqrt(2 * (Math.pow(size, 2))));
		kValue = Math.PI / 2;
		diagonal = true;
		trig = false;
		running = false;
		complete = false;

		borders = new ArrayList<Node>();
		open = new ArrayList<Node>();
		closed = new ArrayList<Node>();
		path = new Stack<Node>();
	}

	public void start(Node s, Node e) {
		running = true;
		startNode = s;
		startNode.g=0;
		endNode = e;

		// Adding the starting node to the closed list
		addClosed(startNode);
		long startTime = System.currentTimeMillis();

		findPath(startNode);

		complete = true;
		long endTime = System.currentTimeMillis();
		runTime = endTime - startTime;
		System.out.println("Completed: " + runTime + "ms");
	}

	public void setup(Node s, Node e) {
		running = true;
		startNode = s;
		startNode.setG(0);
		par = startNode;
		endNode = e;

		// Adding the starting node to the closed list
		addClosed(startNode);
	}

	public void setStart(Node s) {
		startNode = s;
		startNode.setG(0);
	}

	public void setEnd(Node e) {
		endNode = e;
	}

	public boolean isRunning() {
		return running;
	}

	public boolean isComplete() {
		return complete;
	}

	public Node getStart() {
		return startNode;
	}

	public Node getEnd() {
		return endNode;
	}

	public Node getPar() {
		return par;
	}

	public boolean isNoPath() {
		return noPath;
	}

	public boolean isDiagonal() {
		return diagonal;
	}

	public boolean isTrig() {
		return trig;
	}

	public void setDiagonal(boolean d) {
		diagonal = d;
	}

	public void setTrig(boolean t) {
		trig = t;
	}

	public void setSize(int s) {
		size = s;
		diagonalMoveCost = (int) (Math.sqrt(2 * (Math.pow(size, 2))));
	}

	public void findPath(Node parent) {

		if (diagonal) {
			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++) {
					if (i == 1 && j == 1) {
						continue;
					}

					int X = (parent.x - size) + (size * i);
					int Y = (parent.y - size) + (size * j);
					int boundryX = parent.x + (X - parent.x);
					int boundryY = parent.y + (Y - parent.y);

					// Disables ability to cut corners around borders
					if (searchBorder(boundryX, parent.y) != -1
							|| searchBorder(parent.x, boundryY) != -1 && ((j == 0 | j == 2) && i != 1))
						continue;
					calculateNodeValues(X, Y, parent);
				}
			}
		} else if (!trig) {
			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++) {
					if ((i == 0 && j == 0) || (i == 0 && j == 2) || (i == 1 && j == 1) || (i == 2 && j == 0)
							|| (i == 2 && j == 2)) {
						continue;
					}
					int X = (parent.x - size) + (size * i);
					int Y = (parent.y - size) + (size * j);
					calculateNodeValues(X, Y, parent);
				}
			}
		} else {
			for (int i = 0; i < 4; i++) {
				// Uses cosine and sine functions to get circle of points
				// around parent
				int X = (int) Math.round(parent.x + (-size * Math.cos(kValue * i)));
				int Y = (int) Math.round(parent.y + (-size * Math.sin(kValue * i)));
				calculateNodeValues(X, Y, parent);
			}
		}

		// Set the new parent node
		parent = lowestFCost();

		if (parent == null) {
			System.out.println("END: NO PATH");
			noPath = true;
			running = false;
			frame.repaint();
			return;
		}

		if (Node.isEqual(parent, endNode)) {
			endNode.parent = parent.parent;

			connectPath();
			running = false;
			complete = true;
			frame.repaint();
			return;
		}

		removeOpen(parent);
		addClosed(parent);

		// Allows correction for shortest path during runtime
		if (diagonal) {
			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++) {
					if (i == 1 && j == 1) {
						continue;
					}
					int possibleX = (parent.x - size) + (size * i);
					int possibleY = (parent.y - size) + (size * j);
					Node openCheck = getOpenNode(possibleX, possibleY);

					// check if it actually is an open node then use its g cost for further calculation
					if (openCheck != null) {
						int diffX = parent.x - openCheck.x;
						int diffY = parent.y - openCheck.y;
						int newG = parent.getG();

						if (diffX != 0 && diffY != 0) {
							newG += diagonalMoveCost;
						} else {
							newG += size;
						}

						if (newG < openCheck.g) {
							int s = searchOpen(possibleX, possibleY);
							open.get(s).parent = parent;
							open.get(s).g=newG;
							open.get(s).f = open.get(s).g + open.get(s).h;
						}
					}
				}
			}
		}
		if (!frame.showSteps()) {
			findPath(parent);
		} else {
			par = parent;
		}
	}

	public void calculateNodeValues(int x, int y, Node parent) {
		// bounday checks
		if (x < 0 || y < 0 || x >= frame.getWidth() || y >= frame.getHeight())
			return;

		// check if node is already opened or closed or a wall -> dont add it to open
		if (searchBorder(x, y) != -1 || searchClosed(x, y) != -1 || searchOpen(x, y) != -1)
			return;

		Node node = new Node(x, y);
		node.setParent(parent);

		// Calculating G cost
		int Gx = node.x - parent.y;
		int Gy = node.x - parent.y;
		int gCost = parent.g;

		if (Gx != 0 && Gy != 0)
			gCost += diagonalMoveCost;
		else
			gCost += size;
		node.setG(gCost);

		// Calculating H Cost using manhattan distance
		int hCost = Math.abs(endNode.x - node.x) + Math.abs(endNode.y - node.y);
		node.setH(hCost);

		// Calculating F Cost
		int fCost = gCost + hCost;
		node.setF(fCost);
		addOpen(node);
	}

	public void connectPath() {
		if (path.size() == 0) {
			Node parentNode = endNode.getParent();
			while (!Node.isEqual(parentNode, startNode)) {
				addPath(parentNode);
				for (int i = 0; i < closed.size(); i++) {
					Node current = closed.get(i);
					if (Node.isEqual(current, parentNode)) {
						parentNode = current.getParent();
						break;
					}
				}
			}
		}
	}

	public void addBorder(Node node) {
		if (!borders.contains(node))
			borders.add(node);
	}

	public void addOpen(Node node) {
		if (!open.contains(node))
			open.add(node);
	}

	public void addClosed(Node node) {
		if (!closed.contains(node))
			closed.add(node);
	}

	public void addPath(Node node) {
		path.push(node);
	}

	public void removePath(int location) {
		path.remove(location);
	}

	public void removeBorder(int location) {
		borders.remove(location);
	}

	public void removeOpen(int location) {
		open.remove(location);
	}

	public void removeOpen(Node node) {
		open.remove(node);
	}

	public void removeClosed(int location) {
		closed.remove(location);
	}

	public int searchBorder(int x, int y) {
		int Location = -1;

		for (int i = 0; i < borders.size(); i++) {
			if (borders.get(i).x == x && borders.get(i).y == y) {
				Location = i;
				break;
			}
		}
		return Location;
	}

	public int searchClosed(int x, int y) {
		int Location = -1;

		for (int i = 0; i < closed.size(); i++) {
			if (closed.get(i).x == x && closed.get(i).y == y) {
				Location = i;
				break;
			}
		}
		return Location;
	}

	public int searchOpen(int x, int y) {
		int Location = -1;

		for (int i = 0; i < open.size(); i++) {
			if (open.get(i).x == x && open.get(i).y == y) {
				Location = i;
				break;
			}
		}
		return Location;
	}

	public Node lowestFCost() {
		if (open.size() > 0) {
			sort.bubbleSort(open);
			return open.get(0);
		}
		return null;
	}

	public ArrayList<Node> getBorderList() {
		return borders;
	}

	public ArrayList<Node> getOpenList() {
		return open;
	}

	public Node getOpen(int location) {
		return open.get(location);
	}

	public ArrayList<Node> getClosedList() {
		return closed;
	}

	public Stack<Node> getPathList() {
		return path;
	}

	public long getRunTime() {
		return runTime;
	}

	public void reset() {
		open.clear();
		closed.clear();
		path.clear();

		noPath = false;
		running = false;
		complete = false;
	}

	public Node getOpenNode(int x, int y) {
		for (int i = 0; i < open.size(); i++) {
			if (open.get(i).x == x && open.get(i).getY() == y) {
				return open.get(i);
			}
		}
		return null;
	}

	/// Debugging Functions
	public void printBorderList() {
		for (int i = 0; i < borders.size(); i++) {
			System.out.print(borders.get(i).x + ", " + borders.get(i).y);
			System.out.println();
		}
		System.out.println("===============");
	}

	public void printOpenList() {
		for (int i = 0; i < open.size(); i++) {
			System.out.print(open.get(i).x + ", " + open.get(i).y);
			System.out.println();
		}
		System.out.println("===============");
	}

	public void printPathList() {
		for (int i = 0; i < path.size(); i++) {
			System.out.print(i + ": " + path.get(i).getX() + ", " + path.get(i).getY() + ": " + path.get(i).getF());
			System.out.println();
		}
		System.out.println("===============");
	}
}
