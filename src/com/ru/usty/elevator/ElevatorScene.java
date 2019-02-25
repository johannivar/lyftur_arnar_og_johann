package com.ru.usty.elevator;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

/**
 * The base function definitions of this class must stay the same for the test
 * suite and graphics to use. You can add functions and/or change the
 * functionality of the operations at will.
 *
 */

public class ElevatorScene {

	public static final int VISUALIZATION_WAIT_TIME = 2000; // milliseconds
	public static final int ELEVATOR_CAPACITY = 2;
	public static boolean stopElevators;
	
	public static ArrayList<Semaphore> personCountMutex; 
	public static ArrayList<Semaphore> elevatorCapacity;
	public static ArrayList<Semaphore> elevatorAtFloorMutex; // Per floor!
	public static ArrayList<Semaphore> elevatorQueue;
	public static ArrayList<Semaphore> elevatorPositionMutex;
	

	private int numberOfFloors;
	private int numberOfElevators;

	ArrayList<Integer> personCount;				// Person count for each floor
	ArrayList<Integer> elevatorPosition;		// Elevator floor position for each elevator
	ArrayList<Integer> exitedCount = null;		// How many people have exited at each floor
	public static Semaphore exitedCountMutex;	


	//public int elevatorCurrentFloor;
	//public ArrayList<Semaphore> elevatorCurrentFloorMutex;
	//public ArrayList<Semaphore> elevatorMutexes;
	public ArrayList<Thread> elevatorThreads;
	public ArrayList<Elevator> elevatorList;

	// Base function: definition must not change
	// Necessary to add your code in this one
	public void restartScene(int numberOfFloors, int numberOfElevators) {

		// Initialise system for a new run
		this.numberOfFloors = numberOfFloors;
		this.numberOfElevators = numberOfElevators;
		
		
		// Tell any currently running elevator threads to stop
		restartElevators();
		

		// Reset Semaphores, Mutexes and arrays
		exitedCountMutex = new Semaphore(1);
		personCountMutex = new ArrayList<Semaphore>();		// Mutex for person count at each floor
		elevatorCapacity = new ArrayList<Semaphore>();
		elevatorAtFloorMutex = new ArrayList<Semaphore>();  // Elevator presence for each floor
		elevatorPositionMutex = new ArrayList<Semaphore>(); // Position of elevator for each elevator
		elevatorQueue = new ArrayList<Semaphore>();
		
		personCount = new ArrayList<Integer>();
		exitedCount = new ArrayList<Integer>();
		
		// Reset exitedCount
		//if (exitedCount == null) exitedCount = new ArrayList<Integer>();
		//else exitedCount.clear();
		
		// Reset every floor
		for (int i = 0; i < getNumberOfFloors(); i++) {
			
			// Set person count to 0 on every floor
			personCountMutex.add(new Semaphore(1));
			this.personCount.add(0); 
			
			// Set exitedCount to 0 on every floor
			this.exitedCount.add(0);
			
			// Set available elevator to 0 on each floor
			elevatorAtFloorMutex.add(new Semaphore(0));
			
			// Set queue for elevator to 1 on every floor
			elevatorQueue.add(new Semaphore(1));
		}
		
		
		// Reset elevators
		elevatorThreads = new ArrayList<Thread>();
		elevatorList = new ArrayList<Elevator>();
		elevatorPosition = new ArrayList<Integer>();
		for (int i = 0; i < numberOfElevators; i++) {
			Elevator elevator = new Elevator(this, i);
			elevatorThreads.add(new Thread(elevator));
			elevatorThreads.get(i).start();
			elevatorList.add(elevator);
			elevatorCapacity.add(new Semaphore(ELEVATOR_CAPACITY));
			elevatorPosition.add(0);
			elevatorPositionMutex.add(new Semaphore(1));
		}
	}

	// Base function: definition must not change
	// Necessary to add your code in this one
	public Thread addPerson(int sourceFloor, int destinationFloor) {

		Thread personThread = new Thread(new Person(this, sourceFloor, destinationFloor));
		personThread.start();
		return personThread;
	}

