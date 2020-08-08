package cecs277.passengers;

import cecs277.Simulation;
import cecs277.elevators.Elevator;
import cecs277.events.PassengerNextDestinationEvent;

/**
 * A VisitorPassenger has a single destination and a single duration (in seconds), which is how long the Visitor
 * will "disappear" for after departing the elevator on their destination floor. After that duration, the Visitor
 * will reappear on the original destination floor, set its new destination to floor 1, then leave the building when it
 * arrives on floor 1.
 */
public class VisitorPassenger extends Passenger {
	// TODO: add fields, constructors, and accessors to implement this class.
	private int destinationFloor;
	private int durationTime;
	
//	private Elevator elevator;
	
	public VisitorPassenger(int destinationFloor, int durationTime) {
		super();
		// TODO: change this constructor's  body.
		this.destinationFloor = destinationFloor;
		this.durationTime = durationTime;
	}
	
	@Override
	public int getDestination() {
		// TODO: finish this method to return the visitor's destination, which changes to floor 1 when they
		// "reappear".
		return this.destinationFloor; 
	}
	
	// TODO: implement this template method variant. A Visitor will join an elevator whose passenger count is less than its capacity.
	@Override
	protected boolean willBoardElevator(Elevator elevator) {
		// Visitor will join elevator if passenger count is < capacity
		if (elevator.getPassengerCount() < elevator.getCapacity()) {
			return true;
		}
		return false;
	}
	
	/*
	 TODO: implement this template method variant, which is called when the passenger is leaving the elevator it
	 is on. A Visitor that is departing on floor 1 just leaves the building, printing a message to System.out.
	 A visitor that is departing on any other floor sets their new destination to floor 1, and then schedules a
	 PassengerNextDestinationEvent to occur when they are supposed to "reappear" (their duration field).
	*/
	@Override
	protected void leavingElevator(Elevator elevator) {
		/* Example of how to schedule a PassengerNextDestinationEvent:
		Simulation s = elevator.getBuilding().getSimulation();
		PassengerNextDestinationEvent ev = new PassengerNextDestinationEvent(s.currentTime() + 10, this,
		 elevator.getCurrentFloor());
		s.scheduleEvent(ev);
		
		Schedules this passenger to reappear on this floor 10 seconds from now.
		 */
		
		// Visitor is departing on floor 1
		if (elevator.getCurrentFloor().getNumber() == 1) {
			System.out.println("Visitor " + getId() + " is leaving the building. ");
			// Remove the passenger that has left 
//			elevator.removePassenger(this);
			// Remove the passenger as an observer 
//			elevator.removeObserver(this);
		} else { // Visitor departing on any other floor 
			// Sets their new destination to floor 1
//			System.out.println("#### The new destination is 1 ####");
			this.destinationFloor = 1;
			// Schedules a PassengerNextDestinationEvent 
			Simulation s = elevator.getBuilding().getSimulation();
			PassengerNextDestinationEvent ev = new PassengerNextDestinationEvent(s.currentTime() + this.durationTime, this, elevator.getCurrentFloor());
			s.scheduleEvent(ev);
		}
		
	}
	
	// TODO: return "Visitor heading to floor {destination}", replacing {destination} with the floor number.
	@Override
	public String toString() {
		return "Visitor " + getId() + " heading to floor " + destinationFloor;
	}
	
	@Override
	public void elevatorDecelerating(Elevator elevator) {
		// Don't care.
	}
}
