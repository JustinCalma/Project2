package cecs277.elevators;

import cecs277.Simulation;
import cecs277.buildings.Building;
import cecs277.buildings.Floor;
import cecs277.buildings.FloorObserver;
import cecs277.events.ElevatorStateEvent;
import cecs277.passengers.Passenger;

import java.util.*;
import java.util.stream.Collectors;

public class Elevator implements FloorObserver {
	
	public enum ElevatorState {
		IDLE_STATE,
		DOORS_OPENING,
		DOORS_CLOSING,
		DOORS_OPEN,
		ACCELERATING,
		DECELERATING,
		MOVING
	}
	
	public enum Direction {
		NOT_MOVING,
		MOVING_UP,
		MOVING_DOWN
	}
	
	
	private int mNumber;
	private Building mBuilding;

	private ElevatorState mCurrentState = ElevatorState.IDLE_STATE;
	private Direction mCurrentDirection = Direction.NOT_MOVING;
	private Floor mCurrentFloor;
	private List<Passenger> mPassengers = new ArrayList<>();
	
	private List<ElevatorObserver> mObservers = new ArrayList<>();
	
	// TODO: declare a field to keep track of which floors have been requested by passengers.
	
	private ArrayList<Integer> requestedFloor = new ArrayList<>(); 
	
	public Elevator(int number, Building bld) {
		mNumber = number;
		mBuilding = bld;
		mCurrentFloor = bld.getFloor(1);
		
		scheduleStateChange(ElevatorState.IDLE_STATE, 0);
	}
	
	/**
	 * Helper method to schedule a state change in a given number of seconds from now.
	 */
	private void scheduleStateChange(ElevatorState state, long timeFromNow) {
		Simulation sim = mBuilding.getSimulation();
		sim.scheduleEvent(new ElevatorStateEvent(sim.currentTime() + timeFromNow, state, this));
	}
	
	/**
	 * Adds the given passenger to the elevator's list of passengers, and requests the passenger's destination floor.
	 */
	public void addPassenger(Passenger passenger) {
		// TODO: add the passenger's destination to the set of requested floors.
		mPassengers.add(passenger);
		
		requestedFloor.add(passenger.getDestination());
		
	}
	