	// Base function: definition must not change, but add your code
	public int getCurrentFloorForElevator(int elevator) {
		int currentFloor = 0;
		try {
			//sceneMutex.acquire();
			currentFloor = elevatorList.get(elevator).getFloorNumber();
			//sceneMutex.release();
		} catch (Exception  e) {
			e.printStackTrace();
		}
		return currentFloor; //elevatorList.get(elevator).getFloorNumber();
	}

	// Base function: definition must not change, but add your code
	public int getNumberOfPeopleInElevator(int elevator) {
		//System.out.println("Permits " + elevatorCapacity.get(elevator).availablePermits());
		//return elevatorList.get(elevator).getPersonCount();
		return ELEVATOR_CAPACITY - elevatorCapacity.get(elevator).availablePermits();
	}

	// Base function: definition must not change, but add your code
	public int getNumberOfPeopleWaitingAtFloor(int floor) {
		int personsOnFloor = 0;
		try {
			personCountMutex.get(floor).acquire();
			personsOnFloor = personCount.get(floor);
			personCountMutex.get(floor).release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return personsOnFloor;
	}

	// Base function: definition must not change, but add your code if needed
	public int getNumberOfFloors() {
		return numberOfFloors;
	}

	// Base function: definition must not change, but add your code if needed
	public void setNumberOfFloors(int numberOfFloors) {
		this.numberOfFloors = numberOfFloors;
	}

	// Base function: definition must not change, but add your code if needed
	public int getNumberOfElevators() {
		return numberOfElevators;
	}

	// Base function: definition must not change, but add your code if needed
	public void setNumberOfElevators(int numberOfElevators) {
		this.numberOfElevators = numberOfElevators;
	}

	// Base function: no need to change unless you choose
	// not to "open the doors" sometimes
	// even though there are people there
	public boolean isElevatorOpen(int elevator) {

		return isButtonPushedAtFloor(getCurrentFloorForElevator(elevator));
	}

	// Base function: no need to change, just for visualization
	// Feel free to use it though, if it helps
	public boolean isButtonPushedAtFloor(int floor) {

		return (getNumberOfPeopleWaitingAtFloor(floor) > 0);
	}

	// Person threads must call this function to
	// let the system know that they have exited.
	// Person calls it after being let off elevator
	// but before it finishes its run.
	public void personExitsAtFloor(int floor) {
		try {

			exitedCountMutex.acquire();
			exitedCount.set(floor, (exitedCount.get(floor) + 1));
			exitedCountMutex.release();

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// Base function: no need to change, just for visualization
	// Feel free to use it though, if it helps
	public int getExitedCountAtFloor(int floor) {
		if (floor < getNumberOfFloors()) {
			return exitedCount.get(floor);
		} else {
			return 0;
		}
	}
	
	// Custom functions
	public int getElevatorId(int sourceFloor) {
		return 0;
	}
	
	
	public void personEntersAtFloor(int floor) {
		try {
			personCountMutex.get(floor).acquire();
			int personsOnFloor = personCount.get(floor);
			personCount.set(floor, personsOnFloor + 1);
			personCountMutex.get(floor).release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void personExitsFromFloor(int floor) {
		try {
			personCountMutex.get(floor).acquire();
			int personsOnFloor = personCount.get(floor);
			personCount.set(floor, personsOnFloor - 1);
			personCountMutex.get(floor).release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void setElevatorPosition(int elevator, int floor) {
		try {
			elevatorPositionMutex.get(elevator).acquire();
			elevatorPosition.set(elevator, floor);
			elevatorPositionMutex.get(elevator).release();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void restartElevators() {
		stopElevators = true;
		if(elevatorThreads != null) {
			for (Thread thread : elevatorThreads) {
				if (thread != null) {
					if (thread.isAlive()) {
						try {
							thread.join();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		stopElevators = false;
	}

}
