package bgu.spl.mics.application.subscribers;

import bgu.spl.mics.Subscriber;
import bgu.spl.mics.example.messages.ExampleEvent;

/**
 * M handles ReadyEvent - fills a report and sends agents to mission.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class M extends Subscriber {

	public M() {
		super("Change_This_Name");
		// TODO Implement this
	}

	@Override
	protected void initialize() {
		/**example!!!*/
		System.out.println("Event Handler " + getName() + " started");

		/** subsribe to the relevent event*//
		subscribeEvent(MissionReceivedEvent.class, ev -> {
			mbt--;
			System.out.println("Event Handler " + getName() + " got a new event from " + ev.getSenderName() + "! (mbt: " + mbt + ")");
			complete(ev, "Hello from " + getName());
			if (mbt == 0) {
				System.out.println("Event Handler " + getName() + " terminating.");
				terminate();
			}
		});
		/**example!!!*/

	}

}
