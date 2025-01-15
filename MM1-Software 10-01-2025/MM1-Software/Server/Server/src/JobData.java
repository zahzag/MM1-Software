package Server;

/**
 * Stores the measurement values for a Job.
 * 
 * @author Kirschner
 *
 */
public class JobData {

    private long calcTime;
    private long packetLength;
    private long responseTime;
    
    public void setCalcTime (long calcTime) {
        this.calcTime = calcTime;
    }
    
    public void setPacketLength (long packetLength) {
        this.packetLength = packetLength;
    }
    
    public void setResponseTime (long responseTime) {
        this.responseTime = responseTime;
    }
    
    public long getCalcTime () {
        return calcTime;
    }
    
    public long getPacketLength () {
        return packetLength;
    }
    
    public long getResponseTime () {
        return responseTime;
    }
}
