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
    private Point dumbPidgeonSpot;
    private static final long DUMB_PIDGEON_WAIT = 30000; // 30 sec
    private static final long SMART_PIDGEON_THRESHOLD = 40;
    private static final long BUFFER = 20;

    public PidgeonCommander() {
        super();
    }

    @Override
    public void setGame(Game game){
        super.setGame(game);

        // Create three goToAgents, one for each thing
        // Ensure team has at least 3
        float range = game.getShotRange() * 2/3;
        dumbPidgeonSpot = new Point(range*-1, range);
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
        flyingPoints.add(new Point(range*-1,range*-1));
        flyingPoints.add(new Point(BUFFER*-1, range*-1));
        flyingPoints.add(new Point(range*-1, BUFFER*-1));

        smartPoints = new ArrayList<Point>();
        smartPoints.add(new Point(range, range*-1));
        smartPoints.add(new Point(BUFFER, range*-1));
        smartPoints.add(new Point(range, BUFFER*-1));

        // Gentlemen, start your pidgeons
        dumbPidgeon.setDestination(dumbPidgeonSpot);
        flyingPidgeon.setDestination(choosePoint(flyingPoints));
        smartPidgeonPoint = choosePoint(smartPoints);

        // Figure out where to tell him to go
        smartPidgeon.setDestination(smartPidgeonPoint);

    }

    private Point choosePoint(List<Point> points) {
        int index = (int)Math.floor(Math.random() * 2.9999);
        return points.get(index);
    }

    private double distanceBetweenPoints(Point x, Point y) {
        return Math.sqrt(Math.pow(y.getX() - x.getX(), 2) + Math.pow(y.getY() - x.getY(), 2));
    }

    private Point figureOutSmartPoint(Point location) {
        // Based on smartPidgeonPoint, select a point that is slightly closer to that point but not too far from us currently
        // Based on smartPidgeonPoint, select a point that is slightly closer to that point but not too far from us currently
        float ang = (float)Math.atan2(smartPidgeonPoint.getY() - location.getY(),smartPidgeonPoint.getX() - location.getX());
        ang += (Math.random() * 0.767944871) - 0.383972435;
        int x = (int)(location.getX() + SMART_PIDGEON_THRESHOLD * Math.cos(ang));
        int y = (int)(location.getY() + SMART_PIDGEON_THRESHOLD * Math.sin(ang));
        return new Point(x,y);
    }

    @Override
    public void bzrFlagEventHandler(BZRFlagEvent event) {
        if (event instanceof GoToAgent.GoToAlmostThereEvent) {
            GoToAgent agent = ((GoToAgent.GoToAlmostThereEvent)event).getGoToAgent();

            // Determine which pidgeon we are on
            if (agent.getId() == 1) { // Straight line pidgeon
                flyingPidgeon.setDestination(choosePoint(flyingPoints));

            } else if (agent.getId() == 2) { // Smart Pidgeon
                if (((GoToAgent.GoToAlmostThereEvent)event).getDestination().equals(smartPidgeonPoint)) {
                    // Figure out where to tell him to go
                    smartPidgeonPoint = choosePoint(smartPoints);
                    smartPidgeon.setDestination(figureOutSmartPoint(((GoToAgent.GoToAlmostThereEvent)event).getDestination()));

                } else {
                    if (distanceBetweenPoints(smartPidgeonPoint,((GoToAgent.GoToAlmostThereEvent)event).getDestination()) < SMART_PIDGEON_THRESHOLD) {
                        smartPidgeon.setDestination(smartPidgeonPoint);
                    } else {
                        smartPidgeon.setDestination(figureOutSmartPoint(((GoToAgent.GoToAlmostThereEvent)event).getDestination()));
                    }
                }
            }
        }
    }
}
