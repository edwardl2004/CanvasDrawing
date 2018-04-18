package org.edward.canvas.draw;

//This enum represents the "region" of a point is falling in, in relation to the drawing area of canvas. It is used in error handling
//and drawing parameter normalization.

//            |                  |
//UPPERLEFT   |       TOP        |   UPPERRIGHT
//            |                  |
//-------------------------------------------            
//            |  (Drawing Area)  |
//  LEFT      |      WITHIN      |    RIGHT
//            |                  |
//-------------------------------------------            
//            |                  |
//LOWERLEFT   |      BOTTOM      |   LOWERRIGHT
//            |                  |
            
public enum PointPosition {
	UPPERLEFT,
	TOP,
	UPPERRIGHT,
	LEFT,
	WITHIN,
	RIGHT,
	LOWERLEFT,
	BOTTOM,
	LOWERRIGHT;
}
