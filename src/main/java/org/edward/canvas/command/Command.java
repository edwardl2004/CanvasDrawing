package org.edward.canvas.command;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.edward.canvas.entity.Point;
import org.edward.canvas.entity.Size;

//This enum declare a number of drawing commands, each with certain pattern.
public enum Command {
	NEWCANVAS("^\\s*C\\s+(\\d+)\\s+(\\d+)\\s*$"),
	LINE("^\\s*L\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s*$"),
	RECTANGLE("^\\s*R\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s*$"),
	BLOCKFILL("^\\s*B\\s+(\\d+)\\s+(\\d+)\\s+(.)\\s*$"),
	QUIT("^\\s*Q\\s*$"),
	INVALIDCOMMAND("");
	
	private static final char DEFAULT_LINE_COLOUR = 'x';
	
	//Command Pattern
	private String cmdPattern;
	//Command parameters: Points, canvas size, and colours
	//Currently these commands support drawing of vertical and horizontal lines only; however these parameters can be used for more kinds of lines.
	private Point[] points;
	private Size size;
	private char[] colors;
	
	Command(String pattern) {
		this.cmdPattern = pattern;
	}
	
	//parse the input string, and set parameters if the input is a valid command
	//parameters: input string to be parsed
	//return value:
	//True: input string is a valid command;
	//False: input string is not a valid command
	public boolean parseCommand(String strCmd) {
		
		if (this == Command.INVALIDCOMMAND) 
			return false;
		
		Pattern r = Pattern.compile(cmdPattern);
		Matcher m = r.matcher(strCmd);
		
		this.size = null;
		this.points = null;
		this.colors = null;
		
		if (m.find()) {
			//int groupCnt = m.groupCount();
			int width, height;
			int x1, y1, x2, y2;
			
			switch (this) {
			case NEWCANVAS:
				width = Integer.parseInt(m.group(1));
				height = Integer.parseInt(m.group(2));
				this.size = new Size(width, height);
				break;
			case LINE:
			case RECTANGLE:
				x1 = Integer.parseInt(m.group(1));
				y1 = Integer.parseInt(m.group(2));
				x2 = Integer.parseInt(m.group(3));
				y2 = Integer.parseInt(m.group(4));
				points = new Point[] {new Point(x1, y1), new Point(x2, y2)};
				colors = new char[] {DEFAULT_LINE_COLOUR};
				break;
			case BLOCKFILL:
				x1 = Integer.parseInt(m.group(1));
				y1 = Integer.parseInt(m.group(2));
				points = new Point[] {new Point(x1, y1)};
				colors = new char[] {m.group(3).charAt(0)};
				break;
			case QUIT:
				break;
			}
			return true;
		} else {
			return false;
		}
	}

	public Point[] getPoints() {
		return points;
	}

	public Size getSize() {
		return size;
	}

	public char[] getColors() {
		return colors;
	}
}
