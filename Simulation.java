package cecs277;

import cecs277.buildings.Building;
import cecs277.events.SimulationEvent;
import cecs277.events.SpawnPassengerEvent;

import java.util.PriorityQueue;
import java.util.Random;
import java.util.Scanner;

public class Simulation {
	private Random mRandom;
	private PriorityQueue<SimulationEvent> mEvents = new PriorityQueue<>();
	private long mCurrentTime;
	
	/**
	 * Seeds the Simulation with a given random number generator.
	 */
	public Simulation(Random random) {
		mRandom = random;
	}
	
	/**
	 * Gets the current time of the simulation.
	 */
	public long currentTime() {
		return mCurrentTime;
	}
	
	/**
	 * Access the Random object for the simulation.
	 */
	public Random getRandom() {
		return mRandom;
	}
	
	/**
	 * Adds the given event to a priority queue sorted on the scheduled time of execution.
	 */
	public void scheduleEvent(SimulationEvent ev) {
		mEvents.add(ev);
	}
	
	public void startSimulation(Scanner input) {
		Scanner scnr = new Scanner(System.in);
		
		System.out.println("Input the desired amount of floors in the building: ");
		int floors = scnr.nextInt();
		
		System.out.println("Input the desired amount of elevators in the building: ");
		int elevators = scnr.nextInt();
		
		Building b = new Building(floors, elevators, this);
		SpawnPassengerEvent ev = new SpawnPassengerEvent(0, b);
		scheduleEvent(ev);
		
		long nextSimLength = -1;
		
		// Set this boolean to true to make the simulation run at "real time".
		boolean simulateRealTime = false;
		// Change the scale below to less than 1 to speed up the "real time".
		double realTimeScale = 1.0;
		
		// TODO: the simulation currently stops at 200s. Instead, ask the user how long they want to simulate.
		System.out.println("Enter how long you want to simulate: ");
		
		nextSimLength = scnr.nextLong();
		
		long nextStopTime = mCurrentTime + nextSimLength;
		// If the next event in the queue occurs after the requested sim time, then just fast forward to the requested sim time.
		if (mEvents.peek().getScheduledTime() >= nextStopTime) {
			mCurrentTime = nextStopTime;
		}
		
		// As long as there are events that happen between "now" and the requested sim time, process those events and
		// advance the current time along the way.
		while (!mEvents.isEmpty() && mEvents.peek().getScheduledTime() <= nextStopTime) {
			SimulationEvent nextEvent = mEvents.poll();
			
			long diffTime = nextEvent.getScheduledTime() - mCurrentTime;
			if (simulateRealTime && diffTime > 0) {
				try {
					Thread.sleep((long)(realTimeScale * diffTime * 1000));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			mCurrentTime += diffTime;
			nextEvent.execute(this);
			System.out.println(nextEvent);
		}
		
		// TODO: print the Building after simulating the requested time.
		System.out.println(b);
		
		
		/*
		 TODO: the simulation stops after one round of simulation. Write a loop that continues to ask the user
		 how many seconds to simulate, simulates that many seconds, and stops only if they choose -1 seconds.
		*/
		System.out.println("Enter how long to simulate or enter -1 to end: ");
		nextSimLength = scnr.nextLong();
		while(nextSimLength != -1) {
			nextStopTime = mCurrentTime + nextSimLength;
			// If the next event in the queue occurs after the requested sim time, then just fast forward to the requested sim time.
			if (mEvents.peek().getScheduledTime() >= nextStopTime) {
				mCurrentTime = nextStopTime;
			}
			
			// As long as there are events that happen between "now" and the requested sim time, process those events and
			// advance the current time along the way.
			while (!mEvents.isEmpty() && mEvents.peek().getScheduledTime() <= nextStopTime) {
				SimulationEvent nextEvent = mEvents.poll();
				
				long diffTime = nextEvent.getScheduledTime() - mCurrentTime;
				if (simulateRealTime && diffTime > 0) {
					try {
						Thread.sleep((long)(realTimeScale * diffTime * 1000));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
				mCurrentTime += diffTime;
				nextEvent.execute(this);
				System.out.println(nextEvent);
			}
			
			// TODO: print the Building after simulating the requested time.
			System.out.println(b);
			
			
			/*
			 TODO: the simulation stops after one round of simulation. Write a loop that continues to ask the user
			 how many seconds to simulate, simulates that many seconds, and stops only if they choose -1 seconds.
			*/
			System.out.println("Enter how long to simulate or enter -1 to end: ");
			nextSimLength = scnr.nextLong();
		}
	}
	
	public static void main(String[] args) {
		Scanner scnr = new Scanner(System.in);
		// TODO: ask the user for a seed value and change the line below.
		
		System.out.println("Enter the seed value: ");
		int seed = scnr.nextInt();
		
		Simulation sim = new Simulation(new Random(seed));
		sim.startSimulation(scnr);
	}
}
