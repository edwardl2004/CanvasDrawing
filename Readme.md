This is the java application for ASCII draw.

The runnable class is org.edward.canvas.Application.

This Application requires Java 8 to compile and run.

This application support the following commands:
C w h:             create a new canvas;
L x1 y1 x2 y2:     Draw a new horizontal or vertical line
R x1 y1 x2 y2:     Draw a rectangle;
B x y c:           Block fill.
Q:                 Quit the application

Any other commands would not be executed, and the application would 
give "Invalid command" error. 

If the parameters are incorrect, for example, all points are outside
of canvas, the application would give "Invalid parameters" error.

If part of the line or rectangle falls within the canvas, the application
would not give any error, but just draw the part which is in canvas. In 
such scenarios, a rectangle may be reduced to three lines, two lines, or
one line only.

If the two points of the rectangle have the same x or y coordinates, then
that means they are on the same horizontal or vertical line. In such 
scenario, the rectangle is reduced to one line.

