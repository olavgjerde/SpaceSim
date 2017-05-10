package inf101.simulator.objects;

import inf101.simulator.*;
import javafx.scene.canvas.GraphicsContext;

/**
 * An implementation of a meteor, has multiple constructors. One to generate passing meteors in the simulation.
 * Another one to place a "default" meteor on a specific point. And for "internal" use with a exploding effect.
 */
public class SimMeteor extends AbstractMovingObject {
    private static final double defaultSpeed = 1.75;
    private double height, width;
    private int type = 0;

    /**
     * This constructor places the object outside of the viewers sight such that it creates a feeling of appearing form
     * nowhere. It is initially placed on a random point along the x-axis with the upper-bound being a bit wider than
     * the width of the habitat.
     * A "random-generator" is retrieved from SimMain since this class is not yet initialised.
     * The direction of the object is generated by using SimMain's random-generator bound by the dimensions of the habitat.
     * @param hab Habitat to be placed in
     */
    public SimMeteor(Habitat hab) {
        super(new Direction(90), new Position(SimMain.getInstance().getRandom().nextInt((int) (hab.getWidth()*1.1)), -100), defaultSpeed, hab);
        this.height = 120;
        this.width = 120;
        dir = dir.turnTowards(directionTo(new Position((double) randomGen.nextInt((int) hab.getWidth()),
                          (double) randomGen.nextInt((int) hab.getWidth()/2) + 200)), 180);
    }

    /**
     * Easier to use for testing purposes, standard height, width and defaultSpeed.
     * @param dir direction of the object
     * @param pos initial position of the object
     * @param hab the habitat the the object belongs to
     */
    public SimMeteor(Direction dir, Position pos, Habitat hab) {
        super(dir, pos, defaultSpeed, hab);
        this.height = 120;
        this.width = 120;
    }

    /**
     * To use with the illusion of exploding meteors (meteorExplode()-method)
     * Height and width must be larger than zero
     * @param dir direction of the object
     * @param pos initial position of the object
     * @param hab the habitat the the object belongs to
     * @param width of the object
     * @param height of the object
     */
    private SimMeteor(Direction dir, Position pos, Habitat hab, double width, double height) {
        super(dir, pos, defaultSpeed, hab);
        this.height = height;
        this.width = width;
        this.type = 1;
        checkState(this);
    }

    @Override
    public void draw(GraphicsContext context) {
        super.draw(context);
        context.drawImage(MediaHelper.getImage("spaceMeteors_spinning.gif"), 0, 0, getWidth(), getHeight());
    }

    public int getType() {
        return type;
    }
    
    @Override
    public double getHeight() {
        return height;
    }

    @Override
    public double getWidth() {
        return width;
    }

    @Override
    public void decreaseHealth() {
        health = health - 0.5;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SimMeteor simMeteor = (SimMeteor) o;
        if (Double.compare(simMeteor.height, height) != 0) return false;
        if (Double.compare(simMeteor.width, width) != 0) return false;
        return type == simMeteor.type;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        long temp;
        temp = Double.doubleToLongBits(height);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(width);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + type;
        return result;
    }

    /**
     * Checks to see if the dimensions of the meteor is larger than 0 for both width and height
     * @param meteor to be tested
     */
    private static void checkState(SimMeteor meteor) {
        if (meteor.width <= 0)
            throw new IllegalArgumentException("Width must be larger than 0: " + meteor.width);
        if (meteor.height <= 0)
            throw new IllegalArgumentException("Height must be larger than 0: " + meteor.height);
    }

    /**
     * "Splits" the meteor object into two new SimMeteor-objects and sends them in two different directions
     */
    public void meteorExplode() {
        habitat.addObject(new SimMeteor(dir, new Position(getPosition().getX() + getRadius()/2,
                getPosition().getY()), habitat, 50, 50));
        habitat.addObject(new SimMeteor(dir.turnBack(), new Position(getPosition().getX() - getRadius()/2,
                getPosition().getY()), habitat, 50, 50));
        destroy();
    }

    /**
     * Spawns a SimSilverStar and destroys 'this' object
     */
    private void spawnSimSilverStar() {
        habitat.addObject(new SimSilverStar(getPosition(), SimMain.getInstance().getRandom().nextDouble()*2+0.5, 1200));
        destroy();
    }

    /**
     * The step method runs one iteration of the SimMeteor's behaviour.
     * If the object reaches a health level of zero and is of type 0 it will explode generating
     * two new objects using meteorExplode() and then destroy itself. If health hits zero and the meteor
     * is of type 1 (after explosion) then it will place a SimSilverStar on its position and destroy itself.
     * May explode or spawn SimSilverStar if impact is great enough.
     * It will bounce off other SimMeteors, and also off SimPreys and SimHunter (decreasing their health).
     */
    @Override
    public void step() {
        if (getHealth() <= 0 && type == 0) {
            meteorExplode();
        } else if (getHealth() <= 0 && type == 1) {
           spawnSimSilverStar();
        } else {
            ISimObject closestShip = SimObjectHelper.getClosestShip(this, habitat, 100);
            SimMeteor closestMeteor = SimObjectHelper.getClosestMeteor(this, habitat, 175);
            
            if (closestShip != null && distanceToTouch(closestShip) <= 0) {
                dir = new Direction(closestShip.getDirection().toAngle() + getDirection().toAngle());
                closestShip.decreaseHealth(0.4);
            }
            
            // collision detection for meteor impact (changes direction) 
            // may explode (type 0) or spawn IEdibleSim (type 1) if impact is too great
            if (closestMeteor != null && distanceToTouch(closestMeteor) <= 0.5) {
                if (distanceTo(closestMeteor) < getRadius()*2 - 5 && type == 0 && closestMeteor.getType() == 0) meteorExplode();
                if (distanceTo(closestMeteor) < getRadius()*2 - 5 && type == 1 && closestMeteor.getType() == 1) spawnSimSilverStar();
                if (distanceTo(closestMeteor) < getRadius() + closestMeteor.getRadius() - 5 && type == 0 && closestMeteor.getType() == 1) meteorExplode();
                if (distanceTo(closestMeteor) < getRadius() + closestMeteor.getRadius() - 5 && type == 1 && closestMeteor.getType() == 0) spawnSimSilverStar();
                dir = new Direction((getDirection().toAngle() + 180) + (SimMain.getInstance().getRandom().nextInt(120) - 60));
            }
            
            // destroy outside "spawn-range"
            if (!habitat.contains(getPosition(), -150-getRadius())) {
                destroy();
            }
            super.step();
        }
    }
}
