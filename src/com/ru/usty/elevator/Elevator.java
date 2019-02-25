package com.ru.usty.elevator;

public class Elevator implements Runnable{
	private ElevatorScene scene;
	private int elevatorID;
	private boolean movingUp;
	private int floor;
	private int totalCapacity = 6;
	private int availableCapacity = totalCapacity;
	
	public Elevator(ElevatorScene scene, int elevatorID) {
		this.scene = scene;
		this.elevatorID = elevatorID;
		
		movingUp = true;
		floor = 0;
		ElevatorScene.elevatorAtFloorMutex.get(floor).release();
	}
	
	@Override
	public void run() {
		System.out.println("Start elevator " + this.elevatorID);
		while(!ElevatorScene.stopElevators) {
			sleep();
			if(availableCapacity < totalCapacity) unload();
			if(availableCapacity > 0) load();
			move();
		}
		System.out.println("Stop elevator " + this.elevatorID);
	};
	
	
	private void move() {
		if(floor == scene.getNumberOfFloors() - 1) {
			movingUp = false;
		}
		else if(floor == 0) {
			movingUp = true;
		}
		
		if(movingUp) {
			try {
				ElevatorScene.elevatorAtFloorMutex.get(floor).acquire();
				floor++;
				scene.setElevatorPosition(this.elevatorID, floor);
				ElevatorScene.elevatorAtFloorMutex.get(floor).release();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			try {
				ElevatorScene.elevatorAtFloorMutex.get(floor).acquire();
				floor--;
				scene.setElevatorPosition(this.elevatorID, floor);
				ElevatorScene.elevatorAtFloorMutex.get(floor).release();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

	private void unload() {
		System.out.println("Elevator " + this.elevatorID + " unloading on floor " + floor);
		//sleep();
		
	}

	private void load() {
		System.out.println("Elevator " + this.elevatorID + "   loading on floor " + floor);
		//sleep();
	}

	private void sleep() {
		try {
			Thread.sleep(ElevatorScene.VISUALIZATION_WAIT_TIME);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public int getFloorNumber() {
		return floor;
	}
	
	public int getPersonCount() {
		return totalCapacity - availableCapacity;
	}
}
