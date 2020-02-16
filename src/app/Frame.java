package app;

import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import javax.swing.*;
import java.util.List;

public class Frame extends JPanel implements ActionListener, MouseListener, MouseMotionListener, MouseWheelListener, KeyListener
{	
	private static final long serialVersionUID = 1L;
	ControlHandler controller;
	JFrame window;
	APathfinding pathfinding;
	boolean showSteps, btnHover;
	int size;
	double A1, A2;
	char currentKey = (char) 0;
	Node startNode, endNode;
	String mode;

	Timer timer = new Timer(100, this);
	int r = randomInRange(0, 255);
	int G = randomInRange(0, 255);
	int b = randomInRange(0, 255);

	public Frame() {
		controller = new ControlHandler(this);
		size = 25;
		mode = "Map Creation";
		showSteps = true;
		btnHover = false;
		setLayout(null);
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		addKeyListener(this);
		setFocusable(true);
		setFocusTraversalKeysEnabled(false);

		// Set up pathfinding
		pathfinding = new APathfinding(this, size);
		pathfinding.setDiagonal(true);

		// Calculating value of a in speed function
		// jugar to make speed slider work
		A1 = (5000.0000 / (Math.pow(25.0000 / 5000, 1 / 49)));
		A2 = 625.0000;

		// Set up window
		window = new JFrame();
		window.setContentPane(this);
		window.setTitle("Pathfinding Visualization");
		window.getContentPane().setPreferredSize(new Dimension(700, 600));
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.pack();
		window.setLocationRelativeTo(null);
		window.setVisible(true);

		// Add all controls
		controller.addAll();

		this.revalidate();
		this.repaint();
	}
	
	// Starts path finding
	void start() {
		if (startNode != null && endNode != null) {
			if (!showSteps) {
				pathfinding.start(startNode, endNode);
			} else {
				pathfinding.setup(startNode, endNode);
				setSpeed();
				timer.start();
			}
		} else {
			System.out.println("ERROR: Start or End Nodes(s) unspecified.");
		}
	}
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		// Grab dimensions of panel
		int height = getHeight();

		// If no path is found
		if (pathfinding.isNoPath()) {
			// Set timer for animation
			timer.setDelay(50);
			timer.start();

			// Set text of "run" button to "clear"
			controller.getB("run").setText("clear");

			// Set mode to "No Path"
			mode = "No Path";

			// Set up flicker animation
			Color flicker = new Color(r, G, b);
			g.setColor(flicker);
			g.fillRect(0, 0, getWidth(), getHeight());

			// Place "No Path" text on screen in center
			controller.noPathTBounds();
			controller.getL("noPathT").setVisible(true);
			this.add(controller.getL("noPathT"));
			this.revalidate();
		}

		// If pathfinding is complete (found path)
		if (pathfinding.isComplete()) {
			// Set run button to clear
			controller.getB("run").setText("clear");

			// Set timer delay, start for background animation
			timer.setDelay(50);
			timer.start();

			// Make the background flicker
			Color flicker = new Color(r, G, b);
			g.setColor(flicker);
			g.fillRect(0, 0, getWidth(), getHeight());

			// Set completed mode
			if (showSteps) {
				mode = "Completed";
			} else {
				mode = "Completed in " + pathfinding.getRunTime() + "ms";
			}
		}

		// Draws grid
		g.setColor(Color.lightGray);
		for (int j = 0; j < this.getHeight(); j += size) {
			for (int i = 0; i < this.getWidth(); i += size) {
				g.drawRect(i, j, size, size);
			}
		}

		drawNodes(pathfinding.borders,g,Color.DARK_GRAY,false);
		drawNodes(pathfinding.open, g, style.greenHighlight, true);
		drawNodes(pathfinding.closed, g, style.redHighlight, true);
		drawNodes(pathfinding.path, g, style.blueHighlight, true);
		drawNode(startNode, g, Color.BLUE);
		drawNode(endNode, g, Color.red);
		
		// If control panel is being hovered, change colours
		if (btnHover) {
			g.setColor(style.darkText);
			controller.hoverColour();
		} else {
			g.setColor(style.btnPanel);
			controller.nonHoverColour();
		}
		// Drawing control panel rectangle
		g.fillRect(10, height - 96, 322, 90);

		// Setting mode text
		controller.getL("modeText").setText("Mode: " + mode);

		// Position all controls
		controller.fixPosition();

