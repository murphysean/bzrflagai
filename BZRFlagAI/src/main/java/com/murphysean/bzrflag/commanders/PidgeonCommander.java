package com.murphysean.bzrflag.commanders;

import com.murphysean.bzrflag.agents.GoToAgent;
import com.murphysean.bzrflag.events.BZRFlagEvent;
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
    private List<Point> flyingPoints, smartPoints;
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
        tanks.add(dumbPidgeon);

        flyingPidgeon = new GoToAgent();
        flyingPidgeon.setGame(game);
        flyingPidgeon.setId(1);
        flyingPidgeon.setCallsign(game.getTeam().getColor() + 1);
        flyingPidgeon.setTeamColor(game.getTeam().getColor());
        tanks.add(flyingPidgeon);

        smartPidgeon = new GoToAgent();
        smartPidgeon.setGame(game);
        smartPidgeon.setId(2);
        smartPidgeon.setCallsign(game.getTeam().getColor() + 2);
        smartPidgeon.setTeamColor(game.getTeam().getColor());
        tanks.add(smartPidgeon);

        // Points for flying pidgeon
        flyingPoints = new ArrayList<Point>();
        flyingPoints.add(new Point(-20, -20));
        flyingPoints.add(new Point(-380,-380));
        flyingPoints.add(new Point(-20, -380));
        flyingPoints.add(new Point(-380, -20));

        smartPoints = new ArrayList<Point>();
        smartPoints.add(new Point(20, -20));
        smartPoints.add(new Point(380, -380));
        smartPoints.add(new Point(20, -380));
        smartPoints.add(new Point(380, -20));

        // Gentlemen, start your pidgeons
        dumbPidgeon.setDestination(dumbPidgeonSpot);
        flyingPidgeon.setDestination(choosePoint(flyingPoints));
        smartPidgeonPoint = choosePoint(smartPoints);

        // Figure out where to tell him to go
        smartPidgeon.setDestination(figureOutSmartPoint());

    }

    private Point choosePoint(List<Point> points) {
        int index = (int)Math.floor(Math.random() * 3.9999);
        return points.get(index);
    }

    private double distanceBetweenPoints(Point x, Point y) {
        return Math.sqrt(Math.pow(y.getX() - x.getX(), 2) + Math.pow(y.getY() - x.getY(), 2));
    }

    private Point figureOutSmartPoint() {
        // Based on smartPidgeonPoint, select a point that is slightly closer to that point but not too far from us currently
        float ang = (float)Math.atan2(smartPidgeonPoint.getY() - smartPidgeon.getPosition().getY(),smartPidgeonPoint.getX() - smartPidgeon.getPosition().getX());
        ang += (Math.random() * 0.34906585) - 0.174532925;
        int x = (int)(smartPidgeon.getPosition().getX() + SMART_PIDGEON_THRESHOLD * Math.cos(ang));
        int y = (int)(smartPidgeon.getPosition().getY() + SMART_PIDGEON_THRESHOLD * Math.sin(ang));
        return new Point(x,y);
    }

    @Override
    public void bzrFlagEventHandler(BZRFlagEvent event) {
        if (event instanceof GoToAgent.GoToCompleteEvent) {
            GoToAgent agent = ((GoToAgent.GoToCompleteEvent)event).getGoToAgent();

            // Determine which pidgeon we are on
            if (agent.getId() == 1) { // Straight line pidgeon
                flyingPidgeon.setDestination(choosePoint(flyingPoints));

            } else if (agent.getId() == 2) { // Smart Pidgeon
                if (((GoToAgent.GoToCompleteEvent)event).getDestination().equals(smartPidgeonPoint)) {
                    // Figure out where to tell him to go
                    smartPidgeonPoint = choosePoint(smartPoints);
                    smartPidgeon.setDestination(figureOutSmartPoint());

                } else {
                    if (distanceBetweenPoints(smartPidgeonPoint,((GoToAgent.GoToCompleteEvent)event).getDestination()) < SMART_PIDGEON_THRESHOLD) {
                        smartPidgeon.setDestination(smartPidgeonPoint);
                    }
                }
            }
        }
    }
}
