package org.edward.canvas.parser;

import org.edward.canvas.command.Command;

public class CommandParser {
	
	public Command parseCommand(String strCmd) {
		for ( Command cmd : Command.values() ) {
			if (cmd.parseCommand(strCmd)) {
				return cmd;
			}
		}
		return Command.INVALIDCOMMAND;
	}

}