	public void removePassenger(Passenger passenger) {
		mPassengers.remove(passenger);
	}
	
	
	/**
	 * Schedules the elevator's next state change based on its current state.
	 */
	public void tick() {
		// TODO: port the logic of your state changes from Project 1, accounting for the adjustments in the spec.
		// TODO: State changes are no longer immediate; they are scheduled using scheduleStateChange().
		
		// Example of how to trigger a state change:
		// scheduleStateChange(ElevatorState.MOVING, 3); // switch to MOVING and call tick(), 3 seconds from now.
		
            // Idle state
            if (mCurrentState == ElevatorState.IDLE_STATE) {
            	System.out.println("THE ELEVATOR IS IN IDLE STATE ");
            	// Idle State does not transition to any other method in tick()
            	// Add the elevator as an observer of its current floor
            	mCurrentFloor.addObserver(this); // Changed from line below
//            	addObserver(mObservers.get(mCurrentFloor.getNumber()));
            	
            	// Alert all of elevator's observers that it went IDLE
				List<ElevatorObserver> temp = new ArrayList<>(mObservers);
            	for (ElevatorObserver i : temp) {
            		i.elevatorWentIdle(this);
            	}
//            	System.out.println("2222222222222222222222222222222222");
            }
            
            // State DOORS OPEN
            else if (mCurrentState == ElevatorState.DOORS_OPEN) {
            	// Number of passengers waiting in the elevator and on the floor
            	int numPassElevBefore = mPassengers.size();
            	int numPassFloorBefore = mCurrentFloor.getWaitingPassengers().size();
            	
            	// Notify all observers by triggering the elevatorDoorsOpened() method
            	List<ElevatorObserver> temp = new ArrayList<>(mObservers);
            	for (ElevatorObserver i : temp) {
//            		System.out.println("Alerting the observers!!!");
            		i.elevatorDoorsOpened(this);
            	}
            	
            	// Transition to DOORS_CLOSING in 1 + x sec where x is half of the passenger count rounded down
            	int numPassElevAfter = mPassengers.size();
            	int numPassFloorAfter = mCurrentFloor.getWaitingPassengers().size();
            	
            	int numPassJoinedElev = numPassFloorBefore - numPassFloorAfter;
            	int numPassLeftElev = numPassJoinedElev + numPassElevBefore - numPassElevAfter;
            	
            	scheduleStateChange(ElevatorState.DOORS_CLOSING, (int)(Math.floor(numPassJoinedElev + numPassLeftElev) / 2) + 1);
            }

            // State Doors_Opening
            else if (mCurrentState == ElevatorState.DOORS_OPENING) {
            	scheduleStateChange(ElevatorState.DOORS_OPEN, 2);
//            	setState(ElevatorState.DOORS_OPEN);
            }
            
            // State DOORS CLOSING
            else if (mCurrentState == ElevatorState.DOORS_CLOSING) {
            	// If there is another request in our direction
            	
//            	System.out.println("The current direction is: " + mCurrentDirection);
//            	System.out.println("The size of the requested floor array is: " + requestedFloor.size());
//            	System.out.println("Next Request UP " + nextRequestUp(mCurrentFloor.getNumber()));
//            	System.out.println("Next Request DOWN " + nextRequestDown(mCurrentFloor.getNumber()));
            	
            	if ((nextRequestUp(mCurrentFloor.getNumber()) != -1 && mCurrentDirection == Direction.MOVING_UP) // REMOVED && mCurrentDirection == Direction.MOVING_UP
            			|| (nextRequestDown(mCurrentFloor.getNumber()) != -1 && mCurrentDirection == Direction.MOVING_DOWN)) { // REMOVED && mCurrentDirection == Direction.MOVING_DOWN
            		scheduleStateChange(ElevatorState.ACCELERATING, 2);
           		
            	} else if (mCurrentDirection == Direction.MOVING_UP && nextRequestUp(mCurrentFloor.getNumber()) == -1 && nextRequestDown(mCurrentFloor.getNumber()) != -1) {
            			setCurrentDirection(Direction.MOVING_DOWN);
                		scheduleStateChange(ElevatorState.DOORS_OPENING, 2);
                		
            	} else if (mCurrentDirection == Direction.MOVING_DOWN && nextRequestDown(mCurrentFloor.getNumber()) == -1 && nextRequestUp(mCurrentFloor.getNumber()) != -1) {
            		setCurrentDirection(Direction.MOVING_UP);
            		scheduleStateChange(ElevatorState.DOORS_OPENING, 2);

            	} else if (requestedFloor.isEmpty()) {
            		setCurrentDirection(Direction.NOT_MOVING);
            		scheduleStateChange(ElevatorState.IDLE_STATE, 2);
           	}
            }

            // State ACCELERATING
            else if (mCurrentState == ElevatorState.ACCELERATING){
            	// Remove the elevator as an observer of the current floor
            	mCurrentFloor.removeObserver(this);
            	// Transition to MOVING state
            	scheduleStateChange(ElevatorState.MOVING, 3);
            	
            }

            // State DECELERATING
            else if (mCurrentState == ElevatorState.DECELERATING) {
            	// "Clear" the current floor from the requested floor list
                requestedFloor.remove(0); // CHANGED mCurrentFloor.getNumber()
                
//                System.out.println("Current Direction is " + mCurrentDirection);
//                System.out.println("Next Request DOWN " + nextRequestDown(mCurrentFloor.getNumber()));
//                System.out.println("Current Button " + mCurrentFloor.getButtonUp());
//                System.out.println("Next Request UP " + nextRequestUp(mCurrentFloor.getNumber()));
                
                if (mCurrentFloor.directionIsPressed(mCurrentDirection) || (nextRequestUp(mCurrentFloor.getNumber()) != -1 && mCurrentDirection == Direction.MOVING_UP)
                		|| (nextRequestDown(mCurrentFloor.getNumber()) != -1 && mCurrentDirection == Direction.MOVING_DOWN)) {
                	mCurrentDirection = getCurrentDirection();
                	
                } else if ((mCurrentDirection == Direction.MOVING_UP && nextRequestUp(mCurrentFloor.getNumber()) == -1 )) { // && mCurrentFloor.getButtonDown()) && nextRequestDown(mCurrentFloor.getNumber()) != -1
                	setCurrentDirection(Direction.MOVING_DOWN);
                	
                } else if ((mCurrentDirection == Direction.MOVING_DOWN && nextRequestDown(mCurrentFloor.getNumber()) == -1 )) { //&& mCurrentFloor.getButtonUp()) && nextRequestUp(mCurrentFloor.getNumber()) != -1
            		setCurrentDirection(Direction.MOVING_UP);
            		
                } else {
//                	System.out.println("###### DECELERATING / NOT MOVING ######");
                	setCurrentDirection(Direction.NOT_MOVING);
                }
                // Alert all observers and change to DOORS_OPENING
                List<ElevatorObserver> temp = new ArrayList<>(mObservers);
                for (ElevatorObserver i : temp) {
                    i.elevatorDecelerating(this);
                }              
                scheduleStateChange(ElevatorState.DOORS_OPENING, 3);
//        		setState(ElevatorState.DOORS_OPENING);
//            	tick();
            }
                
            // State Moving
            else if (mCurrentState == ElevatorState.MOVING) {
            	if (mCurrentDirection == Direction.MOVING_UP) {
            		setCurrentFloor(mBuilding.getFloor(mCurrentFloor.getNumber() + 1)); // .GETNUMBER() +1 or +2
            	} else {
            		setCurrentFloor(mBuilding.getFloor(mCurrentFloor.getNumber() - 1)); // 
            	}
                // if Elevator floor requests contains the next floor or next floor pressed the same direction 
                if (requestedFloor.contains(getCurrentFloor().getNumber()) || mCurrentFloor.directionIsPressed(mCurrentDirection)) {
                	scheduleStateChange(ElevatorState.DECELERATING, 2);
//            		setState(ElevatorState.DECELERATING);
//                	tick();
                } else {
                	scheduleStateChange(ElevatorState.MOVING, 2);
//            		setState(ElevatorState.MOVING);
//                	tick();
                }
            }            
        }	
	
