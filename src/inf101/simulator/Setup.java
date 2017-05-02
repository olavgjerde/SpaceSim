package inf101.simulator;

import inf101.simulator.objects.ISimObjectFactory;
import inf101.simulator.objects.examples.Blob;
import inf101.simulator.objects.SimAnimal;
import inf101.simulator.objects.examples.SimFeed;
import inf101.simulator.objects.examples.SimRepellant;

public class Setup {
	/** This method is called when the simulation starts */
	public static void setup(SimMain main, Habitat habitat) {
	    
	    // anonymous class alternative for a factory
		/*class SimFactory implements ISimObjectFactory {
            @Override
            public ISimObject create(Position pos, Habitat hab) {
                return new SimAnimal(pos,hab);
            }
        }*/
		
		habitat.addObject(new SimAnimal(new Position(400, 400), habitat));
		habitat.addObject(new Blob(new Direction(0), new Position(400, 400), 1));
		
		// lambda alternative to constructing a factory-class for each object 
		ISimObjectFactory simAnimalFactory = (Position pos, Habitat hab) -> new SimAnimal(pos,hab);

		for (int i = 0; i < 3; i++)
			habitat.addObject(new SimRepellant(main.randomPos()));

		SimMain.registerSimObjectFactory((Position pos, Habitat hab) -> new SimFeed(pos,
                main.getRandom().nextDouble()*2+0.5), "SimFeed™", SimFeed.PAINTER);
		SimMain.registerSimObjectFactory((Position pos, Habitat hab) -> new SimRepellant(pos),
                "SimRepellant™", SimRepellant.PAINTER);
		SimMain.registerSimObjectFactory(simAnimalFactory, "SimAnimal", "pipp.png");
	}

	/**
	 * This method is called for each step, you can use it to add objects at
	 * random intervals
	 */
	public static void step(SimMain main, Habitat habitat) {
		if (main.getRandom().nextInt(300) == 0)
			habitat.addObject(new SimFeed(main.randomPos(), main.getRandom().nextDouble()*2+0.5));

	}
}
