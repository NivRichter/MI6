package bgu.spl.mics;

import bgu.spl.mics.application.messages.AgentsAvailableEvent;
import bgu.spl.mics.application.messages.GadgetAvailableEvent;
import bgu.spl.mics.application.messages.MissionReceivedEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * The {@link MessageBrokerImpl class is the implementation of the MessageBroker interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBrokerImpl implements MessageBroker {
    private ConcurrentHashMap<Class<? extends Message>, ConcurrentLinkedQueue<Subscriber>> eventsBySubs;
    private ConcurrentHashMap<Subscriber, ConcurrentLinkedQueue< Message> > subByMess;
    private ConcurrentHashMap<Event,Future> eventsByFuture;

    public MessageBrokerImpl(){
        eventsBySubs=new ConcurrentHashMap<>();
        eventsBySubs.put(AgentsAvailableEvent.class,new ConcurrentLinkedQueue<>());
        eventsBySubs.put(GadgetAvailableEvent.class,new ConcurrentLinkedQueue<>());
        eventsBySubs.put(MissionReceivedEvent.class,new ConcurrentLinkedQueue<>());
        eventsBySubs.put(TickBroadcast.class,new ConcurrentLinkedQueue<>());
    }
    private static class SingletonHolder {
        private static MessageBrokerImpl instance = new MessageBrokerImpl();
    }

    /**
     * Retrieves the single instance of this class.
     */
    public static MessageBroker getInstance() {
        return SingletonHolder.instance;
    }

    @Override
    public <T> void subscribeEvent(Class<? extends Event<T>> type, Subscriber m) {
        eventsBySubs.get(type).add(m);
    }
    @Override
    public void subscribeBroadcast(Class<? extends Broadcast> type, Subscriber m) {
        eventsBySubs.get(type).add(m);
    }

    @Override
    public <T> void complete(Event<T> e, T result) {
        // TODO Auto-generated method stub
        eventsByFuture.get(e).resolve(result);
        notifyAll();
    }

    @Override
    public void sendBroadcast(Broadcast b) {
        // TODO Auto-generated method stub
        ConcurrentLinkedQueue<Subscriber> Q = eventsBySubs.get(b.getClass());
        for (int i=0;i<Q.size();i++){
            Subscriber s = Q.peek();
            subByMess.get(s).add(b);
            Q.remove();
            Q.add(s);
        }
        /** Hopefully Works :D */
        eventsBySubs.remove(b.getClass());
        eventsBySubs.put(b.getClass(),Q);
    }


    @Override
    public <T> Future<T> sendEvent(Event<T> e) {

       Subscriber s=eventsBySubs.get(e.getClass()).peek();
       subByMess.get(s).add(e);
       eventsBySubs.get(e.getClass()).remove();
       eventsBySubs.get(e.getClass()).add(s);
       Future<T> future=new Future<>();
       eventsByFuture.put(e,future);
       /** notifies all to wake the waiting subscribers at this.awaitMessage() who waits for a message*/
       notifyAll();
       return future;
    }

    @Override
    public void register(Subscriber m) {
        subByMess.put(m,new ConcurrentLinkedQueue<>());
    }

    @Override
    public void unregister(Subscriber m) {
        //check what happens with threads
     if(subByMess.containsValue(m)){
         subByMess.remove(m);
        }
    }

    @Override
    public synchronized Message awaitMessage(Subscriber m) throws InterruptedException {
       try{
            while(subByMess.get(m).peek() == null) wait();
            Message s = subByMess.get(m).peek();
           subByMess.get(m).remove();
           return s;

       }catch(IllegalStateException e ){
           System.out.println("no such subscriber exists, returning null");
           return null;
       }
       catch(InterruptedException e1){
           System.out.println("Thread "+m+ "was interrupted");
           return null;
        }
    }


}
