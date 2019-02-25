package com.ru.usty.elevator;

public class Person implements Runnable{
	private ElevatorScene scene;
	private int sourceFloor;
	private int destinationFloor;
	
	public Person(ElevatorScene scene, int sourceFloor, int destinationFloor) {
		this.scene = scene;
		this.sourceFloor = sourceFloor;
		this.destinationFloor = destinationFloor;
	}
	
	@Override
	public void run() {
		
		enterFloor();
		enterElevator();
		exitElevator();
	}

	private void enterFloor() {
		System.out.println("A person entered floor " + sourceFloor + " and wants to exit at floor " + destinationFloor);
		scene.personEntersAtFloor(sourceFloor);
	}

	private void enterElevator() {
		try {
			
			// Wait in queue
			ElevatorScene.elevatorQueue.get(sourceFloor).acquire();
			
			// Get info on allocated elevator
			int elevatorId = scene.getElevatorId(sourceFloor);

			// Wait to fit inside the elevator
			ElevatorScene.elevatorCapacity.get(elevatorId).acquire();
			
			// Wait for available elevator
			ElevatorScene.elevatorAtFloorMutex.get(sourceFloor).acquire();
			
			// Exit queue
			ElevatorScene.elevatorQueue.get(sourceFloor).release();
			
			// Exit floor and enter elevator
			scene.personExitsFromFloor(sourceFloor);
			
			// Stop waiting for the elevator
			ElevatorScene.elevatorAtFloorMutex.get(sourceFloor).release();
			
			// Wait for elevator to reach destination
			ElevatorScene.elevatorAtFloorMutex.get(destinationFloor).acquire();
			
			// Exit elevator
			ElevatorScene.elevatorCapacity.get(elevatorId).release();
			
			// Stop waiting for elevator to reach destination
			ElevatorScene.elevatorAtFloorMutex.get(destinationFloor).release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}

	private void exitElevator() {
		System.out.println("A person exited at floor " + destinationFloor);
		// TODO Auto-generated method stub
		scene.personExitsAtFloor(destinationFloor);
		
	};
}
