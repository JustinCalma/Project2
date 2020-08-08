package cecs277.buildings;

import cecs277.passengers.Passenger;
import cecs277.Simulation;
import cecs277.elevators.Elevator;
import cecs277.elevators.ElevatorObserver;

import java.util.*;

public class Building implements ElevatorObserver, FloorObserver {
	private List<Elevator> mElevators = new ArrayList<>();
	private List<Floor> mFloors = new ArrayList<>();
	private Simulation mSimulation;
	private Queue<Integer> mWaitingFloors = new ArrayDeque<>();
	
	public Building(int floors, int elevatorCount, Simulation sim) {
		mSimulation = sim;
		
		// Construct the floors, and observe each one.
		for (int i = 0; i < floors; i++) {
			Floor f = new Floor(i + 1, this);
			f.addObserver(this);
			mFloors.add(f);
		}
		
		// Construct the elevators, and observe each one.
		for (int i = 0; i < elevatorCount; i++) {
			Elevator elevator = new Elevator(i + 1, this);
			elevator.addObserver(this);
			for (Floor f : mFloors) {
				elevator.addObserver(f);
			}
			mElevators.add(elevator);
		}
	}
	

	// TODO: recreate your toString() here.
	
	public String toString() {
		 String answer = "";
	        // Gathering information
	        int shafts = mElevators.size();
	        int floors = mFloors.size();

	        // Printing out the building
	        for(int i = floors - 1; i >= 0; i--){
//	            ArrayList<Integer> currfloor = mFloors.get(i-1);
	            if(i + 1 < 10) {
	                answer += ((i + 1) + " :");
	            } else {
	                answer += ((i + 1) + ":");
	            }

	            // Printing out the Shafts
	            for(int j = 0; j < shafts; j++) {
	                if (i + 1 == mElevators.get(j).getCurrentFloor().getNumber()) {
	                    answer += ("| X ");
	                } else {
	                    answer += ("|   ");
	                }
	            }
	            answer += ("|");

	            // Printing out the passenger in each floors w/ the destination
	            if(mFloors.get(i).getWaitingPassengers().size() >= 1) {
	                for(int j = 0; j < mFloors.get(i).getWaitingPassengers().size(); j++ ){
	                    answer += (" " + (mFloors.get(i).getWaitingPassengers().get(j).getDestination()) + " ");
	                }
	            }
	            answer += "\n";

	        }
	        for (Elevator mElevator : mElevators) {
	            answer += (mElevator.toString());
	            answer += "\n";
	        }
	        return answer;
	    }
	
	public int getFloorCount() {
		return mFloors.size();
	}
	
	public Floor getFloor(int floor) {
		return mFloors.get(floor - 1);
	}
	
	public Simulation getSimulation() {
		return mSimulation;
	}
	
	
	@Override
	public void elevatorDecelerating(Elevator elevator) {
		// Have to implement all interface methods even if we don't use them.
	}
	
	@Override
	public void elevatorDoorsOpened(Elevator elevator) {
//		System.out.println("The doors have opened in building ");
		// Don't care.
	}
	
	@Override
	public void elevatorWentIdle(Elevator elevator) {
		// TODO: if mWaitingFloors is not empty, remove the first entry from the queue and dispatch the elevator to that floor.
		
		System.out.println("THE ELEVATOR WENT IDLE ");
		// If mWaitingFloors is not empty
		if (!mWaitingFloors.isEmpty()) {
			// Remove the first entry from the queue and ...
			int a = mWaitingFloors.remove();
			// Dispatch the elevator to that floor
			elevator.dispatchTo(getFloor(a));
		}
	}
	
	@Override
	public void elevatorArriving(Floor sender, Elevator elevator) {
		// TODO: add the floor mWaitingFloors if it is not already in the queue.
//		System.out.println("The elevator is arriving in building "); WORKING FINE
		// Checks if the sent floor is in the queue
		if (!mWaitingFloors.contains(sender.getNumber())) {
			// Add the floor into mWaitingFloors
			mWaitingFloors.add(sender.getNumber());
		} //else { // Else add the sent floor in the queue
//			mWaitingFloors.add(sender.getNumber());
//		}
	}
	
	@Override
	public void directionRequested(Floor floor, Elevator.Direction direction) {
		// TODO: go through each elevator. If an elevator is idle, dispatch it to the given floor.
		// TODO: if no elevators are idle, then add the floor number to the mWaitingFloors queue.
		
		// Create key checking if all the elevators are IDLE
		boolean noElevIdle = true;
		// Iterate through each elevator and check if it is idle
		for (int i = 0; i < mElevators.size(); i++) {
			// If an elevator is idle
			if (mElevators.get(i).isIdle()) {
				// Dispatch the elevator at that iteration to the given floor
				mElevators.get(i).dispatchTo(floor);
				// Sets the key to false if one elevator is IDLE
				noElevIdle = false;
			} 
		}
		// Checks if all elevators are not idle
		if (noElevIdle == true) {
			// Adds the floor number to the queue
			mWaitingFloors.add(floor.getNumber());
		}
	}
}
