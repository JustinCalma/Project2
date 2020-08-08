package cecs277.passengers;

import cecs277.buildings.Floor;

import cecs277.buildings.FloorObserver;
import cecs277.elevators.Elevator;
import cecs277.elevators.Elevator.Direction;
import cecs277.elevators.ElevatorObserver;

/**
 * A passenger that is either waiting on a floor or riding an elevator.
 */
public abstract class Passenger implements FloorObserver, ElevatorObserver {
	// An enum for determining whether a Passenger is on a floor, an elevator, or busy (visiting a room in the building).
	public enum PassengerState {
		WAITING_ON_FLOOR,
		ON_ELEVATOR,
		BUSY
	}
	
	// A cute trick for assigning unique IDs to each object that is created. (See the constructor.)
	private static int mNextId;
	protected static int nextPassengerId() {
		return ++mNextId;
	}
	
	private int mIdentifier;
	private PassengerState mCurrentState;
	
	public Passenger() {
		mIdentifier = nextPassengerId();
		mCurrentState = PassengerState.WAITING_ON_FLOOR;
	}
	
	public void setState(PassengerState state) {
		mCurrentState = state;
	}
	
	/**
	 * Gets the passenger's unique identifier.
	 */
	public int getId() {
		return mIdentifier;
	}
	
	
	/**
	 * Handles an elevator arriving at the passenger's current floor.
	 */
	@Override
	public void elevatorArriving(Floor floor, Elevator elevator) {
		// This is a sanity check. A Passenger should never be observing a Floor they are not waiting on.
//		System.out.println("The elevator is arriving in Passenger"); WORKING FINE
		if (floor.getWaitingPassengers().contains(this) && mCurrentState == PassengerState.WAITING_ON_FLOOR) {
			Elevator.Direction elevatorDirection = elevator.getCurrentDirection();
			
			// TODO: check if the elevator is either NOT_MOVING, or is going in the direction that this passenger wants.
			// If so, this passenger becomes an observer of the elevator.
			
			//If current direction is NOT_MOVING || Passenger wants to go up || Passenger wants to go down
			if (elevatorDirection == Direction.NOT_MOVING || elevator.getCurrentDirection() == Elevator.Direction.MOVING_UP || elevator.getCurrentDirection() == Elevator.Direction.MOVING_DOWN) { // CHANGED floor.directionIsPressed(elevator.getCurrentDirection())
//				System.out.println("Entered if statement "); ENTERED IF STATEMENT
				elevator.addObserver(this);
			}
			
		}
		// This else should not happen if your code is correct. Do not remove this branch; it reveals errors in your code.
		else {
			throw new RuntimeException("Passenger " + toString() + " is observing Floor " + floor.getNumber() + " but they are " +
			 "not waiting on that floor.");
		}
	}
	
	/**
	 * Handles an observed elevator opening its doors. Depart the elevator if we are on it; otherwise, enter the elevator.
	 */
	@Override
	public void elevatorDoorsOpened(Elevator elevator) {
		// The elevator is arriving at our destination. Remove ourselves from the elevator, and stop observing it.
		// Does NOT handle any "next" destination...
//		System.out.println("The elevator doors have opened ");

		if (mCurrentState == PassengerState.ON_ELEVATOR && elevator.getCurrentFloor().getNumber() == getDestination()) {

			// TODO: remove this passenger from the elevator, and as an observer of the elevator. Call the
			// leavingElevator method to allow a derived class to do something when the passenger departs.
			// Set the current state to BUSY.
		
			// Remove this Passenger from the elevator and as an observer of the elevator
			elevator.removePassenger(this);
			elevator.removeObserver(this);
			
			// Call the leavingElevator method
			leavingElevator(elevator);
			
			// Set the current state to BUSY
			setState(PassengerState.BUSY);
			
		}
		// The elevator has arrived on the floor we are waiting on. If the elevator has room for us, remove ourselves
		// from the floor, and enter the elevator.
		else if (mCurrentState == PassengerState.WAITING_ON_FLOOR) {

			// TODO: determine if the passenger will board the elevator using willBoardElevator.
			// If so, remove the passenger from the current floor, and as an observer of the current floor;
			// then add the passenger as an observer of and passenger on the elevator. Then set the mCurrentState
			// to ON_ELEVATOR.

			if (willBoardElevator(elevator)) {
				// Remove the waiting passenger from the current floor
				elevator.getCurrentFloor().removeWaitingPassenger(this);
				// Remove the passenger as an observer of the current floor
				elevator.getCurrentFloor().removeObserver(this);
	//		} else {
				// Add passenger on the elevator
//				System.out.println("#### I have boarded the vator ");
				elevator.addPassenger(this);
				// Add the passenger as an observer of the elevator
				elevator.addObserver(this);
				// Set the current state to ON_ELEVATOR
				setState(PassengerState.ON_ELEVATOR); 
			}
	
		}
	}
	
	/**
	 * Returns the passenger's current destination (what floor they are travelling to).
	 */
	public abstract int getDestination();
	
	/**
	 * Called to determine whether the passenger will board the given elevator that is moving in the direction the
	 * passenger wants to travel.
	 */
	protected abstract boolean willBoardElevator(Elevator elevator);
	
	/**
	 * Called when the passenger is departing the given elevator.
	 */
	protected abstract void leavingElevator(Elevator elevator);
	
	// This will be overridden by derived types.
	@Override
	public String toString() {
		return Integer.toString(getDestination());
	}
	
	@Override
	public void directionRequested(Floor sender, Elevator.Direction direction) {
		// Don't care.
	}
	
	@Override
	public void elevatorWentIdle(Elevator elevator) {
		// Don't care about this.
	}
	
	// The next two methods allow Passengers to be used in data structures, using their id for equality. Don't change 'em.
	@Override
	public int hashCode() {
		return Integer.hashCode(mIdentifier);
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Passenger passenger = (Passenger)o;
		return mIdentifier == passenger.mIdentifier;
	}
	
}