package com.murphysean.bzrflag.commanders;

import com.murphysean.bzrflag.agents.GoToAgent;
import com.murphysean.bzrflag.events.BZRFlagEvent;
import com.murphysean.bzrflag.agents.GoToAgent.GoToCompleteEvent;
import com.murphysean.bzrflag.agents.GoToAgent.KilledEvent;
import com.murphysean.bzrflag.models.Game;
import com.murphysean.bzrflag.models.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by erikdonohoo on 12/11/13.
 */
public class PidgeonCommander extends AbstractCommander {

    private Point smartPidgeonPoint;
    private GoToAgent dumbPidgeon, flyingPidgeon, smartPidgeon;
    private List<Point> flyingPoints;
    private Point dumbPidgeonSpot = new Point(-200,200);
    private static final long DUMB_PIDGEON_WAIT = 30000; // 30 sec
    private static final long SMART_PIDGEON_THRESHOLD = 20;

    public PidgeonCommander() {
        super();
    }

    @Override
    public void setGame(Game game){
        super.setGame(game);

        // Create three goToAgents, one for each thing
        // Ensure team has at least 3
        if (playerCount < 3)
            throw new RuntimeException("Must have 3 tanks for a Pidgeon Commander");
        dumbPidgeon = new GoToAgent();
        dumbPidgeon.setGame(game);
        dumbPidgeon.setId(0);
        dumbPidgeon.setCallsign(game.getTeam().getColor() + 0);
        dumbPidgeon.setTeamColor(game.getTeam().getColor());

        flyingPidgeon = new GoToAgent();
        flyingPidgeon.setGame(game);
        flyingPidgeon.setId(1);
        flyingPidgeon.setCallsign(game.getTeam().getColor() + 1);
        flyingPidgeon.setTeamColor(game.getTeam().getColor());

        smartPidgeon = new GoToAgent();
        smartPidgeon.setGame(game);
        smartPidgeon.setId(2);
        smartPidgeon.setCallsign(game.getTeam().getColor() + 2);
        smartPidgeon.setTeamColor(game.getTeam().getColor());

        // Points for flying pidgeon
        flyingPoints = new ArrayList<Point>();
        flyingPoints.add(new Point(-20, -20));
        flyingPoints.add(new Point(-380,-380));
        flyingPoints.add(new Point(-20, -380));
        flyingPoints.add(new Point(-380, -20));

        // Start dumb pidgeon
        dumbPidgeon.setTarget(dumbPidgeonSpot);

    }

    private Point choosePoint() {
        int index = (int)Math.floor(Math.random() * 3.9999);
        return flyingPoints.get(index);
    }

    private double distanceBetweenPoints(Point x, Point y) {
        return Math.sqrt(Math.pow(y.getX() - x.getX(), 2) + Math.pow(y.getY() - x.getY(), 2));
    }

    @Override
    public void bzrFlagEventHandler(BZRFlagEvent event) {
        if (event instanceof GoToCompleteEvent) {
            GoToAgent agent = ((GoToCompleteEvent)event).getGoToAgent();

            // Determine which pidgeon we are on
            if (agent.getId() == 1) { // Straight line pidgeon
                flyingPidgeon.setTarget(choosePoint());

            } else if (agent.getId() == 2) { // Smart Pidgeon
                smartPidgeonPoint = choosePoint();
            }
        }
    }
}
