package com.pennassurancesoftware.jgroups.distributed_task;

import java.io.DataInput;
import java.io.DataOutput;

import org.jgroups.util.Streamable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pennassurancesoftware.jgroups.distributed_task.type.DistributedTaskType;
import com.pennassurancesoftware.jgroups.distributed_task.type.ResumableTaskStatus;

/** Defines the interface for tasks that will be long running (usually used to poll the database or some other staging area) */
@SuppressWarnings("javadoc")
public abstract class ResumableTask extends AbstractDistributedTask implements Runnable, Streamable {
   public static final int DEFAULT_INTERVAL = 5000;
   private static final Logger LOG = LoggerFactory.getLogger( ResumableTask.class );

   private int interval = DEFAULT_INTERVAL;

   public ResumableTask() {
      super( DistributedTaskType.Resumable );
   }

   public int getInterval() {
      return interval;
   }

   @Override
   public void readFrom( DataInput input ) throws Exception {
      interval = input.readInt();
      doReadFrom( input );
   }

   @Override
   public void run() {
      try {
         ResumableTaskStatus status = doRun();
         while( ResumableTaskStatus.Wait.equals( status ) ) {
            Thread.sleep( getInterval() );
            status = doResume();
         }
      }
      catch( Exception exception ) {
         LOG.error( String.format( "Error processing Long Running Task: %s", getId() ), exception );
      }
   }

   public void setInterval( int interval ) {
      this.interval = interval;
   }

   @Override
   public void writeTo( DataOutput output ) throws Exception {
      output.writeInt( interval );
      doWriteTo( output );
   }

   protected void doReadFrom( DataInput input ) throws Exception {}

   /**
    * Allows the implementing class to implement the long running logic (resumed)
    * @return Status of the run operation
    * @throws Exception {@link Exception}
    */
   protected abstract ResumableTaskStatus doResume() throws Exception;

   /**
    * Allows the implementing class to implement the long running logic 
    * @return Status of the run operation
    * @throws Exception {@link Exception}
    */
   protected abstract ResumableTaskStatus doRun() throws Exception;

   protected void doWriteTo( DataOutput output ) throws Exception {}
}
