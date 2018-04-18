package org.edward.canvas.draw;

import java.util.Arrays;
import java.util.Stack;

import org.edward.canvas.command.Command;
import org.edward.canvas.entity.Point;
import org.edward.canvas.entity.Size;
import org.edward.canvas.exceptions.InvalidParamException;

//This class encapsulate the canvas
public class Canvas {
	
	private static final char WHITECHAR = ' ';
	private static final char X_BORDER = '-';
	private static final char Y_BORDER = '|';
	
	//2-D array representing the canvas. First index is row index and second index is column index
	private char[][] matrix;
	//These fields mark the boundary indices of the drawing canvas. Once the canvas is initialized, these boundary indices are calculated.
	private int leftBoundary = 1;
	private int topBoundary = 1;
	private int rightBoundary;
	private int bottomBoundary;
	
	//Validate the parameters of a drawing command.
	//If the parameters of the command is invalid, this method throws exception carrying the error message.
	//The application class should catch the exception and display the error message.
	public void ValidateCommandParam(Command cmd) throws InvalidParamException {
		Size size = cmd.getSize();
		Point[] points = cmd.getPoints();
		char[] colours = cmd.getColors();
		
		switch (cmd) {
		case NEWCANVAS:
			if (size.getWidth() < 3) {
				//Error: not wide enough
				throw new InvalidParamException("Invalid canvas width");
			}
			if (size.getHeight() < 0) {
				//Error: not high enough
				throw new InvalidParamException("Invalid canvas height");
			}
			break;
		case LINE:
			if (!this.IsCanvasInitialized()) {
				//canvas not created yet, throw exception
				throw new InvalidParamException("Canvas not initialized");
			}
			{
				Point start = points[0];
				Point end = points[1];
				if (!this.IsHorizontalLine(start, end) && !this.IsVerticalLine(start, end)) {
					throw new InvalidParamException("Slope line not supported");
				}
				if (!this.IsLineIntersectWithCanvas(start, end)) {
					throw new InvalidParamException("Line outside of canvas");
				}
			}
			break;
		case RECTANGLE:
			if (!this.IsCanvasInitialized()) {
				//canvas not created yet, throw exception
				throw new InvalidParamException("Canvas not initialized");
			}
			{
				Point start = points[0];
				Point end = points[1];
				if (this.IsHorizontalLine(start, end) || this.IsVerticalLine(start, end)) {
					// start point and end point are on the same vertical or horizontal line, so rectangle is reduced to a line
					if (!this.IsLineIntersectWithCanvas(start, end)) {
						throw new InvalidParamException("Line outside of canvas");
					}
				}
				else {
					//Normal rectangle, check all four border lines
					Point start0 = new Point(start.getX(), end.getY());
					Point end0 = new Point(end.getX(), start.getY());
					if (!this.IsLineIntersectWithCanvas(start, start0) && 
							!this.IsLineIntersectWithCanvas(start0, end) &&
							!this.IsLineIntersectWithCanvas(end, end0) &&
							!this.IsLineIntersectWithCanvas(end0, start)) {
						//non of four border lines of rectangle intersect with the canvas, throw exception
						throw new InvalidParamException("Rectangle outside of canvas");
					}
				}
			}
			break;
		case BLOCKFILL:
			if (!this.IsCanvasInitialized()) {
				//Error: canvas not created yet, throw exception
				throw new InvalidParamException("Canvas not initialized");
			}
			{
				Point start = points[0];
				if (!this.IsPointInCanvas(start)) {
					//Error: start point not in canvas, throw exception
					throw new InvalidParamException("start point is not within canvas");
				}
			}
			break;
		default:
			throw new InvalidParamException("Unsupported command");
		}
	}
	
	public void RunCommand(Command cmd) throws Exception {
		//Check if command parameters are valid
		this.ValidateCommandParam(cmd);	//If parameters are invalid, this method throws exception, carrying out error message
		
		Size size = cmd.getSize();
		Point[] points = cmd.getPoints();
		char[] colours = cmd.getColors();
		
		switch(cmd) {
		case NEWCANVAS:
			CreateNewCanvas(size.getWidth(), size.getHeight());
			break;
		case LINE:
			DrawLine(points[0], points[1], colours[0]);
			break;
		case RECTANGLE:
			DrawRectangle(points[0], points[1], colours[0]);
			break;
		case BLOCKFILL:
			Point start = points[0];
			int xPos = start.getX();
			int yPos = start.getY();
			char currentColor = matrix[yPos][xPos];
			this.BlockFillNonRecursion(xPos, yPos, currentColor, colours[0], MoveDirection.START);
			break;
		default:
			throw new InvalidParamException("Unsupported command");
		}
	}
	