	/**
	 * Sends an idle elevator to the given floor.
	 */
	public void dispatchTo(Floor floor) {
		// TODO: if we are currently idle and not on the given floor, change our direction to move towards the floor.
		// TODO: set a floor request for the given floor, and schedule a state change to ACCELERATING immediately.
		
		if (mCurrentState == ElevatorState.IDLE_STATE && mCurrentFloor.getNumber() != floor.getNumber()) {
			
			if (mCurrentFloor.getNumber() < floor.getNumber()) {
				setCurrentDirection(Direction.MOVING_UP);
			} else if (mCurrentFloor.getNumber() > floor.getNumber()) {
				System.out.println("DISPATCH THE ELEVATOR ");
				setCurrentDirection(Direction.MOVING_DOWN);
			}
			// Set floor request for the given floor 
			requestedFloor.add(floor.getNumber());
			// Schedule a state change to ACCELERATION
			scheduleStateChange(ElevatorState.ACCELERATING, 0);

		}
		
	}
	
	// Simple accessors
	public Floor getCurrentFloor() {
		return mCurrentFloor;
	}
	
	public Direction getCurrentDirection() {
		return mCurrentDirection;
	}
	
	public Building getBuilding() {
		return mBuilding;
	}
	
	/**
	 * Returns true if this elevator is in the idle state.
	 * @return
	 */
	public boolean isIdle() {
		// TODO: complete this method.
		if (mCurrentState == ElevatorState.IDLE_STATE) {
			return true;
		}
		return false;
	}
	
	// All elevators have a capacity of 10, for now.
	public int getCapacity() {
		return 10;
	}
	
	public int getPassengerCount() {
		return mPassengers.size();
	}
	
	// Simple mutators
	public void setState(ElevatorState newState) {
		mCurrentState = newState;
	}
	
	public void setCurrentDirection(Direction direction) {
		mCurrentDirection = direction;
	}
	
	public void setCurrentFloor(Floor floor) {
		mCurrentFloor = floor;
	}
	
	// Observers
	public void addObserver(ElevatorObserver observer) {
		mObservers.add(observer);
	}
	
	public void removeObserver(ElevatorObserver observer) {
		mObservers.remove(observer);
	}
	
	
	// FloorObserver methods
	@Override
	public void elevatorArriving(Floor floor, Elevator elevator) {
		// Not used.
	}
	
	/**
	 * Triggered when our current floor receives a direction request.
	 */
	@Override
	public void directionRequested(Floor sender, Direction direction) {
		// TODO: if we are currently idle, change direction to match the request. Then alert all our observers that we are decelerating,
		// TODO: then schedule an immediate state change to DOORS_OPENING.
		
		if (mCurrentState == ElevatorState.IDLE_STATE) {
			// Change direction to match request
//			System.out.println(direction);
			setCurrentDirection(direction);
			
			// Alert all observers that we are decelerating
			List<ElevatorObserver> temp = new ArrayList<>(mObservers);
			for (ElevatorObserver i : temp) {
				i.elevatorDecelerating(this);
			}
			scheduleStateChange(ElevatorState.DOORS_OPENING, 0);	
//			setState(ElevatorState.DOORS_OPENING);
//			tick();
		}		

	}
	
	private int nextRequestUp(int fromFloor) {
//		System.out.println("I have entered next request up: ");
		for (int i = 0; i < requestedFloor.size(); i++) {
			if (fromFloor < requestedFloor.get(i)) {
//				System.out.println("The requested floor is " + requestedFloor.get(i));
				return requestedFloor.get(i);
			}
		}
		return -1;
	}
	
	private int nextRequestDown(int fromFloor) {
		for (int i = requestedFloor.size() - 1; i > 0 ; i++) {
			if (fromFloor < requestedFloor.get(i)) {
				return requestedFloor.get(i);
			}
		}
		return -1;
	}
	
	// Created Getter
	public ElevatorState getState() {
		return mCurrentState;
	}
	
	// Voodoo magic.
	@Override
	public String toString() {
		return "Elevator " + mNumber + " - " + mCurrentFloor + " - " + mCurrentState + " - " + mCurrentDirection + " - "
		 + "[" + mPassengers.stream().map(p -> Integer.toString(p.getDestination())).collect(Collectors.joining(", "))
		 + "]";
	}
	
}
