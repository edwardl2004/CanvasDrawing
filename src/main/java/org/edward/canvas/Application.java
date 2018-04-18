package org.edward.canvas;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.edward.canvas.command.Command;
import org.edward.canvas.parser.CommandParser;
import org.edward.canvas.draw.Canvas;
import org.edward.canvas.entity.Size;
import org.edward.canvas.exceptions.InvalidParamException;

public class Application {

	public static void main(String[] args) {
		
		try (BufferedReader reader = new BufferedReader( new InputStreamReader( System.in ) )) {

			String input;
			
			CommandParser parser = new CommandParser();
			Canvas canvas = new Canvas();
			
			while( true ) {
				try {
					System.out.print("enter command: ");
					input = reader.readLine();
					Command cmd = parser.parseCommand(input);
					if (cmd == Command.INVALIDCOMMAND) {
						System.out.println("Invalid command: " + input);
						continue;
					}
					else if (cmd == Command.QUIT) {
						System.out.println("Application quit.");
						return;
					}
					else {
						canvas.RunCommand(cmd);
						canvas.DisplayCanvas();
					}
				}
				catch(InvalidParamException e) {
					System.out.println("Invalid Parameter: " + e.getMessage());
					continue;
				}
				catch(Exception e1) {
					throw e1;
				}
			}	
		}
		catch( Exception e ){
			System.out.println( "An exception occured!" );
			e.printStackTrace();
		}		
	}
}