		// Setting numbers in pathfinding lists
		controller.getL("openC").setText(Integer.toString(pathfinding.getOpenList().size()));
		controller.getL("closedC").setText(Integer.toString(pathfinding.getClosedList().size()));
		controller.getL("pathC").setText(Integer.toString(pathfinding.getPathList().size()));
		
		// Setting speed number text in showSteps or !showSteps mode
		if (showSteps) {
			controller.getL("speedC").setText(Integer.toString(controller.getS("speed").getValue()));
		} else {
			controller.getL("speedC").setText("N/A");
		}
		
		// Getting values from checkboxes
		showSteps = controller.getC("showStepsCheck").isSelected();
		pathfinding.setDiagonal(controller.getC("diagonalCheck").isSelected());
		pathfinding.setTrig(controller.getC("trigCheck").isSelected());
	}
	
	// Draws info (f, g, h) on current node
	public void drawInfo(Node current, Graphics g) {
		if (size > 50) {
			g.setFont(style.numbers);
			g.setColor(Color.black);
			g.drawString(Integer.toString(current.getF()), current.getX() + 4, current.getY() + 16);
			g.setFont(style.smallNumbers);
			g.drawString(Integer.toString(current.getG()), current.getX() + 4, current.getY() + size - 7);
			g.drawString(Integer.toString(current.getH()), current.getX() + size - 26, current.getY() + size - 7);
		}
	}

	public void ClickMap(MouseEvent e) {
		// If left mouse button is clicked
		if (SwingUtilities.isLeftMouseButton(e)) 
		{
			int X = e.getX() - (e.getX()%size);
			int Y = e.getY() - (e.getY()%size);
			switch (currentKey) {
				case 's':
					if(startNode== null)
						startNode = new Node();
					startNode.setXY(X, Y);
					break;
				case 'e':
					if(endNode == null)
						endNode = new Node();
					endNode.setXY(X, Y);
					break;
				default:
					Node border = new Node(X,Y);
					pathfinding.addBorder(border);
					break;
			}
			repaint();
		}
		// If right mouse button is clicked
		else if (SwingUtilities.isRightMouseButton(e))
		{
			int X = e.getX() - (e.getX() % size);
			int Y = e.getY() - (e.getY() % size);
			switch (currentKey) {
				case 's':
					if(startNode!=null && startNode.getX()==X && startNode.getY()==Y)
						startNode = null;
					break;
				case 'e':
					if(endNode != null && endNode.getX()==X && endNode.getY()==Y)
						endNode = null;
					break;
				default:
					int index = pathfinding.searchBorder(X, Y);
					if(index>-1)
						pathfinding.removeBorder(index);
					break;
			}
			repaint();
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {ClickMap(e);}
	@Override
	public void mousePressed(MouseEvent e) {}
	@Override
	public void mouseReleased(MouseEvent e){}
	@Override
	public void mouseEntered(MouseEvent e) {}
	@Override
	public void mouseExited(MouseEvent e) {}
	@Override
	public void mouseDragged(MouseEvent e) {ClickMap(e);}
	// Track mouse on movement
	@Override
	public void mouseMoved(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		int height = this.getHeight();

		// Detects if mouse is within button panel
		if (x >= 10 && x <= 332 && y >= (height - 96) && y <= (height - 6)) {
			btnHover = true;
		} else {
			btnHover = false;
		}
		repaint();
	}
	@Override
	public void keyTyped(KeyEvent e) {}
	@Override
	public void keyPressed(KeyEvent e) {
		currentKey=e.getKeyChar();
		// Start if space is pressed
		if (currentKey == KeyEvent.VK_SPACE) {
			controller.getB("run").setText("stop");
			start();
		}
	}
	@Override
	public void keyReleased(KeyEvent e) {currentKey = (char) 0;}


	@Override
	// converts the number of times mouse scroll wheel is moved to a ratio for the nodes to scale to
	public void mouseWheelMoved(MouseWheelEvent m) {
		int rotation = m.getWheelRotation();
		double prevSize = size;
		int scroll = 3;

		// Changes size of grid based on scroll
		if (rotation == -1 && size + scroll < 200) {
			size += scroll;
		} else if (rotation == 1 && size - scroll > 2) {
			size += -scroll;
		}
		pathfinding.setSize(size);
		double ratio = size / prevSize;

		// new X and Y values for Start
		if (startNode != null) {
			int X = (int) Math.round(startNode.getX() * ratio);
			int Y = (int) Math.round(startNode.getY() * ratio);
			startNode.setXY(X, Y);
		}

		// new X and Y values for End
		if (endNode != null) {
			int X = (int) Math.round(endNode.getX() * ratio);
			int Y = (int) Math.round(endNode.getY() * ratio);
			endNode.setXY(X,Y);
		}

		// new X and Y values for borders
		for (int i = 0; i < pathfinding.getBorderList().size(); i++) {
			int X = (int) Math.round((pathfinding.getBorderList().get(i).getX() * ratio));
			int Y = (int) Math.round((pathfinding.getBorderList().get(i).getY() * ratio));
			pathfinding.getBorderList().get(i).setXY(X,Y);
		}

		// New X and Y for Open nodes
		for (int i = 0; i < pathfinding.getOpenList().size(); i++) {
			int X = (int) Math.round((pathfinding.getOpenList().get(i).getX() * ratio));
			int Y = (int) Math.round((pathfinding.getOpenList().get(i).getY() * ratio));
			pathfinding.getOpenList().get(i).setXY(X,Y);
		}

		// New X and Y for Closed Nodes
		for (int i = 0; i < pathfinding.getClosedList().size(); i++) {
			if (!Node.isEqual(pathfinding.getClosedList().get(i), startNode)) {
				int X = (int) Math.round((pathfinding.getClosedList().get(i).getX() * ratio));
				int Y = (int) Math.round((pathfinding.getClosedList().get(i).getY() * ratio));
				pathfinding.getClosedList().get(i).setXY(X, Y);
			}
		}
		repaint();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// Moves one step ahead in path finding (called on timer)
		if (pathfinding.isRunning() && showSteps) {
			pathfinding.findPath(pathfinding.getPar());
			mode = "Running";
		}
		// background animation
		if (pathfinding.isComplete() || pathfinding.isNoPath()) {
			r = (int) (Math.random() * ((r + 15) - (r - 15)) + (r - 15));
			G = (int) (Math.random() * ((G + 15) - (G - 15)) + (G - 15));
			b = (int) (Math.random() * ((b + 15) - (b - 15)) + (b - 15));

			if (r >= 240 | r <= 15) {
				r = randomInRange(0, 255);
			}
			if (G >= 240 | G <= 15) {
				G = randomInRange(0, 255);
			}
			if (b >= 240 | b <= 15) {
				b = randomInRange(0, 255);
			}
		}

		// Actions of run/stop/clear button
		if (e.getActionCommand() != null) {
			if (e.getActionCommand().equals("run") && !pathfinding.isRunning()) {
				controller.getB("run").setText("stop");
				start();
			} else if (e.getActionCommand().equals("clear")) {
				controller.getB("run").setText("run");
				mode = "Map Creation";
				controller.getL("noPathT").setVisible(false);
				pathfinding.reset();
			} else if (e.getActionCommand().equals("stop")) {
				controller.getB("run").setText("start");
				timer.stop();
			} else if (e.getActionCommand().equals("start")) {
				controller.getB("run").setText("stop");
				timer.start();
			}
		}
		repaint();
	}

	// Returns random number between min and max
	int randomInRange(int min, int max) {
		Random rand = new Random();
		return rand.nextInt((max-min) + min);
	}

	// Calculates delay with two exponential functions
	// jugar for controlling the speed at which the algo runs and screen is updated
	void setSpeed() {
		int delay = 0;
		int value = controller.getS("speed").getValue();

		if (value == 0) {
			timer.stop();
		} else if (value >= 1 && value < 50) {
			if (!timer.isRunning()) {
				timer.start();
			}
			// Exponential function. value(1) == delay(5000). value (50) == delay(25)
			delay = (int) (A1 * (Math.pow(25 / 5000.0000, value / 49.0000)));
		} else if (value >= 50 && value <= 100) {
			if (!timer.isRunning()) {
				timer.start();
			}
			// Exponential function. value (50) == delay(25). value(100) == delay(1).
			delay = (int) (A2 * (Math.pow(1 / 25.0000, value / 50.0000)));
		}
		timer.setDelay(delay);
	}

	void drawNode(Node node, Graphics g, Color color)
	{
		if(node!=null){
			g.setColor(color);
			g.fillRect(node.getX()+1, node.getY()+1,size-1,size-1);
		}
	}

	void drawNodes( List<Node> list, Graphics g, Color color, boolean info)
	{
		for(Node i : list)
		{
			g.setColor(color);
			g.fillRect(i.getX()+1, i.getY()+1,size-1,size-1);
			if(info)
				drawInfo(i, g);
		}
	}

	boolean showSteps() {
		return showSteps;
	}
}
