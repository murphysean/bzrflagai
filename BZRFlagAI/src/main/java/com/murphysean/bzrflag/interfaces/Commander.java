package com.murphysean.bzrflag.interfaces;

import com.murphysean.bzrflag.events.BZRFlagEvent;
import com.murphysean.bzrflag.listeners.CommanderListener;
import com.murphysean.bzrflag.models.Game;

/**
 * A Commander is more specifically the current team. It manages it's tanks, which for a commander are all agents, their
 * current assignments, it can answer questions for a tank, it maintains a reference to the game model.
 */

public interface Commander{
	public String getType();

	public Game getGame();

	public void setGame(Game game);

	public boolean isOccGridRequired();

	public void bzrFlagEventHandler(BZRFlagEvent event);

	public CommanderListener getListener();
	public void setListener(CommanderListener commanderListener);
}
