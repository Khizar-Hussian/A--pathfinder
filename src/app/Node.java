package app;

public class Node {
	int x, y, g, h, f;
	Node parent;
	
	public Node()
	{
		
	}
	public Node(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getG() {
		return g;
	}

	public int getH() {
		return h;
	}

	public int getF() {
		return f;
	}

	public Node getNode() {
		return parent;
	}
	
	public Node getParent() {
		return parent;
	}

	public void setXY(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public void setG(int g) {
		this.g = g;
	}

	public void setH(int h) {
		this.h = h;
	}

	public void setF(int f) {
		this.f = f;
	}

	public void setParent(Node parent) {
		this.parent = parent;
	}

	public static boolean isEqual(Node s, Node e) {
		if (s.x == e.x && s.y == e.y)
			return true;
		return false;
	}
}