	public void CreateNewCanvas(int width, int height) {
		//Canvas including two horizontal borders besides the drawing area, so the real height would include that
		int newHeight = height + 2;
		matrix = new char[newHeight][width];
		
		this.rightBoundary = width - 2;
		this.bottomBoundary = newHeight - 2;
		
		//Fill the whole canvas with white char
		for (char[] row : matrix) {
			Arrays.fill(row, WHITECHAR);
		}
		
		//Draw the top and bottom border
		Arrays.fill(matrix[0], X_BORDER);
		Arrays.fill(matrix[newHeight-1], Canvas.X_BORDER);

		//Draw the left and right border
		for (int i = 1; i <= this.bottomBoundary; i++) {
			char[] row = this.matrix[i];
			row[0] = Canvas.Y_BORDER;
			row[width-1] = Canvas.Y_BORDER;
		}
	}
	
	//Draw Horizontal lines. Called in CreateNewCanvas() or any potential scenarios to avoid repetitive Point object creations.
	public void DrawHorizontalLine(int startX, int endX, int yPos, char lineChar) {
		char[] row = matrix[yPos];

		startX = this.NormalizeXPos(startX);
		endX = this.NormalizeXPos(endX);
		
		if (endX >= startX) {
			for (int i = startX; i <= endX; i++) {
				row[i] = lineChar;
			}
		} else {
			for (int i = startX; i >= endX; i--) {
				row[i] = lineChar;
			}
		}
	}
	
	//Draw vertical lines.
	public void DrawVerticalLine(int startY, int endY, int xPos, char lineChar) {
		startY = this.NormalizeYPos(startY);
		endY = this.NormalizeYPos(endY);
		if (endY >= startY) {
			for (int i = startY; i <= endY; i++) {
				matrix[i][xPos] = lineChar;
			}
		} else {
			for (int i = startY; i >= endY; i--) {
				matrix[i][xPos] = lineChar;
			}
		}
	}
	
	//Draw a vertical or horizontal line. 
	//If part of the line is outside of the canvas, this function draws only the part of the line inside canvas
	public void DrawLine(Point start, Point end, char lineChar) {
		if (this.IsHorizontalLine(start, end)) {
			//Start point and end point have the same y cooridnate, draw horizontal line
			this.DrawHorizontalLine(start.getX(), end.getX(), start.getY(), lineChar);
		} else if (this.IsVerticalLine(start, end)) {
			//start point and end point have the same x coordinate, draw vertical line
			this.DrawVerticalLine(start.getY(), end.getY(), start.getX(), lineChar);
		} 
	}
	
	//Draw rectangle in canvas.
	//If part of the rectangle is outside of the canvas, this function draws only the part inside canvas
	public void DrawRectangle(Point start, Point end, char lineChar) {
		if (this.IsHorizontalLine(start, end)) {
			this.DrawHorizontalLine(start.getX(), end.getX(), start.getY(), lineChar);
		}
		else if (this.IsVerticalLine(start, end)) {
			this.DrawVerticalLine(start.getY(), end.getY(), start.getX(), lineChar);
		}
		else {
			Point start0 = new Point(start.getX(), end.getY());
			Point end0 = new Point(end.getX(), start.getY());
			
			if (this.IsLineIntersectWithCanvas(start, start0))
				this.DrawVerticalLine(start.getY(), end.getY(), start.getX(), lineChar);
			
			if (this.IsLineIntersectWithCanvas(start0, end))
				this.DrawHorizontalLine(start0.getX(), end.getX(), start0.getY(), lineChar);
			
			if (this.IsLineIntersectWithCanvas(end, end0))
				this.DrawVerticalLine(end.getY(), end0.getY(), end.getX(), lineChar);
			
			if (this.IsLineIntersectWithCanvas(end0, start))
				this.DrawHorizontalLine(end0.getX(), start.getX(), start.getY(), lineChar);
		}
	}
	
	//Fill the block containing the start point with a new colour.
	//This method is recursive version, for the purpose of demonstration of algorithm implementation only. 
	//Large block size could cause stack overflow.
	//The non-recursive alternative BlockFillNonRecursion() should be used.
	@Deprecated
	public void BlockFill(int xPos, int yPos, char oldColour, char colour, MoveDirection move) {
		
		if (matrix[yPos][xPos] != oldColour) {
			//This point has different colour, it does not belong to this block
			return;
		}
		
		matrix[yPos][xPos] = colour;
		
		//recursive call to try all the neighbours
		//Try the pixel on the left, if "current" point does not come from left
		if (xPos > this.leftBoundary && move != MoveDirection.RIGHT)
			BlockFill(xPos-1, yPos, oldColour, colour, MoveDirection.LEFT);
		//Try the pixel above, if "current" point does not come from the pixel above
		if (yPos > this.topBoundary && move != MoveDirection.DOWN)
			BlockFill(xPos, yPos-1, oldColour, colour, MoveDirection.UP);
		//Try the pixel on the right, if "current" point does not come from right
		if (xPos < this.rightBoundary && move != MoveDirection.LEFT)
			BlockFill(xPos+1, yPos, oldColour, colour, MoveDirection.RIGHT);
		//Try the pixel below, if "current" point does not come from the pixel below
		if (yPos < this.bottomBoundary && move != MoveDirection.UP) {
			BlockFill(xPos, yPos+1, oldColour, colour, MoveDirection.DOWN);
		}
	}
	
	//Fill the block containing the start point with a new colour.
	public void BlockFillNonRecursion(int xPos, int yPos, char oldColour, char colour, MoveDirection move) {
		
		Stack<Point> stack = new Stack<Point>();
		stack.push(new Point(xPos, yPos));
		
		while (!stack.isEmpty()) {
			Point point = stack.pop();
			int x = point.getX();
			int y = point.getY();
			
			if (matrix[y][x] != oldColour)	//Not in the same block, skip
				continue;
			
			matrix[y][x] = colour;
			
			//Check all four neighbour pixels, and if any of them has the same old colour, which means they belong to the same block and push them into stack
			if (x > this.leftBoundary && matrix[y][x-1] == oldColour) {
				stack.push(new Point(x-1, y));
			}
			
			if (y > this.topBoundary && matrix[y-1][x] == oldColour) {
				stack.push(new Point(x, y-1));
			}
			
			if (x < this.rightBoundary && matrix[y][x+1] == oldColour) {
				stack.push(new Point(x+1, y));
			}
			
			if ( y < this.bottomBoundary && matrix[y+1][x] == oldColour) {
				stack.push(new Point(x, y+1));
			}
		}
	}
	
	public void DisplayCanvas() {
		for (char[] row : matrix) {
			System.out.println(row);
		}		
	}
	
	private boolean IsCanvasInitialized() {
		if (this.matrix == null || this.matrix.length == 0 || this.matrix[0].length == 0)
			return false;
		else
			return true;
	}
	
	private boolean IsXPosInCanvas(int xPos) {
		return this.leftBoundary <= xPos && xPos <= this.rightBoundary;
	}
	
	private boolean IsYPosInCanvas(int yPos) {
		return this.topBoundary <= yPos && yPos <= this.bottomBoundary;
	}
	
	//Check if the point is located in the drawing area of the canvas. Return true if this point is in, false if this point is outside of drawing area.
	private boolean IsPointInCanvas(Point point) {
		return this.IsXPosInCanvas(point.getX()) && this.IsYPosInCanvas(point.getY());
	}
	
	private boolean IsPointLeftToCanvas(Point point) {
		return point.getX() < this.leftBoundary;
	}
	
	private boolean IsPointRightToCanvas(Point point) {
		return point.getX() > this.rightBoundary;
	}
	
	private boolean IsPointAboveCanvas(Point point) {
		return point.getY() < this.topBoundary;
	}
	
	private boolean IsPointBelowCanvas(Point point) {
		return point.getY() > this.bottomBoundary;
	}
	
	private boolean IsHorizontalLine(Point start, Point end) {
		return start.getY() == end.getY();
	}
	
	private boolean IsVerticalLine(Point start, Point end) {
		return start.getX() == end.getX();
	}
	
	//Check if this line intersects with the drawing area, or, at least part of this line falls within the drawing area of canvas
	private boolean IsLineIntersectWithCanvas(Point start, Point end) {
		
		if (IsHorizontalLine(start, end)) {	//Horizontal line
			return IsYPosInCanvas(start.getY()) && 
					!( (IsPointLeftToCanvas(start) && IsPointLeftToCanvas(end)) || (IsPointRightToCanvas(start) && IsPointRightToCanvas(end)));
		} 
		else if (IsVerticalLine(start, end)) {	//vertical line
			return IsXPosInCanvas(start.getX()) && 
					!((IsPointAboveCanvas(start) && IsPointAboveCanvas(end)) || (IsPointBelowCanvas(start) && IsPointBelowCanvas(end)));
		} 
		else {	//line to be supported yet
			return false;
		}
	}
	
	private int NormalizeXPos(int xPos) {
		int newPos = Math.max(this.leftBoundary, xPos);
		newPos = Math.min(newPos, this.rightBoundary);
		return newPos;
	}
	
	private int NormalizeYPos(int yPos) {
		int newPos = Math.max(this.topBoundary, yPos);
		newPos = Math.min(newPos, this.bottomBoundary);
		return newPos;
	}
	
}
